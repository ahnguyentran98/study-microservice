import apiClient from './apiClient'

export const productService = {
  async getProducts() {
    const response = await apiClient.get('/products')
    return response.data
  },

  async getProduct(id) {
    const response = await apiClient.get(`/products/${id}`)
    return response.data
  },

  async searchProducts(query) {
    const response = await apiClient.post('/products/search', { query })
    return response.data
  },

  async updateInventory(productId, quantity) {
    const response = await apiClient.put(`/products/${productId}/inventory`, { quantity })
    return response.data
  }
}