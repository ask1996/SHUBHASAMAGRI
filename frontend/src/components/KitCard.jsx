import { useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const KitCard = ({ kit }) => {
  const navigate = useNavigate()
  const { addToCart, loading } = useCart()
  const { isAuthenticated } = useAuth()

  const handleViewDetails = () => {
    navigate(`/kit/${kit.id}`)
  }

  const handleAddToCart = async (e) => {
    e.stopPropagation()
    if (!isAuthenticated) {
      toast.error('Please login to add items to cart')
      navigate('/login')
      return
    }
    await addToCart(kit.id, 1)
  }

  const placeholderImage = `https://via.placeholder.com/400x250/FFA500/FFFFFF?text=${encodeURIComponent(kit.name)}`

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(price)
  }

  return (
    <div className="card kit-card">
      <div className="card-image-wrapper">
        <img
          src={kit.imageUrl || placeholderImage}
          alt={kit.name}
          className="card-image"
          onError={(e) => {
            e.target.src = placeholderImage
          }}
        />
        <div className="card-image-overlay">
          {kit.occasionName && (
            <span className="occasion-badge">{kit.occasionName}</span>
          )}
        </div>
      </div>
      <div className="card-body">
        <h3 className="card-title">{kit.name}</h3>
        <p className="card-description">
          {kit.description && kit.description.length > 100
            ? `${kit.description.substring(0, 100)}...`
            : kit.description}
        </p>

        <div className="kit-meta">
          <span className="kit-items-count">
            {kit.itemCount || 0} items included
          </span>
          <span className="kit-delivery">
            Delivery in {kit.estimatedDeliveryDays} days
          </span>
        </div>

        <div className="card-price-row">
          <span className="kit-price">{formatPrice(kit.price)}</span>
        </div>

        <div className="card-actions">
          <button
            className="btn btn-secondary btn-sm"
            onClick={handleViewDetails}
          >
            View Details
          </button>
          <button
            className="btn btn-primary btn-sm"
            onClick={handleAddToCart}
            disabled={loading}
          >
            {loading ? 'Adding...' : 'Add to Cart'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default KitCard
