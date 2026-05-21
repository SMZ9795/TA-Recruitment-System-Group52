#!/usr/bin/env bash
# Compile production sources (src/) into build/classes.
# test.sh handles compiling tests on top.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

CLASSES_DIR="build/classes"
mkdir -p "$CLASSES_DIR"

echo "==> Compiling production sources (src/) ..."
find src -name "*.java" -print0 \
  | xargs -0 javac -encoding UTF-8 -d "$CLASSES_DIR"

echo "==> Build OK -> $CLASSES_DIR"
