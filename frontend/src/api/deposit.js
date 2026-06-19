import request from '@/utils/request'

export function collectDeposit(data) {
  return request({
    url: '/deposit/collect',
    method: 'post',
    data
  })
}

export function refundDeposit(data) {
  return request({
    url: '/deposit/refund',
    method: 'post',
    data
  })
}

export function getDepositListByCheckin(checkinId) {
  return request({
    url: '/deposit/listByCheckin/' + checkinId,
    method: 'get'
  })
}

export function getDepositDetail(id) {
  return request({
    url: '/deposit/' + id,
    method: 'get'
  })
}
