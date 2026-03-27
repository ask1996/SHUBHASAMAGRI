import axiosInstance from './axios'

export const sendMessage = async (message, sessionId = null) => {
  const response = await axiosInstance.post('/chatbot/chat', {
    message,
    session_id: sessionId
  })
  return response.data
}

export const clearChatSession = async (sessionId) => {
  const response = await axiosInstance.delete(`/chatbot/chat/${sessionId}`)
  return response.data
}

export const checkChatbotHealth = async () => {
  const response = await axiosInstance.get('/chatbot/health')
  return response.data
}
