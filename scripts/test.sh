#!/usr/bin/env bash
# Compile src + tests then run the 42-test integration suite.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

bash scripts/build.sh

TEST_CLASSES_DIR="build/test-classes"
mkdir -p "$TEST_CLASSES_DIR"

echo "==> Compiling tests (tests/) ..."
find tests -name "*.java" -print0 \
  | xargs -0 javac -encoding UTF-8 \
      -cp build/classes \
      -d "$TEST_CLASSES_DIR"

echo "==> Running RecruitmentSystemTestRunner ..."
java -cp "build/classes:$TEST_CLASSES_DIR" \
  com.group52.tarecruitment.tests.RecruitmentSystemTestRunner
