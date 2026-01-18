import { useParams } from 'react-router-dom'

export default function PostDetailPage() {
  const { slug } = useParams<{ slug: string }>()
  
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-gray-900">Post: {slug}</h1>
      <p className="mt-4 text-gray-600">Post detail page</p>
    </div>
  )
}
