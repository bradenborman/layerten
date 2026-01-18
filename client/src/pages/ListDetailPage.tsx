import { useEffect, useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import { listsApi, type RankedListDetail, type RankedEntry } from '../services/lists'
import TagBadge from '../components/TagBadge'
import MarkdownRenderer from '../components/MarkdownRenderer'

export default function ListDetailPage() {
  const { slug } = useParams<{ slug: string }>()
  const location = useLocation()
  const navigate = useNavigate()
  
  const [list, setList] = useState<RankedListDetail | null>(null)
  const [revealedCount, setRevealedCount] = useState(1)
  const [showAllMode, setShowAllMode] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchList = async () => {
      if (!slug) return
      
      try {
        setLoading(true)
        const data = await listsApi.getListBySlug(slug)
        setList(data)
        
        // Check for deep link to specific rank
        const hash = location.hash
        if (hash.startsWith('#rank-')) {
          const rank = parseInt(hash.substring(6))
          if (rank > 0 && rank <= data.entries.length) {
            setRevealedCount(rank)
            setShowAllMode(true)
            // Scroll to the entry after a brief delay
            setTimeout(() => {
              document.getElementById(`rank-${rank}`)?.scrollIntoView({ behavior: 'smooth' })
            }, 100)
          }
        }
      } catch (error) {
        console.error('Failed to fetch list:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchList()
  }, [slug, location.hash])

  const handleRevealNext = () => {
    if (list && revealedCount < list.entries.length) {
      setRevealedCount(prev => prev + 1)
      // Scroll to newly revealed entry
      setTimeout(() => {
        document.getElementById(`rank-${revealedCount + 1}`)?.scrollIntoView({ 
          behavior: 'smooth',
          block: 'center'
        })
      }, 100)
    }
  }

  const toggleShowAll = () => {
    setShowAllMode(prev => !prev)
    if (!showAllMode && list) {
      setRevealedCount(list.entries.length)
    } else {
      setRevealedCount(1)
    }
  }

  const handleEntryClick = (rank: number) => {
    navigate(`#rank-${rank}`, { replace: true })
  }

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center min-h-[400px]">
          <div className="text-gray-600">Loading...</div>
        </div>
      </div>
    )
  }

  if (!list) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">List not found</h1>
          <p className="text-gray-600">The list you're looking for doesn't exist.</p>
        </div>
      </div>
    )
  }

  const visibleEntries = showAllMode ? list.entries : list.entries.slice(0, revealedCount)
  const progress = (revealedCount / list.entries.length) * 100

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Sticky Progress Bar */}
      {!showAllMode && (
        <div className="sticky top-0 z-10 bg-white shadow-sm">
          <div className="container mx-auto px-4 py-3">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-700">
                {revealedCount} of {list.entries.length} revealed
              </span>
              <button
                onClick={toggleShowAll}
                className="text-sm text-blue-600 hover:text-blue-800"
              >
                Show All
              </button>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        </div>
      )}

      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm p-8 mb-8">
          {list.coverImage && (
            <img
              src={list.coverImage.url}
              alt={list.coverImage.altText || list.title}
              className="w-full h-64 object-cover rounded-lg mb-6"
            />
          )}
          
          <h1 className="text-4xl font-bold text-gray-900 mb-2">{list.title}</h1>
          
          {list.subtitle && (
            <p className="text-xl text-gray-600 mb-4">{list.subtitle}</p>
          )}
          
          <div className="flex flex-wrap gap-2 mb-4">
            {list.tags.map(tag => (
              <TagBadge key={tag.id} name={tag.name} slug={tag.slug} />
            ))}
          </div>
          
          <div className="text-sm text-gray-500 mb-6">
            Published {new Date(list.publishedAt).toLocaleDateString()}
          </div>
          
          {list.intro && (
            <div className="prose max-w-none">
              <MarkdownRenderer content={list.intro} />
            </div>
          )}
          
          {showAllMode && (
            <button
              onClick={toggleShowAll}
              className="mt-4 btn btn-secondary"
            >
              Switch to Reveal Mode
            </button>
          )}
        </div>

        {/* Entries */}
        <div className="space-y-6">
          {visibleEntries.map((entry) => (
            <div
              key={entry.id}
              id={`rank-${entry.rank}`}
              onClick={() => handleEntryClick(entry.rank)}
              className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition-shadow cursor-pointer"
            >
              <div className="flex items-start gap-6">
                {/* Rank Badge */}
                <div className="flex-shrink-0">
                  <div className="w-16 h-16 bg-blue-600 text-white rounded-full flex items-center justify-center text-2xl font-bold">
                    {entry.rank}
                  </div>
                </div>
                
                <div className="flex-1">
                  <h2 className="text-2xl font-bold text-gray-900 mb-3">
                    {entry.title}
                  </h2>
                  
                  {entry.heroImage && (
                    <img
                      src={entry.heroImage.url}
                      alt={entry.heroImage.altText || entry.title}
                      className="w-full h-64 object-cover rounded-lg mb-4"
                    />
                  )}
                  
                  {entry.blurb && (
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold text-gray-800 mb-2">Overview</h3>
                      <p className="text-gray-700">{entry.blurb}</p>
                    </div>
                  )}
                  
                  {entry.commentary && (
                    <div className="mb-4">
                      <h3 className="text-lg font-semibold text-gray-800 mb-2">Commentary</h3>
                      <div className="prose max-w-none">
                        <MarkdownRenderer content={entry.commentary} />
                      </div>
                    </div>
                  )}
                  
                  {entry.funFact && (
                    <div className="mb-4 p-4 bg-blue-50 rounded-lg">
                      <h3 className="text-lg font-semibold text-blue-900 mb-2">Fun Fact</h3>
                      <p className="text-blue-800">{entry.funFact}</p>
                    </div>
                  )}
                  
                  {entry.externalLink && (
                    <a
                      href={entry.externalLink}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center text-blue-600 hover:text-blue-800"
                      onClick={(e) => e.stopPropagation()}
                    >
                      Learn more →
                    </a>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Reveal Next Button */}
        {!showAllMode && revealedCount < list.entries.length && (
          <div className="mt-8 text-center">
            <button
              onClick={handleRevealNext}
              className="btn btn-primary btn-lg"
            >
              Reveal #{revealedCount + 1}
            </button>
          </div>
        )}

        {/* Outro */}
        {(showAllMode || revealedCount === list.entries.length) && list.outro && (
          <div className="mt-8 bg-white rounded-lg shadow-sm p-8">
            <div className="prose max-w-none">
              <MarkdownRenderer content={list.outro} />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
