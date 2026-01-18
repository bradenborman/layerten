import { useSearchParams } from 'react-router-dom'
import AdminLayout from '../components/AdminLayout'
import AdminListsTab from '../components/AdminListsTab'
import AdminPostsTab from '../components/AdminPostsTab'
import AdminSuggestionsTab from '../components/AdminSuggestionsTab'
import AdminMediaTab from '../components/AdminMediaTab'

export default function AdminDashboardPage() {
  const [searchParams] = useSearchParams()
  const currentTab = searchParams.get('tab') || 'lists'

  return (
    <AdminLayout>
      {currentTab === 'lists' && <AdminListsTab />}
      
      {currentTab === 'posts' && <AdminPostsTab />}
      
      {currentTab === 'suggestions' && <AdminSuggestionsTab />}
      
      {currentTab === 'media' && <AdminMediaTab />}
    </AdminLayout>
  )
}
