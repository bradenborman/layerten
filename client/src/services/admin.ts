import api from './api'
import type { RankedListDetail, RankedEntry, MediaAsset } from './lists'
import type { BlogPostDetail } from './posts'

export interface Suggestion {
  id: number
  title: string
  description: string
  category?: string
  exampleEntries?: string
  submitterName?: string
  submitterEmail?: string
  status: 'NEW' | 'REVIEWING' | 'ACCEPTED' | 'DECLINED'
  createdAt: string
}

export interface CreateListRequest {
  title: string
  subtitle?: string
  intro: string
  outro?: string
  coverImageId?: number
  tagIds?: number[]
}

export interface UpdateListRequest {
  title?: string
  subtitle?: string
  intro?: string
  outro?: string
  coverImageId?: number
  tagIds?: number[]
}

export interface CreateEntryRequest {
  rank: number
  title: string
  blurb?: string
  commentary?: string
  funFact?: string
  externalLink?: string
  heroImageId?: number
}

export interface EntryRankUpdate {
  entryId: number
  newRank: number
}

export interface CreatePostRequest {
  title: string
  excerpt: string
  body: string
  coverImageId?: number
  tagIds?: number[]
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface UpdatePostRequest {
  title?: string
  excerpt?: string
  body?: string
  coverImageId?: number
  tagIds?: number[]
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface UpdateSuggestionStatusRequest {
  status: 'NEW' | 'REVIEWING' | 'ACCEPTED' | 'DECLINED'
}

export const adminApi = {
  // Lists
  createList: async (data: CreateListRequest) => {
    const response = await api.post<RankedListDetail>('/admin/lists', data)
    return response.data
  },

  updateList: async (id: number, data: UpdateListRequest) => {
    const response = await api.put<RankedListDetail>(`/admin/lists/${id}`, data)
    return response.data
  },

  deleteList: async (id: number) => {
    await api.delete(`/admin/lists/${id}`)
  },

  addEntry: async (listId: number, data: CreateEntryRequest) => {
    const response = await api.post<RankedEntry>(`/admin/lists/${listId}/entries`, data)
    return response.data
  },

  reorderEntries: async (listId: number, updates: EntryRankUpdate[]) => {
    await api.put(`/admin/lists/${listId}/entries/reorder`, updates)
  },

  // Posts
  createPost: async (data: CreatePostRequest) => {
    const response = await api.post<BlogPostDetail>('/admin/posts', data)
    return response.data
  },

  updatePost: async (id: number, data: UpdatePostRequest) => {
    const response = await api.put<BlogPostDetail>(`/admin/posts/${id}`, data)
    return response.data
  },

  deletePost: async (id: number) => {
    await api.delete(`/admin/posts/${id}`)
  },

  // Media
  uploadMedia: async (file: File, altText?: string) => {
    const formData = new FormData()
    formData.append('file', file)
    if (altText) {
      formData.append('altText', altText)
    }
    const response = await api.post<MediaAsset>('/admin/media', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  deleteMedia: async (id: number) => {
    await api.delete(`/admin/media/${id}`)
  },

  // Suggestions
  getAllSuggestions: async () => {
    const response = await api.get<Suggestion[]>('/admin/suggestions')
    return response.data
  },

  updateSuggestionStatus: async (id: number, data: UpdateSuggestionStatusRequest) => {
    const response = await api.put<Suggestion>(`/admin/suggestions/${id}`, data)
    return response.data
  },
}
