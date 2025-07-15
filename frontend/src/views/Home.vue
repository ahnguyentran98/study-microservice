<template>
  <div class="home">
    <v-hero class="mb-8">
      <v-container>
        <v-row align="center" justify="center">
          <v-col cols="12" md="8" class="text-center">
            <h1 class="display-2 font-weight-bold mb-4">
              Welcome to Microservices E-Commerce
            </h1>
            <p class="headline mb-6">
              Discover amazing products in our modern microservices-powered store
            </p>
            <v-btn 
              color="primary" 
              size="large" 
              @click="$router.push('/products')"
              class="mr-4"
            >
              Shop Now
            </v-btn>
            <v-btn 
              color="secondary" 
              size="large" 
              variant="outlined"
              @click="$router.push('/register')"
            >
              Sign Up
            </v-btn>
          </v-col>
        </v-row>
      </v-container>
    </v-hero>

    <v-container>
      <v-row>
        <v-col cols="12" md="4">
          <v-card class="text-center pa-4">
            <v-icon size="64" color="primary">mdi-shopping</v-icon>
            <v-card-title>Wide Product Range</v-card-title>
            <v-card-text>
              Browse through thousands of products across multiple categories
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card class="text-center pa-4">
            <v-icon size="64" color="success">mdi-truck-delivery</v-icon>
            <v-card-title>Fast Delivery</v-card-title>
            <v-card-text>
              Get your orders delivered quickly with our efficient logistics
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card class="text-center pa-4">
            <v-icon size="64" color="warning">mdi-shield-check</v-icon>
            <v-card-title>Secure Payment</v-card-title>
            <v-card-text>
              Shop with confidence using our secure payment system
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'Home'
}
</script>

<style scoped>
.home {
  min-height: 80vh;
}

.v-hero {
  background: linear-gradient(45deg, #1976D2, #42A5F5);
  color: white;
  padding: 80px 0;
}
</style>