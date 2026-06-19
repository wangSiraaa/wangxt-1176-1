import request from '@/utils/request'

export function createCheckin(data) {
  return request({
    url: '/checkin/create',
    method: 'post',
    data
  })
}

export function getCheckinDetail(id) {
  return request({
    url: '/checkin/' + id,
    method: 'get'
  })
}

export function getCheckinList(params) {
  return request({
    url: '/checkin/list',
    method: 'get',
    params
  })
}

export function submitHandover(id) {
  return request({
    url: '/checkin/submitHandover/' + id,
    method: 'post'
  })
}

export function confirmCheckin(id) {
  return request({
    url: '/checkin/confirmCheckin/' + id,
    method: 'post'
  })
}

export function startSettlement(id) {
  return request({
    url: '/checkin/startSettlement/' + id,
    method: 'post'
  })
}

export function cancelCheckin(id) {
  return request({
    url: '/checkin/cancel/' + id,
    method: 'post'
  })
}
