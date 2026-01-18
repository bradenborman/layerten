import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { listsApi, type RankedListSummary } from '../services/lists'
import Pagination from '../components/Pagination'
import TagBadge from '../components/TagBadge'
import LoadingSpinner from '../components/LoadingSpinner'

export default function ListsIndexPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [lists, setLists] = useState<RankedListSummary[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)
  
  const currentPage = parseInt(searchParams.get('page') || '0')
  const searchQuery = searchParams.get('search') || ''
  const tagFilter = searchParams.get('tag') || ''

  useEffect(() => {
    const fetchLists = async () => {
      try {
        setLoading(true)
        const response = await listsApi.getLists({
          search: searchQuery || undefined,
          tag: tagFilter || undefined,
          page: currentPage,
          size: 12
        })
        setLists(response.content)
        setTotalPages(response.totalPages)
        setTotalElements(response.totalElements)
      } catch (error) {
        console.error('Failed to fetch lists:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchLists()
  }, [currentPage, searchQuery, tagFilter])

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)
    const search = formData.get('search') as string
    
    const newParams = new URLSearchParams(searchParams)
    if (search) {
      newParams.set('search', search)
    } else {
      newParams.delete('search')
    }
    newParams.set('page', '0') // Reset to first page on new search
    setSearchParams(newParams)
  }

  const handlePageChange = (page: number) => {
    const newParams = new URLSearchParams(searchParams)
    newParams.set('page', page.toString())
    setSearchParams(newParams)
  }

  const clearFilters = () => {
    setSearchParams({})
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <LoadingSpinner size="lg" className="min-h-[400px]" />
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-gray-900 mb-8">Ranked Lists</h1>
      
      {/* Search and Filters */}
      <div className="mb-8">
        <form onSubmit={handleSearch} className="flex gap-4 mb-4">
          <input
            type="text"
            name="search"
            defaultValue={searchQuery}
            placeholder="Search lists..."
            className="input flex-1"
          />
          <button type="submit" className="btn btn-primary">
            Search
          </button>
        </form>
        
        {(searchQuery || tagFilter) && (
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <span>Filters:</span>
            {searchQuery && (
              <span className="px-3 py-1 bg-gray-200 rounded-full">
                Search: {searchQuery}
              </span>
            )}
            {tagFilter && (
              <span className="px-3 py-1 bg-gray-200 rounded-full">
                Tag: {tagFilter}
              </span>
            )}
            <button
              onClick={clearFilters}
              className="text-blue-600 hover:text-blue-800 ml-2"
            >
              Clear all
            </button>
          </div>
        )}
      </div>

      {/* Results Count */}
      <div className="mb-4 text-gray-600">
        {totalElements} {totalElements === 1 ? 'list' : 'lists'} found
      </div>

      {/* Lists Grid */}
      {lists.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600 text-lg">No lists found</p>
          {(searchQuery || tagFilter) && (
            <button
              onClick={clearFilters}
              className="mt-4 text-blue-600 hover:text-blue-800"
            >
              Clear filters
            </button>
          )}
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {lists.map(list => (
              <Link
                key={list.id}
                to={`/lists/${list.slug}`}
                className="card hover:shadow-lg transition-shadow"
              >
                {list.coverImage && (
                  <img
                    src={list.coverImage.url}
                    alt={list.coverImage.altText || list.title}
                    className="w-full h-48 object-cover rounded-t-lg"
                  />
                )}
                <div className="p-4">
                  <h2 className="text-xl font-bold text-gray-900 mb-2">
                    {list.title}
                  </h2>
                  {list.subtitle && (
                    <p className="text-gray-600 mb-3">{list.subtitle}</p>
                  )}
                  <div className="flex items-center justify-between text-sm text-gray-500 mb-3">
                    <span>{list.entryCount} entries</span>
                    <span>{new Date(list.publishedAt).toLocaleDateString()}</span>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {list.tags.map(tag => (
                      <TagBadge
                        key={tag.id}
                        name={tag.name}
                        slug={tag.slug}
                        clickable={false}
                      />
                    ))}
                  </div>
                </div>
              </Link>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              totalElements={totalElements}
              pageSize={12}
              onPageChange={handlePageChange}
            />
          )}
        </>
      )}
    </div>
  )
}
