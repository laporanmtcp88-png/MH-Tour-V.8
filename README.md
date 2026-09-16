# MH Tour V4.8 — AUTO ONLINE

Versi ini tidak lagi mewajibkan hotspot `192.168.43.1` dan tidak memaksa Wi-Fi-only. HP Guide dan HP Jemaah dapat memakai Wi-Fi atau data seluler operator masing-masing, selama keduanya dapat mencapai server MH Tour melalui internet.

## Cara kerja

```text
HP Guide (Wi-Fi / Data Seluler) ─┐
                                ├── INTERNET ──> MH TOUR API + LIVEKIT ──> INTERNET ──> HP Jemaah
HP Jemaah (Wi-Fi / Data Seluler)┘
```

## AUTO ONLINE

Setelah APK dibuat dengan alamat server produksi, pengguna tidak perlu mengetik alamat server. Alamat API ditanam ke APK saat build melalui GitHub Actions variable `MH_TOUR_API_BASE_URL`.

**Catatan:** aplikasi tetap membutuhkan layanan server internet. Operator seluler menyediakan koneksi internet, bukan server LiveKit/token MH Tour.

## Setup sekali di GitHub

1. Sediakan server publik untuk API MH Tour dan LiveKit.
2. API harus dapat diakses, misalnya `https://api.namadomain.com`.
3. Di GitHub buka **Settings → Secrets and variables → Actions → Variables**.
4. Buat repository variable:
   - Name: `MH_TOUR_API_BASE_URL`
   - Value: URL API publik, misalnya `https://api.namadomain.com`
5. Jalankan **Actions → MH Tour APK Build → Run workflow**.
6. Ambil artifact `MH-Tour-APK`.

## Server

Gunakan `server/.env.example` sebagai acuan. Untuk produksi:
- `LIVEKIT_URL` harus menunjuk ke LiveKit publik (`wss://...`).
- Gunakan secret LiveKit yang panjang dan acak.
- Gunakan HTTPS untuk API.
- Batasi `ALLOWED_ORIGINS` jika diperlukan.

## QR

QR Guide berisi `MHTOUR|JOIN|<API_URL>|<KODE>`. Saat Jemaah scan QR, API URL dari QR disimpan otomatis. Ini membuat alamat server transparan bagi Jemaah.

## Hasil

Guide dan Jemaah dapat berada pada operator atau jaringan berbeda. Yang diperlukan adalah koneksi internet yang memungkinkan keduanya mencapai API dan LiveKit.


## LiveKit connection
This build uses the LiveKit Cloud Development Token Server ID `mhtour-19kg8e` for development/testing, so no separate MH Tour token backend URL is required. The LiveKit project remains unchanged.
