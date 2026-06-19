<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="title">押金结算 - 入住单号: {{ checkinNo }}</div>
      <div>
        <el-tag v-if="checkin" :type="checkinStatusTagType(checkin.status)" style="margin-right: 12px;">
          {{ checkinStatusText(checkin.status) }}
        </el-tag>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div v-if="dataLoaded && !checkin" style="text-align: center; padding: 40px; color: #909399;">
      入住单不存在或已被删除
    </div>

    <template v-if="dataLoaded && checkin">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="入住验房" name="handover">
          <template #label>
            <span>
              <el-icon><House /></el-icon> 入住验房
              <el-tag v-if="!handoverConfirmed" type="danger" size="small" style="margin-left: 6px;">未确认</el-tag>
              <el-tag v-else type="success" size="small" style="margin-left: 6px;">已确认</el-tag>
            </span>
          </template>

          <el-alert
            v-if="!handoverConfirmed"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px;"
            show-icon
            title="住户未确认房间交接，不能办理入住"
          >
            <span>招商主管已创建入住单，但住户尚未完成房间交接签字确认。只有交接确认后才能正式入住。</span>
          </el-alert>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-card style="margin-bottom: 20px;">
                <template #header><span>房间信息</span></template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="入住单号">{{ checkin.checkinNo }}</el-descriptions-item>
                  <el-descriptions-item label="房间ID">{{ checkin.roomId }}</el-descriptions-item>
                  <el-descriptions-item label="住户ID">{{ checkin.residentId }}</el-descriptions-item>
                  <el-descriptions-item label="计划入住">{{ checkin.checkinDate }}</el-descriptions-item>
                  <el-descriptions-item label="实际入住">{{ checkin.actualCheckinDate || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="月租金">¥{{ Number(checkin.monthlyRent).toFixed(2) }}</el-descriptions-item>
                  <el-descriptions-item label="押金标准">¥{{ Number(checkin.depositAmount).toFixed(2) }}</el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card style="margin-bottom: 20px;">
                <template #header>
                  <span>交接确认状态</span>
                </template>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="交接确认">
                    <el-tag :type="handoverConfirmed ? 'success' : 'danger'">
                      {{ handoverConfirmed ? '已确认' : '未确认' }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="入住单状态">{{ checkinStatusText(checkin.status) }}</el-descriptions-item>
                </el-descriptions>

                <div v-if="handoverInfo" style="margin-top: 16px;">
                  <el-descriptions :column="2" border size="small" title="交接记录">
                    <el-descriptions-item label="水表读数">{{ handoverInfo.meterWater || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="电表读数">{{ handoverInfo.meterElectric || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="燃气表读数">{{ handoverInfo.meterGas || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="钥匙数量">{{ handoverInfo.keyCount ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="门禁卡数量">{{ handoverInfo.doorCardCount ?? '-' }}</el-descriptions-item>
                    <el-descriptions-item label="交接时间">{{ handoverInfo.handoverTime || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="住户签字" :span="1">
                      <el-tag :type="handoverInfo.residentSign ? 'success' : 'info'" size="small">
                        {{ handoverInfo.residentSign ? '已签字' : '未签字' }}
                      </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="主管签字" :span="1">
                      <el-tag :type="handoverInfo.merchantSign ? 'success' : 'info'" size="small">
                        {{ handoverInfo.merchantSign ? '已签字' : '未签字' }}
                      </el-tag>
                    </el-descriptions-item>
                  </el-descriptions>
                  <div v-if="handoverInfo.furnitureCondition" style="margin-top: 8px; font-size: 13px; color: #606266;">
                    <strong>家具设施:</strong> {{ handoverInfo.furnitureCondition }}
                  </div>
                  <div v-if="handoverInfo.applianceCondition" style="margin-top: 4px; font-size: 13px; color: #606266;">
                    <strong>家电设施:</strong> {{ handoverInfo.applianceCondition }}
                  </div>
                </div>
                <div v-else style="padding: 20px; text-align: center; color: #909399;">暂无交接记录</div>

                <div style="margin-top: 16px; text-align: right;">
                  <el-button
                    v-if="!handoverConfirmed && (userStore.isMerchant || userStore.isResident)"
                    type="primary"
                    @click="$router.push('/handover/' + checkin.id)"
                  >前往验房交接</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="欠费抵扣" name="deduction">
          <template #label>
            <span>
              <el-icon><Coin /></el-icon> 欠费抵扣
              <el-tag v-if="hasUnpaidFee" type="danger" size="small" style="margin-left: 6px;">有欠费</el-tag>
            </span>
          </template>

          <el-alert
            v-if="hasUnpaidFee"
            type="error"
            :closable="false"
            style="margin-bottom: 20px;"
            show-icon
            title="欠费未清不能退押金"
          >
            <span>存在未结清的费用（共{{ unpaidFeeCount }}项），请先完成费用结算后再退还押金。</span>
          </el-alert>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-card style="margin-bottom: 20px;">
                <template #header><span>未结清费用</span></template>
                <el-table :data="unpaidFeeList" stripe size="small">
                  <el-table-column prop="feeType" label="费用类型" width="120">
                    <template #default="{ row }">{{ feeTypeText(row.feeType) }}</template>
                  </el-table-column>
                  <el-table-column prop="feeName" label="费用名称" show-overflow-tooltip />
                  <el-table-column prop="feeMonth" label="月份" width="100" />
                  <el-table-column prop="amount" label="金额(元)" width="120">
                    <template #default="{ row }">
                      <span style="color: #f56c6c;">¥{{ Number(row.amount).toFixed(2) }}</span>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="unpaidFeeList.length === 0" style="padding: 20px; text-align: center; color: #67c23a;">
                  <el-icon><CircleCheck /></el-icon> 所有费用已结清
                </div>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card style="margin-bottom: 20px;">
                <template #header>
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>押金扣款明细</span>
                    <el-tag v-if="deductionLocked" type="danger" effect="dark" size="small">
                      <el-icon><Lock /></el-icon> 已锁定
                    </el-tag>
                  </div>
                </template>
                <el-table :data="deductionList" stripe size="small">
                  <el-table-column prop="deductionType" label="扣款类型" width="100">
                    <template #default="{ row }">{{ deductionTypeText(row.deductionType) }}</template>
                  </el-table-column>
                  <el-table-column prop="amount" label="金额(元)" width="100">
                    <template #default="{ row }">
                      <span style="color: #e6a23c;">¥{{ Number(row.amount).toFixed(2) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="reason" label="原因" show-overflow-tooltip />
                  <el-table-column label="证据" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.picUrls" type="primary" size="small" @click="showEvidence(row.picUrls)" style="cursor: pointer;">
                        查看
                      </el-tag>
                      <span v-else style="color: #c0c4cc;">-</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="locked" label="状态" width="80" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.locked ? 'danger' : 'info'" size="small">
                        {{ row.locked ? '锁定' : '可编辑' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="deductionList.length === 0" style="padding: 20px; text-align: center; color: #909399;">
                  暂无扣款明细
                </div>
              </el-card>

              <el-card>
                <template #header><span>押金抵扣汇总</span></template>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="已收押金">
                    <span style="color: #67c23a; font-weight: 600;">¥{{ collected }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="已退押金">
                    <span style="color: #f56c6c;">¥{{ refunded }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="扣款总额">
                    <span style="color: #e6a23c;">¥{{ totalDeduction }}</span>
                  </el-descriptions-item>
                  <el-descriptions-item label="可退金额">
                    <span :style="{ color: Number(available) > 0 ? '#67c23a' : '#909399', fontWeight: 700, fontSize: '16px' }">
                      ¥{{ available }}
                    </span>
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="退租争议" name="dispute">
          <template #label>
            <span>
              <el-icon><ChatDotRound /></el-icon> 退租争议
              <el-tag v-if="pendingDisputeCount > 0" type="warning" size="small" style="margin-left: 6px;">
                {{ pendingDisputeCount }}待处理
              </el-tag>
            </span>
          </template>

          <el-alert
            v-if="!deductionLocked"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
            show-icon
            title="争议复核仅在结算确认后可用"
          >
            <span>财务确认结算后扣款项锁定，此时住户可对扣款项发起争议复核。</span>
          </el-alert>

          <el-row :gutter="20">
            <el-col :span="14">
              <el-card style="margin-bottom: 20px;">
                <template #header>
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>争议记录</span>
                    <el-button
                      v-if="deductionLocked && userStore.isResident"
                      type="warning"
                      size="small"
                      @click="showDisputeDialog"
                    >发起争议复核</el-button>
                  </div>
                </template>
                <el-table :data="disputeList" stripe size="small">
                  <el-table-column prop="disputeType" label="争议类型" width="100">
                    <template #default="{ row }">{{ disputeTypeText(row.disputeType) }}</template>
                  </el-table-column>
                  <el-table-column prop="content" label="争议内容" show-overflow-tooltip />
                  <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="disputeStatusTagType(row.status)" size="small">
                        {{ disputeStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="提交时间" width="160" />
                  <el-table-column label="操作" width="80" align="center">
                    <template #default="{ row }">
                      <el-button link type="primary" size="small" @click="$router.push('/dispute/detail/' + row.id)">
                        查看
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="disputeList.length === 0" style="padding: 20px; text-align: center; color: #909399;">
                  暂无争议记录
                </div>
              </el-card>

              <el-card>
                <template #header><span>退款流水</span></template>
                <el-table :data="refundFlowList" stripe size="small">
                  <el-table-column prop="depositNo" label="退款单号" width="180" />
                  <el-table-column prop="amount" label="退款金额" width="120">
                    <template #default="{ row }">
                      <span style="color: #67c23a; font-weight: 600;">¥{{ Number(row.amount).toFixed(2) }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="payMethod" label="支付方式" width="120">
                    <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
                  </el-table-column>
                  <el-table-column prop="payTime" label="退款时间" />
                  <el-table-column prop="status" label="状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag :type="row.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
                        {{ row.status === 'COMPLETED' ? '已完成' : '处理中' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="refundFlowList.length === 0" style="padding: 20px; text-align: center; color: #909399;">
                  暂无退款流水
                </div>
              </el-card>
            </el-col>

            <el-col :span="10">
              <el-card style="margin-bottom: 20px;">
                <template #header><span>争议复核意见</span></template>
                <div v-if="disputeList.length > 0">
                  <div v-for="d in disputeList" :key="d.id" style="margin-bottom: 16px; padding: 12px; border: 1px solid #ebeef5; border-radius: 4px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                      <el-tag :type="disputeStatusTagType(d.status)" size="small">{{ disputeStatusText(d.status) }}</el-tag>
                      <span style="font-size: 12px; color: #909399;">{{ d.reviewTime || '-' }}</span>
                    </div>
                    <div style="font-size: 13px; color: #303133; margin-bottom: 6px;">
                      <strong>争议内容:</strong> {{ d.content }}
                    </div>
                    <div v-if="d.reviewOpinion" style="font-size: 13px; color: #409eff; margin-bottom: 4px;">
                      <strong>复核意见:</strong> {{ d.reviewOpinion }}
                    </div>
                    <div v-if="d.adjustedAmount != null" style="font-size: 13px; color: #e6a23c;">
                      <strong>调整金额:</strong> ¥{{ Number(d.adjustedAmount).toFixed(2) }}
                    </div>
                    <div v-if="d.deductionId" style="font-size: 12px; color: #909399; margin-top: 4px;">
                      关联扣款: {{ deductionTypeText(d.deductionType) }} - ¥{{ Number(d.deductionAmount || 0).toFixed(2) }}
                    </div>
                  </div>
                </div>
                <div v-else style="padding: 20px; text-align: center; color: #909399;">暂无复核意见</div>
              </el-card>

              <el-card>
                <template #header>
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span>退还押金</span>
                    <el-tag v-if="Number(available) > 0" type="success">可退 ¥{{ available }}</el-tag>
                    <el-tag v-else type="info">已结清</el-tag>
                  </div>
                </template>
                <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" size="small">
                  <el-form-item label="退还金额" prop="amount">
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
                      :rows="2"
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
                  </el-form-item>
                </el-form>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </template>

    <el-dialog v-model="evidenceDialog" title="扣款证据" width="700px">
      <div style="display: flex; flex-wrap: wrap; gap: 12px;">
        <el-image
          v-for="(url, idx) in evidenceUrls"
          :key="idx"
          :src="url"
          :preview-src-list="evidenceUrls"
          :initial-index="idx"
          fit="cover"
          style="width: 200px; height: 150px; border-radius: 4px;"
        />
      </div>
      <div v-if="evidenceUrls.length === 0" style="text-align: center; padding: 20px; color: #909399;">暂无证据图片</div>
    </el-dialog>

    <el-dialog v-model="disputeFormDialog" title="发起争议复核" width="600px">
      <el-form ref="disputeFormRef" :model="disputeForm" :rules="disputeRules" label-width="100px">
        <el-form-item label="关联扣款">
          <el-select v-model="disputeForm.deductionId" placeholder="选择要争议的扣款项" clearable style="width: 100%;">
            <el-option
              v-for="d in deductionList"
              :key="d.id"
              :label="deductionTypeText(d.deductionType) + ' - ¥' + Number(d.amount).toFixed(2) + ' (' + (d.reason || d.description || '') + ')'"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="争议类型" prop="disputeType">
          <el-select v-model="disputeForm.disputeType" placeholder="请选择争议类型" style="width: 100%;">
            <el-option label="扣款争议" value="DEDUCTION" />
            <el-option label="费用争议" value="FEE" />
            <el-option label="损坏争议" value="DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="争议内容" prop="content">
          <el-input v-model="disputeForm.content" type="textarea" :rows="4" placeholder="请详细说明争议原因" />
        </el-form-item>
        <el-form-item label="证据图片">
          <el-input v-model="disputeForm.evidenceUrls" placeholder="图片URL，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disputeFormDialog = false">取消</el-button>
        <el-button type="primary" :loading="disputeSubmitting" @click="handleDisputeSubmit">提交争议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, Coin, ChatDotRound, CircleCheck, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getCheckinDetail } from '@/api/checkin'
import { getDepositListByCheckin, refundDeposit } from '@/api/deposit'
import { getDeductionListByCheckin } from '@/api/deduction'
import { getSettlementByCheckin } from '@/api/settlement'
import { getUnpaidFeesByCheckin, getFeeListByCheckin } from '@/api/fee'
import { getHandoverByCheckin } from '@/api/handover'
import { getDisputeListByCheckin, createDispute } from '@/api/dispute'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const disputeFormRef = ref()
const loading = ref(true)
const submitting = ref(false)
const disputeSubmitting = ref(false)
const dataLoaded = ref(false)
const activeTab = ref('handover')

const checkin = ref(null)
const checkinNo = ref('')
const depositList = ref([])
const deductionList = ref([])
const settlement = ref(null)
const handoverInfo = ref(null)
const disputeList = ref([])
const unpaidFeeList = ref([])
const hasUnpaidFee = ref(false)
const unpaidFeeCount = ref(0)

const evidenceDialog = ref(false)
const evidenceUrls = ref([])
const disputeFormDialog = ref(false)

const form = reactive({
  checkinId: route.params.checkinId,
  amount: 0,
  payMethod: '',
  remark: ''
})

const disputeForm = reactive({
  checkinId: route.params.checkinId,
  deductionId: null,
  disputeType: '',
  content: '',
  evidenceUrls: ''
})

const rules = {
  amount: [
    { required: true, message: '请输入退还金额', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (Number(available.value) <= 0) {
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

const disputeRules = {
  disputeType: [{ required: true, message: '请选择争议类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入争议内容', trigger: 'blur' }]
}

const handoverConfirmed = computed(() => {
  return checkin.value?.handoverConfirmed === 1 || checkin.value?.handoverConfirmed === true
})

const deductionLocked = computed(() => {
  return deductionList.value.some(d => d.locked === 1 || d.locked === true)
})

const pendingDisputeCount = computed(() => {
  return disputeList.value.filter(d => d.status === 'PENDING' || d.status === 'REVIEWING').length
})

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

const refundFlowList = computed(() => {
  if (!depositList.value) return []
  return depositList.value.filter(d => d.transType === 'REFUND')
})

function checkinStatusText(code) {
  const map = {
    DRAFT: '草稿', PENDING_HANDOVER: '待交接', HANDED_OVER: '已交接',
    CHECKED_IN: '已入住', SETTLING: '结算中', SETTLED: '已结算', CANCELLED: '已取消'
  }
  return map[code] || code || '-'
}

function checkinStatusTagType(code) {
  const map = {
    DRAFT: 'info', PENDING_HANDOVER: 'warning', HANDED_OVER: '',
    CHECKED_IN: 'success', SETTLING: 'warning', SETTLED: 'success', CANCELLED: 'danger'
  }
  return map[code] || ''
}

function feeTypeText(code) {
  const map = { RENT: '租金', WATER: '水费', ELECTRIC: '电费', GAS: '燃气费', PROPERTY: '物业费', OTHER: '其他' }
  return map[code] || code || '-'
}

function deductionTypeText(code) {
  const map = { DAMAGE: '物品损坏', CLEAN: '清洁费', UTILITY: '水电费', OVERDUE: '逾期罚款', OTHER: '其他' }
  return map[code] || code || '-'
}

function disputeTypeText(code) {
  const map = { DEDUCTION: '扣款争议', FEE: '费用争议', DAMAGE: '损坏争议', OTHER: '其他' }
  return map[code] || code || '-'
}

function disputeStatusText(code) {
  const map = { PENDING: '待审核', REVIEWING: '审核中', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[code] || code || '-'
}

function disputeStatusTagType(code) {
  const map = { PENDING: 'warning', REVIEWING: '', APPROVED: 'success', REJECTED: 'danger' }
  return map[code] || 'info'
}

function payMethodText(code) {
  const map = { CASH: '现金', BANK: '银行转账', ALIPAY: '支付宝', WECHAT: '微信' }
  return map[code] || code || '-'
}

function showEvidence(picUrls) {
  if (!picUrls) return
  evidenceUrls.value = picUrls.split(',').filter(u => u.trim())
  evidenceDialog.value = true
}

function showDisputeDialog() {
  disputeForm.deductionId = null
  disputeForm.disputeType = ''
  disputeForm.content = ''
  disputeForm.evidenceUrls = ''
  disputeFormDialog.value = true
}

async function handleDisputeSubmit() {
  if (!disputeFormRef.value) return
  try {
    await disputeFormRef.value.validate()
    disputeSubmitting.value = true
    await createDispute(disputeForm)
    ElMessage.success('争议已提交')
    disputeFormDialog.value = false
    disputeList.value = await getDisputeListByCheckin(route.params.checkinId) || []
  } catch (e) {
    if (e?.message) ElMessage.error(e.message)
  } finally {
    disputeSubmitting.value = false
  }
}

async function loadData() {
  loading.value = true
  dataLoaded.value = false
  try {
    checkin.value = await getCheckinDetail(route.params.checkinId)
    checkinNo.value = checkin.value?.checkinNo || ''

    const [deposits, deductions, fees, disputes] = await Promise.all([
      getDepositListByCheckin(route.params.checkinId).catch(() => []),
      getDeductionListByCheckin(route.params.checkinId).catch(() => []),
      getFeeListByCheckin(route.params.checkinId).catch(() => []),
      getDisputeListByCheckin(route.params.checkinId).catch(() => [])
    ])

    depositList.value = deposits || []
    deductionList.value = deductions || []
    disputeList.value = disputes || []
    unpaidFeeList.value = (fees || []).filter(f => !f.paid)
    unpaidFeeCount.value = unpaidFeeList.value.length
    hasUnpaidFee.value = unpaidFeeCount.value > 0

    try {
      settlement.value = await getSettlementByCheckin(route.params.checkinId)
    } catch (e) {
      settlement.value = null
    }

    try {
      handoverInfo.value = await getHandoverByCheckin(route.params.checkinId, 'CHECKIN')
    } catch (e) {
      handoverInfo.value = null
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
