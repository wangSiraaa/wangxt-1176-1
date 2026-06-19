import { defineStore } from 'pinia'
import { login, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    roleCode: localStorage.getItem('roleCode') || '',
    roleDesc: localStorage.getItem('roleDesc') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isMerchant: (state) => state.roleCode === 'MERCHANT',
    isResident: (state) => state.roleCode === 'RESIDENT',
    isFinance: (state) => state.roleCode === 'FINANCE'
  },
  actions: {
    async login(loginForm) {
      const data = await login(loginForm)
      this.token = data.token
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName
      }
      this.roleCode = data.roleCode
      this.roleDesc = data.roleDesc
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('roleCode', data.roleCode)
      localStorage.setItem('roleDesc', data.roleDesc)
      return data
    },
    async fetchCurrentUser() {
      try {
        const data = await getCurrentUser()
        this.userInfo = data
        this.roleCode = data.roleCode
        localStorage.setItem('userInfo', JSON.stringify(data))
        localStorage.setItem('roleCode', data.roleCode)
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.roleCode = ''
      this.roleDesc = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('roleCode')
      localStorage.removeItem('roleDesc')
    },
    hasRole(role) {
      if (Array.isArray(role)) {
        return role.includes(this.roleCode)
      }
      return this.roleCode === role
    }
  }
})
