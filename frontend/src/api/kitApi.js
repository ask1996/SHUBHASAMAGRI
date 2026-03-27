import axiosInstance from './axios'

export const getKits = async () => {
  const response = await axiosInstance.get('/kits')
  return response.data
}

export const getKitById = async (id) => {
  const response = await axiosInstance.get(`/kits/${id}`)
  return response.data
}

export const getKitsByOccasion = async (occasionId) => {
  const response = await axiosInstance.get(`/kits/occasion/${occasionId}`)
  return response.data
}

export const createKit = async (kitData) => {
  const response = await axiosInstance.post('/kits', kitData)
  return response.data
}
