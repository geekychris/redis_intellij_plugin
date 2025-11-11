#!/bin/bash

# Release script for Redis IntelliJ Plugin
# Usage: ./release.sh <version>
# Example: ./release.sh 1.0.1

set -e

if [ -z "$1" ]; then
    echo "Error: Version number required"
    echo "Usage: ./release.sh <version>"
    echo "Example: ./release.sh 1.0.1"
    exit 1
fi

VERSION=$1
TAG="v${VERSION}"

echo "=========================================="
echo "Redis IntelliJ Plugin Release Script"
echo "=========================================="
echo "Version: ${VERSION}"
echo "Tag: ${TAG}"
echo ""

# Check if on main branch
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT_BRANCH" != "main" ]; then
    echo "Warning: You are not on the main branch (currently on ${CURRENT_BRANCH})"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Check for uncommitted changes
if ! git diff-index --quiet HEAD --; then
    echo "Error: You have uncommitted changes"
    git status --short
    exit 1
fi

# Update version in build.gradle.kts
echo "Updating version in build.gradle.kts..."
sed -i.bak "s/version = \".*\"/version = \"${VERSION}\"/" build.gradle.kts
rm -f build.gradle.kts.bak

# Commit version change
git add build.gradle.kts
git commit -m "Release version ${VERSION}"

# Create and push tag
echo "Creating tag ${TAG}..."
git tag -a "${TAG}" -m "Release version ${VERSION}"

echo ""
echo "=========================================="
echo "Release ${VERSION} prepared!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Review the changes: git show HEAD"
echo "2. Push to remote: git push origin main && git push origin ${TAG}"
echo ""
echo "The GitHub Actions workflow will automatically:"
echo "  - Build the plugin"
echo "  - Run tests"
echo "  - Create a GitHub release"
echo "  - Upload the plugin artifact"
echo "  - (Optional) Publish to JetBrains Marketplace if PUBLISH_TOKEN is set"
echo ""
echo "To undo this release (before pushing):"
echo "  git reset --hard HEAD~1"
echo "  git tag -d ${TAG}"
echo ""
