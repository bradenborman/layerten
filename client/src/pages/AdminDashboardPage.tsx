import { useSearchParams } from 'react-router-dom'
import AdminLayout from '../components/AdminLayout'
import AdminListsTab from '../components/AdminListsTab'

export default function AdminDashboardPage() {
  const [searchParams] = useSearchParams()
  const currentTab = searchParams.get('tab') || 'lists'

  return (
    <AdminLayout>
      {currentTab === 'lists' && <AdminListsTab />}
      
      {currentTab === 'posts' && (
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Blog Posts</h2>
          <p className="text-gray-600">Posts management coming soon</p>
        </div>
      )}
      
      {currentTab === 'suggestions' && (
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Suggestions</h2>
          <p className="text-gray-600">Suggestions management coming soon</p>
        </div>
      )}
      
      {currentTab === 'media' && (
        <div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Media Assets</h2>
          <p className="text-gray-600">Media management coming soon</p>
        </div>
      )}
    </AdminLayout>
  )
}
