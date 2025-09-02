# 🚀 Quick Start Guide for Manual Releases

> **Note**: Automated releases have been temporarily disabled due to pipeline issues. 
> This guide covers the manual tag-based release process that is currently active.

## How to Create Your First Manual Release

### 1. Make Changes to Your Code
```bash
# Make your feature/fix changes
# ... edit code files ...
```

### 2. Commit Your Changes (Any Format)
```bash
# You can use any commit message format for manual releases
git add .
git commit -m "Add new patient search functionality"

# Or use conventional commits if you prefer
git commit -m "feat(patients): add advanced search with filters"
```

### 3. Create and Push a Version Tag
```bash
# Create a version tag (following semantic versioning)
git tag v1.0.1

# Push the tag to trigger the release build
git push origin v1.0.1
```

### 4. Watch the Magic Happen!
1. **Tag push triggers build** automatically in GitHub Actions
2. **Cross-platform builds** automatically execute:
   - Windows `.exe` installer
   - macOS `.dmg` installer  
   - Linux `.deb` and `.rpm` packages
   - Universal `.jar` file
3. **GitHub Release** created with all artifacts and changelog

## 🛠️ Version Tagging Guidelines

Follow [Semantic Versioning](https://semver.org/) when creating tags:

- **Patch release** (1.0.0 → 1.0.1): Bug fixes, small improvements
- **Minor release** (1.0.0 → 1.1.0): New features, backwards compatible
- **Major release** (1.0.0 → 2.0.0): Breaking changes

```bash
# Examples:
git tag v1.0.1   # Patch release
git tag v1.1.0   # Minor release  
git tag v2.0.0   # Major release
```

## 🎯 Example Release Flow

```bash
# Start with version 1.0.0, make some changes
git add .
git commit -m "Add OAuth2 support and fix login styling"
git commit -m "Add PDF export functionality"

# Create a new minor release
git tag v1.1.0
git push origin v1.1.0

# GitHub Actions automatically builds and releases:
# - Cross-platform installers
# - GitHub release with changelogs
```

## 🔗 Resources

- **Manual Release Process**: Follow the steps above
- **Semantic Versioning**: https://semver.org/
- **Contributing Guide**: `CONTRIBUTING.md`
- **Build Workflows**: `.github/workflows/build.yml`

Ready to create your first manual release! 🎉

## 🔄 Automated Releases (Not Available)

The automated Release Please system has been removed to simplify the pipeline. If you need to add automated releases in the future:

1. Create a new `.github/workflows/release.yml` file with Release Please action
2. Configure the push trigger for the main branch
3. Test the workflow thoroughly before merging