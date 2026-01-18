import { createContext, useContext, useState, useEffect, ReactNode } from 'react'

interface AuthContextType {
  isAuthenticated: boolean
  login: (credentials: string) => void
  logout: () => void
  getAuthHeader: () => string | null
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  useEffect(() => {
    // Check if user is already logged in on mount
    const credentials = localStorage.getItem('adminAuth')
    if (credentials) {
      // Simply set authenticated if credentials exist
      // The API interceptor will handle validation on actual requests
      setIsAuthenticated(true)
    }
  }, [])

  const login = (credentials: string) => {
    localStorage.setItem('adminAuth', credentials)
    setIsAuthenticated(true)
  }

  const logout = () => {
    localStorage.removeItem('adminAuth')
    setIsAuthenticated(false)
  }

  const getAuthHeader = (): string | null => {
    const credentials = localStorage.getItem('adminAuth')
    return credentials ? `Basic ${credentials}` : null
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout, getAuthHeader }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
