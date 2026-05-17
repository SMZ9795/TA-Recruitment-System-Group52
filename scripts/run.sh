#!/usr/bin/env bash
# Build (if needed) then launch the Swing GUI.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -d "build/classes/com/group52/tarecruitment" ]; then
  bash scripts/build.sh
fi

echo "==> Launching Swing GUI (SwingMain) ..."
java -cp build/classes com.group52.tarecruitment.SwingMain
