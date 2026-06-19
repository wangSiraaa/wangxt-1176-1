<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">创建入住单</div>
      <div>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div class="form-container">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="选择房间" prop="roomId">
          <el-select v-model="form.roomId" placeholder="请选择房间" style="width: 100%;" filterable>
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="room.roomNo + ' - ' + room.building + room.floor + '层 ' + (room.roomType || '')"
              :value="room.id"
            >
              <span>{{ room.roomNo }} - {{ room.building }}{{ room.floor }}层 {{ room.roomType || '' }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px;">
                租金: ¥{{ room.monthlyRent }} / 押金: ¥{{ room.depositAmount }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="选择住户" prop="residentId">
          <el-select v-model="form.residentId" placeholder="请选择住户" style="width: 100%;" filterable>
            <el-option
              v-for="user in residentList"
              :key="user.id"
              :label="user.realName + ' (' + user.username + ')'"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="入住日期" prop="checkinDate">
          <el-date-picker v-model="form.checkinDate" type="date" placeholder="选择入住日期" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="预计退房日期" prop="checkoutDate">
          <el-date-picker v-model="form.checkoutDate" type="date" placeholder="选择预计退房日期" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="月租金(元)" prop="monthlyRent">
          <el-input-number v-model="form.monthlyRent" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="押金金额(元)" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" :step="100" style="width: 100%;" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">提交</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVacantRooms } from '@/api/room'
import { getUserListByRole } from '@/api/auth'
import { createCheckin } from '@/api/checkin'

const router = useRouter()

const formRef = ref()
const loading = ref(false)
const roomList = ref([])
const residentList = ref([])

const form = reactive({
  roomId: null,
  residentId: null,
  checkinDate: '',
  checkoutDate: '',
  monthlyRent: 0,
  depositAmount: 0,
  remark: ''
})

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  residentId: [{ required: true, message: '请选择住户', trigger: 'change' }],
  checkinDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  monthlyRent: [{ required: true, message: '请输入月租金', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '请输入押金金额', trigger: 'blur' }]
}

watch(() => form.roomId, (val) => {
  if (val) {
    const room = roomList.value.find(r => r.id === val)
    if (room) {
      form.monthlyRent = room.monthlyRent
      form.depositAmount = room.depositAmount
    }
  }
})

async function loadData() {
  roomList.value = await getVacantRooms()
  residentList.value = await getUserListByRole('RESIDENT')
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    await createCheckin(form)
    ElMessage.success('创建成功')
    router.push('/checkin')
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
