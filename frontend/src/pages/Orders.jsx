import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getOrders, cancelOrder } from '../api/orderApi'
import LoadingSpinner from '../components/LoadingSpinner'
import toast from 'react-hot-toast'

const STATUS_CONFIG = {
  PENDING: { label: 'Pending', className: 'status-pending', icon: '&#9203;' },
  CONFIRMED: { label: 'Confirmed', className: 'status-confirmed', icon: '&#10003;' },
  SHIPPED: { label: 'Shipped', className: 'status-shipped', icon: '&#128666;' },
  DELIVERED: { label: 'Delivered', className: 'status-delivered', icon: '&#127774;' },
  CANCELLED: { label: 'Cancelled', className: 'status-cancelled', icon: '&#10007;' },
}

const Orders = () => {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [cancellingId, setCancellingId] = useState(null)

  useEffect(() => {
    fetchOrders()
  }, [])

  const fetchOrders = async () => {
    try {
      setLoading(true)
      const response = await getOrders()
      if (response.success) {
        setOrders(response.data)
      }
    } catch (err) {
      toast.error('Failed to load orders')
      console.error('Error fetching orders:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return

    try {
      setCancellingId(orderId)
      const response = await cancelOrder(orderId)
      if (response.success) {
        setOrders(prev =>
          prev.map(order => order.id === orderId ? response.data : order)
        )
        toast.success('Order cancelled successfully')
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel order')
    } finally {
      setCancellingId(null)
    }
  }

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0,
    }).format(price)
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A'
    return new Date(dateStr).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (loading) {
    return <LoadingSpinner message="Loading your orders..." />
  }

  if (orders.length === 0) {
    return (
      <div className="page-container empty-orders-page">
        <div className="empty-state">
          <div className="empty-icon">&#128219;</div>
          <h2>No Orders Yet</h2>
          <p>You haven't placed any orders yet. Start exploring our pooja kits!</p>
          <Link to="/" className="btn btn-primary btn-lg">
            Browse Occasions
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="orders-page page-container">
      <h1 className="page-title">My Orders</h1>
      <p className="page-subtitle">{orders.length} {orders.length === 1 ? 'order' : 'orders'} placed</p>

      <div className="orders-list">
        {orders.map((order) => {
          const statusConfig = STATUS_CONFIG[order.status] || STATUS_CONFIG.PENDING

          return (
            <div key={order.id} className="order-card">
              <div className="order-header">
                <div className="order-header-left">
                  <h3 className="order-id">Order #{order.id}</h3>
                  <span className="order-date">{formatDate(order.createdAt)}</span>
                </div>
                <div className="order-header-right">
                  <span className={`status-badge ${statusConfig.className}`}>
                    <span dangerouslySetInnerHTML={{ __html: statusConfig.icon }} />
                    {' '}{statusConfig.label}
                  </span>
                </div>
              </div>

              <div className="order-items-list">
                {order.orderItems && order.orderItems.map((item) => (
                  <div key={item.id} className="order-item-row">
                    <div className="order-item-image">
                      <img
                        src={item.kitImageUrl || `https://via.placeholder.com/60x60/FF6B00/FFFFFF?text=Kit`}
                        alt={item.kitName}
                        onError={(e) => {
                          e.target.src = `https://via.placeholder.com/60x60/FF6B00/FFFFFF?text=Kit`
                        }}
                      />
                    </div>
                    <div className="order-item-info">
                      <p className="order-item-name">{item.kitName}</p>
                      {item.occasionName && (
                        <p className="order-item-occasion">{item.occasionName}</p>
                      )}
                      <p className="order-item-qty">Qty: {item.quantity}</p>
                    </div>
                    <div className="order-item-price">
                      <p className="order-item-subtotal">{formatPrice(item.subtotal)}</p>
                      <p className="order-item-unit-price">{formatPrice(item.priceAtOrder)} each</p>
                    </div>
                  </div>
                ))}
              </div>

              <div className="order-footer">
                <div className="order-delivery-info">
                  <p className="delivery-label">Delivery to:</p>
                  <p className="delivery-address">{order.deliveryAddress}</p>
                  <p className="delivery-phone">Phone: {order.phone}</p>
                </div>
                <div className="order-summary">
                  <div className="order-total">
                    Total: <strong>{formatPrice(order.totalAmount)}</strong>
                  </div>
                  {order.status === 'PENDING' && (
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => handleCancelOrder(order.id)}
                      disabled={cancellingId === order.id}
                    >
                      {cancellingId === order.id ? 'Cancelling...' : 'Cancel Order'}
                    </button>
                  )}
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default Orders
