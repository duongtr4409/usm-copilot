import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)
  const setFromLocalStorage = useAuthStore((s) => s.setFromLocalStorage)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    try {
      await login(username, password)
      setFromLocalStorage()
      navigate('/')
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Login failed')
    }
  }

  return (
    <div>
      <h2>Login</h2>
      <form onSubmit={submit}>
        <div>
          <label>Username</label>
          <input value={username} onChange={(e) => setUsername(e.target.value)} />
        </div>
        <div>
          <label>Password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </div>
        <div>
          <button type="submit">Login</button>
        </div>
      </form>
      {error && <div style={{ color: 'red' }}>{error}</div>}
    </div>
  )
}
