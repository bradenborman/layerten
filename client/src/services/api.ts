import axios from 'axios'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for adding auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('adminAuth')
    if (token) {
      config.headers.Authorization = `Basic ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear auth token on 401
      localStorage.removeItem('adminAuth')
      // Use absolute URL to ensure we go to the frontend, not the API
      if (typeof window !== 'undefined') {
        window.location.href = 'http://localhost:3000/admin/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
