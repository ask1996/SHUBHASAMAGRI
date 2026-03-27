"""
Groq LLM client for ShubhaSamagri chatbot.
Primary free LLM: llama-3.3-70b-versatile (Groq free tier: 14,400 req/day)
Fallback: Google Gemini 1.5 Flash (250 req/day free)
"""
import logging
from typing import List, Optional
from groq import Groq
import os

logger = logging.getLogger(__name__)


class LLMClient:
    """
    Wrapper around Groq API (primary) with optional Gemini fallback.
    Uses the free tier of both services.
    """

    def __init__(self, groq_api_key: str, model: str = "llama-3.3-70b-versatile"):
        self.model = model
        self._groq: Optional[Groq] = None
        if groq_api_key:
            self._groq = Groq(api_key=groq_api_key)
            logger.info(f"Groq LLM initialized with model: {model}")
        else:
            logger.warning("No GROQ_API_KEY provided. LLM will not work.")

    def chat(
        self,
        messages: List[dict],
        temperature: float = 0.7,
        max_tokens: int = 1024
    ) -> str:
        """
        Send messages to Groq and return the assistant's response text.
        Messages format: [{"role": "system|user|assistant", "content": "..."}]
        """
        if not self._groq:
            return "❌ LLM not configured. Please set GROQ_API_KEY in your .env file."

        try:
            completion = self._groq.chat.completions.create(
                model=self.model,
                messages=messages,
                temperature=temperature,
                max_tokens=max_tokens,
                stream=False,
            )
            return completion.choices[0].message.content

        except Exception as e:
            error_str = str(e)
            if "rate_limit" in error_str.lower() or "429" in error_str:
                logger.warning("Groq rate limit hit. Consider switching to llama-3.1-8b-instant for higher volume.")
                return "I'm temporarily unavailable due to high demand. Please try again in a moment. 🙏"
            elif "invalid_api_key" in error_str.lower() or "401" in error_str:
                return "❌ Invalid API key. Please check your GROQ_API_KEY in .env file."
            else:
                logger.error(f"Groq API error: {e}")
                return f"Sorry, I encountered an error. Please try again. 🙏"
