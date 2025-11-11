# Release Process

This document describes the release workflow for the Redis IntelliJ Plugin.

## Quick Reference

**To create a release (manual push):**
```bash
# 1. Prepare and create release tag
./release.sh 1.0.1  # Use version WITHOUT "v" prefix!

# 2. Push BOTH commit and tag to trigger workflow
git push origin main && git push origin v1.0.1

# 3. Monitor the Release workflow in Actions tab
# 4. Download plugin from Releases section when complete
```

**To create a release (auto-push):**
```bash
# One command does everything - creates tag AND pushes
./release.sh 1.0.1 --push

# Then monitor the Release workflow in Actions tab
```

**Key Points:**
- ✅ Use `./release.sh 1.0.1` (script adds "v" automatically)
- ❌ Don't use `./release.sh v1.0.1` (creates wrong tag `vv1.0.1`)
- ✅ Must push BOTH main branch AND the tag
- ✅ Check "Release" workflow in Actions (not "Build")
- ✅ Plugin appears in Releases section (not just Actions artifacts)

## Overview

The project uses GitHub Actions for automated builds and releases. The workflow is triggered by pushing version tags to the repository.

## Prerequisites

### Required GitHub Secrets

To publish releases, configure the following secrets in your GitHub repository (`Settings` → `Secrets and variables` → `Actions`):

#### For Signing (Optional but Recommended)
- `CERTIFICATE_CHAIN`: Plugin signing certificate chain
- `PRIVATE_KEY`: Private key for plugin signing
- `PRIVATE_KEY_PASSWORD`: Password for the private key

See [JetBrains documentation](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html) for details on obtaining signing certificates.

#### For JetBrains Marketplace Publishing (Optional)
- `PUBLISH_TOKEN`: Token for publishing to JetBrains Marketplace

Obtain this from your [JetBrains Account](https://plugins.jetbrains.com/author/me/tokens).

## Release Workflow

### Automated Release (Recommended)

1. **Prepare your changes**
   ```bash
   # Ensure you're on main branch with latest changes
   git checkout main
   git pull origin main
   
   # Ensure all tests pass
   ./gradlew test
   ```

2. **Create a release using the release script**
   ```bash
   ./release.sh 1.0.1
   ```
   
   **IMPORTANT:** Use the version number WITHOUT the "v" prefix (e.g., `1.0.1`, NOT `v1.0.1`)
   
   This script will:
   - Update the version in `build.gradle.kts`
   - Commit the version change
   - Create a git tag with "v" prefix (e.g., `v1.0.1`)
   - Display next steps
   
   **Optional:** Add `--push` flag to automatically push to GitHub:
   ```bash
   ./release.sh 1.0.1 --push
   ```
   If you use `--push`, skip step 3 below.

3. **Push BOTH the commit AND the tag** (if you didn't use `--push`)
   ```bash
   git push origin main && git push origin v1.0.1
   ```
   
   **CRITICAL:** You must push BOTH:
   - `git push origin main` - Pushes the version commit
   - `git push origin v1.0.1` - Pushes the tag that triggers the release workflow
   
   The tag push is what triggers the GitHub Actions release workflow!

4. **Monitor the workflow**
   - Go to your GitHub repository → `Actions` tab
   - In the left sidebar, click **"Release"** (not "Build")
   - Watch the release workflow execute
   - The workflow will:
     - Build and test the plugin
     - Create a GitHub Release
     - Attach the plugin ZIP file to the release

5. **Verify the release**
   - Go to your repository main page
   - Click **"Releases"** in the right sidebar
   - Find your release (e.g., "Release 1.0.1")
   - Verify the plugin ZIP file appears under **"Assets"**
   - Users can now download the plugin from this page!

### Manual Release

If you prefer not to use the script:

1. **Update version in build.gradle.kts**
   ```kotlin
   version = "1.0.1"
   ```

2. **Commit and tag**
   ```bash
   git add build.gradle.kts
   git commit -m "Release version 1.0.1"
   git tag -a v1.0.1 -m "Release version 1.0.1"
   git push origin main
   git push origin v1.0.1
   ```

## What Happens During a Release

When you push a version tag (e.g., `v1.0.1`), the GitHub Actions workflow will:

1. ✅ Checkout the code
2. ✅ Setup Java (Amazon Corretto 21)
3. ✅ Run all tests
4. ✅ Build the plugin
5. ✅ Verify the plugin structure
6. ✅ Sign the plugin (if credentials are configured)
7. ✅ Generate a changelog from git commits
8. ✅ Create a GitHub Release with the plugin ZIP file
9. ✅ Publish to JetBrains Marketplace (if token is configured)

## Continuous Integration

The project also has a CI workflow that runs on every push and pull request to `main` or `develop` branches:

- Runs tests
- Builds the plugin
- Verifies plugin structure
- Uploads build artifacts (retained for 7 days)

## Version Numbering

Follow [Semantic Versioning](https://semver.org/):
- **MAJOR.MINOR.PATCH** (e.g., 1.2.3)
- **MAJOR**: Breaking changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

## Troubleshooting

### Release workflow doesn't run

**Problem:** You don't see the Release workflow in the Actions tab.

**Solution:** Make sure you pushed the tag! The Release workflow only triggers when you push a tag starting with `v`:
```bash
git push origin v1.0.1
```

Check if the tag exists on GitHub:
```bash
git ls-remote --tags origin
```

### Release created but no plugin ZIP in Assets

**Problem:** The GitHub Release exists but only shows "Source code" in Assets.

**Solution:** 
1. Go to Actions → Click on the Release workflow run
2. Check the "Create GitHub Release" step for errors
3. The workflow may have failed before uploading the asset

### Wrong tag name (e.g., vv1.0.1 instead of v1.0.1)

**Problem:** Used `./release.sh v1.0.1` instead of `./release.sh 1.0.1`

**Solution:** The script adds the "v" prefix automatically. Always use:
```bash
./release.sh 1.0.1  # Correct - creates tag v1.0.1
./release.sh v1.0.1 # Wrong - creates tag vv1.0.1
```

To fix incorrect tags:
```bash
git tag -d vv1.0.1              # Delete locally
git push origin :refs/tags/vv1.0.1  # Delete on GitHub
git reset --hard origin/main    # Reset commits
./release.sh 1.0.1              # Try again correctly
```

### Release workflow fails during signing

If you haven't set up signing secrets, the signing step is disabled by default (`if: false`). The plugin will still be built and released, just unsigned.

### Release workflow fails during publishing

If you haven't set up the `PUBLISH_TOKEN` secret, the publishing step is disabled by default (`if: false`). The plugin will still be released on GitHub.

### Need to delete a release

```bash
# Delete the tag locally
git tag -d v1.0.1

# Delete the tag remotely
git push origin :refs/tags/v1.0.1

# Delete the GitHub release manually from the Releases page
```

## Testing Before Release

Always test your plugin before releasing:

```bash
# Run tests
./gradlew test

# Build and verify
./gradlew buildPlugin verifyPlugin

# Test in a development IDE
./gradlew runIde
```

## Release Checklist

- [ ] All tests passing
- [ ] Version updated in `build.gradle.kts`
- [ ] Changes documented in commit messages
- [ ] Plugin tested in development IDE
- [ ] README.md updated if needed
- [ ] On main branch with clean working directory
- [ ] Tag created and pushed
- [ ] GitHub Actions workflow completed successfully
- [ ] Release artifacts available on GitHub
- [ ] (Optional) Plugin published to JetBrains Marketplace
