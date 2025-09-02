# 📋 Manual Release Guide

This guide explains how to create releases now that the automated Release Please workflow has been disabled..

## 🚀 Quick Release Steps

1. **Make your changes** and commit them:
   ```bash
   git add .
   git commit -m "Add new features and bug fixes"
   ```

2. **Create a version tag** (following [Semantic Versioning](https://semver.org/)):
   ```bash
   # For bug fixes (patch): 1.0.0 → 1.0.1
   git tag v1.0.1
   
   # For new features (minor): 1.0.0 → 1.1.0  
   git tag v1.1.0
   
   # For breaking changes (major): 1.0.0 → 2.0.0
   git tag v2.0.0
   ```

3. **Push the tag** to trigger the release:
   ```bash
   git push origin v1.0.1
   ```

4. **Watch the build** in GitHub Actions:
   - Go to the "Actions" tab in your repository
   - The "Cross-Platform Build" workflow will run automatically
   - All platforms will be built (Windows, macOS, Linux)
   - A GitHub release will be created with all installers

## ✅ What Works Now

- ✅ Manual tag-based releases
- ✅ Cross-platform builds (Windows .exe, macOS .dmg, Linux .deb/.rpm)
- ✅ Automatic GitHub release creation
- ✅ No more pipeline failures
- ✅ Complete control over release timing

## ❌ What's Disabled

- ❌ Automatic Release Please workflow
- ❌ Automatic version bumping based on commit messages
- ❌ Automatic changelog generation
- ❌ Release PR creation

## 🔧 Example Release Session

```bash
# Start with some development work
git checkout main
git pull origin main

# Make your changes
git add .
git commit -m "Fix patient search bug and add export feature"

# Check current version (look at existing tags)
git tag --list | sort -V | tail -5

# Create next version tag
git tag v1.2.0

# Push the tag to trigger release
git push origin v1.2.0

# 🎉 GitHub Actions will now:
# 1. Build for all platforms
# 2. Create installers and packages  
# 3. Create a GitHub release
# 4. Upload all artifacts
```

## 📚 Version Number Guidelines

Follow [Semantic Versioning](https://semver.org/):

- **PATCH** (1.0.0 → 1.0.1): Bug fixes, security patches
- **MINOR** (1.0.0 → 1.1.0): New features, backwards compatible
- **MAJOR** (1.0.0 → 2.0.0): Breaking changes, incompatible API changes

## 🔄 Re-enabling Automated Releases (Future)

If you want to add an automated Release Please system:

1. Create a new `.github/workflows/release.yml` file
2. Configure it with Release Please action and appropriate triggers:
   ```yaml
   on:
     push:
       branches:
         - main
     workflow_dispatch:
   ```
3. Test the workflow thoroughly
4. Update documentation back to automated process

## 💡 Tips

- **Check existing tags** before creating new ones: `git tag --list | sort -V`
- **Delete a tag** if you make a mistake: `git tag -d v1.0.1 && git push origin :refs/tags/v1.0.1`
- **View build progress** in the Actions tab
- **Download artifacts** directly from the GitHub release page
