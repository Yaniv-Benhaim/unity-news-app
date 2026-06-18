#!/usr/bin/env bash
set -euo pipefail

UI_AIDL_DIR="UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract"
BACKEND_AIDL_DIR="backend/features/server/data/src/main/aidl/com/unitynews/contract"
UI_DTO_DIR="UnityNewsApp/features/news/data/src/main/java/com/unitynews/contract"
BACKEND_DTO_DIR="backend/features/server/data/src/main/java/com/unitynews/contract"

if [[ ! -d "$UI_AIDL_DIR" ]]; then
  echo "Missing UI AIDL contract directory: $UI_AIDL_DIR" >&2
  exit 1
fi

if [[ ! -d "$BACKEND_AIDL_DIR" ]]; then
  echo "Missing backend AIDL contract directory: $BACKEND_AIDL_DIR" >&2
  exit 1
fi

if [[ ! -d "$UI_DTO_DIR" ]]; then
  echo "Missing UI DTO contract directory: $UI_DTO_DIR" >&2
  exit 1
fi

if [[ ! -d "$BACKEND_DTO_DIR" ]]; then
  echo "Missing backend DTO contract directory: $BACKEND_DTO_DIR" >&2
  exit 1
fi

diff -ru "$UI_AIDL_DIR" "$BACKEND_AIDL_DIR"
diff -ru "$UI_DTO_DIR" "$BACKEND_DTO_DIR"
echo "AIDL and DTO contracts match."
