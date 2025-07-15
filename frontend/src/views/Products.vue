<template>
  <div class="products">
    <v-container>
      <v-row class="mb-6">
        <v-col cols="12" md="6">
          <h1 class="display-1">Products</h1>
        </v-col>
        <v-col cols="12" md="6">
          <v-text-field
            v-model="searchQuery"
            label="Search products..."
            prepend-icon="mdi-magnify"
            @input="handleSearch"
            clearable
          />
        </v-col>
      </v-row>

      <v-row v-if="loading" class="text-center">
        <v-col cols="12">
          <v-progress-circular indeterminate color="primary" />
        </v-col>
      </v-row>

      <v-row v-else>
        <v-col
          v-for="product in displayProducts"
          :key="product.id"
          cols="12"
          sm="6"
          md="4"
          lg="3"
        >
          <v-card class="product-card">
            <v-img
              :src="product.imageUrl || '/placeholder.jpg'"
              height="200"
              cover
            />
            <v-card-title>{{ product.name }}</v-card-title>
            <v-card-subtitle>{{ product.category }}</v-card-subtitle>
            <v-card-text>
              <p class="text-truncate">{{ product.description }}</p>
              <div class="d-flex justify-space-between align-center">
                <span class="text-h6 font-weight-bold">
                  ${{ product.price }}
                </span>
                <v-chip
                  :color="product.stock > 0 ? 'success' : 'error'"
                  small
                >
                  {{ product.stock > 0 ? 'In Stock' : 'Out of Stock' }}
                </v-chip>
              </div>
            </v-card-text>
            <v-card-actions>
              <v-btn
                color="primary"
                :disabled="product.stock <= 0"
                @click="addToCart(product)"
              >
                Add to Cart
              </v-btn>
              <v-spacer />
              <v-btn icon @click="toggleFavorite(product)">
                <v-icon>
                  {{ product.favorite ? 'mdi-heart' : 'mdi-heart-outline' }}
                </v-icon>
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-row v-if="!loading && displayProducts.length === 0" class="text-center">
        <v-col cols="12">
          <v-card class="pa-8">
            <v-icon size="64" color="grey">mdi-package-variant</v-icon>
            <h3 class="mt-4">No products found</h3>
            <p>Try adjusting your search query</p>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <v-snackbar
      v-model="snackbar"
      :timeout="3000"
      color="success"
    >
      {{ snackbarMessage }}
      <template v-slot:actions>
        <v-btn text @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
import { useProductStore } from '@/stores/productStore'
import { useOrderStore } from '@/stores/orderStore'
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'Products',
  setup() {
    const productStore = useProductStore()
    const orderStore = useOrderStore()
    
    const searchQuery = ref('')
    const snackbar = ref(false)
    const snackbarMessage = ref('')
    
    const loading = computed(() => productStore.loading)
    const products = computed(() => productStore.products)
    const searchResults = computed(() => productStore.searchResults)
    
    const displayProducts = computed(() => {
      return searchQuery.value ? searchResults.value : products.value
    })
    
    const handleSearch = async () => {
      if (searchQuery.value.trim()) {
        await productStore.searchProducts(searchQuery.value)
      }
    }
    
    const addToCart = (product) => {
      orderStore.addToCart(product)
      snackbarMessage.value = `${product.name} added to cart`
      snackbar.value = true
    }
    
    const toggleFavorite = (product) => {
      product.favorite = !product.favorite
    }
    
    onMounted(async () => {
      await productStore.fetchProducts()
    })
    
    return {
      searchQuery,
      snackbar,
      snackbarMessage,
      loading,
      displayProducts,
      handleSearch,
      addToCart,
      toggleFavorite
    }
  }
}
</script>

<style scoped>
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card .v-card-text {
  flex-grow: 1;
}
</style>