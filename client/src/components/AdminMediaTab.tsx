import { useEffect, useState } from 'react'
import { adminApi } from '../services/admin'
import type { MediaAsset } from '../services/lists'
import ImageUploader from './ImageUploader'

export default function AdminMediaTab() {
  const [media, setMedia] = useState<MediaAsset[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [showUploader, setShowUploader] = useState(false)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  useEffect(() => {
    fetchMedia()
  }, [])

  const fetchMedia = async () => {
    try {
      setLoading(true)
      const data = await adminApi.getAllMedia()
      setMedia(data)
    } catch (err) {
      console.error('Error fetching media:', err)
      setError('An error occurred while fetching media')
    } finally {
      setLoading(false)
    }
  }

  const handleUpload = async (file: File, altText: string) => {
    try {
      const uploaded = await adminApi.uploadMedia(file, altText)
      setMedia([uploaded, ...media])
      setShowUploader(false)
    } catch (err) {
      console.error('Error uploading media:', err)
      throw err // Re-throw to let ImageUploader handle the error
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this media asset? This action cannot be undone.')) {
      return
    }

    setDeletingId(id)
    try {
      await adminApi.deleteMedia(id)
      setMedia(media.filter(m => m.id !== id))
    } catch (err) {
      console.error('Error deleting media:', err)
      alert('Failed to delete media asset')
    } finally {
      setDeletingId(null)
    }
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
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
        <h2 className="text-2xl font-bold text-gray-900">Media Assets</h2>
        <button
          onClick={() => setShowUploader(!showUploader)}
          className="btn btn-primary"
        >
          {showUploader ? 'Cancel Upload' : 'Upload New Media'}
        </button>
      </div>

      {/* Upload Section */}
      {showUploader && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Upload New Image</h3>
          <ImageUploader onUpload={handleUpload} />
        </div>
      )}

      {/* Media Grid */}
      {media.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
          <p className="mt-2 text-gray-600">No media assets found</p>
          <p className="text-sm text-gray-500">Upload your first image to get started</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {media.map(item => (
            <div
              key={item.id}
              className="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition-shadow"
            >
              {/* Image Thumbnail */}
              <div className="aspect-square bg-gray-100 relative">
                <img
                  src={item.url}
                  alt={item.altText || item.filename}
                  className="w-full h-full object-cover"
                  loading="lazy"
                />
              </div>

              {/* Media Info */}
              <div className="p-4">
                <div className="mb-2">
                  <p className="text-sm font-medium text-gray-900 truncate" title={item.filename}>
                    {item.filename}
                  </p>
                  {item.altText && (
                    <p className="text-xs text-gray-500 truncate" title={item.altText}>
                      Alt: {item.altText}
                    </p>
                  )}
                </div>

                <div className="flex justify-between items-center text-xs text-gray-500 mb-3">
                  <span>{formatFileSize(item.fileSize)}</span>
                  <span>{item.contentType?.split('/')[1]?.toUpperCase()}</span>
                </div>

                {/* Actions */}
                <div className="flex gap-2">
                  <button
                    onClick={() => window.open(item.url, '_blank')}
                    className="flex-1 px-3 py-1.5 text-sm text-blue-600 border border-blue-600 rounded hover:bg-blue-50"
                  >
                    View
                  </button>
                  <button
                    onClick={() => handleDelete(item.id)}
                    disabled={deletingId === item.id}
                    className="flex-1 px-3 py-1.5 text-sm text-red-600 border border-red-600 rounded hover:bg-red-50 disabled:opacity-50"
                  >
                    {deletingId === item.id ? 'Deleting...' : 'Delete'}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Summary Stats */}
      <div className="mt-6 bg-white rounded-lg shadow p-4">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div>
            <div className="text-sm text-gray-600">Total Assets</div>
            <div className="text-2xl font-bold text-gray-900">{media.length}</div>
          </div>
          <div>
            <div className="text-sm text-gray-600">Total Size</div>
            <div className="text-2xl font-bold text-gray-900">
              {formatFileSize(media.reduce((sum, m) => sum + m.fileSize, 0))}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600">Images</div>
            <div className="text-2xl font-bold text-gray-900">
              {media.filter(m => m.contentType?.startsWith('image/')).length}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600">Average Size</div>
            <div className="text-2xl font-bold text-gray-900">
              {media.length > 0 
                ? formatFileSize(media.reduce((sum, m) => sum + m.fileSize, 0) / media.length)
                : '0 B'}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
