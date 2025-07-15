import apiClient from './apiClient'

export const orderService = {
  async createOrder(orderData) {
    const response = await apiClient.post('/orders', orderData)
    return response.data
  },

  async getUserOrders(userId) {
    const response = await apiClient.get(`/orders/${userId}`)
    return response.data
  },

  async getOrder(orderId) {
    const response = await apiClient.get(`/orders/${orderId}`)
    return response.data
  },

  async updateOrderStatus(orderId, status) {
    const response = await apiClient.put(`/orders/${orderId}/status`, { status })
    return response.data
  }
}