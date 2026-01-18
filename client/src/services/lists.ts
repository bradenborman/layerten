import api from './api'

export interface RankedListSummary {
  id: number
  title: string
  subtitle?: string
  slug: string
  coverImage?: MediaAsset
  tags: Tag[]
  entryCount: number
  publishedAt: string
}

export interface RankedListDetail {
  id: number
  title: string
  subtitle?: string
  slug: string
  intro: string
  outro?: string
  coverImage?: MediaAsset
  tags: Tag[]
  entries: RankedEntry[]
  publishedAt: string
}

export interface RankedEntry {
  id: number
  rank: number
  title: string
  blurb?: string
  commentary?: string
  funFact?: string
  externalLink?: string
  heroImage?: MediaAsset
}

export interface MediaAsset {
  id: number
  filename: string
  contentType: string
  fileSize: number
  altText?: string
  url: string
}

export interface Tag {
  id: number
  name: string
  slug: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const listsApi = {
  // Get paginated lists with optional filters
  getLists: async (params?: {
    search?: string
    tag?: string
    page?: number
    size?: number
  }) => {
    const response = await api.get<PageResponse<RankedListSummary>>('/lists', { params })
    return response.data
  },

  // Get list by slug
  getListBySlug: async (slug: string) => {
    const response = await api.get<RankedListDetail>(`/lists/${slug}`)
    return response.data
  },

  // Get entries for a list
  getListEntries: async (slug: string) => {
    const response = await api.get<RankedEntry[]>(`/lists/${slug}/entries`)
    return response.data
  },
}
