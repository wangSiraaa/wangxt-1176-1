<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">退租结算</div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 160px;">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="已完成" value="COMPLETED" />
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
        <el-table-column prop="settlementNo" label="结算单号" width="180" />
        <el-table-column prop="checkinId" label="入住单ID" width="120" />
        <el-table-column prop="totalRent" label="应付租金(元)" width="130" />
        <el-table-column prop="totalPaid" label="已缴金额(元)" width="130" />
        <el-table-column prop="unpaidAmount" label="欠费(元)" width="130">
          <template #default="{ row }">
            <span :style="{ color: Number(row.unpaidAmount) > 0 ? '#f56c6c' : '#67c23a' }">
              ¥{{ row.unpaidAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalDeposit" label="押金总额(元)" width="130" />
        <el-table-column prop="totalDeduction" label="扣款总额(元)" width="130" />
        <el-table-column prop="refundAmount" label="应退/应补(元)" width="130">
          <template #default="{ row }">
            <span :style="{ color: Number(row.refundAmount) >= 0 ? '#67c23a' : '#f56c6c' }">
              {{ Number(row.refundAmount) >= 0 ? '应退 ' : '应补 ' }}¥{{ Math.abs(row.refundAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/settlement/detail/' + row.id)">详情</el-button>
            <el-button
              v-if="row.status === 'DRAFT' && userStore.isFinance"
              link type="primary"
              @click="handleConfirm(row)"
            >确认结算</el-button>
            <el-button
              v-if="row.status === 'CONFIRMED' && userStore.isFinance && Number(row.unpaidAmount) === 0"
              link type="success"
              @click="handleComplete(row)"
            >完成结算</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getSettlementList, confirmSettlement, completeSettlement } from '@/api/settlement'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  status: ''
})

function statusText(code) {
  const map = { DRAFT: '草稿', CONFIRMED: '已确认', COMPLETED: '已完成' }
  return map[code] || code
}

function statusTagType(code) {
  const map = { DRAFT: 'info', CONFIRMED: 'warning', COMPLETED: 'success' }
  return map[code] || ''
}

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getSettlementList(searchForm)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  searchForm.status = ''
  loadData()
}

async function handleConfirm(row) {
  await ElMessageBox.confirm(
    '确认结算后将锁定所有押金扣款明细，无法再修改，是否继续？',
    '确认结算',
    { type: 'warning' }
  )
  await confirmSettlement(row.id)
  ElMessage.success('结算已确认')
  loadData()
}

async function handleComplete(row) {
  if (Number(row.unpaidAmount) > 0) {
    ElMessage.warning('存在未结清费用，不能完成结算')
    return
  }
  await ElMessageBox.confirm('确认完成该退租结算吗？', '完成结算', { type: 'warning' })
  await completeSettlement(row.id)
  ElMessage.success('结算已完成')
  loadData()
}

onMounted(loadData)
</script>
