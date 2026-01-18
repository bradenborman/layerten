import { useNavigate } from 'react-router-dom'

interface TagBadgeProps {
  name: string
  slug: string
  onClick?: () => void
  clickable?: boolean
}

export default function TagBadge({ name, slug, onClick, clickable = true }: TagBadgeProps) {
  const navigate = useNavigate()
  
  const handleClick = () => {
    if (onClick) {
      onClick()
    } else if (clickable) {
      // Navigate to lists filtered by this tag
      navigate(`/lists?tag=${slug}`)
    }
  }
  
  return (
    <span
      onClick={clickable ? handleClick : undefined}
      className={`
        inline-block px-3 py-1 text-sm font-medium rounded-full
        bg-primary-100 text-primary-800
        ${clickable ? 'cursor-pointer hover:bg-primary-200 transition-colors' : ''}
      `}
    >
      {name}
    </span>
  )
}
