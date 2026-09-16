# MH Tour V4.7 Hardened — Build Audit

## Pemeriksaan yang sudah dilakukan

- AndroidX: `android.useAndroidX=true` dan Jetifier aktif.
- Java/Kotlin: target JVM 17 konsisten.
- LiveKit Android: dipatok ke `2.28.2`.
- API LiveKit: koneksi menggunakan `LiveKit.create(...); room.connect(...)`, bukan API ambigu.
- Permission Android: memakai Activity Result API; `onRequestPermissionsResult` dihapus.
- QR scanner: tetap memakai JourneyApps ZXing dan `onActivityResult` yang dibutuhkan integrasi tersebut.
- Native WebRTC/LiveKit: legacy JNI packaging diaktifkan untuk mengurangi masalah stripping native library.
- `buildConfigField` lama yang pernah menyebabkan error Kotlin DSL dihapus.
- Workflow GitHub Actions: hanya `workflow_dispatch`, sehingga tidak lagi salah terpicu sebagai push.
- Workflow memeriksa file AndroidX/JVM/LiveKit/permission sebelum build.
- APK diverifikasi harus ada dan tidak kosong sebelum artifact di-upload.
- Server token: subnet hotspot diperiksa dari alamat socket langsung, bukan `X-Forwarded-For` yang dapat dipalsukan.
- Session room memiliki masa berlaku default 12 jam dan dibersihkan saat diperlukan.

## Error yang sebelumnya teridentifikasi

1. `buildConfigField` unresolved pada Kotlin DSL.
2. AndroidX belum diaktifkan.
3. Java 8 vs Kotlin JVM 17 tidak konsisten.
4. `onRequestPermissionsResult` tidak cocok dengan callback Activity yang dipakai.
5. `Array<out String>` vs `Array<String>` pada callback permission.
6. Pemanggilan `connect` LiveKit yang tidak tepat.
7. Peringatan stripping `liblkjingle_peerconnection_so.so`.
8. Workflow GitHub yang pernah kosong/malformed atau terpicu `push`.
9. Validasi proyek sebelum build belum cukup ketat.

## Catatan

Build Android final tetap harus dijalankan oleh GitHub Actions/Android Studio karena environment pemeriksaan ini tidak menyediakan Gradle executable. Paket ini sudah dilengkapi preflight check agar kesalahan konfigurasi yang diketahui dihentikan sebelum proses compile.
