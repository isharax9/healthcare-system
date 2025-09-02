# 🚀 Quick Start Guide for Automated Releases

## How to Create Your First Automated Release

### 1. Make Changes to Your Code
```bash
# Make your feature/fix changes
# ... edit code files ...
```

### 2. Commit Using Conventional Format
```bash
# For a new feature (minor version bump: 1.0.0 → 1.1.0)
git add .
git commit -m "feat(patients): add advanced patient search functionality"

# For a bug fix (patch version bump: 1.0.0 → 1.0.1)  
git commit -m "fix(database): resolve connection timeout issues"

# For breaking changes (major version bump: 1.0.0 → 2.0.0)
git commit -m "feat!: redesign authentication API

BREAKING CHANGE: The old auth methods have been removed"
```

### 3. Push to Main Branch
```bash
git push origin main
```

### 4. Watch the Magic Happen!
1. **Release Please** automatically analyzes your commits
2. Creates a **Release PR** with:
   - Automatically calculated version number
   - Generated changelog with your changes
   - Updated version in `pom.xml`
3. **Review and merge** the Release PR
4. **Cross-platform builds** automatically trigger:
   - Windows `.exe` installer
   - macOS `.dmg` installer  
   - Linux `.deb` and `.rpm` packages
   - Universal `.jar` file
5. **GitHub Release** created with all artifacts

## 🛠️ Alternative: Use the Release Helper Script

For interactive commit creation:

```bash
# Interactive commit helper
./release-helper.sh commit

# Check current status
./release-helper.sh status

# View changelog
./release-helper.sh changelog
```

## 📋 Commit Types Reference

| Type | Description | Version Impact |
|------|-------------|----------------|
| `feat` | New feature | Minor (1.0.0 → 1.1.0) |
| `fix` | Bug fix | Patch (1.0.0 → 1.0.1) |
| `feat!` | Breaking change | Major (1.0.0 → 2.0.0) |
| `docs` | Documentation | Patch |
| `refactor` | Code refactoring | Patch |
| `perf` | Performance improvement | Patch |

## 🎯 Example Release Flow

```bash
# Start with version 1.0.0
git commit -m "feat(auth): add OAuth2 support"
git commit -m "fix(ui): resolve login button styling"
git commit -m "feat(reports): add PDF export functionality"
git push origin main

# Release Please creates PR: v1.0.0 → v1.1.0
# Changelog includes:
# - Features: OAuth2 support, PDF export
# - Bug Fixes: Login button styling
```

## 🔗 Resources

- **Conventional Commits Guide**: `CONVENTIONAL_COMMITS.md`
- **Changelog**: `CHANGELOG.md`
- **Contributing Guide**: `CONTRIBUTING.md`
- **Release Helper**: `./release-helper.sh help`

Ready to create your first automated release! 🎉