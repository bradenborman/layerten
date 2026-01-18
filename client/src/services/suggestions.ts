import api from './api'

export interface CreateSuggestionRequest {
  title: string
  description: string
  category?: string
  exampleEntries?: string
  submitterName?: string
  submitterEmail?: string
}

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

export const suggestionsApi = {
  // Create a new suggestion
  createSuggestion: async (data: CreateSuggestionRequest) => {
    const response = await api.post<Suggestion>('/suggestions', data)
    return response.data
  },
}
