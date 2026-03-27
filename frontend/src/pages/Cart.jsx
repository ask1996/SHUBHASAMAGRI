import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { placeOrder } from '../api/orderApi'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

const Cart = () => {
  const { cart, loading, removeFromCart, updateQuantity, fetchCart } = useCart()
  const navigate = useNavigate()
  const [showOrderForm, setShowOrderForm] = useState(false)
  const [orderLoading, setOrderLoading] = useState(false)
  const [orderForm, setOrderForm] = useState({
    deliveryAddress: '',
    phone: '',
  })
  const [formErrors, setFormErrors] = useState({})

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(price)
  }

  const validateOrderForm = () => {
    const errors = {}
    if (!orderForm.deliveryAddress || orderForm.deliveryAddress.trim().length < 10) {
      errors.deliveryAddress = 'Please enter a complete delivery address (at least 10 characters)'
    }
    if (!orderForm.phone || !/^[0-9]{10}$/.test(orderForm.phone)) {
      errors.phone = 'Please enter a valid 10-digit phone number'
    }
    setFormErrors(errors)
    return Object.keys(errors).length === 0
  }

  const handlePlaceOrder = async (e) => {
    e.preventDefault()
    if (!validateOrderForm()) return

    try {
      setOrderLoading(true)
      const response = await placeOrder(orderForm)
      if (response.success) {
        toast.success('Order placed successfully! Jai Shri Ram!')
        await fetchCart()
        setShowOrderForm(false)
        navigate('/orders')
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to place order. Please try again.')
    } finally {
      setOrderLoading(false)
    }
  }

  if (loading && !cart?.items?.length) {
    return <LoadingSpinner message="Loading your cart..." />
  }

  if (!cart?.items || cart.items.length === 0) {
    return (
      <div className="page-container empty-cart-page">
        <div className="empty-state">
          <div className="empty-icon">&#128722;</div>
          <h2>Your Cart is Empty</h2>
          <p>Add pooja kits for your sacred occasions to get started</p>
          <Link to="/" className="btn btn-primary btn-lg">
            Browse Occasions
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="cart-page page-container">
      <h1 className="page-title">Your Pooja Cart</h1>

      <div className="cart-layout">
        {/* Cart Items */}
        <div className="cart-items-section">
          {cart.items.map((item) => (
            <div key={item.id} className="cart-item-card">
              <div className="cart-item-image">
                <img
                  src={item.kitImageUrl || `https://via.placeholder.com/100x100/FF6B00/FFFFFF?text=Kit`}
                  alt={item.kitName}
                  onError={(e) => { e.target.src = `https://via.placeholder.com/100x100/FF6B00/FFFFFF?text=Kit` }}
                />
              </div>
              <div className="cart-item-details">
                <h3 className="cart-item-name">{item.kitName}</h3>
                <p className="cart-item-occasion">{item.occasionName}</p>
                <p className="cart-item-price">
                  {formatPrice(item.unitPrice)} each
                </p>
                <p className="cart-item-delivery">
                  Estimated delivery: {item.estimatedDeliveryDays} days
                </p>
              </div>
              <div className="cart-item-controls">
                <div className="quantity-controls">
                  <button
                    className="qty-btn"
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    disabled={loading}
                  >
                    -
                  </button>
                  <span className="qty-display">{item.quantity}</span>
                  <button
                    className="qty-btn"
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    disabled={loading || item.quantity >= 10}
                  >
                    +
                  </button>
                </div>
                <p className="cart-item-subtotal">
                  {formatPrice(item.subtotal)}
                </p>
                <button
                  className="btn btn-danger btn-sm"
                  onClick={() => removeFromCart(item.id)}
                  disabled={loading}
                >
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Order Summary */}
        <div className="cart-summary-section">
          <div className="cart-summary-card">
            <h2 className="summary-title">Order Summary</h2>

            <div className="summary-row">
              <span>Total Items:</span>
              <span>{cart.totalItems}</span>
            </div>
            <div className="summary-row">
              <span>Kits in Cart:</span>
              <span>{cart.items.length}</span>
            </div>
            <div className="summary-divider"></div>
            <div className="summary-row summary-total">
              <span>Total Amount:</span>
              <span>{formatPrice(cart.totalAmount)}</span>
            </div>
            <p className="summary-note">Delivery charges calculated at checkout</p>

            {!showOrderForm ? (
              <button
                className="btn btn-primary btn-full btn-lg"
                onClick={() => setShowOrderForm(true)}
              >
                Proceed to Order
              </button>
            ) : (
              <div className="order-form">
                <h3 className="order-form-title">Delivery Details</h3>
                <form onSubmit={handlePlaceOrder}>
                  <div className="form-group">
                    <label htmlFor="deliveryAddress">Delivery Address *</label>
                    <textarea
                      id="deliveryAddress"
                      className={`form-control ${formErrors.deliveryAddress ? 'error' : ''}`}
                      rows="3"
                      placeholder="Enter your complete delivery address including flat/house no., street, city, state, pincode"
                      value={orderForm.deliveryAddress}
                      onChange={(e) => setOrderForm({ ...orderForm, deliveryAddress: e.target.value })}
                    />
                    {formErrors.deliveryAddress && (
                      <span className="error-text">{formErrors.deliveryAddress}</span>
                    )}
                  </div>
                  <div className="form-group">
                    <label htmlFor="phone">Contact Phone *</label>
                    <input
                      id="phone"
                      type="tel"
                      className={`form-control ${formErrors.phone ? 'error' : ''}`}
                      placeholder="10-digit mobile number"
                      value={orderForm.phone}
                      onChange={(e) => setOrderForm({ ...orderForm, phone: e.target.value })}
                      maxLength="10"
                    />
                    {formErrors.phone && (
                      <span className="error-text">{formErrors.phone}</span>
                    )}
                  </div>
                  <button
                    type="submit"
                    className="btn btn-primary btn-full"
                    disabled={orderLoading}
                  >
                    {orderLoading ? 'Placing Order...' : `Place Order — ${formatPrice(cart.totalAmount)}`}
                  </button>
                  <button
                    type="button"
                    className="btn btn-ghost btn-full"
                    onClick={() => setShowOrderForm(false)}
                    disabled={orderLoading}
                  >
                    Cancel
                  </button>
                </form>
              </div>
            )}

            <Link to="/" className="continue-shopping-link">
              &#8592; Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Cart
