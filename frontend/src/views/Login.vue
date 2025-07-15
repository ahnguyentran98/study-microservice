<template>
  <v-container class="fill-height">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="elevation-8">
          <v-card-title class="text-center">
            <h2>Login</h2>
          </v-card-title>
          <v-card-text>
            <v-form @submit.prevent="handleLogin">
              <v-text-field
                v-model="form.email"
                label="Email"
                type="email"
                required
                :rules="[rules.required, rules.email]"
                prepend-icon="mdi-email"
              />
              <v-text-field
                v-model="form.password"
                label="Password"
                type="password"
                required
                :rules="[rules.required]"
                prepend-icon="mdi-lock"
              />
              <v-alert
                v-if="error"
                type="error"
                dismissible
                class="mb-4"
              >
                {{ error }}
              </v-alert>
              <v-btn
                type="submit"
                color="primary"
                block
                :loading="loading"
                class="mb-4"
              >
                Login
              </v-btn>
              <div class="text-center">
                <span>Don't have an account?</span>
                <v-btn
                  text
                  color="primary"
                  @click="$router.push('/register')"
                >
                  Sign Up
                </v-btn>
              </div>
            </v-form>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { useUserStore } from '@/stores/userStore'
import { ref } from 'vue'

export default {
  name: 'Login',
  setup() {
    const userStore = useUserStore()
    
    const form = ref({
      email: '',
      password: ''
    })
    
    const rules = {
      required: value => !!value || 'This field is required',
      email: value => /.+@.+\..+/.test(value) || 'Email must be valid'
    }
    
    const loading = ref(false)
    const error = ref('')
    
    const handleLogin = async () => {
      loading.value = true
      error.value = ''
      
      try {
        await userStore.login(form.value)
        this.$router.push('/')
      } catch (err) {
        error.value = userStore.error
      } finally {
        loading.value = false
      }
    }
    
    return {
      form,
      rules,
      loading,
      error,
      handleLogin
    }
  }
}
</script>