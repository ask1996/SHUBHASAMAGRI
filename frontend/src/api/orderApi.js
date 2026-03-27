import axiosInstance from './axios'

export const placeOrder = async (orderData) => {
  const response = await axiosInstance.post('/orders', orderData)
  return response.data
}

export const getOrders = async () => {
  const response = await axiosInstance.get('/orders')
  return response.data
}

export const getOrderById = async (orderId) => {
  const response = await axiosInstance.get(`/orders/${orderId}`)
  return response.data
}

export const cancelOrder = async (orderId) => {
  const response = await axiosInstance.put(`/orders/${orderId}/cancel`)
  return response.data
}
