<script setup lang="ts">
import { ref } from 'vue'
import { showToast } from 'vant'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { useAppStore } from '@/stores/app'
import { useI18n } from '@/i18n'
const router = useRouter(); const store = useAppStore(); const { t } = useI18n()
const email = ref(''); const password = ref(''); const loading = ref(false); const error = ref('')
async function submit() {
  error.value = ''
  if (!email.value.includes('@')) return showToast(t('emailError'))
  if (password.value.length < 6) return showToast(t('passwordError'))
  loading.value = true
  try { await store.login(email.value, password.value); await router.push('/album') }
  catch (e: any) { error.value = e?.response?.data?.errorMag || t('loginError') }
  finally { loading.value = false }
}
</script>
<template><main class="auth-page"><div class="brand"><span class="logo">⌁</span><b>{{ t('brand') }}</b></div><section class="auth-content"><h1>{{ t('welcome') }}</h1><p>{{ t('loginHint') }}</p><van-field v-model="email" :label="t('email')" type="email" placeholder="name@example.com" inset /><van-field v-model="password" :label="t('password')" type="password" placeholder="••••••••" inset /><p v-if="error" class="form-error">{{ error }}</p><van-button type="primary" block round :loading="loading" :disabled="loading" @click="submit">{{ t('login') }}</van-button><div class="auth-links"><button @click="router.push('/register')">{{ t('register') }}</button><button @click="router.push('/forgot-password')">{{ t('forgot') }}</button></div></section></main></template>
