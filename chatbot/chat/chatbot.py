"""
Core chatbot logic for ShubhaSamagri.
Implements a multi-turn conversation with context memory,
RAG-based knowledge retrieval, and structured recommendation generation.
"""
import logging
from typing import List, Dict, Optional
from dataclasses import dataclass, field

logger = logging.getLogger(__name__)


SYSTEM_PROMPT = """You are ShubhaSamagri's AI assistant — a knowledgeable, warm, and helpful guide for Hindu pooja rituals and ceremonies. Your name is "Samagri Sakhi" (Sacred Companion).

Your expertise:
- Hindu pooja rituals for all major occasions (Marriage, Gruha Pravesh, Satyanarayana Vratham, Naming Ceremony, Upanayanam, etc.)
- Regional variations: South India (Telugu, Tamil, Kannada, Malayalam), North India, West India (Gujarati, Marathi), East India (Bengali, Odia)
- Community/caste-specific customs (Brahmin Vaishnav/Shaivite, Kshatriya, Vaishya, OBC, SC/ST, etc.)
- Budget-based recommendations (Basic ₹300-700 / Standard ₹700-2000 / Premium ₹2000-5000 / Grand ₹5000+)
- Significance and spiritual meaning behind each ritual item

How you respond:
1. Be warm, respectful, and spiritually sensitive
2. When recommending items, always explain WHY each item is needed (spiritual significance)
3. Group recommendations by: Core Items → Regional Items → Community-specific Items
4. Mention budget-appropriate alternatives when relevant
5. Use Indian terms naturally (e.g., "Haldi" instead of just "turmeric", "Prasad" instead of "food offering")
6. Add the ₹ symbol for all prices
7. When you don't know something specific, say so honestly and suggest consulting a local pandit

Context from knowledge base:
{context}

Important: Only recommend items that are relevant to the user's specific combination of occasion + region + community + budget. Avoid generic lists.
"""

CONVERSATION_STARTERS = {
    "greeting": """Namaste! 🙏 I'm **Samagri Sakhi**, your ShubhaSamagri guide.

I can help you find the perfect pooja items for any Hindu occasion, customized for your region and community traditions.

To give you the best recommendations, could you tell me:
1. **What's the occasion?** (Marriage / Gruha Pravesh / Satyanarayana Vratham / Naming Ceremony / Upanayanam / Other)
2. **Which region are you from?** (Andhra/Telangana / Tamil Nadu / Karnataka / Kerala / North India / Maharashtra/Gujarat / Bengal)
3. **Your community?** (Brahmin / Kshatriya / Vaishya / OBC / Other) — *optional but helps personalize*
4. **Budget range?** (Basic ₹300-700 / Standard ₹700-2000 / Premium ₹2000-5000 / Grand ₹5000+)

You can also just describe what you need and I'll guide you! 🌸"""
}


@dataclass
class UserContext:
    """Tracks what we know about the user across the conversation."""
    occasion: Optional[str] = None
    region: Optional[str] = None
    community: Optional[str] = None
    budget: Optional[str] = None
    guest_count: Optional[int] = None
    additional_notes: List[str] = field(default_factory=list)

    def is_complete(self) -> bool:
        """Check if we have enough context to make a recommendation."""
        return bool(self.occasion and self.region)

    def summary(self) -> str:
        parts = []
        if self.occasion:
            parts.append(f"Occasion: {self.occasion}")
        if self.region:
            parts.append(f"Region: {self.region}")
        if self.community:
            parts.append(f"Community: {self.community}")
        if self.budget:
            parts.append(f"Budget: {self.budget}")
        if self.guest_count:
            parts.append(f"Guests: {self.guest_count}")
        return " | ".join(parts) if parts else "Not specified yet"


