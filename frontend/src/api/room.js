import request from '@/utils/request'

export function getRoomList(params) {
  return request({
    url: '/room/list',
    method: 'get',
    params
  })
}

export function getVacantRooms() {
  return request({
    url: '/room/vacant',
    method: 'get'
  })
}

export function getRoomDetail(id) {
  return request({
    url: '/room/' + id,
    method: 'get'
  })
}
