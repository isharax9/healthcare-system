# GitHub Repository Secrets Setup Guide

This guide explains how to properly configure GitHub repository secrets for the healthcare system project, including database configuration for cross-platform builds.

## Overview

The healthcare system uses environment variables for database configuration. For GitHub Actions workflows (cross-platform builds), these need to be set up as **GitHub Repository Secrets**, not GitHub Copilot environment variables.

## Types of Environment Configuration

### 1. **GitHub Repository Secrets** (For CI/CD Builds)
Used by GitHub Actions workflows for cross-platform builds and automated testing.

**Access:** Go to your repository → Settings → Secrets and variables → Actions

### 2. **GitHub Codespaces/Copilot Secrets** (For Development)
Used when developing in GitHub Codespaces or using GitHub Copilot.

**Access:** GitHub Settings → Codespaces → Repository secrets

### 3. **Local Development** (For Your Machine)
Used when running the application locally on your development machine.

**Setup:** Environment variables or application.properties file

## Setting Up GitHub Repository Secrets

### Step 1: Access Repository Secrets

1. Go to your GitHub repository: `https://github.com/helasoftLK/healthcare-system`
2. Click on **Settings** (in the repository menu)
3. In the left sidebar, click **Secrets and variables**
4. Click **Actions**

### Step 2: Add Database Secrets

Click **New repository secret** and add each of the following:

| Secret Name | Example Value | Description |
|-------------|---------------|-------------|
| `DB_URL` | `jdbc:mysql://your-server:3306/globemed_db` | Database connection URL |
| `DB_USERNAME` | `your_username` | Database username |
| `DB_PASSWORD` | `your_secure_password` | Database password |

### Step 3: Verify Setup

After adding the secrets:

1. Go to **Actions** tab in your repository
2. Trigger a new build by:
   - Pushing a commit to `main` or `develop` branch
   - Creating a new tag (for releases)
   - Manually triggering the workflow

The workflow will now use your secrets for database configuration during builds.

## Secret Values for Your Environment

Based on your provided configuration, add these secrets:

| Secret Name | Value |
|-------------|-------|
| `DB_URL` | `jdbc:mysql://localhost:3306/globemed_db` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `NewPassword123!` |

⚠️ **Note:** If your database is not accessible from GitHub Actions runners (which is typical for localhost), the connectivity test will fail gracefully without breaking the build.

## Workflow Integration

The updated workflow now:

1. **Uses your secrets** if they are provided
2. **Falls back to defaults** if secrets are not configured
3. **Tests connectivity** when secrets are available
4. **Continues building** even if database is not accessible

## Troubleshooting

### Build Still Failing?

1. **Check secret names:** Ensure they match exactly (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
2. **Check secret values:** Verify the database connection string is correct
3. **Check workflow logs:** Go to Actions tab → Latest workflow run → View logs

### Database Connection Issues in CI

This is normal if:
- Your database runs on `localhost` (not accessible from GitHub runners)
- Your database requires VPN access
- Your database is behind a firewall

The application handles this gracefully and continues building.

## Security Best Practices

1. **Never commit secrets** to source code
2. **Use different credentials** for CI/CD vs production
3. **Rotate secrets regularly**
4. **Limit secret access** to required workflows only
5. **Use least privilege principle** for database users

## Alternative Configurations

### For Production Deployment

```bash
# Set environment variables on your production server
export DB_URL="jdbc:mysql://prod-server:3306/globemed_db"
export DB_USERNAME="prod_user"
export DB_PASSWORD="production_password"
```

### For Local Development

Add to your shell profile (`.bashrc`, `.zshrc`, etc.):

```bash
export DB_URL="jdbc:mysql://localhost:3306/globemed_db"
export DB_USERNAME="root"
export DB_PASSWORD="NewPassword123!"
```

Or edit `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/globemed_db
db.username=root
db.password=NewPassword123!
```

## Need Help?

If you're still experiencing issues:

1. Check the [Environment Setup Guide](ENVIRONMENT_SETUP.md)
2. Review the [Contributing Guide](CONTRIBUTING.md)
3. Open an issue with:
   - Steps you followed
   - Error messages (without sensitive data)
   - Your environment details