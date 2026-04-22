import React from 'react'
import { Routes, Route, Link, useNavigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import Home from './pages/Home'
import AddStudentForm from './features/classes/AddStudentForm'

import OrgTree from './features/org/OrgTree'
import { useAuthStore } from './store/auth'

export default function App() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div style={{ padding: 16 }}>
      <header style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
        <Link to="/">Home</Link>
        {!user && <Link to="/login">Login</Link>}
        {user && (
          <>
            <span>Hi {user.firstName} {user.lastName} ({user.role})</span>
            <button onClick={handleLogout}>Logout</button>
          </>
        )}
        {/* RBAC-aware admin control */}
        {user && user.role !== 'STUDENT' && (
          <>
            <Link to="/org-units">Org Units</Link>
            <span style={{ marginLeft: 'auto', fontWeight: 'bold' }}>Admin Controls</span>
          </>
        )}
        <Link to="/org-units">Org units</Link>
      </header>

      <main style={{ marginTop: 16 }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/classes/:id/add-student" element={<AddStudentForm />} />
          <Route path="/org-units" element={<OrgTree />} />
        </Routes>
      </main>
    </div>
  )
}
