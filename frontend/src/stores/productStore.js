import { defineStore } from 'pinia'
import { productService } from '@/services/productService'

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [],
    currentProduct: null,
    loading: false,
    error: null,
    searchResults: []
  }),

  getters: {
    getProducts: (state) => state.products,
    getCurrentProduct: (state) => state.currentProduct,
    getSearchResults: (state) => state.searchResults
  },

  actions: {
    async fetchProducts() {
      this.loading = true
      this.error = null
      
      try {
        const products = await productService.getProducts()
        this.products = products
        return products
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch products'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProduct(id) {
      this.loading = true
      this.error = null
      
      try {
        const product = await productService.getProduct(id)
        this.currentProduct = product
        return product
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to fetch product'
        throw error
      } finally {
        this.loading = false
      }
    },

    async searchProducts(query) {
      this.loading = true
      this.error = null
      
      try {
        const results = await productService.searchProducts(query)
        this.searchResults = results
        return results
      } catch (error) {
        this.error = error.response?.data?.message || 'Search failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateInventory(productId, quantity) {
      this.loading = true
      this.error = null
      
      try {
        const updatedProduct = await productService.updateInventory(productId, quantity)
        const index = this.products.findIndex(p => p.id === productId)
        if (index !== -1) {
          this.products[index] = updatedProduct
        }
        return updatedProduct
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to update inventory'
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})