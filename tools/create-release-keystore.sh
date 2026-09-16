#!/usr/bin/env bash
set -euo pipefail
KS="mh-tour-release.jks"
read -rp "Key alias [mh-tour]: " ALIAS
ALIAS="${ALIAS:-mh-tour}"
read -rsp "Keystore password: " STOREPASS; echo
read -rsp "Key password: " KEYPASS; echo
keytool -genkeypair -v -keystore "$KS" -alias "$ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass "$STOREPASS" -keypass "$KEYPASS" \
  -dname "CN=MH Tour, OU=MH Tour, O=MH Tour, L=Indonesia, ST=Indonesia, C=ID"
echo "Created $KS. Do NOT commit it to Git."