def extract_user_context(message: str, current_context: UserContext) -> UserContext:
    """
    Extract occasion, region, community, budget from user message.
    Updates the current context with any new info found.
    """
    msg_lower = message.lower()

    # Detect occasion
    occasion_map = {
        "marriage": ["marriage", "wedding", "pelli", "shaadi", "vivah", "kalyanam", "biye"],
        "gruha_pravesh": ["gruha pravesh", "griha pravesh", "housewarming", "new home", "new house", "house warming"],
        "satyanarayana": ["satyanarayana", "satyanarayan", "satya narayana", "vratham", "vrat"],
        "naming_ceremony": ["naming", "namakarana", "namakaran", "baby name", "11th day"],
        "upanayanam": ["upanayanam", "upanayana", "munja", "janeu", "sacred thread", "thread ceremony"],
    }
    for occasion_key, keywords in occasion_map.items():
        if any(kw in msg_lower for kw in keywords):
            current_context.occasion = occasion_key
            break

    # Detect region
    region_map = {
        "andhra_telangana": ["andhra", "telangana", "telugu", "hyderabad", "vijayawada", "vizag"],
        "tamil_nadu": ["tamil", "tamil nadu", "chennai", "coimbatore", "madurai"],
        "karnataka": ["karnataka", "kannada", "bangalore", "bengaluru", "mysore"],
        "kerala": ["kerala", "malayalam", "trivandrum", "kochi", "calicut"],
        "north_india": ["north india", "hindi", "up", "bihar", "delhi", "punjab", "haryana", "rajasthan", "mp"],
        "west_india": ["maharashtra", "gujarat", "marathi", "gujarati", "mumbai", "pune", "ahmedabad"],
        "east_india": ["bengal", "bengali", "odisha", "odia", "kolkata", "bhubaneswar"],
    }
    for region_key, keywords in region_map.items():
        if any(kw in msg_lower for kw in keywords):
            current_context.region = region_key
            break

    # Detect community
    community_map = {
        "brahmin_vaishnav": ["brahmin vaishnav", "vaishnavite", "iyengar", "madhva", "srivaishnava"],
        "brahmin_shaivite": ["brahmin shaivite", "shaivite", "shaiva", "iyer", "smartha brahmin"],
        "brahmin_smarta": ["smarta", "smartha", "niyogi", "brahmin"],
        "kshatriya": ["kshatriya", "rajput", "kamma", "kapu", "reddy", "naidu"],
        "vaishya_komati": ["vaishya", "komati", "baniya", "chettiars", "chettiar", "marwari"],
        "obc_bc": ["obc", "backward class", "yadav", "kuruma", "vishwakarma", "nayee"],
    }
    for community_key, keywords in community_map.items():
        if any(kw in msg_lower for kw in keywords):
            current_context.community = community_key
            break

    # Detect budget
    budget_map = {
        "basic": ["basic", "simple", "budget", "cheap", "affordable", "300", "500", "700"],
        "standard": ["standard", "normal", "regular", "medium", "1000", "1500", "2000"],
        "premium": ["premium", "good quality", "3000", "4000", "5000"],
        "grand": ["grand", "elaborate", "luxury", "lavish", "big", "large"],
    }
    for budget_key, keywords in budget_map.items():
        if any(kw in msg_lower for kw in keywords):
            current_context.budget = budget_key
            break

    return current_context


class ShubhaSamagriChatbot:
    """
    Main chatbot class that manages conversation state and generates responses.
    Uses RAG (vector store) for context and Groq LLM for generation.
    """

    def __init__(self, llm_client, vector_store):
        self.llm = llm_client
        self.vector_store = vector_store

    def get_response(
        self,
        user_message: str,
        conversation_history: List[Dict[str, str]],
        user_context: UserContext
    ) -> tuple[str, UserContext]:
        """
        Generate a response to the user's message.
        Returns (response_text, updated_user_context).
        """
        # Handle greetings
        if not conversation_history and user_message.strip().lower() in [
            "hi", "hello", "namaste", "helo", "hey", "start", ""
        ]:
            return CONVERSATION_STARTERS["greeting"], user_context

        # Extract context from user message
        user_context = extract_user_context(user_message, user_context)

        # Retrieve relevant knowledge from vector store
        context = self.vector_store.search_for_recommendation(
            occasion=user_context.occasion,
            region=user_context.region,
            community=user_context.community,
            budget=user_context.budget,
            query_text=user_message
        )

        # Build the system prompt with retrieved context
        system_content = SYSTEM_PROMPT.format(context=context)

        # Build message history for the LLM
        # Keep last 8 messages for context window efficiency
        recent_history = conversation_history[-8:] if len(conversation_history) > 8 else conversation_history

        messages = [{"role": "system", "content": system_content}]

        # Add context summary if we have user context
        if user_context.is_complete():
            messages.append({
                "role": "system",
                "content": f"Current user context: {user_context.summary()}"
            })

        messages.extend(recent_history)
        messages.append({"role": "user", "content": user_message})

        # Generate response
        response = self.llm.chat(messages, temperature=0.7, max_tokens=1200)

        return response, user_context
