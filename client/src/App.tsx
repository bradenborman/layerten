import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './contexts/AuthContext'
import HomePage from './pages/HomePage'
import ListsIndexPage from './pages/ListsIndexPage'
import ListDetailPage from './pages/ListDetailPage'
import PostsIndexPage from './pages/PostsIndexPage'
import PostDetailPage from './pages/PostDetailPage'
import SuggestPage from './pages/SuggestPage'
import AdminLoginPage from './pages/AdminLoginPage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import ProtectedRoute from './components/ProtectedRoute'

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-gray-50">
          <Routes>
            {/* Public routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/lists" element={<ListsIndexPage />} />
            <Route path="/lists/:slug" element={<ListDetailPage />} />
            <Route path="/posts" element={<PostsIndexPage />} />
            <Route path="/posts/:slug" element={<PostDetailPage />} />
            <Route path="/suggest" element={<SuggestPage />} />
            
            {/* Admin routes */}
            <Route path="/admin/login" element={<AdminLoginPage />} />
            <Route 
              path="/admin/dashboard" 
              element={
                <ProtectedRoute>
                  <AdminDashboardPage />
                </ProtectedRoute>
              } 
            />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  )
}

export default App
