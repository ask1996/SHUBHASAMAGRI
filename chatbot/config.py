from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # LLM
    groq_api_key: Optional[str] = None
    google_api_key: Optional[str] = None
    llm_provider: str = "groq"
    groq_model: str = "llama-3.3-70b-versatile"
    gemini_model: str = "gemini-1.5-flash-latest"

    # Embeddings
    embedding_model: str = "paraphrase-multilingual-MiniLM-L12-v2"

    # Vector store
    chroma_persist_dir: str = "./chroma_db"

    # Spring Boot API
    spring_boot_api_url: str = "http://localhost:8080/api"

    # Server
    port: int = 8001

    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()
