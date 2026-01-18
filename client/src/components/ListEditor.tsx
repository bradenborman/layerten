import { useState, useEffect } from 'react'
import { adminApi } from '../services/admin'
import type { RankedEntry, Tag, MediaAsset } from '../services/lists'
import type { CreateListRequest, UpdateListRequest, CreateEntryRequest, EntryRankUpdate } from '../services/admin'
import EntryEditor from './EntryEditor'
import LoadingSpinner from './LoadingSpinner'

interface ListEditorProps {
  listId?: number
  onSave: () => void
  onCancel: () => void
}

export default function ListEditor({ listId, onSave, onCancel }: ListEditorProps) {
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  
  // Form state
  const [title, setTitle] = useState('')
  const [subtitle, setSubtitle] = useState('')
  const [intro, setIntro] = useState('')
  const [outro, setOutro] = useState('')
  const [coverImageId, setCoverImageId] = useState<number | undefined>()
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([])
  const [entries, setEntries] = useState<RankedEntry[]>([])
  
  // Available options
  const [availableTags, setAvailableTags] = useState<Tag[]>([])
  const [availableMedia, setAvailableMedia] = useState<MediaAsset[]>([])
  
  // Drag state
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null)
  
  // Entry editor state
  const [showEntryEditor, setShowEntryEditor] = useState(false)
  const [editingEntry, setEditingEntry] = useState<RankedEntry | undefined>()
  const [editingEntryIndex, setEditingEntryIndex] = useState<number | undefined>()
  
  const [errors, setErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    loadData()
  }, [listId])

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
      
      // Load existing list if editing
      if (listId) {
        const list = await adminApi.getListById(listId)
        setTitle(list.title)
        setSubtitle(list.subtitle || '')
        setIntro(list.intro)
        setOutro(list.outro || '')
        setCoverImageId(list.coverImage?.id)
        setSelectedTagIds(Array.from(list.tags).map(t => t.id))
        setEntries(list.entries.sort((a, b) => a.rank - b.rank))
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
    if (!intro.trim()) {
      newErrors.intro = 'Intro is required'
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSave = async () => {
    if (!validate()) {
      return
    }
    
    setSaving(true)
    try {
      const listData = {
        title,
        subtitle: subtitle || undefined,
        intro,
        outro: outro || undefined,
        coverImageId,
        tagIds: selectedTagIds.length > 0 ? new Set(selectedTagIds) : undefined
      }
      
      let savedListId = listId
      
      if (listId) {
        // Update existing list
        await adminApi.updateList(listId, listData as UpdateListRequest)
      } else {
        // Create new list
        const createdList = await adminApi.createList(listData as CreateListRequest)
        savedListId = createdList.id
      }
      
      // Handle entries for the list
      if (savedListId) {
        // Add new entries (those with temporary IDs > 1000000000000)
        for (const entry of entries) {
          if (entry.id > 1000000000000) {
            await adminApi.addEntry(savedListId, {
              rank: entry.rank,
              title: entry.title,
              blurb: entry.blurb,
              commentary: entry.commentary,
              funFact: entry.funFact,
              externalLink: entry.externalLink,
              heroImageId: entry.heroImage?.id
            })
          }
        }
        
        // Update entry ranks if changed (only for existing entries)
        const existingEntries = entries.filter(e => e.id <= 1000000000000)
        if (existingEntries.length > 0) {
          const rankUpdates: EntryRankUpdate[] = existingEntries.map((entry, index) => ({
            entryId: entry.id,
            newRank: index + 1
          }))
          
          await adminApi.reorderEntries(savedListId, rankUpdates)
        }
      }
      
      onSave()
    } catch (err) {
      console.error('Error saving list:', err)
      alert('Failed to save list')
    } finally {
      setSaving(false)
    }
  }

  const handleDragStart = (index: number) => {
    setDraggedIndex(index)
  }

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault()
    
    if (draggedIndex === null || draggedIndex === index) {
      return
    }
    
    const newEntries = [...entries]
    const draggedEntry = newEntries[draggedIndex]
    newEntries.splice(draggedIndex, 1)
    newEntries.splice(index, 0, draggedEntry)
    
    setEntries(newEntries)
    setDraggedIndex(index)
  }

  const handleDragEnd = () => {
    setDraggedIndex(null)
  }

  const handleDeleteEntry = (index: number) => {
    if (confirm('Are you sure you want to delete this entry?')) {
      setEntries(entries.filter((_, i) => i !== index))
    }
  }

  const handleAddEntry = () => {
    setEditingEntry(undefined)
    setEditingEntryIndex(undefined)
    setShowEntryEditor(true)
  }

  const handleEditEntry = (index: number) => {
    setEditingEntry(entries[index])
    setEditingEntryIndex(index)
    setShowEntryEditor(true)
  }

  const handleEntrySave = async (entryData: CreateEntryRequest & { id?: number }) => {
    if (editingEntryIndex !== undefined) {
      // Update existing entry
      const newEntries = [...entries]
      newEntries[editingEntryIndex] = {
        ...newEntries[editingEntryIndex],
        rank: entryData.rank,
        title: entryData.title,
        blurb: entryData.blurb,
        commentary: entryData.commentary,
        funFact: entryData.funFact,
        externalLink: entryData.externalLink,
        heroImage: entryData.heroImageId 
          ? availableMedia.find(m => m.id === entryData.heroImageId)
          : undefined
      }
      setEntries(newEntries)
    } else {
      // Add new entry
      const newEntry: RankedEntry = {
        id: Date.now(), // Temporary ID for new entries
        rank: entryData.rank,
        title: entryData.title,
        blurb: entryData.blurb,
        commentary: entryData.commentary,
        funFact: entryData.funFact,
        externalLink: entryData.externalLink,
        heroImage: entryData.heroImageId 
          ? availableMedia.find(m => m.id === entryData.heroImageId)
          : undefined
      }
      setEntries([...entries, newEntry])
    }
    
    setShowEntryEditor(false)
    setEditingEntry(undefined)
    setEditingEntryIndex(undefined)
  }

  const handleEntryCancel = () => {
    setShowEntryEditor(false)
    setEditingEntry(undefined)
    setEditingEntryIndex(undefined)
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
        {listId ? 'Edit List' : 'Create New List'}
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
            placeholder="Enter list title"
          />
          {errors.title && (
            <p className="mt-1 text-sm text-red-600">{errors.title}</p>
          )}
        </div>

        {/* Subtitle */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Subtitle
          </label>
          <input
            type="text"
            value={subtitle}
            onChange={(e) => setSubtitle(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="Enter subtitle (optional)"
          />
        </div>

        {/* Intro */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Intro *
          </label>
          <textarea
            value={intro}
            onChange={(e) => setIntro(e.target.value)}
            rows={4}
            className={`w-full px-3 py-2 border rounded-md ${
              errors.intro ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Enter introduction text"
          />
          {errors.intro && (
            <p className="mt-1 text-sm text-red-600">{errors.intro}</p>
          )}
        </div>

        {/* Outro */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Outro
          </label>
          <textarea
            value={outro}
            onChange={(e) => setOutro(e.target.value)}
            rows={4}
            className="w-full px-3 py-2 border border-gray-300 rounded-md"
            placeholder="Enter outro text (optional)"
          />
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
              onClick={() => window.open(`${window.location.origin}/admin/dashboard?tab=media`, '_blank')}
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

        {/* Entries Section */}
        <div>
          <div className="flex justify-between items-center mb-4">
            <label className="block text-sm font-medium text-gray-700">
              Entries
            </label>
            <button
              type="button"
              onClick={handleAddEntry}
              className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700"
            >
              Add Entry
            </button>
          </div>

          {entries.length === 0 ? (
            <div className="text-center py-8 bg-gray-50 rounded-lg">
              <p className="text-gray-600">No entries yet. Add your first entry!</p>
            </div>
          ) : (
            <div className="space-y-2">
              {entries.map((entry, index) => (
                <div
                  key={entry.id || index}
                  draggable
                  onDragStart={() => handleDragStart(index)}
                  onDragOver={(e) => handleDragOver(e, index)}
                  onDragEnd={handleDragEnd}
                  className={`flex items-center gap-4 p-4 bg-gray-50 rounded-lg cursor-move hover:bg-gray-100 ${
                    draggedIndex === index ? 'opacity-50' : ''
                  }`}
                >
                  <div className="flex-shrink-0 w-8 h-8 bg-blue-600 text-white rounded-full flex items-center justify-center font-bold">
                    {index + 1}
                  </div>
                  <div className="flex-1">
                    <div className="font-medium text-gray-900">{entry.title}</div>
                    {entry.blurb && (
                      <div className="text-sm text-gray-600 truncate">{entry.blurb}</div>
                    )}
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => handleEditEntry(index)}
                      className="px-3 py-1 text-sm text-blue-600 hover:text-blue-900"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDeleteEntry(index)}
                      className="px-3 py-1 text-sm text-red-600 hover:text-red-900"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
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
            onClick={handleSave}
            disabled={saving}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
          >
            {saving ? 'Saving...' : 'Save List'}
          </button>
        </div>
      </div>

      {/* Entry Editor Modal */}
      {showEntryEditor && (
        <EntryEditor
          entry={editingEntry}
          listId={listId || 0}
          suggestedRank={entries.length + 1}
          availableMedia={availableMedia}
          onSave={handleEntrySave}
          onCancel={handleEntryCancel}
        />
      )}
    </div>
  )
}
