<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">创建退租结算 - 入住单号: {{ checkinNo }}</div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div class="form-container">
      <el-descriptions v-if="checkin" :column="2" border style="margin-bottom: 24px;">
        <el-descriptions-item label="入住单号">{{ checkin.checkinNo }}</el-descriptions-item>
        <el-descriptions-item label="房间ID">{{ checkin.roomId }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ checkin.checkinDate }}</el-descriptions-item>
        <el-descriptions-item label="月租金(元)">¥{{ checkin.monthlyRent }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="实际退房日期" prop="checkoutDate">
          <el-date-picker v-model="form.checkoutDate" type="date" placeholder="选择实际退房日期" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="应付租金说明">
          <el-input
            v-model="form.rentRemark"
            type="textarea"
            :rows="2"
            placeholder="租金计算说明（如：按实际入住天数计算）"
          />
        </el-form-item>
        <el-form-item label="结算备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="其他结算说明" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">创建结算单</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCheckinDetail } from '@/api/checkin'
import { createSettlement, getSettlementByCheckin } from '@/api/settlement'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const checkin = ref(null)
const checkinNo = ref('')

const form = reactive({
  checkinId: route.params.checkinId,
  checkoutDate: '',
  rentRemark: '',
  remark: ''
})

const rules = {
  checkoutDate: [{ required: true, message: '请选择实际退房日期', trigger: 'change' }]
}

async function loadData() {
  checkin.value = await getCheckinDetail(route.params.checkinId)
  checkinNo.value = checkin.value.checkinNo
  form.checkoutDate = checkin.value.checkoutDate || new Date()

  try {
    const existing = await getSettlementByCheckin(route.params.checkinId)
    if (existing) {
      ElMessage.warning('该入住单已存在结算单，跳转到详情页')
      router.push('/settlement/detail/' + existing.id)
    }
  } catch (e) {}
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const result = await createSettlement(form)
    ElMessage.success('创建成功')
    router.push('/settlement/detail/' + (result.id || result))
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
