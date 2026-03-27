import axiosInstance from './axios'

export const loginUser = async (credentials) => {
  const response = await axiosInstance.post('/auth/login', credentials)
  return response.data
}

export const signupUser = async (userData) => {
  const response = await axiosInstance.post('/auth/signup', userData)
  return response.data
}
