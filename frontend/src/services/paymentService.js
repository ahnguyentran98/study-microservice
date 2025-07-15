import apiClient from './apiClient'

export const paymentService = {
  async processPayment(paymentData) {
    const response = await apiClient.post('/payments/process', paymentData)
    return response.data
  },

  async refundPayment(paymentId) {
    const response = await apiClient.post('/payments/refund', { paymentId })
    return response.data
  },

  async getPaymentByOrderId(orderId) {
    const response = await apiClient.get(`/payments/${orderId}`)
    return response.data
  }
}