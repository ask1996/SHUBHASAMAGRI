# ShubhaSamagri AI Chatbot - Documentation

## Overview

"Samagri Sakhi" is an AI-powered pooja item recommendation assistant using:
- **RAG (Retrieval Augmented Generation)** — answers grounded in a real knowledge base
- **Groq API (free)** — Llama 3.3 70B model, 14,400 requests/day free
- **ChromaDB** — local vector database for semantic search
- **Multilingual embeddings** — supports Hindi, Telugu, Tamil, Sanskrit, English

```
User Query
    │
    ▼
Extract Context (occasion, region, community, budget)
    │
    ▼
Semantic Search → ChromaDB (cosine similarity)
    │
    ▼
Retrieved Docs (occasion knowledge + regional customs + community rules)
    │
    ▼
LLM Prompt = System Prompt + Retrieved Context + Conversation History + User Query
    │
    ▼
Groq API (Llama 3.3 70B) → Response
    │
    ▼
React Chat Widget → User
```

## Knowledge Base Contents

| File | Contents |
|------|----------|
| `data/occasions.json` | 5 occasions with 15-20 items each + regional variations |
| `data/regional_customs.json` | 7 regions (South India subdivided, North, West, East) |
| `data/community_customs.json` | 6 community types + 4 budget tiers |

**Total knowledge documents embedded: ~40-50 chunks**

## Setup (2 minutes)

### Step 1: Get Free Groq API Key
1. Go to [console.groq.com](https://console.groq.com)
2. Sign up with Google (free, no credit card)
3. Go to **API Keys** → Create new key
4. Copy the key

### Step 2: Setup Python Service
```bash
cd chatbot
./setup.sh          # Creates venv, installs deps, creates .env
nano .env           # Add your GROQ_API_KEY
./start.sh          # Starts on http://localhost:8001
```

### Step 3: Start Everything
```bash
# Terminal 1: Backend
cd backend && mvn spring-boot:run

# Terminal 2: Chatbot
cd chatbot && ./start.sh

# Terminal 3: Frontend
cd frontend && npm run dev
```

The chatbot widget appears as a floating button (🙏 Ask AI) on the bottom-right of the app.

## API Endpoints

### Python Service (port 8001)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /health | Check service health + doc count |
| POST | /chat | Send message, get response |
| DELETE | /chat/{session_id} | Clear conversation |
| POST | /sync-knowledge | Re-sync with Spring Boot API |

### Spring Boot Proxy (port 8080)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/chatbot/chat | Proxies to Python service |
| GET | /api/chatbot/health | Check chatbot availability |

## Chat API Usage

```bash
# New conversation
curl -X POST http://localhost:8001/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "I need items for Telugu marriage"}'

# Continue conversation (use session_id from above response)
curl -X POST http://localhost:8001/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What about Satyanarayana Vratham?", "session_id": "abc123"}'
```

## How "Real-Time Training" Works

This uses **RAG** (not fine-tuning). The knowledge base updates dynamically:

1. **On startup**: Loads static JSON knowledge + fetches live kits from Spring Boot API
2. **On demand**: Call `POST /sync-knowledge` to pull latest occasions/kits
3. **Auto-sync**: Can be scheduled as a cron job

When you add new kits or occasions to the Spring Boot backend, the chatbot learns about them by calling the sync endpoint.

## Recommendation Logic

The chatbot personalizes responses based on:

| Dimension | Examples | Impact |
|-----------|---------|---------|
| Occasion | Marriage, Gruha Pravesh | Core item list |
| Region | Telugu, Tamil, Bengali | Additional regional items |
| Community | Brahmin Vaishnav, Kshatriya | Specific deity items, restrictions |
| Budget | Basic/Standard/Premium/Grand | Quantity and quality adjustments |

## Free Tier Limits

| Service | Free Limit | Notes |
|---------|-----------|-------|
| Groq (Llama 3.3 70B) | 30 req/min, 14,400/day | Use 8B model for higher volume |
| Groq (Llama 3.1 8B) | 30 req/min, 14,400/day | Faster, slightly lower quality |
| ChromaDB | Unlimited | Runs locally |
| Embeddings (sentence-transformers) | Unlimited | Runs locally |

## Adding More Knowledge

Add more data by editing the JSON files in `chatbot/data/`:

```json
// Add to occasions.json
{
  "id": "navratri",
  "name": "Navratri",
  "core_items": [
    {"item": "Ghatasthapana pot", "quantity": "1", "reason": "..."}
  ]
}
```

Then call `POST /sync-knowledge?force_reload=true` to re-embed.

## Future Enhancements

- [ ] Voice input (Web Speech API)
- [ ] Product image thumbnails in chat
- [ ] Direct "Add to Cart" button from chat response
- [ ] WhatsApp chatbot integration (Twilio)
- [ ] Astrological timing recommendations (Panchanga API)
- [ ] Photo-based item identification (multimodal LLM)
