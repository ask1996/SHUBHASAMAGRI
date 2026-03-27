import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { getCart, addToCart as addToCartApi, removeFromCart as removeFromCartApi,
         updateCartItem as updateCartItemApi, clearCart as clearCartApi } from '../api/cartApi'
import { useAuth } from './AuthContext'
import toast from 'react-hot-toast'

const CartContext = createContext(null)

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState({ items: [], totalItems: 0, totalAmount: 0 })
  const [loading, setLoading] = useState(false)
  const { isAuthenticated } = useAuth()

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart({ items: [], totalItems: 0, totalAmount: 0 })
      return
    }
    try {
      setLoading(true)
      const response = await getCart()
      if (response.success) {
        setCart(response.data)
      }
    } catch (error) {
      console.error('Failed to fetch cart:', error)
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    fetchCart()
  }, [fetchCart])

  const addToCart = async (kitId, quantity = 1) => {
    if (!isAuthenticated) {
      toast.error('Please login to add items to cart')
      return false
    }
    try {
      setLoading(true)
      const response = await addToCartApi(kitId, quantity)
      if (response.success) {
        setCart(response.data)
        toast.success('Added to cart!')
        return true
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add to cart')
      return false
    } finally {
      setLoading(false)
    }
  }

  const removeFromCart = async (cartItemId) => {
    try {
      setLoading(true)
      const response = await removeFromCartApi(cartItemId)
      if (response.success) {
        setCart(response.data)
        toast.success('Item removed from cart')
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to remove item')
    } finally {
      setLoading(false)
    }
  }

  const updateQuantity = async (cartItemId, quantity) => {
    try {
      setLoading(true)
      const response = await updateCartItemApi(cartItemId, quantity)
      if (response.success) {
        setCart(response.data)
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update quantity')
    } finally {
      setLoading(false)
    }
  }

  const clearCart = async () => {
    try {
      await clearCartApi()
      setCart({ items: [], totalItems: 0, totalAmount: 0 })
    } catch (error) {
      console.error('Failed to clear cart:', error)
    }
  }

  const cartCount = cart?.totalItems || 0

  return (
    <CartContext.Provider value={{
      cart,
      cartCount,
      loading,
      fetchCart,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart
    }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => {
  const context = useContext(CartContext)
  if (!context) {
    throw new Error('useCart must be used within CartProvider')
  }
  return context
}

export default CartContext
