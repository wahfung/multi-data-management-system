import request from '@/utils/request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  }
}

export const vendorApi = {
  page(params) {
    return request.get('/vendor/page', { params })
  },
  list() {
    return request.get('/vendor/list')
  },
  getById(id) {
    return request.get(`/vendor/${id}`)
  },
  create(data) {
    return request.post('/vendor', data)
  },
  update(data) {
    return request.put('/vendor', data)
  },
  delete(id) {
    return request.delete(`/vendor/${id}`)
  },
  testConnection(id) {
    return request.post(`/vendor/${id}/test`)
  },
  getAdapterTypes() {
    return request.get('/vendor/adapter-types')
  }
}

export const syncApi = {
  sync(data) {
    return request.post('/sync', data)
  },
  pull(vendorId, endpoint) {
    return request.post('/sync/pull', null, {
      params: { vendorId, endpoint }
    })
  },
  push(vendorId, endpoint, data) {
    return request.post('/sync/push', data, {
      params: { vendorId, endpoint }
    })
  }
}

export const recordApi = {
  page(params) {
    return request.get('/record/page', { params })
  },
  getById(id) {
    return request.get(`/record/${id}`)
  },
  statistics() {
    return request.get('/record/statistics')
  }
}

export const dashboardApi = {
  getData() {
    return request.get('/dashboard')
  }
}
