<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">押金收费</div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 160px;">
            <el-option label="已入住" value="CHECKED_IN" />
            <el-option label="结算中" value="SETTLING" />
            <el-option label="已结算" value="SETTLED" />
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
        <el-table-column prop="residentId" label="住户ID" width="100" />
        <el-table-column prop="depositAmount" label="押金金额(元)" width="140" />
        <el-table-column label="已收押金(元)" width="140">
          <template #default="{ row }">¥{{ getCollected(row.id) }}</template>
        </el-table-column>
        <el-table-column label="已退押金(元)" width="140">
          <template #default="{ row }">¥{{ getRefunded(row.id) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="入住状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDeposits(row)">明细</el-button>
            <el-button
              v-if="userStore.isFinance && (row.status === 'CHECKED_IN' || row.status === 'SETTLING')"
              link type="primary"
              @click="$router.push('/deposit/collect/' + row.id)"
            >收取押金</el-button>
            <el-button
              v-if="userStore.isFinance && (row.status === 'SETTLING' || row.status === 'SETTLED')"
              link type="success"
              @click="$router.push('/deposit/refund/' + row.id)"
            >退还押金</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDetail" title="押金收支明细" width="800px">
      <el-table :data="depositDetails" v-loading="detailLoading" stripe>
        <el-table-column prop="depositNo" label="押金单号" width="180" />
        <el-table-column prop="transType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.transType === 'COLLECT' ? 'primary' : 'success'">
              {{ row.transType === 'COLLECT' ? '收取' : '退还' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" />
        <el-table-column prop="payMethod" label="支付方式" width="100" />
        <el-table-column prop="payTime" label="时间" width="180" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCheckinList } from '@/api/checkin'
import { getDepositListByCheckin } from '@/api/deposit'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const showDetail = ref(false)
const detailLoading = ref(false)
const depositDetails = ref([])
const depositMap = ref({})

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
    DRAFT: 'info', PENDING_HANDOVER: 'warning', HANDED_OVER: '',
    CHECKED_IN: 'success', SETTLING: 'warning', SETTLED: 'success', CANCELLED: 'danger'
  }
  return map[code] || ''
}

async function loadData() {
  loading.value = true
  try {
    let data = await getCheckinList(searchForm)
    if (!searchForm.status) {
      data = data.filter(d => ['CHECKED_IN', 'SETTLING', 'SETTLED'].includes(d.status))
    }
    tableData.value = data

    for (const row of data) {
      try {
        const deposits = await getDepositListByCheckin(row.id)
        depositMap.value[row.id] = deposits
      } catch (e) {
        depositMap.value[row.id] = []
      }
    }
  } finally {
    loading.value = false
  }
}

function resetForm() {
  searchForm.status = ''
  loadData()
}

function getCollected(checkinId) {
  const list = depositMap.value[checkinId] || []
  return list.filter(d => d.transType === 'COLLECT').reduce((sum, d) => sum + Number(d.amount), 0).toFixed(2)
}

function getRefunded(checkinId) {
  const list = depositMap.value[checkinId] || []
  return list.filter(d => d.transType === 'REFUND').reduce((sum, d) => sum + Number(d.amount), 0).toFixed(2)
}

async function viewDeposits(row) {
  detailLoading.value = true
  showDetail.value = true
  try {
    depositDetails.value = await getDepositListByCheckin(row.id)
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadData)
</script>
