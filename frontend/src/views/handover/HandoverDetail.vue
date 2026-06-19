<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="title">房间交接 - 入住单号: {{ checkinNo }}</div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>交接信息</span>
            <el-tag v-if="detail?.confirmed" type="success" style="margin-left: 10px;">交接已完成</el-tag>
            <el-tag v-else type="warning" style="margin-left: 10px;">交接进行中</el-tag>
          </template>

          <el-form ref="formRef" :model="form" label-width="120px" v-if="detail">
            <el-form-item label="水表读数">
              <el-input-number
                v-model="form.meterWater"
                :min="0"
                :precision="2"
                :disabled="!canEditMerchant"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="电表读数">
              <el-input-number
                v-model="form.meterElectric"
                :min="0"
                :precision="2"
                :disabled="!canEditMerchant"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="燃气表读数">
              <el-input-number
                v-model="form.meterGas"
                :min="0"
                :precision="2"
                :disabled="!canEditMerchant"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="钥匙数量">
              <el-input-number
                v-model="form.keyCount"
                :min="0"
                :disabled="!canEditMerchant"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="门禁卡数量">
              <el-input-number
                v-model="form.doorCardCount"
                :min="0"
                :disabled="!canEditMerchant"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="家具设施情况">
              <el-input
                v-model="form.furnitureCondition"
                type="textarea"
                :rows="2"
                :disabled="!canEditMerchant"
                placeholder="请描述家具设施情况"
              />
            </el-form-item>
            <el-form-item label="家电设施情况">
              <el-input
                v-model="form.applianceCondition"
                type="textarea"
                :rows="2"
                :disabled="!canEditMerchant"
                placeholder="请描述家电设施情况"
              />
            </el-form-item>
            <el-form-item label="其他说明">
              <el-input
                v-model="form.otherCondition"
                type="textarea"
                :rows="2"
                :disabled="!canEditMerchant"
                placeholder="其他需要说明的事项"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card style="margin-bottom: 20px;">
          <template #header><span>签字确认</span></template>
          <div style="display: flex; gap: 16px;">
            <div style="flex: 1; text-align: center; padding: 20px; border: 1px dashed #dcdfe6; border-radius: 4px;">
              <div style="font-weight: 600; margin-bottom: 8px;">住户签字</div>
              <el-tag v-if="detail?.residentSign" type="success">
                <el-icon><CircleCheck /></el-icon> 已签字
              </el-tag>
              <el-tag v-else type="info">未签字</el-tag>
              <div style="margin-top: 12px;">
                <el-button
                  v-if="userStore.isResident && !detail?.residentSign && !detail?.confirmed"
                  type="primary"
                  @click="handleResidentSign"
                >我确认签字</el-button>
              </div>
              <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                住户确认以上交接信息无误
              </div>
            </div>
            <div style="flex: 1; text-align: center; padding: 20px; border: 1px dashed #dcdfe6; border-radius: 4px;">
              <div style="font-weight: 600; margin-bottom: 8px;">招商主管签字</div>
              <el-tag v-if="detail?.merchantSign" type="success">
                <el-icon><CircleCheck /></el-icon> 已签字
              </el-tag>
              <el-tag v-else type="info">未签字</el-tag>
              <div style="margin-top: 12px;">
                <el-button
                  v-if="userStore.isMerchant && !detail?.merchantSign && !detail?.confirmed"
                  type="primary"
                  @click="handleMerchantSign"
                >抄表并签字</el-button>
              </div>
              <div style="margin-top: 8px; color: #909399; font-size: 12px;">
                招商主管填写表数并确认
              </div>
            </div>
          </div>
        </el-card>

        <el-card>
          <template #header><span>交接时间</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="创建时间">{{ detail?.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交接完成时间">{{ detail?.handoverTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getHandoverByCheckin, createCheckinHandover, residentSign, merchantSign } from '@/api/handover'
import { getCheckinDetail } from '@/api/checkin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)
const checkinNo = ref('')

const form = reactive({
  meterWater: 0,
  meterElectric: 0,
  meterGas: 0,
  keyCount: 0,
  doorCardCount: 0,
  furnitureCondition: '',
  applianceCondition: '',
  otherCondition: ''
})

const canEditMerchant = computed(() => {
  return userStore.isMerchant && !detail.value?.confirmed
})

async function loadData() {
  loading.value = true
  try {
    const checkin = await getCheckinDetail(route.params.checkinId)
    checkinNo.value = checkin.checkinNo

    if (checkin.status === 'DRAFT') {
      ElMessage.warning('请先提交交接')
      router.back()
      return
    }

    let handover = await getHandoverByCheckin(route.params.checkinId, 'CHECKIN')
    if (!handover) {
      handover = await createCheckinHandover(route.params.checkinId)
    }
    detail.value = handover

    if (handover) {
      form.meterWater = handover.meterWater || 0
      form.meterElectric = handover.meterElectric || 0
      form.meterGas = handover.meterGas || 0
      form.keyCount = handover.keyCount || 0
      form.doorCardCount = handover.doorCardCount || 0
      form.furnitureCondition = handover.furnitureCondition || ''
      form.applianceCondition = handover.applianceCondition || ''
      form.otherCondition = handover.otherCondition || ''
    }
  } finally {
    loading.value = false
  }
}

async function handleResidentSign() {
  await residentSign(detail.value.id)
  ElMessage.success('签字确认成功')
  loadData()
}

async function handleMerchantSign() {
  await merchantSign(detail.value.id, form)
  ElMessage.success('抄表并签字成功')
  loadData()
}

onMounted(loadData)
</script>
