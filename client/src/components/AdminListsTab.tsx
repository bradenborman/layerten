import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import type { RankedListSummary } from '../services/lists'

export default function AdminListsTab() {
  const { getAuthHeader } = useAuth()
  const [lists, setLists] = useState<RankedListSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchLists()
  }, [])

  const fetchLists = async () => {
    try {
      setLoading(true)
      const authHeader = getAuthHeader()
      const response = await fetch('/api/lists', {
        headers: authHeader ? { 'Authorization': authHeader } : {}
      })
      
      if (response.ok) {
        const data = await response.json()
        setLists(data.content || [])
      } else {
        setError('Failed to fetch lists')
      }
    } catch (err) {
      console.error('Error fetching lists:', err)
      setError('An error occurred while fetching lists')
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this list?')) {
      return
    }

    try {
      const authHeader = getAuthHeader()
      const response = await fetch(`/api/admin/lists/${id}`, {
        method: 'DELETE',
        headers: authHeader ? { 'Authorization': authHeader } : {}
      })

      if (response.ok) {
        setLists(lists.filter(list => list.id !== id))
      } else {
        alert('Failed to delete list')
      }
    } catch (err) {
      console.error('Error deleting list:', err)
      alert('An error occurred while deleting the list')
    }
  }

  if (loading) {
    return <div className="text-center py-8">Loading...</div>
  }

  if (error) {
    return <div className="text-center py-8 text-red-600">{error}</div>
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Ranked Lists</h2>
        <button className="btn btn-primary">
          Create New List
        </button>
      </div>

      {lists.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <p className="text-gray-600">No lists found</p>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Title
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Entries
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Published
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {lists.map(list => (
                <tr key={list.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium text-gray-900">{list.title}</div>
                    {list.subtitle && (
                      <div className="text-sm text-gray-500">{list.subtitle}</div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {list.entryCount}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(list.publishedAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      className="text-blue-600 hover:text-blue-900 mr-4"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(list.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
