import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function getCurrentUser() {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

export function getUserListByRole(roleCode) {
  return request({
    url: '/user/listByRole',
    method: 'get',
    params: { roleCode }
  })
}
