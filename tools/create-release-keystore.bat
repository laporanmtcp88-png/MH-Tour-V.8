@echo off
setlocal
set "KS=mh-tour-release.jks"
set /p ALIAS=Key alias [mh-tour]:
if "%ALIAS%"=="" set "ALIAS=mh-tour"
set /p STOREPASS=Keystore password:
set /p KEYPASS=Key password:
keytool -genkeypair -v -keystore "%KS%" -alias "%ALIAS%" -keyalg RSA -keysize 2048 -validity 10000 -storepass "%STOREPASS%" -keypass "%KEYPASS%" -dname "CN=MH Tour, OU=MH Tour, O=MH Tour, L=Indonesia, ST=Indonesia, C=ID"
echo.
echo Created %KS%
echo Do NOT commit this file to Git.
pause
