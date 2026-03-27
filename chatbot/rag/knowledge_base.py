"""
Knowledge Base Loader for ShubhaSamagri Chatbot.
Loads JSON data files and optionally syncs from Spring Boot API.
"""
import json
import os
import logging
import httpx
from pathlib import Path
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

DATA_DIR = Path(__file__).parent.parent / "data"


def load_json_file(filename: str) -> Dict:
    """Load a JSON file from the data directory."""
    filepath = DATA_DIR / filename
    if not filepath.exists():
        logger.warning(f"Data file not found: {filepath}")
        return {}
    with open(filepath, "r", encoding="utf-8") as f:
        return json.load(f)


def build_documents() -> List[Dict[str, Any]]:
    """
    Build a flat list of documents for embedding into ChromaDB.
    Each document = one chunk of knowledge with metadata for filtering.
    """
    documents = []

    # ── 1. Occasions ────────────────────────────────────────────────
    occasions_data = load_json_file("occasions.json")
    for occasion in occasions_data.get("occasions", []):
        # Main occasion document
        items_text = "\n".join([
            f"- {item['item']} ({item['quantity']}): {item['reason']}"
            for item in occasion.get("core_items", [])
        ])
        doc_text = f"""
Occasion: {occasion['name']} ({occasion.get('telugu_name', '')} / {occasion.get('hindi_name', '')})
Description: {occasion['description']}
Duration: {occasion.get('duration_days', 'one day')}

Required Pooja Items:
{items_text}
""".strip()

        documents.append({
            "id": f"occasion_{occasion['id']}",
            "text": doc_text,
            "metadata": {
                "type": "occasion",
                "occasion_id": occasion["id"],
                "occasion_name": occasion["name"],
            }
        })

        # Regional variation documents
        for region_id, region_data in occasion.get("regional_variations", {}).items():
            add_items = region_data.get("additional_items", [])
            rituals = region_data.get("unique_rituals", [])
            if add_items or rituals:
                regional_text = f"""
{occasion['name']} - {region_data.get('name', region_id)} Specific Items:
Additional required items: {', '.join(add_items)}
Unique rituals: {', '.join(rituals)}
""".strip()
                documents.append({
                    "id": f"occasion_{occasion['id']}_region_{region_id}",
                    "text": regional_text,
                    "metadata": {
                        "type": "regional_occasion",
                        "occasion_id": occasion["id"],
                        "region_id": region_id,
                    }
                })

    # ── 2. Regional Customs ─────────────────────────────────────────
    regional_data = load_json_file("regional_customs.json")
    for region in regional_data.get("regions", []):
        items_text = "\n".join([
            f"- {item['item']}: {item['description']}"
            for item in region.get("unique_items", [])
        ])
        traditions = "\n".join([f"- {t}" for t in region.get("traditions", [])])

        doc_text = f"""
Region: {region['name']}
Languages: {', '.join(region.get('languages', []))}
Key characteristics: {region.get('key_characteristics', '')}

Unique Pooja Items for this region:
{items_text}

Regional Traditions:
{traditions}
""".strip()

        documents.append({
            "id": f"region_{region['id']}",
            "text": doc_text,
            "metadata": {
                "type": "region",
                "region_id": region["id"],
                "region_name": region["name"],
            }
        })

    # ── 3. Community Customs ────────────────────────────────────────
    community_data = load_json_file("community_customs.json")
    for community in community_data.get("communities", []):
        items_text = "\n".join([
            f"- {item['item']}: {item['reason']}"
            for item in community.get("unique_items", [])
        ])
        restrictions = ", ".join(community.get("restrictions", ["None"]))

        doc_text = f"""
Community: {community['name']}
Primary Deity: {community.get('deity', 'Various')}
Characteristics: {community.get('characteristics', '')}
Dietary/Ritual Restrictions: {restrictions}

Community-specific Pooja Items:
{items_text}
""".strip()

        documents.append({
            "id": f"community_{community['id']}",
            "text": doc_text,
            "metadata": {
                "type": "community",
                "community_id": community["id"],
                "community_name": community["name"],
            }
        })

    # ── 4. Budget Tiers ─────────────────────────────────────────────
    for tier in community_data.get("budget_tiers", []):
        doc_text = f"""
Budget Tier: {tier['tier'].title()} (₹{tier['range_inr']})
Description: {tier['description']}
Included items: {', '.join(tier['includes'])}
""".strip()
        documents.append({
            "id": f"budget_{tier['tier']}",
            "text": doc_text,
            "metadata": {"type": "budget", "tier": tier["tier"]}
        })

    logger.info(f"Built {len(documents)} knowledge documents for embedding")
    return documents


async def sync_from_api(api_url: str) -> List[Dict[str, Any]]:
    """
    Fetch live occasions and kits from Spring Boot API and convert to documents.
    Call this periodically to keep the chatbot up-to-date with the product catalog.
    """
    live_docs = []
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            # Fetch occasions
            resp = await client.get(f"{api_url}/occasions")
            if resp.status_code == 200:
                occasions = resp.json().get("data", [])
                for occ in occasions:
                    # Fetch kits for this occasion
                    kit_resp = await client.get(f"{api_url}/kits/occasion/{occ['id']}")
                    kits = kit_resp.json().get("data", []) if kit_resp.status_code == 200 else []

                    kit_texts = []
                    for kit in kits:
                        items = kit.get("kitItems", [])
                        items_str = ", ".join([
                            f"{ki['poojaItem']['name']} x{ki['quantity']}"
                            for ki in items[:10]  # limit to 10 items in text
                        ])
                        kit_texts.append(
                            f"Kit: {kit['name']} | Price: ₹{kit['price']} | "
                            f"Delivery: {kit['estimatedDeliveryDays']} days | "
                            f"Includes: {items_str}"
                        )

                    if kit_texts:
                        doc = {
                            "id": f"live_occasion_{occ['id']}",
                            "text": f"Available kits for {occ['name']}:\n" + "\n".join(kit_texts),
                            "metadata": {
                                "type": "live_product",
                                "occasion_id": str(occ["id"]),
                                "occasion_name": occ["name"]
                            }
                        }
                        live_docs.append(doc)

        logger.info(f"Synced {len(live_docs)} live product documents from Spring Boot API")
    except Exception as e:
        logger.warning(f"Failed to sync from API (is backend running?): {e}")

    return live_docs
