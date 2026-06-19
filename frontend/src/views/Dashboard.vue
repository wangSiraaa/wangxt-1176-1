<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title">欢迎，{{ userStore.userInfo?.realName }}</div>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>入住单状态说明</span>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="草稿">招商主管刚创建，尚未提交交接</el-descriptions-item>
            <el-descriptions-item label="待交接">已提交，等待住户和招商主管双方确认房间交接</el-descriptions-item>
            <el-descriptions-item label="已交接">双方已确认交接，可确认入住</el-descriptions-item>
            <el-descriptions-item label="已入住">住户已入住</el-descriptions-item>
            <el-descriptions-item label="结算中">已发起退租结算</el-descriptions-item>
            <el-descriptions-item label="已结算">退租结算完成，房间已释放</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>业务流程说明</span>
          </template>
          <el-steps direction="vertical" :active="6" finish-status="success">
            <el-step title="招商主管创建入住单" description="选择空闲房间和住户" />
            <el-step title="提交房间交接" description="等待双方确认验房" />
            <el-step title="双方确认交接" description="住户确认签字 + 招商主管抄表签字" />
            <el-step title="确认入住" description="交接完成后确认正式入住" />
            <el-step title="财务收取押金" description="确认入住后财务收取押金" />
            <el-step title="退租结算" description="添加扣款 → 创建结算 → 确认 → 完成 → 退还押金" />
          </el-steps>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const stats = ref([
  { label: '房间总数', value: 6, icon: 'House', color: '#409eff' },
  { label: '空置房间', value: 6, icon: 'HomeFilled', color: '#67c23a' },
  { label: '进行中入住', value: 0, icon: 'OfficeBuilding', color: '#e6a23c' },
  { label: '已完成结算', value: 0, icon: 'DataAnalysis', color: '#909399' }
])
</script>

<style lang="scss" scoped>
.stat-card {
  .stat-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: $text-color;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 14px;
      color: $text-color-secondary;
      margin-top: 4px;
    }
  }
}
</style>
