#!/usr/bin/env bash
set -euo pipefail

QUERY="${1:-}"

echo "## Project files"
fd -e java -e gradle -e properties -e toml -e json . src build.gradle gradle.properties 2>/dev/null | sed 's#^\./##' | head -300

echo
echo "## Matches in project"
rg -n --glob '*.java' --glob '*.gradle' --glob '*.properties' --glob '*.toml' "$QUERY" . || true

echo
echo "## Git status"
git status --short