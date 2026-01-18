import api from './api'
import type { MediaAsset, Tag, PageResponse } from './lists'

export interface BlogPostSummary {
  id: number
  title: string
  slug: string
  excerpt: string
  coverImage?: MediaAsset
  tags: Tag[]
  status: 'DRAFT' | 'PUBLISHED'
  publishedAt?: string
}

export interface BlogPostDetail {
  id: number
  title: string
  slug: string
  excerpt: string
  body: string
  coverImage?: MediaAsset
  tags: Tag[]
  status: 'DRAFT' | 'PUBLISHED'
  publishedAt?: string
}

export const postsApi = {
  // Get paginated posts with optional filters
  getPosts: async (params?: {
    search?: string
    tag?: string
    page?: number
    size?: number
  }) => {
    const response = await api.get<PageResponse<BlogPostSummary>>('/posts', { params })
    return response.data
  },

  // Get post by slug
  getPostBySlug: async (slug: string) => {
    const response = await api.get<BlogPostDetail>(`/posts/${slug}`)
    return response.data
  },
}
