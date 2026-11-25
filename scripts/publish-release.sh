#!/usr/bin/env bash
set -euo pipefail

# Fast local release publisher for Credit Auto (~15 seconds end-to-end on Apple Silicon)
# Usage: ./scripts/publish-release.sh v1.4.1 "Release notes here"

TAG="${1:-}"
NOTES="${2:-Release ${TAG}}"
REPO="prasanthgajula08/credit-auto"
TOKEN_FILE="$HOME/.config/credit-auto/github_token"
APK_PATH="app/build/outputs/apk/release/app-release.apk"

if [[ -z "$TAG" ]]; then
  echo "Usage: $0 <tag e.g. v1.4.1> [release notes]"
  exit 1
fi

if [[ ! -f "$TOKEN_FILE" ]]; then
  echo "Error: Missing GitHub token at $TOKEN_FILE"
  echo "Create a fine-grained PAT (Contents: Read & Write on $REPO) and save it with:"
  echo "  mkdir -p ~/.config/credit-auto && chmod 700 ~/.config/credit-auto"
  echo "  pbpaste > ~/.config/credit-auto/github_token && chmod 600 ~/.config/credit-auto/github_token"
  exit 1
fi

TOKEN="$(tr -d '[:space:]' < "$TOKEN_FILE")"
if [[ -z "$TOKEN" ]]; then
  echo "Error: $TOKEN_FILE is empty"
  exit 1
fi

export JAVA_HOME="${JAVA_HOME:-/Users/prasanthgajula/Library/Java/JavaVirtualMachines/jbr-17.0.12/Contents/Home}"

START_TS=$(date +%s)
echo "==> Running unit tests & building R8-optimized release APK locally..."
./gradlew testReleaseUnitTest assembleRelease --build-cache --parallel

if [[ ! -f "$APK_PATH" ]]; then
  echo "Error: APK not found at $APK_PATH"
  exit 1
fi

echo "==> Pushing branch and tag $TAG..."
if ! git rev-parse "$TAG" >/dev/null 2>&1; then
  git tag -a "$TAG" -m "$NOTES"
fi
git push origin main --tags

echo "==> Creating GitHub Release $TAG..."
PAYLOAD=$(python3 -c 'import json,sys; print(json.dumps({"tag_name": sys.argv[1], "name": sys.argv[1], "body": sys.argv[2], "draft": False, "prerelease": False}))' "$TAG" "$NOTES")

CREATE_RESP=$(curl -sS -w "\n%{http_code}" \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "https://api.github.com/repos/$REPO/releases" \
  -d "$PAYLOAD")

HTTP_CODE=$(echo "$CREATE_RESP" | tail -n1)
RESP_BODY=$(echo "$CREATE_RESP" | sed '$d')

if [[ "$HTTP_CODE" != "201" ]]; then
  echo "Failed to create release (HTTP $HTTP_CODE):"
  echo "$RESP_BODY"
  exit 1
fi

RELEASE_ID=$(echo "$RESP_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
HTML_URL=$(echo "$RESP_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['html_url'])")

echo "==> Uploading CreditAuto.apk ($(ls -lh "$APK_PATH" | awk '{print $5}'))..."
UPLOAD_RESP=$(curl -sS -w "\n%{http_code}" \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "Content-Type: application/vnd.android.package-archive" \
  "https://uploads.github.com/repos/$REPO/releases/$RELEASE_ID/assets?name=CreditAuto.apk" \
  --data-binary "@$APK_PATH")

UPLOAD_CODE=$(echo "$UPLOAD_RESP" | tail -n1)
if [[ "$UPLOAD_CODE" != "201" ]]; then
  echo "Failed to upload APK (HTTP $UPLOAD_CODE):"
  echo "$UPLOAD_RESP"
  exit 1
fi

ELAPSED=$(( $(date +%s) - START_TS ))
echo ""
echo "DONE in ${ELAPSED}s!"
echo "Release URL: $HTML_URL"
