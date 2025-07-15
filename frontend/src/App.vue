<template>
  <v-app>
    <v-navigation-drawer v-model="drawer" app temporary>
      <v-list>
        <v-list-item
          v-for="item in menuItems"
          :key="item.title"
          :to="item.to"
          link
        >
          <v-list-item-icon>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>{{ item.title }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-app-bar app color="primary" dark>
      <v-app-bar-nav-icon @click="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>Microservices E-Commerce</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn v-if="!isAuthenticated" @click="$router.push('/login')" text>
        Login
      </v-btn>
      <v-btn v-if="!isAuthenticated" @click="$router.push('/register')" text>
        Register
      </v-btn>
      <v-btn v-if="isAuthenticated" @click="logout" text>
        Logout
      </v-btn>
    </v-app-bar>

    <v-main>
      <v-container>
        <router-view />
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import { useUserStore } from '@/stores/userStore'
import { computed } from 'vue'

export default {
  name: 'App',
  data() {
    return {
      drawer: false,
      menuItems: [
        { title: 'Home', icon: 'mdi-home', to: '/' },
        { title: 'Products', icon: 'mdi-shopping', to: '/products' },
        { title: 'Orders', icon: 'mdi-clipboard-list', to: '/orders' },
        { title: 'Profile', icon: 'mdi-account', to: '/profile' }
      ]
    }
  },
  setup() {
    const userStore = useUserStore()
    
    const isAuthenticated = computed(() => userStore.isAuthenticated)
    
    const logout = () => {
      userStore.logout()
    }
    
    return {
      isAuthenticated,
      logout
    }
  }
}
</script>

<style>
.v-application {
  font-family: 'Roboto', sans-serif;
}
</style>