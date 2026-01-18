import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { listsApi, type RankedListSummary, type Tag } from '../services/lists'
import { postsApi, type BlogPostSummary } from '../services/posts'
import TagBadge from '../components/TagBadge'

export default function HomePage() {
  const navigate = useNavigate()
  const [featuredLists, setFeaturedLists] = useState<RankedListSummary[]>([])
  const [latestPosts, setLatestPosts] = useState<BlogPostSummary[]>([])
  const [tags, setTags] = useState<Tag[]>([])
  const [searchQuery, setSearchQuery] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        
        // Fetch 3 most recent lists
        const listsResponse = await listsApi.getLists({ page: 0, size: 3 })
        setFeaturedLists(listsResponse.content)
        
        // Fetch 3 most recent posts
        const postsResponse = await postsApi.getPosts({ page: 0, size: 3 })
        setLatestPosts(postsResponse.content)
        
        // Extract unique tags from lists and posts
        const allTags = new Map<number, Tag>()
        listsResponse.content.forEach(list => {
          list.tags.forEach(tag => allTags.set(tag.id, tag))
        })
        postsResponse.content.forEach(post => {
          post.tags.forEach(tag => allTags.set(tag.id, tag))
        })
        setTags(Array.from(allTags.values()))
      } catch (error) {
        console.error('Failed to fetch homepage data:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      navigate(`/lists?search=${encodeURIComponent(searchQuery.trim())}`)
    }
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

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Hero Section */}
      <div className="text-center mb-12">
        <h1 className="text-5xl font-bold text-gray-900 mb-4">Welcome to LayerTen</h1>
        <p className="text-xl text-gray-600 mb-8">
          Discover amazing countdown lists and blog posts
        </p>
        
        {/* Search Bar */}
        <form onSubmit={handleSearch} className="max-w-2xl mx-auto">
          <div className="flex gap-2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search lists..."
              className="input flex-1"
            />
            <button type="submit" className="btn btn-primary">
              Search
            </button>
          </div>
        </form>
      </div>

      {/* Tag Navigation */}
      {tags.length > 0 && (
        <div className="mb-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">Browse by Tag</h2>
          <div className="flex flex-wrap gap-2">
            {tags.map(tag => (
              <TagBadge key={tag.id} name={tag.name} slug={tag.slug} />
            ))}
          </div>
        </div>
      )}

      {/* Featured Lists */}
      <div className="mb-12">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-3xl font-bold text-gray-900">Featured Lists</h2>
          <Link to="/lists" className="text-blue-600 hover:text-blue-800">
            View All →
          </Link>
        </div>
        
        {featuredLists.length === 0 ? (
          <p className="text-gray-600">No lists available yet.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {featuredLists.map(list => (
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
                  <h3 className="text-xl font-bold text-gray-900 mb-2">{list.title}</h3>
                  {list.subtitle && (
                    <p className="text-gray-600 mb-3">{list.subtitle}</p>
                  )}
                  <div className="flex flex-wrap gap-2">
                    {list.tags.map(tag => (
                      <span
                        key={tag.id}
                        className="px-2 py-1 bg-gray-200 text-gray-700 text-sm rounded"
                      >
                        {tag.name}
                      </span>
                    ))}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>

      {/* Latest Blog Posts */}
      <div>
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-3xl font-bold text-gray-900">Latest Posts</h2>
          <Link to="/posts" className="text-blue-600 hover:text-blue-800">
            View All →
          </Link>
        </div>
        
        {latestPosts.length === 0 ? (
          <p className="text-gray-600">No posts available yet.</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {latestPosts.map(post => (
              <Link
                key={post.id}
                to={`/posts/${post.slug}`}
                className="card hover:shadow-lg transition-shadow"
              >
                {post.coverImage && (
                  <img
                    src={post.coverImage.url}
                    alt={post.coverImage.altText || post.title}
                    className="w-full h-48 object-cover rounded-t-lg"
                  />
                )}
                <div className="p-4">
                  <h3 className="text-xl font-bold text-gray-900 mb-2">{post.title}</h3>
                  {post.excerpt && (
                    <p className="text-gray-600 mb-3 line-clamp-3">{post.excerpt}</p>
                  )}
                  <div className="flex items-center justify-between text-sm text-gray-500">
                    <span>
                      {post.publishedAt 
                        ? new Date(post.publishedAt).toLocaleDateString()
                        : 'Draft'}
                    </span>
                    <div className="flex flex-wrap gap-2">
                      {post.tags.slice(0, 2).map(tag => (
                        <span
                          key={tag.id}
                          className="px-2 py-1 bg-gray-200 text-gray-700 rounded"
                        >
                          {tag.name}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
