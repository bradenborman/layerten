import { useParams } from 'react-router-dom'

export default function ListDetailPage() {
  const { slug } = useParams<{ slug: string }>()
  
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-gray-900">List: {slug}</h1>
      <p className="mt-4 text-gray-600">List detail page</p>
    </div>
  )
}
