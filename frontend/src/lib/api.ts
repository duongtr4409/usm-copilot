import axios from 'axios'

const API_BASE = (import.meta.env.VITE_API_BASE as string) || 'http://localhost:8080/api/v1'

export const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
})

// Attach Authorization header from localStorage
api.interceptors.request.use((config) => {
  try {
    const token = localStorage.getItem('usm_token')
    if (token) {
      config.headers = config.headers || {}
      config.headers['Authorization'] = `Bearer ${token}`
    }
  } catch (e) {
    // ignore
  }
  return config
})

export default api
