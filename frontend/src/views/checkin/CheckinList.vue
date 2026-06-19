<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">入住管理</div>
      <div>
        <el-button v-if="userStore.isMerchant" type="primary" :icon="Plus" @click="$router.push('/checkin/create')">
          创建入住单
        </el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 160px;">
            <el-option v-for="item in statusOptions" :key="item.code" :label="item.desc" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="checkinNo" label="入住单号" width="180" />
        <el-table-column prop="roomId" label="房间ID" width="100" />
        <el-table-column prop="checkinDate" label="入住日期" width="120" />
        <el-table-column prop="checkoutDate" label="预计退房" width="120" />
        <el-table-column prop="monthlyRent" label="月租金(元)" width="120" />
        <el-table-column prop="depositAmount" label="押金(元)" width="120" />
        <el-table-column prop="handoverConfirmed" label="交接状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.handoverConfirmed ? 'success' : 'warning'">
              {{ row.handoverConfirmed ? '已确认' : '未确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/checkin/detail/' + row.id)">详情</el-button>
            <el-button v-if="row.status === 'DRAFT' && userStore.isMerchant" link type="primary" @click="handleSubmitHandover(row)">
              提交交接
            </el-button>
            <el-button v-if="row.status === 'HANDED_OVER' && userStore.isMerchant" link type="success" @click="handleConfirmCheckin(row)">
              确认入住
            </el-button>
            <el-button v-if="row.status === 'CHECKED_IN' && userStore.isFinance" link type="warning" @click="handleStartSettlement(row)">
              发起结算
            </el-button>
            <el-button v-if="(row.status === 'DRAFT' || row.status === 'PENDING_HANDOVER') && userStore.isMerchant" link type="danger" @click="handleCancel(row)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getCheckinList, submitHandover, confirmCheckin, startSettlement, cancelCheckin } from '@/api/checkin'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  status: ''
})

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
    DRAFT: 'info',
    PENDING_HANDOVER: 'warning',
    HANDED_OVER: '',
    CHECKED_IN: 'success',
    SETTLING: 'warning',
    SETTLED: 'success',
    CANCELLED: 'danger'
  }
  return map[code] || ''
}

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getCheckinList(searchForm)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  searchForm.status = ''
  loadData()
}

async function handleSubmitHandover(row) {
  await ElMessageBox.confirm('确定提交房间交接吗？', '提示', { type: 'warning' })
  await submitHandover(row.id)
  ElMessage.success('已提交交接')
  loadData()
}

async function handleConfirmCheckin(row) {
  if (!row.handoverConfirmed) {
    ElMessage.warning('房间交接未确认，不能入住')
    return
  }
  await ElMessageBox.confirm('确认该住户已入住吗？', '提示', { type: 'warning' })
  await confirmCheckin(row.id)
  ElMessage.success('已确认入住')
  loadData()
}

async function handleStartSettlement(row) {
  await ElMessageBox.confirm('确定发起退租结算吗？', '提示', { type: 'warning' })
  await startSettlement(row.id)
  ElMessage.success('已发起结算')
  loadData()
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该入住单吗？', '提示', { type: 'warning' })
  await cancelCheckin(row.id)
  ElMessage.success('已取消')
  loadData()
}

onMounted(loadData)
</script>
