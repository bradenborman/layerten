import { useState, useEffect } from 'react'
import { adminApi } from '../services/admin'
import type { Tag, MediaAsset } from '../services/lists'
import type { BlogPostDetail } from '../services/posts'
import type { CreatePostRequest, UpdatePostRequest } from '../services/admin'
import MarkdownRenderer from './MarkdownRenderer'
import LoadingSpinner from './LoadingSpinner'

interface PostEditorProps {
  postId?: number
  onSave: () => void
  onCancel: () => void
}

export default function PostEditor({ postId, onSave, onCancel }: PostEditorProps) {
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [showPreview, setShowPreview] = useState(false)
  
  // Form state
  const [title, setTitle] = useState('')
  const [excerpt, setExcerpt] = useState('')
  const [body, setBody] = useState('')
  const [coverImageId, setCoverImageId] = useState<number | undefined>()
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([])
  const [status, setStatus] = useState<'DRAFT' | 'PUBLISHED'>('DRAFT')
  
  // Available options
  const [availableTags, setAvailableTags] = useState<Tag[]>([])
  const [availableMedia, setAvailableMedia] = useState<MediaAsset[]>([])
  
  const [errors, setErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    loadData()
  }, [postId])

  const loadData = async () => {
    setLoading(true)
    try {
      // Load tags and media
      const [tagsRes, mediaRes] = await Promise.all([
        fetch('/api/lists?size=1'),
        fetch('/api/media?size=100')
      ])
      
      if (tagsRes.ok) {
        const tagsData = await tagsRes.json()
        // Extract unique tags from lists
        const tagSet = new Map<number, Tag>()
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        tagsData.content?.forEach((list: any) => {
          list.tags?.forEach((tag: Tag) => {
            tagSet.set(tag.id, tag)
          })
        })
        setAvailableTags(Array.from(tagSet.values()))
      }
      
      if (mediaRes.ok) {
        const mediaData = await mediaRes.json()
        setAvailableMedia(mediaData.content || [])
      }
      
      // Load existing post if editing
      if (postId) {
        // First try to get from admin API, fallback to public API
        try {
          const response = await fetch(`/api/posts/${postId}`)
          if (response.ok) {
            const post: BlogPostDetail = await response.json()
            setTitle(post.title)
            setExcerpt(post.excerpt)
            setBody(post.body)
            setCoverImageId(post.coverImage?.id)
            setSelectedTagIds(post.tags.map((t: Tag) => t.id))
            setStatus(post.status)
          }
        } catch (err) {
          console.error('Error loading post:', err)
        }
      }
    } catch (err) {
      console.error('Error loading data:', err)
    } finally {
      setLoading(false)
    }
  }

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {}
    
    if (!title.trim()) {
      newErrors.title = 'Title is required'
    }
    if (!excerpt.trim()) {
      newErrors.excerpt = 'Excerpt is required'
    }
    if (!body.trim()) {
      newErrors.body = 'Body is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSave = async (saveStatus: 'DRAFT' | 'PUBLISHED') => {
    if (!validate()) {
      return
    }
    
    setSaving(true)
    try {
      const postData = {
        title,
        excerpt,
        body,
        coverImageId,
        tagIds: selectedTagIds.length > 0 ? selectedTagIds : undefined,
        status: saveStatus
      }
      
      if (postId) {
        // Update existing post
        await adminApi.updatePost(postId, postData as UpdatePostRequest)
      } else {
        // Create new post
        await adminApi.createPost(postData as CreatePostRequest)
      }
      
      onSave()
    } catch (err) {
      console.error('Error saving post:', err)
      alert('Failed to save post')
    } finally {
      setSaving(false)
    }
  }

  const toggleTag = (tagId: number) => {
    setSelectedTagIds(prev =>
      prev.includes(tagId)
        ? prev.filter(id => id !== tagId)
        : [...prev, tagId]
    )
  }

  if (loading) {
    return <LoadingSpinner size="lg" className="py-8" />
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-2xl font-bold text-gray-900 mb-6">
        {postId ? 'Edit Post' : 'Create New Post'}
      </h2>

      <div className="space-y-6">
        {/* Title */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Title *
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className={`w-full px-3 py-2 border rounded-md ${
              errors.title ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Enter post title"
          />
          {errors.title && (
            <p className="mt-1 text-sm text-red-600">{errors.title}</p>
          )}
        </div>

        {/* Excerpt */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Excerpt *
          </label>
          <textarea
            value={excerpt}
            onChange={(e) => setExcerpt(e.target.value)}
            rows={3}
            className={`w-full px-3 py-2 border rounded-md ${
              errors.excerpt ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Enter a brief excerpt or summary"
          />
          {errors.excerpt && (
            <p className="mt-1 text-sm text-red-600">{errors.excerpt}</p>
          )}
        </div>

        {/* Cover Image */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Cover Image
          </label>
          <div className="flex gap-2">
            <select
              value={coverImageId || ''}
              onChange={(e) => setCoverImageId(e.target.value ? Number(e.target.value) : undefined)}
              className="flex-1 px-3 py-2 border border-gray-300 rounded-md"
            >
              <option value="">No cover image</option>
              {availableMedia.map(media => (
                <option key={media.id} value={media.id}>
                  {media.filename}
                </option>
              ))}
            </select>
            <button
              type="button"
              onClick={() => window.open('/admin/dashboard?tab=media', '_blank')}
              className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 whitespace-nowrap"
            >
              Upload New
            </button>
          </div>
        </div>

        {/* Tags */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Tags
          </label>
          <div className="flex flex-wrap gap-2">
            {availableTags.map(tag => (
              <button
                key={tag.id}
                type="button"
                onClick={() => toggleTag(tag.id)}
                className={`px-3 py-1 rounded-full text-sm ${
                  selectedTagIds.includes(tag.id)
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                {tag.name}
              </button>
            ))}
          </div>
        </div>

        {/* Markdown Editor with Preview Toggle */}
        <div>
          <div className="flex justify-between items-center mb-2">
            <label className="block text-sm font-medium text-gray-700">
              Body (Markdown) *
            </label>
            <button
              type="button"
              onClick={() => setShowPreview(!showPreview)}
              className="px-3 py-1 text-sm text-blue-600 hover:text-blue-900"
            >
              {showPreview ? 'Edit' : 'Preview'}
            </button>
          </div>
          
          {showPreview ? (
            <div className="border border-gray-300 rounded-md p-4 min-h-[400px] bg-gray-50">
              <MarkdownRenderer content={body} />
            </div>
          ) : (
            <textarea
              value={body}
              onChange={(e) => setBody(e.target.value)}
              rows={20}
              className={`w-full px-3 py-2 border rounded-md font-mono text-sm ${
                errors.body ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="Enter post content in Markdown format..."
            />
          )}
          {errors.body && (
            <p className="mt-1 text-sm text-red-600">{errors.body}</p>
          )}
          
          {!showPreview && (
            <div className="mt-2 text-sm text-gray-500">
              <p className="font-medium mb-1">Markdown Tips:</p>
              <ul className="list-disc list-inside space-y-1">
                <li># Heading 1, ## Heading 2, ### Heading 3</li>
                <li>**bold text**, *italic text*</li>
                <li>[link text](url)</li>
              </ul>
            </div>
          )}
        </div>

        {/* Status */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Status
          </label>
          <div className="flex gap-4">
            <label className="flex items-center">
              <input
                type="radio"
                value="DRAFT"
                checked={status === 'DRAFT'}
                onChange={(e) => setStatus(e.target.value as 'DRAFT' | 'PUBLISHED')}
                className="mr-2"
              />
              <span className="text-sm">Draft</span>
            </label>
            <label className="flex items-center">
              <input
                type="radio"
                value="PUBLISHED"
                checked={status === 'PUBLISHED'}
                onChange={(e) => setStatus(e.target.value as 'DRAFT' | 'PUBLISHED')}
                className="mr-2"
              />
              <span className="text-sm">Published</span>
            </label>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex justify-end gap-4 pt-6 border-t">
          <button
            type="button"
            onClick={onCancel}
            disabled={saving}
            className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => handleSave('DRAFT')}
            disabled={saving}
            className="px-6 py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 disabled:opacity-50"
          >
            {saving ? 'Saving...' : 'Save Draft'}
          </button>
          <button
            type="button"
            onClick={() => handleSave('PUBLISHED')}
            disabled={saving}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {saving ? 'Publishing...' : 'Publish'}
          </button>
        </div>
      </div>
    </div>
  )
}
