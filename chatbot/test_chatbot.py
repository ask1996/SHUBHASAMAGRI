"""
Quick test script to verify the chatbot works end-to-end.
Run: python test_chatbot.py
"""
import asyncio
import httpx
import json

BASE_URL = "http://localhost:8001"

async def test():
    async with httpx.AsyncClient(timeout=30.0) as client:
        # Health check
        print("1. Checking health...")
        r = await client.get(f"{BASE_URL}/health")
        print(json.dumps(r.json(), indent=2))

        # Test chat
        print("\n2. Sending test message...")
        r = await client.post(f"{BASE_URL}/chat", json={
            "message": "I need items for a Telugu marriage ceremony, I'm from Andhra Pradesh, Brahmin community, budget around 3000 rupees"
        })
        data = r.json()
        print(f"Session ID: {data['session_id']}")
        print(f"Context: {data['user_context']}")
        print(f"\nResponse:\n{data['response']}")

        # Follow-up
        print("\n3. Follow-up question...")
        r = await client.post(f"{BASE_URL}/chat", json={
            "message": "What about for Gruha Pravesh?",
            "session_id": data["session_id"]
        })
        print(r.json()["response"])

asyncio.run(test())
