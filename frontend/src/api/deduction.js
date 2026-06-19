import request from '@/utils/request'

export function createDeduction(data) {
  return request({
    url: '/deduction/create',
    method: 'post',
    data
  })
}

export function updateDeduction(id, data) {
  return request({
    url: '/deduction/update/' + id,
    method: 'put',
    data
  })
}

export function deleteDeduction(id) {
  return request({
    url: '/deduction/delete/' + id,
    method: 'delete'
  })
}

export function getDeductionListByCheckin(checkinId) {
  return request({
    url: '/deduction/listByCheckin/' + checkinId,
    method: 'get'
  })
}

export function getDeductionDetail(id) {
  return request({
    url: '/deduction/' + id,
    method: 'get'
  })
}
