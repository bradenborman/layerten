import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import type { RankedEntry, MediaAsset } from '../services/lists'
import type { CreateEntryRequest } from '../services/admin'

interface EntryEditorProps {
  entry?: RankedEntry
  listId: number
  suggestedRank: number
  availableMedia: MediaAsset[]
  onSave: (entryData: CreateEntryRequest & { id?: number }) => void
  onCancel: () => void
}

export default function EntryEditor({
  entry,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  listId: _listId, // Not currently used but kept for future functionality
  suggestedRank,
  availableMedia,
  onSave,
  onCancel
}: EntryEditorProps) {
  const navigate = useNavigate()
  const [rank, setRank] = useState(suggestedRank)
  const [title, setTitle] = useState('')
  const [blurb, setBlurb] = useState('')
  const [commentary, setCommentary] = useState('')
  const [funFact, setFunFact] = useState('')
  const [externalLink, setExternalLink] = useState('')
  const [heroImageId, setHeroImageId] = useState<number | undefined>()
  
  const [errors, setErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    if (entry) {
      setRank(entry.rank)
      setTitle(entry.title)
      setBlurb(entry.blurb || '')
      setCommentary(entry.commentary || '')
      setFunFact(entry.funFact || '')
      setExternalLink(entry.externalLink || '')
      setHeroImageId(entry.heroImage?.id)
    }
  }, [entry])

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {}
    
    if (!rank || rank < 1) {
      newErrors.rank = 'Rank must be a positive number'
    }
    
    if (!title.trim()) {
      newErrors.title = 'Title is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validate()) {
      return
    }
    
    const entryData = {
      id: entry?.id,
      rank,
      title,
      blurb: blurb || undefined,
      commentary: commentary || undefined,
      funFact: funFact || undefined,
      externalLink: externalLink || undefined,
      heroImageId
    }
    
    onSave(entryData)
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b px-6 py-4">
          <h2 className="text-2xl font-bold text-gray-900">
            {entry ? 'Edit Entry' : 'Add New Entry'}
          </h2>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Rank */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Rank *
            </label>
            <input
              type="number"
              min="1"
              value={rank}
              onChange={(e) => setRank(Number(e.target.value))}
              className={`w-full px-3 py-2 border rounded-md ${
                errors.rank ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="Enter rank (e.g., 1, 2, 3...)"
            />
            {errors.rank && (
              <p className="mt-1 text-sm text-red-600">{errors.rank}</p>
            )}
            <p className="mt-1 text-sm text-gray-500">
              Higher numbers appear first in the list
            </p>
          </div>

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
              placeholder="Enter entry title"
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600">{errors.title}</p>
            )}
          </div>

          {/* Blurb */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Blurb
            </label>
            <textarea
              value={blurb}
              onChange={(e) => setBlurb(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              placeholder="Short description or teaser (optional)"
            />
          </div>

          {/* Commentary */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Commentary
            </label>
            <textarea
              value={commentary}
              onChange={(e) => setCommentary(e.target.value)}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              placeholder="Detailed commentary or analysis (optional)"
            />
          </div>

          {/* Fun Fact */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Fun Fact
            </label>
            <textarea
              value={funFact}
              onChange={(e) => setFunFact(e.target.value)}
              rows={2}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              placeholder="Interesting trivia or fact (optional)"
            />
          </div>

          {/* External Link */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              External Link
            </label>
            <input
              type="url"
              value={externalLink}
              onChange={(e) => setExternalLink(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              placeholder="https://example.com (optional)"
            />
          </div>

          {/* Hero Image */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Hero Image
            </label>
            <div className="flex gap-2">
              <select
                value={heroImageId || ''}
                onChange={(e) => setHeroImageId(e.target.value ? Number(e.target.value) : undefined)}
                className="flex-1 px-3 py-2 border border-gray-300 rounded-md"
              >
                <option value="">No hero image</option>
                {availableMedia.map(media => (
                  <option key={media.id} value={media.id}>
                    {media.filename}
                  </option>
                ))}
              </select>
              <button
                type="button"
                onClick={() => navigate('/admin/dashboard?tab=media')}
                className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 whitespace-nowrap"
              >
                Upload New
              </button>
            </div>
            {heroImageId && (
              <div className="mt-2">
                <img
                  src={availableMedia.find(m => m.id === heroImageId)?.url}
                  alt="Preview"
                  className="h-32 w-auto rounded border border-gray-300"
                />
              </div>
            )}
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end gap-4 pt-4 border-t">
            <button
              type="button"
              onClick={onCancel}
              className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              {entry ? 'Update Entry' : 'Add Entry'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
