import { useState, useRef, useEffect } from 'react'
import { sendMessage, clearChatSession } from '../api/chatbotApi'

// Renders markdown-style bold (**text**) and bullet points in bot responses
function MessageText({ text }) {
  const lines = text.split('\n')
  return (
    <div className="msg-text">
      {lines.map((line, i) => {
        // Convert **bold** markers
        const parts = line.split(/\*\*(.*?)\*\*/g)
        const rendered = parts.map((part, j) =>
          j % 2 === 1 ? <strong key={j}>{part}</strong> : part
        )
        if (line.startsWith('- ') || line.startsWith('• ')) {
          return <div key={i} className="msg-bullet">• {rendered.slice(1)}</div>
        }
        if (line.match(/^\d+\./)) {
          return <div key={i} className="msg-numbered">{rendered}</div>
        }
        if (line.trim() === '') return <div key={i} className="msg-spacer" />
        return <div key={i}>{rendered}</div>
      })}
    </div>
  )
}

const QUICK_REPLIES = [
  { label: '💍 Marriage Kit', message: 'I need items for a Telugu marriage ceremony' },
  { label: '🏠 Gruha Pravesh', message: 'What do I need for Gruha Pravesh?' },
  { label: '🙏 Satyanarayana Pooja', message: 'Help me with Satyanarayana Vratham items' },
  { label: '👶 Naming Ceremony', message: "Baby's naming ceremony items please" },
  { label: '🧵 Upanayanam', message: 'Sacred thread ceremony requirements' },
]

export default function FloatingChatbot() {
  const [isOpen, setIsOpen] = useState(false)
  const [messages, setMessages] = useState([])
  const [inputText, setInputText] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [sessionId, setSessionId] = useState(null)
  const [userContext, setUserContext] = useState({})
  const [showQuickReplies, setShowQuickReplies] = useState(true)
  const messagesEndRef = useRef(null)
  const inputRef = useRef(null)

  // Scroll to bottom whenever messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  // Focus input when chat opens
  useEffect(() => {
    if (isOpen && inputRef.current) {
      setTimeout(() => inputRef.current?.focus(), 100)
    }
  }, [isOpen])

  // Send initial greeting when first opened
  useEffect(() => {
    if (isOpen && messages.length === 0) {
      sendGreeting()
    }
  }, [isOpen])

  const sendGreeting = async () => {
    setIsLoading(true)
    try {
      const data = await sendMessage('namaste', null)
      setSessionId(data.session_id)
      setMessages([{ role: 'bot', text: data.response, id: Date.now() }])
    } catch {
      setMessages([{
        role: 'bot',
        text: 'Namaste! 🙏 I\'m Samagri Sakhi. Tell me about your occasion and I\'ll recommend the perfect pooja items!',
        id: Date.now()
      }])
    } finally {
      setIsLoading(false)
    }
  }

  const handleSend = async (text) => {
    const message = (text || inputText).trim()
    if (!message || isLoading) return

    setInputText('')
    setShowQuickReplies(false)

    // Add user message immediately
    const userMsg = { role: 'user', text: message, id: Date.now() }
    setMessages(prev => [...prev, userMsg])
    setIsLoading(true)

    try {
      const data = await sendMessage(message, sessionId)
      setSessionId(data.session_id)
      if (data.user_context) setUserContext(data.user_context)

      setMessages(prev => [...prev, {
        role: 'bot',
        text: data.response,
        id: Date.now() + 1
      }])
    } catch (error) {
      setMessages(prev => [...prev, {
        role: 'bot',
        text: '🙏 Sorry, I\'m having trouble connecting. Please make sure the chatbot service is running.',
        id: Date.now() + 1,
        isError: true
      }])
    } finally {
      setIsLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const handleReset = async () => {
    if (sessionId) {
      try { await clearChatSession(sessionId) } catch {}
    }
    setMessages([])
    setSessionId(null)
    setUserContext({})
    setShowQuickReplies(true)
    sendGreeting()
  }

  const contextBadges = Object.entries(userContext)
    .filter(([, v]) => v)
    .map(([k, v]) => ({ key: k, value: v }))

  return (
    <>
      {/* Floating button */}
      <button
        className={`chat-fab ${isOpen ? 'chat-fab-open' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Open AI chatbot"
        title="Ask Samagri Sakhi - AI Pooja Guide"
      >
        {isOpen ? (
          <span className="chat-fab-icon">✕</span>
        ) : (
          <>
            <span className="chat-fab-icon">🙏</span>
            <span className="chat-fab-label">Ask AI</span>
          </>
        )}
      </button>

      {/* Chat window */}
      {isOpen && (
        <div className="chat-window">
          {/* Header */}
          <div className="chat-header">
            <div className="chat-header-info">
              <div className="chat-avatar">ॐ</div>
              <div>
                <div className="chat-title">Samagri Sakhi</div>
                <div className="chat-subtitle">AI Pooja Guide • Powered by Llama 3.3</div>
              </div>
            </div>
            <button className="chat-reset-btn" onClick={handleReset} title="Start new conversation">
              ↺
            </button>
          </div>

          {/* Context badges */}
          {contextBadges.length > 0 && (
            <div className="chat-context-bar">
              {contextBadges.map(({ key, value }) => (
                <span key={key} className="chat-context-badge">
                  {key.replace('_', ' ')}: {value.replace('_', ' ')}
                </span>
              ))}
            </div>
          )}

          {/* Messages */}
          <div className="chat-messages">
            {messages.map((msg) => (
              <div key={msg.id} className={`chat-msg chat-msg-${msg.role}`}>
                {msg.role === 'bot' && <div className="chat-msg-avatar">ॐ</div>}
                <div className={`chat-bubble ${msg.isError ? 'chat-bubble-error' : ''}`}>
                  {msg.role === 'bot'
                    ? <MessageText text={msg.text} />
                    : msg.text
                  }
                </div>
              </div>
            ))}

            {/* Typing indicator */}
            {isLoading && (
              <div className="chat-msg chat-msg-bot">
                <div className="chat-msg-avatar">ॐ</div>
                <div className="chat-bubble chat-typing">
                  <span></span><span></span><span></span>
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>

          {/* Quick replies */}
          {showQuickReplies && messages.length <= 1 && (
            <div className="chat-quick-replies">
              {QUICK_REPLIES.map((qr) => (
                <button
                  key={qr.label}
                  className="chat-quick-btn"
                  onClick={() => handleSend(qr.message)}
                  disabled={isLoading}
                >
                  {qr.label}
                </button>
              ))}
            </div>
          )}

          {/* Input */}
          <div className="chat-input-area">
            <textarea
              ref={inputRef}
              className="chat-input"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask about any occasion, region, or tradition..."
              rows={1}
              disabled={isLoading}
            />
            <button
              className="chat-send-btn"
              onClick={() => handleSend()}
              disabled={!inputText.trim() || isLoading}
              aria-label="Send message"
            >
              ➤
            </button>
          </div>
          <div className="chat-footer-note">
            Free AI • Llama 3.3 via Groq • RAG Knowledge Base
          </div>
        </div>
      )}
    </>
  )
}
