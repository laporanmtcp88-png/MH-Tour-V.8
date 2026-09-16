# MH Tour — GitHub Build Ready

This package is prepared for GitHub Actions.

## Expected repository root

- `.github/workflows/build-release-signed.yml`
- `android/`
- `server/`
- `tools/`
- `.gitignore`
- `README.md`

## Build

1. Upload the extracted contents to the repository root.
2. Commit to `main`.
3. Open **Actions**.
4. Select **Build MH Tour APK**.
5. Click **Run workflow**.
6. Select `main` and run it.
7. Wait for the job **Build Android APK** to finish.
8. Download artifact **MH-Tour-APK**.

The artifact contains:
- `MH_Tour_Debug.apk`
- `MH_Tour_Release.apk` (signed with a temporary CI key for testing/installing)

No GitHub Secrets are required for this test build.


Build CI fix: Android BuildConfig is explicitly enabled and the GitHub Actions workflow uses a valid workflow_dispatch trigger.
