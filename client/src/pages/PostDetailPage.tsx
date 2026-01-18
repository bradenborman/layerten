import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { postsApi, type BlogPostDetail } from '../services/posts'
import TagBadge from '../components/TagBadge'
import MarkdownRenderer from '../components/MarkdownRenderer'
import LoadingSpinner from '../components/LoadingSpinner'

export default function PostDetailPage() {
  const { slug } = useParams<{ slug: string }>()
  const [post, setPost] = useState<BlogPostDetail | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchPost = async () => {
      if (!slug) return
      
      try {
        setLoading(true)
        const data = await postsApi.getPostBySlug(slug)
        setPost(data)
      } catch (error) {
        console.error('Failed to fetch post:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchPost()
  }, [slug])

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <LoadingSpinner size="lg" className="min-h-[400px]" />
      </div>
    )
  }

  if (!post) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center py-12">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Post not found</h1>
          <p className="text-gray-600">The post you're looking for doesn't exist.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <article className="container mx-auto px-4 py-8">
        {/* Cover Image */}
        {post.coverImage && (
          <img
            src={post.coverImage.url}
            alt={post.coverImage.altText || post.title}
            className="w-full h-96 object-cover rounded-lg mb-8"
          />
        )}

        {/* Header */}
        <div className="bg-white rounded-lg shadow-sm p-8 mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">{post.title}</h1>
          
          {post.excerpt && (
            <p className="text-xl text-gray-600 mb-6">{post.excerpt}</p>
          )}
          
          <div className="flex items-center justify-between mb-6 pb-6 border-b border-gray-200">
            <div className="text-sm text-gray-500">
              {post.publishedAt 
                ? `Published ${new Date(post.publishedAt).toLocaleDateString()}`
                : 'Draft'}
            </div>
            
            <div className="flex flex-wrap gap-2">
              {post.tags.map(tag => (
                <TagBadge key={tag.id} name={tag.name} slug={tag.slug} />
              ))}
            </div>
          </div>
          
          {/* Body */}
          <div className="prose prose-lg max-w-none">
            <MarkdownRenderer content={post.body} />
          </div>
        </div>
      </article>
    </div>
  )
}
