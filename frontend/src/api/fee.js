import request from '@/utils/request'

export function getUnpaidFeesByCheckin(checkinId) {
  return request({
    url: '/fee/listUnpaid/' + checkinId,
    method: 'get'
  })
}

export function getFeeListByCheckin(checkinId) {
  return request({
    url: '/fee/listByCheckin/' + checkinId,
    method: 'get'
  })
}
