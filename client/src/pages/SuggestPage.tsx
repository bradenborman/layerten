import { useState } from 'react'
import { suggestionsApi, type CreateSuggestionRequest } from '../services/suggestions'

export default function SuggestPage() {
  const [formData, setFormData] = useState<CreateSuggestionRequest>({
    title: '',
    description: '',
    category: '',
    exampleEntries: '',
    submitterName: '',
    submitterEmail: '',
  })
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.title.trim()) {
      newErrors.title = 'Title is required'
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Description is required'
    }

    if (formData.submitterEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.submitterEmail)) {
      newErrors.submitterEmail = 'Invalid email address'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    try {
      setSubmitting(true)
      await suggestionsApi.createSuggestion(formData)
      setSubmitted(true)
      // Reset form
      setFormData({
        title: '',
        description: '',
        category: '',
        exampleEntries: '',
        submitterName: '',
        submitterEmail: '',
      })
    } catch (error) {
      console.error('Failed to submit suggestion:', error)
      setErrors({ submit: 'Failed to submit suggestion. Please try again.' })
    } finally {
      setSubmitting(false)
    }
  }

  if (submitted) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <div className="bg-green-50 border border-green-200 rounded-lg p-8 text-center">
            <div className="text-green-600 text-5xl mb-4">✓</div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              Thank you for your suggestion!
            </h2>
            <p className="text-gray-600 mb-6">
              We've received your idea and will review it soon. We appreciate your contribution!
            </p>
            <button
              onClick={() => setSubmitted(false)}
              className="btn btn-primary"
            >
              Submit Another Suggestion
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">Suggest a List</h1>
        <p className="text-gray-600 mb-8">
          Have an idea for a ranked list? We'd love to hear it! Fill out the form below to share your suggestion.
        </p>

        <form onSubmit={handleSubmit} className="card space-y-6">
          {/* Title */}
          <div>
            <label htmlFor="title" className="label">
              List Title <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              id="title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              className={`input ${errors.title ? 'border-red-500' : ''}`}
              placeholder="e.g., Top 10 Best Sci-Fi Movies"
            />
            {errors.title && (
              <p className="mt-1 text-sm text-red-600">{errors.title}</p>
            )}
          </div>

          {/* Description */}
          <div>
            <label htmlFor="description" className="label">
              Description <span className="text-red-500">*</span>
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={4}
              className={`input ${errors.description ? 'border-red-500' : ''}`}
              placeholder="Describe your list idea and why it would be interesting..."
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-600">{errors.description}</p>
            )}
          </div>

          {/* Category */}
          <div>
            <label htmlFor="category" className="label">
              Category
            </label>
            <input
              type="text"
              id="category"
              name="category"
              value={formData.category}
              onChange={handleChange}
              className="input"
              placeholder="e.g., Movies, Technology, Food"
            />
          </div>

          {/* Example Entries */}
          <div>
            <label htmlFor="exampleEntries" className="label">
              Example Entries
            </label>
            <textarea
              id="exampleEntries"
              name="exampleEntries"
              value={formData.exampleEntries}
              onChange={handleChange}
              rows={4}
              className="input"
              placeholder="List a few examples of what would be included (one per line)"
            />
            <p className="mt-1 text-sm text-gray-500">
              Optional: Provide 3-5 examples to help us understand your vision
            </p>
          </div>

          {/* Submitter Name */}
          <div>
            <label htmlFor="submitterName" className="label">
              Your Name
            </label>
            <input
              type="text"
              id="submitterName"
              name="submitterName"
              value={formData.submitterName}
              onChange={handleChange}
              className="input"
              placeholder="Optional"
            />
          </div>

          {/* Submitter Email */}
          <div>
            <label htmlFor="submitterEmail" className="label">
              Your Email
            </label>
            <input
              type="email"
              id="submitterEmail"
              name="submitterEmail"
              value={formData.submitterEmail}
              onChange={handleChange}
              className={`input ${errors.submitterEmail ? 'border-red-500' : ''}`}
              placeholder="Optional - if you'd like us to follow up"
            />
            {errors.submitterEmail && (
              <p className="mt-1 text-sm text-red-600">{errors.submitterEmail}</p>
            )}
          </div>

          {/* Submit Error */}
          {errors.submit && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4">
              <p className="text-red-600">{errors.submit}</p>
            </div>
          )}

          {/* Submit Button */}
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {submitting ? 'Submitting...' : 'Submit Suggestion'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
