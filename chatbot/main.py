"""
ShubhaSamagri Chatbot - FastAPI Service
Provides RAG-based pooja item recommendations using Groq LLM.

Run: uvicorn main:app --host 0.0.0.0 --port 8001 --reload
"""
import logging
import asyncio
from contextlib import asynccontextmanager
from typing import List, Dict, Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uuid

from config import settings
from rag.knowledge_base import build_documents, sync_from_api
from rag.vector_store import ShubhaSamagriVectorStore
from llm.groq_client import LLMClient
from chat.chatbot import ShubhaSamagriChatbot, UserContext

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(name)s | %(message)s"
)
logger = logging.getLogger(__name__)

# ── Global state ────────────────────────────────────────────────────
vector_store = ShubhaSamagriVectorStore(
    persist_dir=settings.chroma_persist_dir,
    model_name=settings.embedding_model
)
llm_client: Optional[LLMClient] = None
chatbot: Optional[ShubhaSamagriChatbot] = None

# In-memory session storage (use Redis in production)
# session_id → { "history": [...], "context": UserContext }
sessions: Dict[str, dict] = {}


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Initialize all components on startup."""
    global llm_client, chatbot

    logger.info("🚀 Starting ShubhaSamagri Chatbot Service...")

    # Initialize vector store and embedding model
    vector_store.initialize()

    # Load static knowledge base
    documents = build_documents()

    # Try to sync live data from Spring Boot API
    live_docs = await sync_from_api(settings.spring_boot_api_url)
    documents.extend(live_docs)

    # Embed all documents into ChromaDB
    vector_store.load_documents(documents)
    logger.info(f"Knowledge base ready: {vector_store.document_count} documents")

    # Initialize LLM
    llm_client = LLMClient(
        groq_api_key=settings.groq_api_key or "",
        model=settings.groq_model
    )

    # Initialize chatbot
    chatbot = ShubhaSamagriChatbot(llm_client, vector_store)
    logger.info("✅ Chatbot ready!")

    yield  # App runs

    logger.info("Shutting down chatbot service...")


app = FastAPI(
    title="ShubhaSamagri Chatbot API",
    description="AI-powered pooja item recommendation chatbot using RAG + Groq LLM",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:5173", "*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Request/Response Models ─────────────────────────────────────────
class ChatRequest(BaseModel):
    message: str
    session_id: Optional[str] = None  # None = new session


class ChatResponse(BaseModel):
    response: str
    session_id: str
    user_context: dict  # what we know about the user so far


class SyncRequest(BaseModel):
    force_reload: bool = False


# ── Endpoints ───────────────────────────────────────────────────────
@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "knowledge_documents": vector_store.document_count,
        "llm_model": settings.groq_model,
        "embedding_model": settings.embedding_model
    }


@app.post("/chat", response_model=ChatResponse)
def chat(request: ChatRequest):
    """
    Main chat endpoint. Maintains multi-turn conversation via session_id.
    Each session has its own conversation history and user context.
    """
    if not chatbot:
        raise HTTPException(status_code=503, detail="Chatbot not initialized yet. Please wait.")

    # Get or create session
    session_id = request.session_id or str(uuid.uuid4())
    if session_id not in sessions:
        sessions[session_id] = {
            "history": [],
            "context": UserContext()
        }

    session = sessions[session_id]
    history: List[dict] = session["history"]
    user_context: UserContext = session["context"]

    # Validate message
    message = request.message.strip()
    if not message:
        return ChatResponse(
            response="Please type a message 🙏",
            session_id=session_id,
            user_context=vars(user_context)
        )

    # Generate response
    response_text, updated_context = chatbot.get_response(message, history, user_context)

    # Update session history (keep last 20 messages)
    history.append({"role": "user", "content": message})
    history.append({"role": "assistant", "content": response_text})
    if len(history) > 20:
        history = history[-20:]

    sessions[session_id]["history"] = history
    sessions[session_id]["context"] = updated_context

    return ChatResponse(
        response=response_text,
        session_id=session_id,
        user_context={
            "occasion": updated_context.occasion,
            "region": updated_context.region,
            "community": updated_context.community,
            "budget": updated_context.budget,
        }
    )


@app.delete("/chat/{session_id}")
def clear_session(session_id: str):
    """Clear a conversation session."""
    if session_id in sessions:
        del sessions[session_id]
    return {"message": "Session cleared"}


@app.post("/sync-knowledge")
async def sync_knowledge(request: SyncRequest):
    """
    Sync knowledge base with latest data from Spring Boot API.
    Call this when new occasions or kits are added.
    """
    documents = build_documents()
    live_docs = await sync_from_api(settings.spring_boot_api_url)
    documents.extend(live_docs)
    vector_store.load_documents(documents, force_reload=request.force_reload)
    return {
        "message": f"Knowledge base updated with {len(documents)} documents",
        "total_documents": vector_store.document_count
    }


@app.get("/sessions")
def list_sessions():
    """Debug endpoint: list active sessions (remove in production)."""
    return {
        "active_sessions": len(sessions),
        "session_ids": list(sessions.keys())
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=settings.port, reload=True)
