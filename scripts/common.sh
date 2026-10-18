#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_NAME="${APP_NAME:-testApplication}"
if [[ ! "$APP_NAME" =~ ^[a-zA-Z0-9_-]+$ ]]; then
    echo "APP_NAME doit contenir uniquement lettres, chiffres, tirets ou underscores." >&2
    exit 1
fi
cd "$PROJECT_ROOT"
