import { useNavigate } from 'react-router-dom'

const OccasionCard = ({ occasion }) => {
  const navigate = useNavigate()

  const handleExploreKits = () => {
    navigate(`/occasion/${occasion.id}`)
  }

  const placeholderImage = `https://via.placeholder.com/400x250/FF6B00/FFFFFF?text=${encodeURIComponent(occasion.name)}`

  return (
    <div className="card occasion-card">
      <div className="card-image-wrapper">
        <img
          src={occasion.imageUrl || placeholderImage}
          alt={occasion.name}
          className="card-image"
          onError={(e) => {
            e.target.src = placeholderImage
          }}
        />
        <div className="card-image-overlay">
          {occasion.kitCount !== undefined && (
            <span className="kit-count-badge">
              {occasion.kitCount} {occasion.kitCount === 1 ? 'Kit' : 'Kits'} Available
            </span>
          )}
        </div>
      </div>
      <div className="card-body">
        <h3 className="card-title">{occasion.name}</h3>
        <p className="card-description">
          {occasion.description && occasion.description.length > 120
            ? `${occasion.description.substring(0, 120)}...`
            : occasion.description}
        </p>
        <button
          className="btn btn-primary btn-full"
          onClick={handleExploreKits}
        >
          Explore Kits
        </button>
      </div>
    </div>
  )
}

export default OccasionCard
