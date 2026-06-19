<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="title">入住单详情</div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <el-card v-if="detail" style="margin-bottom: 20px;">
      <template #header>
        <span>基本信息</span>
        <el-tag :type="statusTagType(detail.status)" style="margin-left: 10px;">
          {{ statusText(detail.status) }}
        </el-tag>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入住单号">{{ detail.checkinNo }}</el-descriptions-item>
        <el-descriptions-item label="房间ID">{{ detail.roomId }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ detail.checkinDate }}</el-descriptions-item>
        <el-descriptions-item label="预计退房">{{ detail.checkoutDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际入住">{{ detail.actualCheckinDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际退房">{{ detail.actualCheckoutDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="月租金(元)">¥{{ detail.monthlyRent }}</el-descriptions-item>
        <el-descriptions-item label="押金金额(元)">¥{{ detail.depositAmount }}</el-descriptions-item>
        <el-descriptions-item label="交接确认">
          <el-tag :type="detail.handoverConfirmed ? 'success' : 'warning'">
            {{ detail.handoverConfirmed ? '已确认' : '未确认' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>操作记录</span>
          </template>
          <el-timeline>
            <el-timeline-item timestamp="创建" placement="top">
              <el-card shadow="never" style="background: #f5f7fa;">
                创建人: 招商主管<br>
                时间: {{ detail?.createTime }}
              </el-card>
            </el-timeline-item>
            <el-timeline-item v-if="detail?.status !== 'DRAFT'" timestamp="提交交接" placement="top" type="warning">
              <el-card shadow="never" style="background: #fdf6ec;">
                状态变更为: 待交接
              </el-card>
            </el-timeline-item>
            <el-timeline-item v-if="detail?.handoverConfirmed" timestamp="交接完成" placement="top" type="primary">
              <el-card shadow="never" style="background: #ecf5ff;">
                住户与招商主管双方已确认交接
              </el-card>
            </el-timeline-item>
            <el-timeline-item v-if="detail?.status === 'CHECKED_IN' || detail?.status === 'SETTLING' || detail?.status === 'SETTLED'"
              timestamp="已入住" placement="top" type="success">
              <el-card shadow="never" style="background: #f0f9eb;">
                已确认正式入住
              </el-card>
            </el-timeline-item>
            <el-timeline-item v-if="detail?.status === 'SETTLING' || detail?.status === 'SETTLED'"
              timestamp="结算中" placement="top" type="warning">
              <el-card shadow="never" style="background: #fdf6ec;">
                已发起退租结算
              </el-card>
            </el-timeline-item>
            <el-timeline-item v-if="detail?.status === 'SETTLED'" timestamp="已结算" placement="top" type="success">
              <el-card shadow="never" style="background: #f0f9eb;">
                退租结算已完成
              </el-card>
            </el-timeline-item>
            <el-timeline-item v-if="detail?.status === 'CANCELLED'" timestamp="已取消" placement="top" type="danger">
              <el-card shadow="never" style="background: #fef0f0;">
                入住单已取消
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>快捷操作</span>
          </template>
          <div style="display: flex; flex-direction: column; gap: 12px;">
            <el-button
              v-if="detail?.status === 'DRAFT' && userStore.isMerchant"
              type="primary"
              @click="handleSubmitHandover"
            >提交交接</el-button>
            <el-button
              v-if="(detail?.status === 'PENDING_HANDOVER' || detail?.status === 'HANDED_OVER')
                && (userStore.isMerchant || userStore.isResident)"
              type="primary"
              @click="$router.push('/handover/' + detail?.id)"
            >前往验房交接</el-button>
            <el-button
              v-if="detail?.status === 'HANDED_OVER' && userStore.isMerchant"
              type="success"
              @click="handleConfirmCheckin"
            >确认入住</el-button>
            <el-button
              v-if="detail?.status === 'CHECKED_IN' && userStore.isFinance"
              type="primary"
              @click="$router.push('/deposit/collect/' + detail?.id)"
            >收取押金</el-button>
            <el-button
              v-if="(detail?.status === 'CHECKED_IN' || detail?.status === 'SETTLING') && userStore.isFinance"
              type="warning"
              @click="$router.push('/settlement/create/' + detail?.id)"
            >创建退租结算</el-button>
            <el-button
              v-if="(detail?.status === 'SETTLING' || detail?.status === 'SETTLED') && userStore.isFinance"
              type="success"
              @click="$router.push('/deposit/refund/' + detail?.id)"
            >退还押金</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCheckinDetail, submitHandover, confirmCheckin } from '@/api/checkin'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(null)

const statusOptions = [
  { code: 'DRAFT', desc: '草稿' },
  { code: 'PENDING_HANDOVER', desc: '待交接' },
  { code: 'HANDED_OVER', desc: '已交接' },
  { code: 'CHECKED_IN', desc: '已入住' },
  { code: 'SETTLING', desc: '结算中' },
  { code: 'SETTLED', desc: '已结算' },
  { code: 'CANCELLED', desc: '已取消' }
]

function statusText(code) {
  const item = statusOptions.find(s => s.code === code)
  return item ? item.desc : code
}

function statusTagType(code) {
  const map = {
    DRAFT: 'info', PENDING_HANDOVER: 'warning', HANDED_OVER: '',
    CHECKED_IN: 'success', SETTLING: 'warning', SETTLED: 'success', CANCELLED: 'danger'
  }
  return map[code] || ''
}

async function loadData() {
  loading.value = true
  try {
    detail.value = await getCheckinDetail(route.params.id)
  } finally {
    loading.value = false
  }
}

async function handleSubmitHandover() {
  await ElMessageBox.confirm('确定提交房间交接吗？', '提示', { type: 'warning' })
  await submitHandover(route.params.id)
  ElMessage.success('已提交交接')
  loadData()
}

async function handleConfirmCheckin() {
  if (!detail.value.handoverConfirmed) {
    ElMessage.warning('房间交接未确认，不能入住')
    return
  }
  await ElMessageBox.confirm('确认该住户已入住吗？', '提示', { type: 'warning' })
  await confirmCheckin(route.params.id)
  ElMessage.success('已确认入住')
  loadData()
}

onMounted(loadData)
</script>
