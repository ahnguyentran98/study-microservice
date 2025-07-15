<template>
  <div class="orders">
    <v-container>
      <v-row class="mb-6">
        <v-col cols="12" md="8">
          <h1 class="display-1">My Orders</h1>
        </v-col>
        <v-col cols="12" md="4" class="text-right">
          <v-btn
            color="primary"
            @click="showCart = true"
            prepend-icon="mdi-cart"
          >
            View Cart ({{ cartItemCount }})
          </v-btn>
        </v-col>
      </v-row>

      <v-row v-if="loading" class="text-center">
        <v-col cols="12">
          <v-progress-circular indeterminate color="primary" />
        </v-col>
      </v-row>

      <v-row v-else>
        <v-col cols="12">
          <v-card v-if="orders.length === 0" class="pa-8 text-center">
            <v-icon size="64" color="grey">mdi-clipboard-list-outline</v-icon>
            <h3 class="mt-4">No orders yet</h3>
            <p>Start shopping to see your orders here</p>
            <v-btn color="primary" @click="$router.push('/products')">
              Shop Now
            </v-btn>
          </v-card>

          <v-expansion-panels v-else>
            <v-expansion-panel
              v-for="order in orders"
              :key="order.id"
              class="mb-2"
            >
              <v-expansion-panel-title>
                <v-row no-gutters>
                  <v-col cols="4">
                    <strong>Order #{{ order.id }}</strong>
                  </v-col>
                  <v-col cols="4" class="text-center">
                    <v-chip
                      :color="getStatusColor(order.status)"
                      small
                    >
                      {{ order.status }}
                    </v-chip>
                  </v-col>
                  <v-col cols="4" class="text-right">
                    ${{ order.totalAmount }}
                  </v-col>
                </v-row>
              </v-expansion-panel-title>
              <v-expansion-panel-text>
                <v-row>
                  <v-col cols="12" md="6">
                    <h4>Order Items</h4>
                    <v-list>
                      <v-list-item
                        v-for="item in order.items"
                        :key="item.id"
                      >
                        <v-list-item-content>
                          <v-list-item-title>{{ item.productName }}</v-list-item-title>
                          <v-list-item-subtitle>
                            Quantity: {{ item.quantity }} × ${{ item.price }}
                          </v-list-item-subtitle>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list>
                  </v-col>
                  <v-col cols="12" md="6">
                    <h4>Order Details</h4>
                    <p><strong>Date:</strong> {{ formatDate(order.createdAt) }}</p>
                    <p><strong>Status:</strong> {{ order.status }}</p>
                    <p><strong>Total:</strong> ${{ order.totalAmount }}</p>
                    <v-btn
                      v-if="order.status === 'PENDING'"
                      color="error"
                      small
                      @click="cancelOrder(order.id)"
                    >
                      Cancel Order
                    </v-btn>
                  </v-col>
                </v-row>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>
        </v-col>
      </v-row>
    </v-container>

    <!-- Cart Dialog -->
    <v-dialog v-model="showCart" max-width="600">
      <v-card>
        <v-card-title>Shopping Cart</v-card-title>
        <v-card-text>
          <v-list v-if="cart.length > 0">
            <v-list-item
              v-for="item in cart"
              :key="item.id"
            >
              <v-list-item-content>
                <v-list-item-title>{{ item.name }}</v-list-item-title>
                <v-list-item-subtitle>
                  ${{ item.price }} × {{ item.quantity }}
                </v-list-item-subtitle>
              </v-list-item-content>
              <v-list-item-action>
                <v-btn-group>
                  <v-btn
                    size="small"
                    @click="updateQuantity(item.id, item.quantity - 1)"
                  >
                    -
                  </v-btn>
                  <v-btn size="small" disabled>
                    {{ item.quantity }}
                  </v-btn>
                  <v-btn
                    size="small"
                    @click="updateQuantity(item.id, item.quantity + 1)"
                  >
                    +
                  </v-btn>
                </v-btn-group>
              </v-list-item-action>
            </v-list-item>
          </v-list>
          <div v-else class="text-center pa-4">
            <p>Your cart is empty</p>
          </div>
        </v-card-text>
        <v-card-actions v-if="cart.length > 0">
          <v-spacer />
          <div class="mr-4">
            <strong>Total: ${{ cartTotal }}</strong>
          </div>
          <v-btn color="error" @click="clearCart">Clear Cart</v-btn>
          <v-btn color="primary" @click="checkout">Checkout</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { useOrderStore } from '@/stores/orderStore'
import { useUserStore } from '@/stores/userStore'
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'Orders',
  setup() {
    const orderStore = useOrderStore()
    const userStore = useUserStore()
    
    const showCart = ref(false)
    
    const loading = computed(() => orderStore.loading)
    const orders = computed(() => orderStore.orders)
    const cart = computed(() => orderStore.cart)
    const cartTotal = computed(() => orderStore.getCartTotal)
    const cartItemCount = computed(() => orderStore.getCartItemCount)
    
    const getStatusColor = (status) => {
      const colors = {
        'PENDING': 'warning',
        'PROCESSING': 'info',
        'SHIPPED': 'primary',
        'DELIVERED': 'success',
        'CANCELLED': 'error'
      }
      return colors[status] || 'grey'
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleDateString()
    }
    
    const updateQuantity = (productId, quantity) => {
      orderStore.updateCartItemQuantity(productId, quantity)
    }
    
    const clearCart = () => {
      orderStore.clearCart()
    }
    
    const checkout = async () => {
      try {
        const orderData = {
          userId: userStore.user.id,
          items: cart.value.map(item => ({
            productId: item.id,
            quantity: item.quantity,
            price: item.price
          }))
        }
        
        await orderStore.createOrder(orderData)
        showCart.value = false
        
        // Refresh orders
        await orderStore.fetchUserOrders(userStore.user.id)
      } catch (error) {
        console.error('Checkout failed:', error)
      }
    }
    
    const cancelOrder = async (orderId) => {
      try {
        // Implementation depends on backend API
        console.log('Cancel order:', orderId)
      } catch (error) {
        console.error('Cancel order failed:', error)
      }
    }
    
    onMounted(async () => {
      if (userStore.user?.id) {
        await orderStore.fetchUserOrders(userStore.user.id)
      }
    })
    
    return {
      showCart,
      loading,
      orders,
      cart,
      cartTotal,
      cartItemCount,
      getStatusColor,
      formatDate,
      updateQuantity,
      clearCart,
      checkout,
      cancelOrder
    }
  }
}
</script>