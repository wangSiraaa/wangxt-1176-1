<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="28" color="#fff"><House /></el-icon>
        <span>人才公寓管理</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#b8c2cc"
        active-text-color="#fff"
        class="menu"
      >
        <el-menu-item v-for="item in menuList" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ $route.meta?.title || '首页' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="user-info">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-name">
              <el-icon size="16"><User /></el-icon>
              {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              <el-tag size="small" :type="roleTagType" style="margin-left: 8px;">
                {{ userStore.roleDesc }}
              </el-tag>
              <el-icon size="14" style="margin-left: 4px;"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const roleTagType = computed(() => {
  const map = {
    MERCHANT: '',
    RESIDENT: 'success',
    FINANCE: 'warning'
  }
  return map[userStore.roleCode] || 'info'
})

const menuList = computed(() => {
  const allMenus = [
    { path: '/dashboard', title: '首页', icon: 'HomeFilled', roles: ['MERCHANT', 'FINANCE', 'RESIDENT'] },
    { path: '/checkin', title: '入住管理', icon: 'OfficeBuilding', roles: ['MERCHANT', 'FINANCE', 'RESIDENT'] },
    { path: '/checkin/create', title: '创建入住单', icon: 'Plus', roles: ['MERCHANT'] },
    { path: '/handover', title: '验房交接', icon: 'Select', roles: ['MERCHANT', 'RESIDENT'] },
    { path: '/deposit', title: '押金收费', icon: 'Money', roles: ['FINANCE', 'RESIDENT'] },
    { path: '/settlement', title: '退租结算', icon: 'DataAnalysis', roles: ['FINANCE', 'MERCHANT', 'RESIDENT'] },
    { path: '/room', title: '房间管理', icon: 'House', roles: ['MERCHANT', 'FINANCE'] }
  ]
  return allMenus.filter(m => m.roles.includes(userStore.roleCode))
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #001529;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 16px;
    font-weight: 600;
    gap: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .menu {
    border: none;
  }
}

.header {
  background: #fff;
  border-bottom: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .breadcrumb {
    flex: 1;
  }

  .user-info {
    .user-name {
      cursor: pointer;
      display: flex;
      align-items: center;
      color: $text-color;
    }
  }
}

.main {
  background-color: $bg-color;
  padding: 0;
  overflow-y: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
