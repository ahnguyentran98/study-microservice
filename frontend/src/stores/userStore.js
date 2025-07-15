import { defineStore } from 'pinia'
import { userService } from '@/services/userService'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: false,
    loading: false,
    error: null
  }),

  getters: {
    getCurrentUser: (state) => state.user,
    getToken: (state) => state.token,
    isLoggedIn: (state) => state.isAuthenticated
  },

  actions: {
    async register(userData) {
      this.loading = true
      this.error = null
      
      try {
        const response = await userService.register(userData)
        this.user = response.user
        this.token = response.token
        this.isAuthenticated = true
        localStorage.setItem('token', response.token)
        return response
      } catch (error) {
        this.error = error.response?.data?.message || 'Registration failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async login(credentials) {
      this.loading = true
      this.error = null
      
      try {
        const response = await userService.login(credentials)
        this.user = response.user
        this.token = response.token
        this.isAuthenticated = true
        localStorage.setItem('token', response.token)
        return response
      } catch (error) {
        this.error = error.response?.data?.message || 'Login failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProfile() {
      if (!this.token) return
      
      this.loading = true
      try {
        const user = await userService.getProfile()
        this.user = user
        this.isAuthenticated = true
      } catch (error) {
        this.logout()
      } finally {
        this.loading = false
      }
    },

    async updateProfile(profileData) {
      this.loading = true
      this.error = null
      
      try {
        const updatedUser = await userService.updateProfile(profileData)
        this.user = updatedUser
        return updatedUser
      } catch (error) {
        this.error = error.response?.data?.message || 'Profile update failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      localStorage.removeItem('token')
    },

    initializeAuth() {
      if (this.token) {
        this.fetchProfile()
      }
    }
  }
})