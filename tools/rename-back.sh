#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
IN=$(ls build/libs/agartha-*.jar | grep -v sources | grep -v orig-names | head -1)
mkdir -p dist
java -jar tools/tiny-remapper.jar "$IN" dist/agartha-orig-names.jar tools/rename-back.tiny named orig
echo "wrote dist/agartha-orig-names.jar"
