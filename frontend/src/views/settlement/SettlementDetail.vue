<template>
  <div class="page-container" v-loading="loading">
    <div class="page-header">
      <div class="title">结算单详情 - {{ settlement?.settlementNo }}</div>
      <div>
        <el-tag :type="statusTagType(settlement?.status)" style="margin-right: 12px;">
          {{ statusText(settlement?.status) }}
        </el-tag>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <el-card style="margin-bottom: 20px;">
      <template #header>
        <span>结算概览</span>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="入住单号">{{ settlement?.checkinNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间ID">{{ settlement?.roomId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="住户ID">{{ settlement?.residentId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际入住日">{{ settlement?.actualCheckinDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实际退房日">{{ settlement?.actualCheckoutDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="租赁月数">{{ settlement?.rentMonths || 0 }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-statistic title="应付租金" :value="settlement?.totalRent || 0" :precision="2" />
      </el-col>
      <el-col :span="8">
        <el-statistic title="已缴金额" :value="settlement?.totalPaid || 0" :precision="2" />
      </el-col>
      <el-col :span="8">
        <el-statistic
          title="欠费金额"
          :value="settlement?.unpaidAmount || 0"
          :precision="2"
          :value-style="{ color: Number(settlement?.unpaidAmount) > 0 ? '#f56c6c' : '#67c23a' }"
        />
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-statistic title="押金总额" :value="settlement?.totalDeposit || 0" :precision="2" />
      </el-col>
      <el-col :span="8">
        <el-statistic title="扣款总额" :value="settlement?.totalDeduction || 0" :precision="2" />
      </el-col>
      <el-col :span="8">
        <el-statistic
          :title="Number(settlement?.refundAmount) >= 0 ? '应退住户' : '住户应补'"
          :value="Math.abs(settlement?.refundAmount || 0)"
          :precision="2"
          :value-style="{ color: Number(settlement?.refundAmount) >= 0 ? '#67c23a' : '#f56c6c' }"
        />
      </el-col>
    </el-row>

    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>押金扣款明细</span>
          <div>
            <el-tag v-if="locked" type="danger" effect="dark" style="margin-right: 10px;">
              <el-icon><Lock /></el-icon> 结算已确认/完成，扣款明细已锁定
            </el-tag>
            <el-button
              v-if="!locked && userStore.isFinance"
              type="primary"
              size="small"
              :icon="Plus"
              @click="showAddDeduction"
            >新增扣款</el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="locked"
        type="warning"
        :closable="false"
        style="margin-bottom: 16px;"
        title="退租结算完成后不能修改押金扣款项"
        show-icon
      />

      <el-table :data="deductions" stripe>
        <el-table-column prop="deductionType" label="扣款类型" width="140">
          <template #default="{ row }">{{ deductionTypeText(row.deductionType) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" width="120" />
        <el-table-column prop="reason" label="扣款原因" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="160" v-if="!locked && userStore.isFinance">
          <template #default="{ row }">
            <el-button link type="primary" @click="editDeduction(row)">编辑</el-button>
            <el-button link type="danger" @click="deleteDeduction(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="deductions.length === 0" style="padding: 40px; text-align: center; color: #909399;">
        暂无扣款明细
      </div>
    </el-card>

    <el-card v-if="settlement" style="margin-bottom: 20px;">
      <template #header><span>操作</span></template>
      <div style="display: flex; gap: 12px;">
        <el-button
          v-if="settlement.status === 'DRAFT' && userStore.isFinance"
          type="primary"
          @click="handleConfirm"
        >确认结算（锁定扣款）</el-button>
        <el-button
          v-if="settlement.status === 'CONFIRMED' && userStore.isFinance"
          type="success"
          :disabled="Number(settlement.unpaidAmount) > 0"
          @click="handleComplete"
        >完成结算</el-button>
        <el-button
          v-if="(settlement.status === 'CONFIRMED' || settlement.status === 'COMPLETED')
            && userStore.isFinance"
          @click="$router.push('/deposit/refund/' + settlement.checkinId)"
        >退还押金</el-button>
      </div>
    </el-card>

    <el-dialog v-model="deductionDialog" :title="editingDeduction.id ? '编辑扣款' : '新增扣款'" width="600px">
      <el-form ref="deductionFormRef" :model="deductionForm" :rules="deductionRules" label-width="120px">
        <el-form-item label="扣款类型" prop="deductionType">
          <el-select v-model="deductionForm.deductionType" placeholder="请选择" style="width: 100%;">
            <el-option label="物品损坏" value="DAMAGE" />
            <el-option label="清洁费" value="CLEAN" />
            <el-option label="水电费" value="UTILITY" />
            <el-option label="逾期罚款" value="OVERDUE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="扣款金额(元)" prop="amount">
          <el-input-number v-model="deductionForm.amount" :min="0.01" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="扣款原因" prop="reason">
          <el-input v-model="deductionForm.reason" type="textarea" :rows="3" placeholder="请说明扣款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deductionDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDeduction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getSettlementDetail, confirmSettlement, completeSettlement } from '@/api/settlement'
import {
  createDeduction, updateDeduction, deleteDeduction as apiDeleteDeduction,
  getDeductionListByCheckin
} from '@/api/deduction'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const settlement = ref(null)
const deductions = ref([])

const locked = computed(() => {
  return settlement.value && ['CONFIRMED', 'COMPLETED'].includes(settlement.value.status)
})

const deductionDialog = ref(false)
const deductionFormRef = ref()
const editingDeduction = reactive({ id: null })
const deductionForm = reactive({
  checkinId: null,
  deductionType: '',
  amount: 0,
  reason: ''
})
const deductionRules = {
  deductionType: [{ required: true, message: '请选择扣款类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入扣款金额', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入扣款原因', trigger: 'blur' }]
}

function statusText(code) {
  const map = { DRAFT: '草稿', CONFIRMED: '已确认', COMPLETED: '已完成' }
  return map[code] || code
}

function statusTagType(code) {
  const map = { DRAFT: 'info', CONFIRMED: 'warning', COMPLETED: 'success' }
  return map[code] || ''
}

function deductionTypeText(code) {
  const map = { DAMAGE: '物品损坏', CLEAN: '清洁费', UTILITY: '水电费', OVERDUE: '逾期罚款', OTHER: '其他' }
  return map[code] || code
}

async function loadData() {
  loading.value = true
  try {
    settlement.value = await getSettlementDetail(route.params.id)
    deductions.value = await getDeductionListByCheckin(settlement.value.checkinId)
    deductionForm.checkinId = settlement.value.checkinId
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  await ElMessageBox.confirm(
    '确认结算后将锁定所有押金扣款明细，无法再修改，是否继续？',
    '确认结算',
    { type: 'warning' }
  )
  await confirmSettlement(route.params.id)
  ElMessage.success('结算已确认')
  loadData()
}

async function handleComplete() {
  if (Number(settlement.value.unpaidAmount) > 0) {
    ElMessage.warning('存在未结清费用，不能完成结算')
    return
  }
  await ElMessageBox.confirm('确认完成该退租结算吗？', '完成结算', { type: 'warning' })
  await completeSettlement(route.params.id)
  ElMessage.success('结算已完成')
  loadData()
}

function showAddDeduction() {
  editingDeduction.id = null
  deductionForm.deductionType = ''
  deductionForm.amount = 0
  deductionForm.reason = ''
  deductionDialog.value = true
}

function editDeduction(row) {
  editingDeduction.id = row.id
  deductionForm.deductionType = row.deductionType
  deductionForm.amount = row.amount
  deductionForm.reason = row.reason
  deductionDialog.value = true
}

async function saveDeduction() {
  if (!deductionFormRef.value) return
  try {
    await deductionFormRef.value.validate()
    if (editingDeduction.id) {
      await updateDeduction(editingDeduction.id, deductionForm)
      ElMessage.success('更新成功')
    } else {
      await createDeduction(deductionForm)
      ElMessage.success('添加成功')
    }
    deductionDialog.value = false
    loadData()
  } catch (e) {
    if (e?.message) ElMessage.warning(e.message)
  }
}

async function deleteDeduction(row) {
  await ElMessageBox.confirm('确定删除该扣款明细吗？', '提示', { type: 'warning' })
  await apiDeleteDeduction(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
