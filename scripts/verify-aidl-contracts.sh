#!/usr/bin/env bash
set -euo pipefail

UI_DIR="UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract"
BACKEND_DIR="backend/features/server/data/src/main/aidl/com/unitynews/contract"

if [[ ! -d "$UI_DIR" ]]; then
  echo "Missing UI AIDL contract directory: $UI_DIR" >&2
  exit 1
fi

if [[ ! -d "$BACKEND_DIR" ]]; then
  echo "Missing backend AIDL contract directory: $BACKEND_DIR" >&2
  exit 1
fi

diff -ru "$UI_DIR" "$BACKEND_DIR"
echo "AIDL contracts match."
