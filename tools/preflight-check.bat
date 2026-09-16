@echo off
setlocal
cd /d "%~dp0..\android"
if not exist settings.gradle.kts exit /b 1
if not exist build.gradle.kts exit /b 1
if not exist gradle.properties exit /b 1
if not exist app\build.gradle.kts exit /b 1
if not exist app\src\main\AndroidManifest.xml exit /b 1
if not exist app\src\main\java\com\mhtour\audio\MainActivity.kt exit /b 1
findstr /C:"android.useAndroidX=true" gradle.properties >nul || exit /b 1
findstr /C:"android.enableJetifier=true" gradle.properties >nul || exit /b 1
findstr /C:"jvmTarget = \"17\"" app\build.gradle.kts >nul || exit /b 1
findstr /C:"io.livekit:livekit-android:2.28.2" app\build.gradle.kts >nul || exit /b 1
findstr /C:"newRoom.connect(" app\src\main\java\com\mhtour\audio\MainActivity.kt >nul || exit /b 1
findstr /C:"onRequestPermissionsResult" app\src\main\java\com\mhtour\audio\MainActivity.kt >nul && exit /b 1
findstr /C:"buildConfigField" app\build.gradle.kts >nul && exit /b 1
findstr /C:"useLegacyPackaging = true" app\build.gradle.kts >nul || exit /b 1
echo PREFLIGHT OK
endlocal
