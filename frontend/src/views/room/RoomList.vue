<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">房间管理</div>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="楼栋">
          <el-input v-model="searchForm.building" placeholder="请输入楼栋" clearable />
        </el-form-item>
        <el-form-item label="房号">
          <el-input v-model="searchForm.roomNo" placeholder="请输入房号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px;">
            <el-option label="空置" value="VACANT" />
            <el-option label="已入住" value="OCCUPIED" />
            <el-option label="维修中" value="MAINTENANCE" />
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
        <el-table-column prop="roomNo" label="房间编号" width="120" />
        <el-table-column prop="building" label="楼栋" width="100" />
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="roomType" label="房型" width="120" />
        <el-table-column prop="area" label="面积(㎡)" width="100" />
        <el-table-column prop="monthlyRent" label="月租金(元)" width="120" />
        <el-table-column prop="depositAmount" label="押金(元)" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoomList } from '@/api/room'

const loading = ref(false)
const tableData = ref([])

const searchForm = reactive({
  status: '',
  building: '',
  roomNo: ''
})

function statusText(code) {
  const map = { VACANT: '空置', OCCUPIED: '已入住', MAINTENANCE: '维修中' }
  return map[code] || code
}

function statusTagType(code) {
  const map = { VACANT: 'success', OCCUPIED: 'warning', MAINTENANCE: 'danger' }
  return map[code] || ''
}

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getRoomList(searchForm)
  } finally {
    loading.value = false
  }
}

function resetForm() {
  searchForm.status = ''
  searchForm.building = ''
  searchForm.roomNo = ''
  loadData()
}

onMounted(loadData)
</script>
