import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('cloud_album_access_token')
  const language = localStorage.getItem('cloud_album_language') || 'zh-CN'
  if (token) config.headers.Authorization = `Bearer ${token}`
  config.headers['Accept-Language'] = language
  return config
})

let refreshPromise: Promise<string> | null = null

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const code = error.response?.data?.errorCode
    const original = error.config
    const refreshToken = localStorage.getItem('cloud_album_refresh_token')
    if ((code === 'A10003' || code === 'A10004') && refreshToken && !original?._retry && !original?.url?.endsWith('/api/auth/refresh')) {
      original._retry = true
      refreshPromise ||= api.post('/api/auth/refresh', { refreshToken }).then((response) => {
        const data = response.data.data
        localStorage.setItem('cloud_album_access_token', data.accessToken)
        localStorage.setItem('cloud_album_refresh_token', data.refreshToken)
        return data.accessToken
      }).finally(() => { refreshPromise = null })
      try {
        const accessToken = await refreshPromise
        original.headers.Authorization = `Bearer ${accessToken}`
        return api(original)
      } catch {
        localStorage.removeItem('cloud_album_access_token')
        localStorage.removeItem('cloud_album_refresh_token')
      }
    }
    if (code === 'A10003' || code === 'A10004') {
      localStorage.removeItem('cloud_album_access_token')
      localStorage.removeItem('cloud_album_refresh_token')
      window.dispatchEvent(new Event('cloud-album-auth-expired'))
    }
    return Promise.reject(error)
  },
)

export default api
