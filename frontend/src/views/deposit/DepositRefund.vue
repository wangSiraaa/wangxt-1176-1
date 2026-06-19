<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="title">退还押金 - 入住单号: {{ checkinNo }}</div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <el-alert
      v-if="hasUnpaidFee && dataLoaded"
      type="error"
      :closable="false"
      style="margin-bottom: 20px;"
      show-icon
      title="欠费未清不能退押金"
    >
      <span>存在未结清的费用（共{{ unpaidFeeCount }}项），请先完成费用结算后再退还押金。</span>
    </el-alert>

    <el-alert
      v-if="dataLoaded && !hasUnpaidFee && Number(available) <= 0"
      type="warning"
      :closable="false"
      style="margin-bottom: 20px;"
      show-icon
      title="无可退押金"
    >
      <span>当前可退金额为¥{{ available }}，已收押金：¥{{ collected }}，已退押金：¥{{ refunded }}，扣款总额：¥{{ totalDeduction }}。</span>
    </el-alert>

    <div class="form-container">
      <el-descriptions v-if="checkin" :column="2" border style="margin-bottom: 24px;">
        <el-descriptions-item label="入住单号">{{ checkin.checkinNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间ID">{{ checkin.roomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="住户ID">{{ checkin.residentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住状态">{{ statusText(checkin.status) }}</el-descriptions-item>
        <el-descriptions-item label="押金标准">¥{{ Number(checkin.depositAmount).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="已收押金">
          <span style="color: #67c23a;">¥{{ collected }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已退押金">
          <span style="color: #f56c6c;">¥{{ refunded }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="扣款总额">
          <span style="color: #e6a23c;">¥{{ totalDeduction }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="可退金额" :span="2">
          <span :style="{ color: Number(available) > 0 ? '#67c23a' : '#909399', fontWeight: 600, fontSize: '18px' }">
            ¥{{ available }}
          </span>
          <el-tag v-if="Number(available) <= 0" type="info" size="small" style="margin-left: 10px;">已结清</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-form
        v-if="dataLoaded && checkin"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="退还金额(元)" prop="amount">
          <el-input-number
            v-if="Number(available) > 0"
            v-model="form.amount"
            :min="minAmount"
            :max="Number(available)"
            :precision="2"
            :step="100"
            :disabled="hasUnpaidFee || Number(available) <= 0"
            style="width: 100%;"
          />
          <el-input
            v-else
            v-model="form.amount"
            disabled
            placeholder="无可退金额"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="支付方式" prop="payMethod">
          <el-select
            v-model="form.payMethod"
            placeholder="请选择支付方式"
            :disabled="hasUnpaidFee || Number(available) <= 0"
            style="width: 100%;"
          >
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
            :disabled="hasUnpaidFee || Number(available) <= 0"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="submitDisabled"
            @click="handleSubmit"
          >
            {{ submitButtonText }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>

      <div v-if="dataLoaded && !checkin" style="text-align: center; padding: 40px; color: #909399;">
        入住单不存在或已被删除
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCheckinDetail } from '@/api/checkin'
import { getDepositListByCheckin, refundDeposit } from '@/api/deposit'
import { getDeductionListByCheckin } from '@/api/deduction'
import { getSettlementByCheckin } from '@/api/settlement'
import { getUnpaidFeesByCheckin } from '@/api/fee'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(true)
const submitting = ref(false)
const dataLoaded = ref(false)
const checkin = ref(null)
const checkinNo = ref('')
const depositList = ref([])
const deductionList = ref([])
const settlement = ref(null)
const hasUnpaidFee = ref(false)
const unpaidFeeCount = ref(0)

const form = reactive({
  checkinId: route.params.checkinId,
  amount: 0,
  payMethod: '',
  remark: ''
})

const rules = {
  amount: [
    { required: true, message: '请输入退还金额', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (Number(available) <= 0) {
          callback(new Error('无可退金额'))
        } else if (value < minAmount.value) {
          callback(new Error('最小退款金额为¥' + minAmount.value))
        } else if (value > Number(available.value)) {
          callback(new Error('最大退款金额为¥' + available.value))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const minAmount = computed(() => {
  const avail = Number(available.value)
  if (avail <= 0) return 0
  return avail < 0.01 ? avail : 0.01
})

const submitDisabled = computed(() => {
  return hasUnpaidFee.value || Number(available.value) <= 0 || submitting.value
})

const submitButtonText = computed(() => {
  if (hasUnpaidFee.value) return '存在未结清费用'
  if (Number(available.value) <= 0) return '无可退金额'
  return '确认退还'
})

const collected = computed(() => {
  if (!depositList.value || depositList.value.length === 0) return '0.00'
  return depositList.value
    .filter(d => d.transType === 'COLLECT')
    .reduce((sum, d) => sum + Number(d.amount || 0), 0)
    .toFixed(2)
})

const refunded = computed(() => {
  if (!depositList.value || depositList.value.length === 0) return '0.00'
  return depositList.value
    .filter(d => d.transType === 'REFUND')
    .reduce((sum, d) => sum + Number(d.amount || 0), 0)
    .toFixed(2)
})

const totalDeduction = computed(() => {
  if (!deductionList.value || deductionList.value.length === 0) return '0.00'
  return deductionList.value
    .reduce((sum, d) => sum + Number(d.amount || 0), 0)
    .toFixed(2)
})

const available = computed(() => {
  const val = Number(collected.value) - Number(refunded.value) - Number(totalDeduction.value)
  return val > 0 ? val.toFixed(2) : '0.00'
})

function statusText(code) {
  const map = {
    DRAFT: '草稿',
    PENDING_HANDOVER: '待交接',
    HANDED_OVER: '已交接',
    CHECKED_IN: '已入住',
    SETTLING: '结算中',
    SETTLED: '已结算'
  }
  return map[code] || code || '-'
}

async function loadData() {
  loading.value = true
  dataLoaded.value = false
  try {
    checkin.value = await getCheckinDetail(route.params.checkinId)
    checkinNo.value = checkin.value?.checkinNo || ''
    depositList.value = await getDepositListByCheckin(route.params.checkinId) || []
    deductionList.value = await getDeductionListByCheckin(route.params.checkinId) || []

    try {
      settlement.value = await getSettlementByCheckin(route.params.checkinId)
    } catch (e) {
      settlement.value = null
    }

    try {
      const unpaid = await getUnpaidFeesByCheckin(route.params.checkinId)
      unpaidFeeCount.value = unpaid?.length || 0
      hasUnpaidFee.value = unpaidFeeCount.value > 0
    } catch (e) {
      hasUnpaidFee.value = false
      unpaidFeeCount.value = 0
    }

    if (Number(available.value) > 0) {
      form.amount = Number(available.value)
    } else {
      form.amount = 0
    }
  } catch (e) {
    console.error('加载数据失败:', e)
    checkin.value = null
    ElMessage.error('加载数据失败：' + (e?.message || '未知错误'))
  } finally {
    loading.value = false
    dataLoaded.value = true
  }
}

async function handleSubmit() {
  if (hasUnpaidFee.value) {
    ElMessage.warning('欠费未清不能退押金，请先结清所有费用')
    return
  }
  if (Number(available.value) <= 0) {
    ElMessage.warning('当前无可退押金')
    return
  }
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    await ElMessageBox.confirm(
      `确认退还押金 ¥${form.amount.toFixed(2)} 吗？`,
      '确认退还',
      { type: 'warning' }
    )

    submitting.value = true
    await refundDeposit(form)
    ElMessage.success('退还成功')
    router.back()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '退还失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>
