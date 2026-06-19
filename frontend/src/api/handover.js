import request from '@/utils/request'

export function getHandoverByCheckin(checkinId, handoverType = 'CHECKIN') {
  return request({
    url: '/handover/getByCheckin',
    method: 'get',
    params: { checkinId, handoverType }
  })
}

export function createCheckinHandover(checkinId) {
  return request({
    url: '/handover/createCheckin/' + checkinId,
    method: 'post'
  })
}

export function residentSign(id) {
  return request({
    url: '/handover/residentSign/' + id,
    method: 'post'
  })
}

export function merchantSign(id, data) {
  return request({
    url: '/handover/merchantSign/' + id,
    method: 'post',
    data
  })
}
