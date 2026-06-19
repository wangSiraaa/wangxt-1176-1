import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'checkin',
        name: 'Checkin',
        component: () => import('@/views/checkin/CheckinList.vue'),
        meta: { title: '入住管理', icon: 'OfficeBuilding', roles: ['MERCHANT', 'FINANCE', 'RESIDENT'] }
      },
      {
        path: 'checkin/create',
        name: 'CheckinCreate',
        component: () => import('@/views/checkin/CheckinCreate.vue'),
        meta: { title: '创建入住单', icon: 'Plus', roles: ['MERCHANT'] }
      },
      {
        path: 'checkin/detail/:id',
        name: 'CheckinDetail',
        component: () => import('@/views/checkin/CheckinDetail.vue'),
        meta: { title: '入住单详情', icon: 'Document', roles: ['MERCHANT', 'FINANCE', 'RESIDENT'] }
      },
      {
        path: 'handover',
        name: 'Handover',
        component: () => import('@/views/handover/HandoverList.vue'),
        meta: { title: '验房交接', icon: 'Select', roles: ['MERCHANT', 'RESIDENT'] }
      },
      {
        path: 'handover/:checkinId',
        name: 'HandoverDetail',
        component: () => import('@/views/handover/HandoverDetail.vue'),
        meta: { title: '交接详情', icon: 'Document', roles: ['MERCHANT', 'RESIDENT'] }
      },
      {
        path: 'deposit',
        name: 'Deposit',
        component: () => import('@/views/deposit/DepositList.vue'),
        meta: { title: '押金收费', icon: 'Money', roles: ['FINANCE', 'RESIDENT'] }
      },
      {
        path: 'deposit/collect/:checkinId',
        name: 'DepositCollect',
        component: () => import('@/views/deposit/DepositCollect.vue'),
        meta: { title: '收取押金', icon: 'Wallet', roles: ['FINANCE'] }
      },
      {
        path: 'deposit/refund/:checkinId',
        name: 'DepositRefund',
        component: () => import('@/views/deposit/DepositRefund.vue'),
        meta: { title: '退还押金', icon: 'Reading', roles: ['FINANCE'] }
      },
      {
        path: 'settlement',
        name: 'Settlement',
        component: () => import('@/views/settlement/SettlementList.vue'),
        meta: { title: '退租结算', icon: 'DataAnalysis', roles: ['FINANCE', 'MERCHANT', 'RESIDENT'] }
      },
      {
        path: 'settlement/create/:checkinId',
        name: 'SettlementCreate',
        component: () => import('@/views/settlement/SettlementCreate.vue'),
        meta: { title: '创建结算单', icon: 'EditPen', roles: ['FINANCE'] }
      },
      {
        path: 'settlement/detail/:id',
        name: 'SettlementDetail',
        component: () => import('@/views/settlement/SettlementDetail.vue'),
        meta: { title: '结算单详情', icon: 'Document', roles: ['FINANCE', 'MERCHANT', 'RESIDENT'] }
      },
      {
        path: 'dispute/detail/:id',
        name: 'DisputeDetail',
        component: () => import('@/views/dispute/DisputeDetail.vue'),
        meta: { title: '争议详情', icon: 'Document', roles: ['FINANCE', 'MERCHANT', 'RESIDENT'] }
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/room/RoomList.vue'),
        meta: { title: '房间管理', icon: 'House', roles: ['MERCHANT', 'FINANCE'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  document.title = (to.meta?.title || '人才公寓押金结算') + ' - 人才公寓入住押金结算系统'

  if (to.meta?.requiresAuth === false) {
    next()
    return
  }

  if (!userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta?.roles && !userStore.hasRole(to.meta.roles)) {
    next('/dashboard')
    return
  }

  next()
})

export default router
