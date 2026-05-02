import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const client = axios.create({ baseURL: API_BASE, timeout: 10000 })

// Attach JWT token on every request
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Handle 401 globally — clear token and redirect to login
client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('jwt_token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default client
