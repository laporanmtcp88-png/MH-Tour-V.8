# MH Tour V3.3 — Hotspot Wi-Fi Only — One-Click Signed Release APK

Project ini sudah disiapkan untuk membuat **APK Release yang ditandatangani** melalui GitHub Actions,
tanpa Android Studio.

## Cara paling mudah

### 1. Upload project ke GitHub
Upload isi folder project ini ke repository GitHub.

### 2. Buat keystore release
Di PC yang memiliki JDK, jalankan:

- Windows: `tools\create-release-keystore.bat`
- Linux/macOS: `bash tools/create-release-keystore.sh`

**Simpan file `mh-tour-release.jks` dan password di tempat aman. Jangan commit ke GitHub.**

### 3. Encode keystore
Linux/macOS/Git Bash:
```bash
base64 -w 0 mh-tour-release.jks > keystore.b64
```

Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("mh-tour-release.jks")) | Set-Content keystore.b64
```

### 4. Isi GitHub Secrets

Repository → **Settings → Secrets and variables → Actions → New repository secret**

Buat 4 secrets:

- `MH_KEYSTORE_B64` = isi file `keystore.b64`
- `MH_KEYSTORE_PASSWORD` = password keystore
- `MH_KEY_ALIAS` = alias yang dibuat
- `MH_KEY_PASSWORD` = password key

Jangan taruh password di source code.

### 5. Sekali klik build APK

GitHub → **Actions** → **Build MH Tour Release APK** → **Run workflow**.

Setelah selesai:

**Artifacts → MH-Tour-Hotspot-WiFi-Only-Release-Signed**

APK:
`MH_Tour_Hotspot_WiFi_Only-release.apk`

### Build otomatis dengan tag

Push tag seperti:
```bash
git tag v3.3.0
git push origin v3.3.0
```

Workflow akan membuat GitHub Release dan melampirkan APK Release yang sudah signed.

## Keamanan signing

Keystore release adalah identitas permanen aplikasi. **Backup file `.jks` dan password.**
Jika keystore hilang, update aplikasi Android dengan identitas signing yang sama dapat menjadi
bermasalah.

File `.jks`, `.b64`, dan secret tidak boleh dimasukkan ke repository.

## Catatan

Workflow menggunakan JDK 17, Android SDK API 35, Build Tools 35.0.0 dan Gradle 8.11.1.


Build CI fix: Android BuildConfig is explicitly enabled and the GitHub Actions workflow uses a valid workflow_dispatch trigger.
