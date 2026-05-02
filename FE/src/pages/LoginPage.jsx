import { useState } from 'react'
import client from '../api/client'
import useAppStore from '../store/appStore'
import { useNavigate } from 'react-router-dom'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const setUser = useAppStore((s) => s.setUser)
  const nav = useNavigate()

  const login = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      // Mock login for local dev
      if (email === 'test@test.com' && password === 'password') {
        const data = { token: 'mock_jwt_token', user: { name: 'Hacker', teamId: 'team1' } }
        localStorage.setItem('jwt_token', data.token)
        setUser(data.user)
        nav('/')
      } else {
        await client.post('/auth/login', { email, password })
      }
    } catch {
      alert('Invalid credentials. Use test@test.com / password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center">
      <form onSubmit={login} className="w-full max-w-sm bg-gray-900 rounded-2xl p-8 border border-gray-800 space-y-4">
        <h1 className="text-2xl font-bold text-white text-center">⚡ HackCoord</h1>
        <p className="text-gray-400 text-sm text-center">Sign in to your team workspace</p>
        <input
          type="email" required placeholder="Email (use test@test.com)"
          value={email} onChange={(e) => setEmail(e.target.value)}
          className="w-full bg-gray-800 text-white rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <input
          type="password" required placeholder="Password (use password)"
          value={password} onChange={(e) => setPassword(e.target.value)}
          className="w-full bg-gray-800 text-white rounded-lg px-4 py-2.5 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
        />
        <button
          type="submit" disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg py-2.5 text-sm font-medium transition-colors"
        >
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>
    </div>
  )
}
