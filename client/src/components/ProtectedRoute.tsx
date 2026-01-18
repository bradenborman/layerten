import { Navigate } from 'react-router-dom'

interface ProtectedRouteProps {
  children: React.ReactNode
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  // TODO: Check authentication status
  const isAuthenticated = false
  
  if (!isAuthenticated) {
    return <Navigate to="/admin/login" replace />
  }
  
  return <>{children}</>
}
