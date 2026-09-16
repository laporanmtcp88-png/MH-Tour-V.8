#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/android"

echo "[1/8] File Android utama"
test -f settings.gradle.kts
test -f build.gradle.kts
test -f gradle.properties
test -f app/build.gradle.kts
test -f app/src/main/AndroidManifest.xml
test -f app/src/main/java/com/mhtour/audio/MainActivity.kt

echo "[2/8] AndroidX"
grep -q '^android.useAndroidX=true$' gradle.properties
grep -q '^android.enableJetifier=true$' gradle.properties

echo "[3/8] JVM 17"
grep -q 'sourceCompatibility = JavaVersion.VERSION_17' app/build.gradle.kts
grep -q 'targetCompatibility = JavaVersion.VERSION_17' app/build.gradle.kts
grep -q 'jvmTarget = "17"' app/build.gradle.kts

echo "[4/8] LiveKit version and API"
grep -q 'io.livekit:livekit-android:2.28.2' app/build.gradle.kts
grep -q 'newRoom.connect(' app/src/main/java/com/mhtour/audio/MainActivity.kt
! grep -q 'LiveKit.connect(' app/src/main/java/com/mhtour/audio/MainActivity.kt

echo "[5/8] Permission API"
! grep -q 'onRequestPermissionsResult' app/src/main/java/com/mhtour/audio/MainActivity.kt
grep -q 'RequestMultiplePermissions' app/src/main/java/com/mhtour/audio/MainActivity.kt

echo "[6/8] No old buildConfigField mistake"
! grep -q 'buildConfigField' app/build.gradle.kts

echo "[7/8] Local endpoint"
grep -q '192.168.43.1:8080' app/src/main/java/com/mhtour/audio/MainActivity.kt

echo "[8/8] Packaging native libraries"
grep -q 'useLegacyPackaging = true' app/build.gradle.kts

echo "PREFLIGHT OK"
