# Release Process

This document describes the release workflow for the Redis IntelliJ Plugin.

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
   
   This script will:
   - Update the version in `build.gradle.kts`
   - Commit the version change
   - Create a git tag (e.g., `v1.0.1`)
   - Provide instructions for pushing

3. **Push the release**
   ```bash
   git push origin main && git push origin v1.0.1
   ```

4. **Monitor the workflow**
   - Go to your GitHub repository → `Actions` tab
   - Watch the "Release" workflow execute
   - Once complete, check the `Releases` section for your new release

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
2. ✅ Setup Java (Amazon Corretto 23)
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

### Release workflow fails during signing

If you haven't set up signing secrets, the signing step will be skipped automatically. The plugin will still be built and released, just unsigned.

### Release workflow fails during publishing

If you haven't set up the `PUBLISH_TOKEN` secret, the publishing step will be skipped. The plugin will still be released on GitHub.

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
