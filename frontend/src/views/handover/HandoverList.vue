<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">验房交接</div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 160px;">
            <el-option label="待交接" value="PENDING_HANDOVER" />
            <el-option label="已交接" value="HANDED_OVER" />
            <el-option label="已入住" value="CHECKED_IN" />
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
        <el-table-column prop="checkinDate" label="入住日期" width="120" />
        <el-table-column prop="status" label="入住单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="交接状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.handoverConfirmed ? 'success' : 'warning'">
              {{ row.handoverConfirmed ? '已确认' : '未确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push('/handover/' + row.id)">
              {{ row.handoverConfirmed ? '查看交接' : '办理交接' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCheckinList } from '@/api/checkin'

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
      data = data.filter(d => ['PENDING_HANDOVER', 'HANDED_OVER', 'CHECKED_IN'].includes(d.status))
    }
    tableData.value = data
  } finally {
    loading.value = false
  }
}

function resetForm() {
  searchForm.status = ''
  loadData()
}

onMounted(loadData)
</script>
