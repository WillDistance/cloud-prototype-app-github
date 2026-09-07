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

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const code = error.response?.data?.errorCode
    if (code === 'A10003' || code === 'A10004') {
      localStorage.removeItem('cloud_album_access_token')
      window.dispatchEvent(new Event('cloud-album-auth-expired'))
    }
    return Promise.reject(error)
  },
)

export default api
