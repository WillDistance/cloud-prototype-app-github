import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi, deviceApi, storageApi, userApi, type Device, type Entitlement, type StorageOverview, type UserProfile } from '@/api'

export const useAppStore = defineStore('app', () => {
  const token = ref(localStorage.getItem('cloud_album_access_token') || '')
  const language = ref(localStorage.getItem('cloud_album_language') || initialLanguage())
  const profile = ref<UserProfile | null>(null)
  const device = ref<Device | null>(null)
  const overview = ref<StorageOverview | null>(null)
  const entitlements = ref<Entitlement[]>([])

  const isLoggedIn = computed(() => Boolean(token.value))
  const hasDevice = computed(() => Boolean(device.value))

  function setToken(value: string) {
    token.value = value
    if (value) localStorage.setItem('cloud_album_access_token', value)
    else localStorage.removeItem('cloud_album_access_token')
  }

  async function login(email: string, password: string) {
    const response = await authApi.login({ email, password })
    setToken(response.data.data.accessToken)
    localStorage.setItem('cloud_album_refresh_token', response.data.data.refreshToken)
    await refreshUser()
  }

  async function refreshUser() {
    const [userResponse, deviceResponse] = await Promise.all([userApi.profile(), deviceApi.mine()])
    profile.value = userResponse.data.data
    device.value = deviceResponse.data.data
    if (device.value) await refreshStorage()
  }

  async function refreshStorage() {
    const [overviewResponse, entitlementsResponse] = await Promise.all([storageApi.overview(), storageApi.entitlements()])
    overview.value = overviewResponse.data.data
    entitlements.value = entitlementsResponse.data.data
  }

  async function updateLanguage(value: string) {
    language.value = value
    localStorage.setItem('cloud_album_language', value)
    if (isLoggedIn.value) {
      const response = await userApi.updateLanguage(value)
      profile.value = response.data.data
    }
  }

  async function logout() {
    const refreshToken = localStorage.getItem('cloud_album_refresh_token') || ''
    try { if (isLoggedIn.value) await authApi.logout(refreshToken) } finally {
      setToken('')
      localStorage.removeItem('cloud_album_refresh_token')
      profile.value = null
      device.value = null
      overview.value = null
      entitlements.value = []
    }
  }

  async function bindDevice(deviceId: string, password: string) {
    const response = await deviceApi.bind(deviceId, password)
    device.value = response.data.data
    await refreshStorage()
  }

  return { token, language, profile, device, overview, entitlements, isLoggedIn, hasDevice, login, refreshUser, refreshStorage, updateLanguage, logout, bindDevice }
})

function initialLanguage() {
  const system = navigator.language.toLowerCase()
  return system.startsWith('zh') ? 'zh-CN' : system.startsWith('de') ? 'de' : 'en'
}
