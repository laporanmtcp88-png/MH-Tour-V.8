# Upload MH Tour to GitHub

## Important
The `.github` folder starts with a dot and may be hidden by Windows. Do not delete it.

After extracting this ZIP, the first level must contain `.github`, `android`, `server`, and `tools`.

If GitHub Web upload hides `.github`, create the workflow directly in GitHub at:

`.github/workflows/build-release-signed.yml`

and copy the workflow from this package.

## Run the build

Actions → Build MH Tour APK → Run workflow → branch `main` → Run workflow.

The build produces an artifact named `MH-Tour-APK` containing Debug and signed CI Release APKs.
