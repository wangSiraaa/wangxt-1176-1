import request from '@/utils/request'

export function createSettlement(data) {
  return request({
    url: '/settlement/create',
    method: 'post',
    data
  })
}

export function confirmSettlement(id) {
  return request({
    url: '/settlement/confirm/' + id,
    method: 'post'
  })
}

export function completeSettlement(id) {
  return request({
    url: '/settlement/complete/' + id,
    method: 'post'
  })
}

export function getSettlementDetail(id) {
  return request({
    url: '/settlement/' + id,
    method: 'get'
  })
}

export function getSettlementByCheckin(checkinId) {
  return request({
    url: '/settlement/getByCheckin/' + checkinId,
    method: 'get'
  })
}

export function getSettlementList(params) {
  return request({
    url: '/settlement/list',
    method: 'get',
    params
  })
}
