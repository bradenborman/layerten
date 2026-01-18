import { useEffect, useState } from 'react'
import { adminApi } from '../services/admin'
import type { Suggestion } from '../services/admin'
import LoadingSpinner from './LoadingSpinner'

type SuggestionStatus = 'NEW' | 'REVIEWING' | 'ACCEPTED' | 'DECLINED'

export default function AdminSuggestionsTab() {
  const [suggestions, setSuggestions] = useState<Suggestion[]>([])
  const [filteredSuggestions, setFilteredSuggestions] = useState<Suggestion[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [statusFilter, setStatusFilter] = useState<SuggestionStatus | 'ALL'>('ALL')
  const [updatingId, setUpdatingId] = useState<number | null>(null)

  useEffect(() => {
    fetchSuggestions()
  }, [])

  useEffect(() => {
    // Apply status filter
    if (statusFilter === 'ALL') {
      setFilteredSuggestions(suggestions)
    } else {
      setFilteredSuggestions(suggestions.filter(s => s.status === statusFilter))
    }
  }, [suggestions, statusFilter])

  const fetchSuggestions = async () => {
    try {
      setLoading(true)
      const data = await adminApi.getAllSuggestions()
      setSuggestions(data)
    } catch (err) {
      console.error('Error fetching suggestions:', err)
      setError('An error occurred while fetching suggestions')
    } finally {
      setLoading(false)
    }
  }

  const handleStatusUpdate = async (id: number, newStatus: SuggestionStatus) => {
    setUpdatingId(id)
    try {
      const updated = await adminApi.updateSuggestionStatus(id, { status: newStatus })
      setSuggestions(suggestions.map(s => s.id === id ? updated : s))
    } catch (err) {
      console.error('Error updating suggestion status:', err)
      alert('Failed to update suggestion status')
    } finally {
      setUpdatingId(null)
    }
  }

  const getStatusBadgeClass = (status: SuggestionStatus) => {
    switch (status) {
      case 'NEW':
        return 'bg-blue-100 text-blue-800'
      case 'REVIEWING':
        return 'bg-yellow-100 text-yellow-800'
      case 'ACCEPTED':
        return 'bg-green-100 text-green-800'
      case 'DECLINED':
        return 'bg-red-100 text-red-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  if (loading) {
    return <LoadingSpinner size="lg" className="py-8" />
  }

  if (error) {
    return <div className="text-center py-8 text-red-600">{error}</div>
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Suggestions</h2>
        
        {/* Status Filter */}
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium text-gray-700">Filter by status:</label>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as SuggestionStatus | 'ALL')}
            className="px-3 py-2 border border-gray-300 rounded-md text-sm"
          >
            <option value="ALL">All</option>
            <option value="NEW">New</option>
            <option value="REVIEWING">Reviewing</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="DECLINED">Declined</option>
          </select>
        </div>
      </div>

      {filteredSuggestions.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <p className="text-gray-600">
            {statusFilter === 'ALL' 
              ? 'No suggestions found' 
              : `No ${statusFilter.toLowerCase()} suggestions`}
          </p>
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
                  Description
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Submitter
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredSuggestions.map(suggestion => (
                <tr key={suggestion.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium text-gray-900">{suggestion.title}</div>
                    {suggestion.category && (
                      <div className="text-xs text-gray-500">Category: {suggestion.category}</div>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm text-gray-700 max-w-md">
                      <p className="line-clamp-2">{suggestion.description}</p>
                      {suggestion.exampleEntries && (
                        <details className="mt-1">
                          <summary className="text-xs text-blue-600 cursor-pointer">
                            View examples
                          </summary>
                          <p className="text-xs text-gray-600 mt-1">{suggestion.exampleEntries}</p>
                        </details>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {suggestion.submitterName || 'Anonymous'}
                    </div>
                    {suggestion.submitterEmail && (
                      <div className="text-xs text-gray-500">{suggestion.submitterEmail}</div>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(suggestion.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadgeClass(suggestion.status)}`}>
                      {suggestion.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                    <select
                      value={suggestion.status}
                      onChange={(e) => handleStatusUpdate(suggestion.id, e.target.value as SuggestionStatus)}
                      disabled={updatingId === suggestion.id}
                      className="px-2 py-1 border border-gray-300 rounded text-sm disabled:opacity-50"
                    >
                      <option value="NEW">New</option>
                      <option value="REVIEWING">Reviewing</option>
                      <option value="ACCEPTED">Accepted</option>
                      <option value="DECLINED">Declined</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Summary Stats */}
      <div className="mt-6 grid grid-cols-4 gap-4">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-sm text-gray-600">Total</div>
          <div className="text-2xl font-bold text-gray-900">{suggestions.length}</div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-sm text-gray-600">New</div>
          <div className="text-2xl font-bold text-blue-600">
            {suggestions.filter(s => s.status === 'NEW').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-sm text-gray-600">Reviewing</div>
          <div className="text-2xl font-bold text-yellow-600">
            {suggestions.filter(s => s.status === 'REVIEWING').length}
          </div>
        </div>
        <div className="bg-white rounded-lg shadow p-4">
          <div className="text-sm text-gray-600">Accepted</div>
          <div className="text-2xl font-bold text-green-600">
            {suggestions.filter(s => s.status === 'ACCEPTED').length}
          </div>
        </div>
      </div>
    </div>
  )
}
