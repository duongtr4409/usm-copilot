import create from 'zustand'
import api from '../lib/api'

type User = {
  id: string
  firstName: string
  lastName: string
  role: string
}

type AuthState = {
  token: string | null
  user: User | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  setFromLocalStorage: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  user: null,
  login: async (username: string, password: string) => {
    const res = await api.post('/auth/login', { username, password })
    const data = res.data
    const token = data.accessToken || data.token
    // store token and user
    localStorage.setItem('usm_token', token)
    if (data.user) localStorage.setItem('usm_user', JSON.stringify(data.user))
    set({ token, user: data.user })
  },
  logout: () => {
    localStorage.removeItem('usm_token')
    localStorage.removeItem('usm_user')
    set({ token: null, user: null })
  },
  setFromLocalStorage: () => {
    const token = localStorage.getItem('usm_token')
    const userJson = localStorage.getItem('usm_user')
    const user = userJson ? JSON.parse(userJson) : null
    set({ token, user })
  },
}))
