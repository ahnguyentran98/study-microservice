import apiClient from './apiClient'

export const userService = {
  async register(userData) {
    const response = await apiClient.post('/users/register', userData)
    return response.data
  },

  async login(credentials) {
    const response = await apiClient.post('/users/login', credentials)
    return response.data
  },

  async getProfile() {
    const response = await apiClient.get('/users/profile')
    return response.data
  },

  async updateProfile(profileData) {
    const response = await apiClient.put('/users/profile', profileData)
    return response.data
  }
}