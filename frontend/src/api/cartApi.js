import axiosInstance from './axios'

export const getCart = async () => {
  const response = await axiosInstance.get('/cart')
  return response.data
}

export const addToCart = async (kitId, quantity = 1) => {
  const response = await axiosInstance.post('/cart/add', { kitId, quantity })
  return response.data
}

export const updateCartItem = async (cartItemId, quantity) => {
  const response = await axiosInstance.put(`/cart/${cartItemId}?quantity=${quantity}`)
  return response.data
}

export const removeFromCart = async (cartItemId) => {
  const response = await axiosInstance.delete(`/cart/${cartItemId}`)
  return response.data
}

export const clearCart = async () => {
  const response = await axiosInstance.delete('/cart/clear')
  return response.data
}
