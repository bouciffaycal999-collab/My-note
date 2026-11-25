#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

PORT="${PORT:-8173}"
URL="http://localhost:${PORT}"
echo "CERAMICA POS PC -> ${URL}"

if command -v open >/dev/null 2>&1; then
  (sleep 1 && open "${URL}") &
elif command -v xdg-open >/dev/null 2>&1; then
  (sleep 1 && xdg-open "${URL}") &
fi

if command -v python3 >/dev/null 2>&1; then
  exec python3 -m http.server "${PORT}"
elif command -v python >/dev/null 2>&1; then
  exec python -m http.server "${PORT}"
elif command -v npx >/dev/null 2>&1; then
  exec npx --yes serve -l "${PORT}" .
else
  echo "Erreur: installez Python 3 ou Node.js pour lancer le serveur local."
  exit 1
fi
