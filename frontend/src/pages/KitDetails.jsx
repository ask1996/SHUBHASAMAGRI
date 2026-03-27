import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getKitById } from '../api/kitApi'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

const KitDetails = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [kit, setKit] = useState(null)
  const [loading, setLoading] = useState(true)
  const [quantity, setQuantity] = useState(1)
  const { addToCart, loading: cartLoading } = useCart()
  const { isAuthenticated } = useAuth()

  useEffect(() => {
    fetchKit()
  }, [id])

  const fetchKit = async () => {
    try {
      setLoading(true)
      const response = await getKitById(id)
      if (response.success) {
        setKit(response.data)
      }
    } catch (err) {
      toast.error('Failed to load kit details')
      console.error('Error fetching kit:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      toast.error('Please login to add items to cart')
      navigate('/login')
      return
    }
    const success = await addToCart(kit.id, quantity)
    if (success) {
      // Cart toast is handled by CartContext
    }
  }

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(price)
  }

  if (loading) {
    return <LoadingSpinner message="Loading kit details..." />
  }

  if (!kit) {
    return (
      <div className="page-container error-page">
        <h2>Kit not found</h2>
        <button className="btn btn-primary" onClick={() => navigate('/')}>Go Home</button>
      </div>
    )
  }

  return (
    <div className="kit-details-page page-container">
      <button
        className="btn btn-ghost back-btn"
        onClick={() => navigate(kit.occasionId ? `/occasion/${kit.occasionId}` : '/')}
      >
        &#8592; Back to Kits
      </button>

      <div className="kit-details-layout">
        {/* Left: Image */}
        <div className="kit-details-image-section">
          <img
            src={kit.imageUrl || `https://via.placeholder.com/500x400/FF6B00/FFFFFF?text=${encodeURIComponent(kit.name)}`}
            alt={kit.name}
            className="kit-details-image"
            onError={(e) => {
              e.target.src = `https://via.placeholder.com/500x400/FF6B00/FFFFFF?text=Pooja+Kit`
            }}
          />
          <div className="kit-occasion-tag">
            <span>For: {kit.occasionName}</span>
          </div>
        </div>

        {/* Right: Details */}
        <div className="kit-details-info">
          <h1 className="kit-details-name">{kit.name}</h1>

          <div className="kit-details-meta">
            <span className="meta-item">&#128667; {kit.estimatedDeliveryDays} days delivery</span>
            <span className="meta-item">&#128219; {kit.itemCount} items included</span>
          </div>

          <div className="kit-price-section">
            <span className="kit-price-large">{formatPrice(kit.price)}</span>
            <span className="price-note">Inclusive of all taxes</span>
          </div>

          <p className="kit-details-description">{kit.description}</p>

          <div className="quantity-section">
            <label className="quantity-label">Quantity:</label>
            <div className="quantity-controls">
              <button
                className="qty-btn"
                onClick={() => setQuantity(q => Math.max(1, q - 1))}
                disabled={quantity <= 1}
              >
                -
              </button>
              <span className="qty-display">{quantity}</span>
              <button
                className="qty-btn"
                onClick={() => setQuantity(q => Math.min(10, q + 1))}
                disabled={quantity >= 10}
              >
                +
              </button>
            </div>
          </div>

          <div className="kit-total">
            Total: <strong>{formatPrice(kit.price * quantity)}</strong>
          </div>

          <button
            className="btn btn-primary btn-lg btn-full"
            onClick={handleAddToCart}
            disabled={cartLoading}
          >
            {cartLoading ? 'Adding to Cart...' : 'Add to Cart'}
          </button>

          {!isAuthenticated && (
            <p className="auth-hint">
              <a href="/login">Login</a> to add to cart
            </p>
          )}
        </div>
      </div>

      {/* Kit Items Section */}
      {kit.kitItems && kit.kitItems.length > 0 && (
        <div className="kit-items-section">
          <h2 className="kit-items-title">What's Included in This Kit</h2>
          <div className="kit-items-grid">
            {kit.kitItems.map((item) => (
              <div key={item.id} className="kit-item-card">
                <div className="kit-item-image-wrapper">
                  <img
                    src={item.imageUrl || `https://via.placeholder.com/80x80/FFA500/FFFFFF?text=${encodeURIComponent(item.itemName?.charAt(0) || 'P')}`}
                    alt={item.itemName}
                    className="kit-item-image"
                    onError={(e) => {
                      e.target.src = `https://via.placeholder.com/80x80/FFA500/FFFFFF?text=Item`
                    }}
                  />
                </div>
                <div className="kit-item-info">
                  <h4 className="kit-item-name">{item.itemName}</h4>
                  <p className="kit-item-qty">
                    {item.quantity} {item.unit}
                  </p>
                  {item.itemDescription && (
                    <p className="kit-item-desc">{item.itemDescription}</p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

export default KitDetails
