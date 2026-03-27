#!/bin/bash
# Start the chatbot service
cd "$(dirname "$0")"

if [ ! -d "venv" ]; then
  echo "First run setup.sh to install dependencies"
  exit 1
fi

source venv/bin/activate
echo "Starting ShubhaSamagri Chatbot on port 8001..."
uvicorn main:app --host 0.0.0.0 --port 8001 --reload
