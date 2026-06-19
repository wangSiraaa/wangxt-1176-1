import request from '@/utils/request'

export function createDispute(data) {
  return request({
    url: '/dispute/create',
    method: 'post',
    data
  })
}

export function reviewDispute(id, data) {
  return request({
    url: '/dispute/review/' + id,
    method: 'post',
    data
  })
}

export function getDisputeDetail(id) {
  return request({
    url: '/dispute/' + id,
    method: 'get'
  })
}

export function getDisputeListByCheckin(checkinId) {
  return request({
    url: '/dispute/listByCheckin/' + checkinId,
    method: 'get'
  })
}

export function getDisputeList(params) {
  return request({
    url: '/dispute/list',
    method: 'get',
    params
  })
}
