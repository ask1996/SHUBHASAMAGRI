import axiosInstance from './axios'

export const getOccasions = async () => {
  const response = await axiosInstance.get('/occasions')
  return response.data
}

export const getOccasionById = async (id) => {
  const response = await axiosInstance.get(`/occasions/${id}`)
  return response.data
}

export const createOccasion = async (occasionData) => {
  const response = await axiosInstance.post('/occasions', occasionData)
  return response.data
}

export const updateOccasion = async (id, occasionData) => {
  const response = await axiosInstance.put(`/occasions/${id}`, occasionData)
  return response.data
}

export const deleteOccasion = async (id) => {
  const response = await axiosInstance.delete(`/occasions/${id}`)
  return response.data
}
