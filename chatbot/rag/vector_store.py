"""
ChromaDB vector store setup for ShubhaSamagri chatbot.
Manages embedding and retrieval of knowledge base documents.
"""
import logging
from typing import List, Dict, Any, Optional
import chromadb
from chromadb.config import Settings as ChromaSettings
from sentence_transformers import SentenceTransformer

logger = logging.getLogger(__name__)

COLLECTION_NAME = "shubha_samagri_kb"


class ShubhaSamagriVectorStore:
    """
    Manages the ChromaDB vector store for the ShubhaSamagri knowledge base.
    Uses paraphrase-multilingual-MiniLM-L12-v2 for embeddings to support
    Hindi, Telugu, Tamil, Sanskrit, and English mixed text.
    """

    def __init__(self, persist_dir: str = "./chroma_db", model_name: str = "paraphrase-multilingual-MiniLM-L12-v2"):
        self.persist_dir = persist_dir
        self.model_name = model_name
        self._client: Optional[chromadb.PersistentClient] = None
        self._collection = None
        self._embedder: Optional[SentenceTransformer] = None

    def initialize(self):
        """Initialize ChromaDB client and embedding model."""
        logger.info(f"Initializing ChromaDB at: {self.persist_dir}")
        self._client = chromadb.PersistentClient(
            path=self.persist_dir,
            settings=ChromaSettings(anonymized_telemetry=False)
        )

        logger.info(f"Loading embedding model: {self.model_name}")
        self._embedder = SentenceTransformer(self.model_name)

        # Get or create collection
        self._collection = self._client.get_or_create_collection(
            name=COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"}  # cosine similarity for semantic search
        )
        logger.info(f"Vector store ready. Documents in collection: {self._collection.count()}")

    def load_documents(self, documents: List[Dict[str, Any]], force_reload: bool = False):
        """
        Embed and store documents in ChromaDB.
        Skips if documents already exist (unless force_reload=True).
        """
        if self._collection.count() > 0 and not force_reload:
            logger.info(f"Collection already has {self._collection.count()} docs. Skipping reload.")
            return

        if force_reload and self._collection.count() > 0:
            logger.info("Force reload: clearing existing documents")
            self._client.delete_collection(COLLECTION_NAME)
            self._collection = self._client.create_collection(
                name=COLLECTION_NAME,
                metadata={"hnsw:space": "cosine"}
            )

        logger.info(f"Embedding {len(documents)} documents...")
        ids = [doc["id"] for doc in documents]
        texts = [doc["text"] for doc in documents]
        metadatas = [doc.get("metadata", {}) for doc in documents]

        # Generate embeddings in batches to avoid memory issues
        batch_size = 32
        all_embeddings = []
        for i in range(0, len(texts), batch_size):
            batch = texts[i:i + batch_size]
            batch_embeddings = self._embedder.encode(batch, show_progress_bar=False).tolist()
            all_embeddings.extend(batch_embeddings)

        self._collection.add(
            ids=ids,
            documents=texts,
            embeddings=all_embeddings,
            metadatas=metadatas
        )
        logger.info(f"Stored {len(documents)} documents in ChromaDB")

    def add_or_update_documents(self, documents: List[Dict[str, Any]]):
        """
        Add or update documents in the collection (upsert).
        Use this for real-time syncing from Spring Boot API.
        """
        if not documents:
            return

        ids = [doc["id"] for doc in documents]
        texts = [doc["text"] for doc in documents]
        metadatas = [doc.get("metadata", {}) for doc in documents]
        embeddings = self._embedder.encode(texts).tolist()

        self._collection.upsert(
            ids=ids,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas
        )
        logger.info(f"Upserted {len(documents)} documents")

    def search(
        self,
        query: str,
        n_results: int = 5,
        filter_metadata: Optional[Dict] = None
    ) -> List[Dict[str, Any]]:
        """
        Semantic search in the knowledge base.
        Returns top N most relevant documents for the query.
        """
        query_embedding = self._embedder.encode([query])[0].tolist()

        results = self._collection.query(
            query_embeddings=[query_embedding],
            n_results=min(n_results, max(1, self._collection.count())),
            where=filter_metadata,
            include=["documents", "metadatas", "distances"]
        )

        docs = []
        if results["documents"] and results["documents"][0]:
            for doc, meta, dist in zip(
                results["documents"][0],
                results["metadatas"][0],
                results["distances"][0]
            ):
                docs.append({
                    "text": doc,
                    "metadata": meta,
                    "similarity": round(1 - dist, 3)  # cosine distance → similarity
                })

        return docs

    def search_for_recommendation(
        self,
        occasion: Optional[str] = None,
        region: Optional[str] = None,
        community: Optional[str] = None,
        budget: Optional[str] = None,
        query_text: Optional[str] = None
    ) -> str:
        """
        High-level search that retrieves relevant context for a recommendation.
        Returns formatted context string for the LLM.
        """
        context_parts = []

        # Build compound query from user inputs
        query_parts = []
        if occasion:
            query_parts.append(f"pooja items for {occasion}")
        if region:
            query_parts.append(f"{region} regional customs and traditions")
        if community:
            query_parts.append(f"{community} community specific items")
        if budget:
            query_parts.append(f"{budget} budget puja kit")
        if query_text:
            query_parts.append(query_text)

        if not query_parts:
            query_parts = ["pooja essentials ritual items"]

        combined_query = " | ".join(query_parts)
        results = self.search(combined_query, n_results=6)

        for result in results:
            if result["similarity"] > 0.2:  # filter out low-relevance results
                context_parts.append(result["text"])

        return "\n\n---\n\n".join(context_parts) if context_parts else "No specific context found."

    @property
    def document_count(self) -> int:
        return self._collection.count() if self._collection else 0
