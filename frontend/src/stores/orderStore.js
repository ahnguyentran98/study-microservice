import { defineStore } from 'pinia'
import { orderService } from '@/services/orderService'
import { paymentService } from '@/services/paymentService'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null,
    cart: [],
    loading: false,
    error: null
  }),

  getters: {
    getOrders: (state) => state.orders,
    getCurrentOrder: (state) => state.currentOrder,
    getCart: (state) => state.cart,
    getCartTotal: (state) => {
      return state.cart.reduce((total, item) => total + (item.price * item.quantity), 0)
    },
    getCartItemCount: (state) => {
      return state.cart.reduce((total, item) => total + item.quantity, 0)
    }
  },

  actions: {
    async createOrder(orderData) {
      this.loading = true
      this.error = null
      
      try {
        const order = await orderService.createOrder(orderData)
        this.orders.push(order)
        this.currentOrder = order
        this.cart = []
        return order
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to create order'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchUserOrders(userId) {
      this.loading = true
      this.error = null
      
      try {
        const orders = await orderService.getUserOrders(userId)
        this.orders = orders
        return orders
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch orders'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchOrder(orderId) {
      this.loading = true
      this.error = null
      
      try {
        const order = await orderService.getOrder(orderId)
        this.currentOrder = order
        return order
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch order'
        throw error
      } finally {
        this.loading = false
      }
    },

    async processPayment(paymentData) {
      this.loading = true
      this.error = null
      
      try {
        const payment = await paymentService.processPayment(paymentData)
        return payment
      } catch (error) {
        this.error = error.response?.data?.message || 'Payment failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    addToCart(product) {
      const existingItem = this.cart.find(item => item.id === product.id)
      
      if (existingItem) {
        existingItem.quantity += 1
      } else {
        this.cart.push({
          ...product,
          quantity: 1
        })
      }
    },

    removeFromCart(productId) {
      this.cart = this.cart.filter(item => item.id !== productId)
    },

    updateCartItemQuantity(productId, quantity) {
      const item = this.cart.find(item => item.id === productId)
      if (item) {
        if (quantity <= 0) {
          this.removeFromCart(productId)
        } else {
          item.quantity = quantity
        }
      }
    },

    clearCart() {
      this.cart = []
    }
  }
})