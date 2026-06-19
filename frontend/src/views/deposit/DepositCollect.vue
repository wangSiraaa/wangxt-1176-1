<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">收取押金 - 入住单号: {{ checkinNo }}</div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div class="form-container">
      <el-descriptions v-if="checkin" :column="2" border style="margin-bottom: 24px;">
        <el-descriptions-item label="入住单号">{{ checkin.checkinNo }}</el-descriptions-item>
        <el-descriptions-item label="房间ID">{{ checkin.roomId }}</el-descriptions-item>
        <el-descriptions-item label="住户ID">{{ checkin.residentId }}</el-descriptions-item>
        <el-descriptions-item label="押金标准">¥{{ checkin.depositAmount }}</el-descriptions-item>
        <el-descriptions-item label="已收押金">¥{{ collected }}</el-descriptions-item>
        <el-descriptions-item label="待收押金">¥{{ (Number(checkin.depositAmount) - Number(collected)).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="收取金额(元)" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :step="100"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="form.payMethod" placeholder="请选择支付方式" style="width: 100%;">
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">确认收取</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCheckinDetail } from '@/api/checkin'
import { getDepositListByCheckin, collectDeposit } from '@/api/deposit'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const checkin = ref(null)
const checkinNo = ref('')
const depositList = ref([])

const form = reactive({
  checkinId: route.params.checkinId,
  amount: 0,
  payMethod: '',
  remark: ''
})

const rules = {
  amount: [{ required: true, message: '请输入收取金额', trigger: 'blur' }],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const collected = computed(() => {
  return depositList.value
    .filter(d => d.transType === 'COLLECT')
    .reduce((sum, d) => sum + Number(d.amount), 0)
    .toFixed(2)
})

async function loadData() {
  checkin.value = await getCheckinDetail(route.params.checkinId)
  checkinNo.value = checkin.value.checkinNo
  form.amount = checkin.value.depositAmount
  depositList.value = await getDepositListByCheckin(route.params.checkinId)
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await collectDeposit(form)
    ElMessage.success('收取成功')
    router.back()
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
