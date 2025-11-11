#!/bin/bash

# Release script for Redis IntelliJ Plugin
# Usage: ./release.sh <version> [--push]
# Example: ./release.sh 1.0.1
# Example: ./release.sh 1.0.1 --push  (automatically pushes to GitHub)

set -e

if [ -z "$1" ]; then
    echo "Error: Version number required"
    echo "Usage: ./release.sh <version> [--push]"
    echo "Example: ./release.sh 1.0.1"
    echo "Example: ./release.sh 1.0.1 --push  (auto-push to GitHub)"
    exit 1
fi

VERSION=$1
TAG="v${VERSION}"
AUTO_PUSH=false

# Check for --push flag
if [ "$2" = "--push" ]; then
    AUTO_PUSH=true
fi

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

if [ "$AUTO_PUSH" = true ]; then
    echo "Pushing to GitHub..."
    git push origin main
    git push origin ${TAG}
    echo ""
    echo "✅ Release ${VERSION} pushed to GitHub!"
    echo ""
    echo "Monitor the release workflow:"
    echo "  https://github.com/$(git remote get-url origin | sed 's/.*github.com[:/]\(.*\)\.git/\1/')/actions"
    echo ""
    echo "The GitHub Actions workflow will automatically:"
    echo "  - Build the plugin"
    echo "  - Run tests"
    echo "  - Create a GitHub release"
    echo "  - Upload the plugin artifact"
    echo "  - (Optional) Publish to JetBrains Marketplace if PUBLISH_TOKEN is set"
    echo ""
else
    echo "⚠️  IMPORTANT: Release NOT yet pushed to GitHub!"
    echo "⚠️  The build workflow has NOT been triggered yet."
    echo ""
    echo "Next steps:"
    echo "1. Review the changes:"
    echo "     git show HEAD"
    echo ""
    echo "2. Push to GitHub to trigger the release workflow:"
    echo "     git push origin main && git push origin ${TAG}"
    echo ""
    echo "   This will trigger the GitHub Actions workflow that will:"
    echo "     • Build the plugin"
    echo "     • Run tests"
    echo "     • Create a GitHub release"
    echo "     • Upload the plugin artifact"
    echo "     • (Optional) Publish to JetBrains Marketplace if PUBLISH_TOKEN is set"
    echo ""
    echo "3. Monitor the workflow:"
    echo "     GitHub → Actions tab → 'Release' workflow"
    echo ""
    echo "=========================================="
    echo "Tips:"
    echo "=========================================="
    echo "• To push automatically next time:"
    echo "    ./release.sh ${VERSION} --push"
    echo ""
    echo "• To undo this release (before pushing):"
    echo "    git reset --hard HEAD~1"
    echo "    git tag -d ${TAG}"
    echo ""
fi
