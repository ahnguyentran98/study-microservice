<template>
  <v-container class="fill-height">
    <v-row align="center" justify="center">
      <v-col cols="12" sm="8" md="6" lg="4">
        <v-card class="elevation-8">
          <v-card-title class="text-center">
            <h2>Register</h2>
          </v-card-title>
          <v-card-text>
            <v-form @submit.prevent="handleRegister">
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
                :rules="[rules.required, rules.minLength]"
                prepend-icon="mdi-lock"
              />
              <v-text-field
                v-model="form.confirmPassword"
                label="Confirm Password"
                type="password"
                required
                :rules="[rules.required, rules.passwordMatch]"
                prepend-icon="mdi-lock-check"
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
                Register
              </v-btn>
              <div class="text-center">
                <span>Already have an account?</span>
                <v-btn
                  text
                  color="primary"
                  @click="$router.push('/login')"
                >
                  Login
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
  name: 'Register',
  setup() {
    const userStore = useUserStore()
    
    const form = ref({
      email: '',
      password: '',
      confirmPassword: ''
    })
    
    const rules = {
      required: value => !!value || 'This field is required',
      email: value => /.+@.+\..+/.test(value) || 'Email must be valid',
      minLength: value => value.length >= 6 || 'Password must be at least 6 characters',
      passwordMatch: value => value === form.value.password || 'Passwords must match'
    }
    
    const loading = ref(false)
    const error = ref('')
    
    const handleRegister = async () => {
      loading.value = true
      error.value = ''
      
      try {
        await userStore.register({
          email: form.value.email,
          password: form.value.password
        })
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
      handleRegister
    }
  }
}
</script>