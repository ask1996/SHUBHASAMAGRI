#!/bin/bash
# ============================================
# ShubhaSamagri Chatbot - One-time Setup
# ============================================
echo "Setting up ShubhaSamagri Chatbot..."

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install --upgrade pip
pip install -r requirements.txt

# Create .env from template
if [ ! -f .env ]; then
  cp .env.example .env
  echo ""
  echo "⚠️  Please edit .env and add your GROQ_API_KEY"
  echo "   Get free key at: https://console.groq.com"
  echo ""
fi

echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "  1. Edit .env and set GROQ_API_KEY"
echo "  2. Run: source venv/bin/activate && uvicorn main:app --port 8001 --reload"
