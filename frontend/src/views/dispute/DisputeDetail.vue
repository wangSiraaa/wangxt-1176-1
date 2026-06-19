<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="title">争议详情</div>
      <div>
        <el-tag v-if="dispute" :type="disputeStatusTagType(dispute.status)" style="margin-right: 12px;">
          {{ disputeStatusText(dispute.status) }}
        </el-tag>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div v-if="dispute">
      <el-row :gutter="20">
        <el-col :span="14">
          <el-card style="margin-bottom: 20px;">
            <template #header><span>争议信息</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="争议类型">{{ disputeTypeText(dispute.disputeType) }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="disputeStatusTagType(dispute.status)" size="small">
                  {{ disputeStatusText(dispute.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="提交时间" :span="2">{{ dispute.createTime }}</el-descriptions-item>
              <el-descriptions-item label="争议内容" :span="2">{{ dispute.content }}</el-descriptions-item>
            </el-descriptions>

            <div v-if="dispute.evidenceUrls" style="margin-top: 16px;">
              <div style="font-weight: 600; margin-bottom: 8px;">住户证据</div>
              <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                <el-image
                  v-for="(url, idx) in dispute.evidenceUrls.split(',').filter(u => u.trim())"
                  :key="idx"
                  :src="url.trim()"
                  :preview-src-list="dispute.evidenceUrls.split(',').filter(u => u.trim()).map(u => u.trim())"
                  :initial-index="idx"
                  fit="cover"
                  style="width: 160px; height: 120px; border-radius: 4px;"
                />
              </div>
            </div>
          </el-card>

          <el-card v-if="dispute.deductionId" style="margin-bottom: 20px;">
            <template #header><span>关联扣款信息</span></template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="扣款类型">{{ deductionTypeText(dispute.deductionType) }}</el-descriptions-item>
              <el-descriptions-item label="扣款金额">¥{{ Number(dispute.deductionAmount || 0).toFixed(2) }}</el-descriptions-item>
              <el-descriptions-item label="扣款说明" :span="2">{{ dispute.deductionDescription || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <el-col :span="10">
          <el-card style="margin-bottom: 20px;">
            <template #header><span>复核意见</span></template>
            <div v-if="dispute.status === 'PENDING' || dispute.status === 'REVIEWING'">
              <div v-if="userStore.isFinance || userStore.isMerchant" style="margin-bottom: 16px;">
                <el-form :model="reviewForm" label-width="100px" size="small">
                  <el-form-item label="审核意见">
                    <el-input v-model="reviewForm.reviewOpinion" type="textarea" :rows="3" placeholder="请输入审核意见" />
                  </el-form-item>
                  <el-form-item label="调整金额">
                    <el-input-number
                      v-model="reviewForm.adjustedAmount"
                      :min="0"
                      :precision="2"
                      placeholder="通过时调整后的金额"
                      style="width: 100%;"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="success" size="small" @click="handleReview('APPROVED')">通过</el-button>
                    <el-button type="danger" size="small" @click="handleReview('REJECTED')">驳回</el-button>
                  </el-form-item>
                </el-form>
              </div>
              <div v-else style="padding: 20px; text-align: center; color: #909399;">
                等待财务/招商主管审核
              </div>
            </div>
            <div v-else-if="dispute.reviewOpinion">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="审核结果">
                  <el-tag :type="dispute.status === 'APPROVED' ? 'success' : 'danger'">
                    {{ disputeStatusText(dispute.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="审核意见">{{ dispute.reviewOpinion }}</el-descriptions-item>
                <el-descriptions-item v-if="dispute.adjustedAmount != null" label="调整后金额">
                  <span style="color: #e6a23c; font-weight: 600;">¥{{ Number(dispute.adjustedAmount).toFixed(2) }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="审核时间">{{ dispute.reviewTime || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
            <div v-else style="padding: 20px; text-align: center; color: #909399;">暂无复核意见</div>
          </el-card>

          <el-card>
            <template #header><span>快捷操作</span></template>
            <div style="display: flex; flex-direction: column; gap: 8px;">
              <el-button @click="$router.push('/deposit/refund/' + dispute.checkinId)">查看押金结算</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="!dispute && !loading" style="text-align: center; padding: 40px; color: #909399;">
      争议记录不存在或已被删除
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getDisputeDetail, reviewDispute } from '@/api/dispute'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const dispute = ref(null)

const reviewForm = reactive({
  reviewOpinion: '',
  adjustedAmount: 0
})

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

function deductionTypeText(code) {
  const map = { DAMAGE: '物品损坏', CLEAN: '清洁费', UTILITY: '水电费', OVERDUE: '逾期罚款', OTHER: '其他' }
  return map[code] || code || '-'
}

async function loadData() {
  loading.value = true
  try {
    dispute.value = await getDisputeDetail(route.params.id)
  } catch (e) {
    dispute.value = null
    ElMessage.error('加载失败：' + (e?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

async function handleReview(status) {
  try {
    await ElMessageBox.confirm(
      status === 'APPROVED' ? '确认通过该争议复核吗？' : '确认驳回该争议复核吗？',
      '审核确认',
      { type: 'warning' }
    )

    await reviewDispute(route.params.id, {
      reviewOpinion: reviewForm.reviewOpinion,
      adjustedAmount: status === 'APPROVED' ? reviewForm.adjustedAmount : null,
      status
    })
    ElMessage.success(status === 'APPROVED' ? '已通过' : '已驳回')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '审核失败')
    }
  }
}

onMounted(loadData)
</script>
