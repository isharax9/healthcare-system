# Pipeline Troubleshooting Guide

## Recent Fixes Applied

### Issue 1: Release-Please Configuration Errors

**Error Message:**
```
Unexpected input(s) 'package-name', valid inputs are ['token', 'release-type', 'path', 'target-branch', 'config-file', 'manifest-file', 'repo-url', 'github-api-url', 'github-graphql-url', 'fork', 'include-component-in-tag', 'proxy-server', 'skip-github-release', 'skip-github-pull-request', 'skip-labeling', 'changelog-host']
```

**Root Cause:** 
- The release workflow was using deprecated parameters (`package-name` and inline `release-type`)
- googleapis/release-please-action@v4 requires configuration via files, not inline parameters

**Fix Applied:**
- Removed `package-name` and `release-type` from workflow
- Added `config-file: .release-please-config.json` 
- Added `manifest-file: .release-please-manifest.json`
- Updated `.release-please-config.json` to use `release-type: java` instead of `maven`

### Issue 2: GitHub Actions Permissions

**Error Message:**
```
release-please failed: GitHub Actions is not permitted to create or approve pull requests.
```

**Root Cause:**
- Missing proper token configuration for the release-please action

**Fix Applied:**
- Added `token: ${{ secrets.GITHUB_TOKEN }}` to the release-please step
- This uses the built-in GitHub token with proper permissions

### Issue 3: Incorrect Artifact and Class Names

**Issues Found:**
- Wrong main class: `com.globemed.healthcare.Main` (should be `com.globemed.Main`)
- Wrong artifact path: missing `-executable` suffix
- Wrong Java version: using 21 instead of 17

**Fix Applied:**
- Updated main class to `com.globemed.Main` (matches actual code)
- Updated artifact paths to include `-executable.jar` suffix
- Changed Java version from 21 to 17 (matches project requirements)

## How Release-Please Works Now

### Configuration Files

**`.release-please-config.json`:**
```json
{
  "packages": {
    ".": {
      "package-name": "healthcare-system",
      "release-type": "java",
      "bump-minor-pre-major": false,
      "bump-patch-for-minor-pre-major": false,
      "draft": false,
      "prerelease": false
    }
  }
}
```

**`.release-please-manifest.json`:**
```json
{
  ".": "1.0.0"
}
```

### Release Workflow

1. **On push to main:** Release-please analyzes conventional commits
2. **Creates Release PR:** If changes warrant a release
3. **When PR merged:** Triggers cross-platform builds
4. **Creates GitHub Release:** With all platform artifacts

## Creating Releases

### Method 1: Conventional Commits (Recommended)

```bash
# For a new feature (minor version bump)
git commit -m "feat(auth): add OAuth2 support"

# For a bug fix (patch version bump)  
git commit -m "fix(database): resolve connection timeout"

# For breaking changes (major version bump)
git commit -m "feat!: redesign user API

BREAKING CHANGE: Old authentication methods removed"

# Push to main
git push origin main
```

### Method 2: Using the Release Helper Script

```bash
./release-helper.sh commit
```

## No Manual Token Creation Needed

The pipeline now uses the built-in `GITHUB_TOKEN` which has the necessary permissions to:
- Create pull requests
- Create releases
- Upload artifacts

You **do not** need to create a personal access token.

## Troubleshooting Future Issues

### Build Failures
1. Check the Actions tab in your repository
2. Look for specific error messages in the logs
3. Verify Java version compatibility (should be 17)
4. Ensure Maven build succeeds locally first

### Release-Please Issues
1. Verify conventional commit format
2. Check that commits are on the `main` branch
3. Ensure `.release-please-config.json` is valid JSON

### Permission Issues
1. Check repository settings → Actions → General
2. Ensure "Allow GitHub Actions to create pull requests" is enabled
3. Verify workflow permissions in the YAML file

## Testing the Fix

To test that the fix works:

1. Make a small change to the code
2. Commit with conventional format: `git commit -m "feat: test automated release"`  
3. Push to main: `git push origin main`
4. Check the Actions tab for the release workflow
5. Look for a new release PR to be created

The pipeline should now work correctly without requiring any manual token setup!