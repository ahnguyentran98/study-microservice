<template>
  <div class="profile">
    <v-container>
      <v-row>
        <v-col cols="12" md="8" offset-md="2">
          <v-card class="elevation-4">
            <v-card-title class="text-center">
              <h1>My Profile</h1>
            </v-card-title>
            <v-card-text>
              <v-form @submit.prevent="updateProfile">
                <v-row>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model="form.firstName"
                      label="First Name"
                      prepend-icon="mdi-account"
                      :rules="[rules.required]"
                    />
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model="form.lastName"
                      label="Last Name"
                      prepend-icon="mdi-account"
                      :rules="[rules.required]"
                    />
                  </v-col>
                </v-row>
                
                <v-text-field
                  v-model="form.email"
                  label="Email"
                  type="email"
                  prepend-icon="mdi-email"
                  :rules="[rules.required, rules.email]"
                />
                
                <v-text-field
                  v-model="form.phone"
                  label="Phone Number"
                  prepend-icon="mdi-phone"
                />
                
                <v-textarea
                  v-model="form.address"
                  label="Address"
                  prepend-icon="mdi-home"
                  rows="3"
                />
                
                <v-divider class="my-4" />
                
                <h3 class="mb-4">Change Password</h3>
                
                <v-text-field
                  v-model="form.currentPassword"
                  label="Current Password"
                  type="password"
                  prepend-icon="mdi-lock"
                />
                
                <v-text-field
                  v-model="form.newPassword"
                  label="New Password"
                  type="password"
                  prepend-icon="mdi-lock-plus"
                  :rules="form.newPassword ? [rules.minLength] : []"
                />
                
                <v-text-field
                  v-model="form.confirmPassword"
                  label="Confirm New Password"
                  type="password"
                  prepend-icon="mdi-lock-check"
                  :rules="form.confirmPassword ? [rules.passwordMatch] : []"
                />
                
                <v-alert
                  v-if="error"
                  type="error"
                  dismissible
                  class="mb-4"
                >
                  {{ error }}
                </v-alert>
                
                <v-alert
                  v-if="successMessage"
                  type="success"
                  dismissible
                  class="mb-4"
                >
                  {{ successMessage }}
                </v-alert>
                
                <v-btn
                  type="submit"
                  color="primary"
                  :loading="loading"
                  class="mr-4"
                >
                  Update Profile
                </v-btn>
                
                <v-btn
                  color="grey"
                  @click="resetForm"
                >
                  Reset
                </v-btn>
              </v-form>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
import { useUserStore } from '@/stores/userStore'
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'Profile',
  setup() {
    const userStore = useUserStore()
    
    const form = ref({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const rules = {
      required: value => !!value || 'This field is required',
      email: value => /.+@.+\..+/.test(value) || 'Email must be valid',
      minLength: value => value.length >= 6 || 'Password must be at least 6 characters',
      passwordMatch: value => value === form.value.newPassword || 'Passwords must match'
    }
    
    const loading = ref(false)
    const error = ref('')
    const successMessage = ref('')
    
    const user = computed(() => userStore.user)
    
    const updateProfile = async () => {
      loading.value = true
      error.value = ''
      successMessage.value = ''
      
      try {
        const profileData = {
          firstName: form.value.firstName,
          lastName: form.value.lastName,
          email: form.value.email,
          phone: form.value.phone,
          address: form.value.address
        }
        
        // Add password fields if provided
        if (form.value.currentPassword && form.value.newPassword) {
          profileData.currentPassword = form.value.currentPassword
          profileData.newPassword = form.value.newPassword
        }
        
        await userStore.updateProfile(profileData)
        successMessage.value = 'Profile updated successfully!'
        
        // Clear password fields
        form.value.currentPassword = ''
        form.value.newPassword = ''
        form.value.confirmPassword = ''
        
      } catch (err) {
        error.value = userStore.error
      } finally {
        loading.value = false
      }
    }
    
    const resetForm = () => {
      if (user.value) {
        form.value = {
          firstName: user.value.firstName || '',
          lastName: user.value.lastName || '',
          email: user.value.email || '',
          phone: user.value.phone || '',
          address: user.value.address || '',
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
      }
    }
    
    onMounted(() => {
      resetForm()
    })
    
    return {
      form,
      rules,
      loading,
      error,
      successMessage,
      updateProfile,
      resetForm
    }
  }
}
</script>