# Conventional Commits Guide

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification for automatic changelog generation and version bumping.

## Commit Message Format

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

## Types

| Type | Description | Version Impact |
|------|-------------|----------------|
| `feat` | A new feature | **Minor** version bump (1.0.0 → 1.1.0) |
| `fix` | A bug fix | **Patch** version bump (1.0.0 → 1.0.1) |
| `docs` | Documentation only changes | **Patch** version bump |
| `style` | Changes that do not affect the meaning of the code | **Patch** version bump |
| `refactor` | A code change that neither fixes a bug nor adds a feature | **Patch** version bump |
| `perf` | A code change that improves performance | **Patch** version bump |
| `test` | Adding missing tests or correcting existing tests | **Patch** version bump |
| `build` | Changes that affect the build system or external dependencies | **Patch** version bump |
| `ci` | Changes to CI configuration files and scripts | **Patch** version bump |
| `chore` | Other changes that don't modify src or test files | No version bump |
| `revert` | Reverts a previous commit | **Patch** version bump |

## Breaking Changes

To trigger a **Major** version bump (1.0.0 → 2.0.0), include `BREAKING CHANGE:` in the commit footer or add `!` after the type:

```
feat!: remove deprecated authentication method

BREAKING CHANGE: The old authentication method has been removed. Use the new OAuth2 implementation instead.
```

## Examples

### Feature Addition (Minor version bump)
```
feat(auth): add OAuth2 authentication support

- Implement OAuth2 login flow
- Add user session management
- Support for Google and Microsoft providers
```

### Bug Fix (Patch version bump)
```
fix(database): resolve connection timeout issues

Increase connection timeout from 5s to 30s to handle slow network conditions.

Fixes #123
```

### Documentation (Patch version bump)
```
docs: update installation guide with Java 17 requirements

- Add Java 17 installation instructions
- Update system requirements
- Fix broken links in README
```

### Performance Improvement (Patch version bump)
```
perf(reports): optimize PDF generation performance

Reduce PDF generation time by 40% by implementing lazy loading for images.
```

### Breaking Change (Major version bump)
```
feat!: redesign user management API

BREAKING CHANGE: The user management API has been completely redesigned.
- UserService.getUser() now returns Optional<User>
- UserService.createUser() now requires UserCreateRequest
- Removed deprecated UserService.findUserByEmail()

Migration guide: See docs/migration-v2.md
```

## Scopes (Optional)

Common scopes for this project:

- `auth` - Authentication and authorization
- `database` - Database operations and connections
- `ui` - User interface components
- `api` - API endpoints and services
- `reports` - Report generation functionality
- `patients` - Patient management features
- `appointments` - Appointment scheduling
- `billing` - Billing and insurance processing
- `build` - Build system and dependencies
- `ci` - Continuous integration
- `docs` - Documentation

## Release Process

1. **Development**: Commit your changes using conventional commits
2. **Automatic**: Release Please will:
   - Analyze commit messages
   - Calculate the next version number
   - Generate changelog
   - Create a release PR
3. **Manual**: Review and merge the release PR
4. **Automatic**: Cross-platform builds and GitHub release creation

## Changelog Sections

Your commits will automatically appear in these changelog sections:

- **Features** - `feat` commits
- **Bug Fixes** - `fix` commits
- **Performance Improvements** - `perf` commits
- **Reverts** - `revert` commits
- **Documentation** - `docs` commits
- **Styles** - `style` commits
- **Code Refactoring** - `refactor` commits
- **Tests** - `test` commits
- **Build System** - `build` commits
- **Continuous Integration** - `ci` commits

## Tips

1. **Be descriptive**: Write clear, concise commit messages
2. **Use imperative mood**: "Add feature" not "Added feature"
3. **Reference issues**: Include issue numbers when fixing bugs
4. **Group related changes**: Make atomic commits for related changes
5. **Test before committing**: Ensure your changes work and tests pass

## Example Workflow

```bash
# Work on a new feature
git checkout -b feature/patient-search

# Make changes and commit with conventional format
git add .
git commit -m "feat(patients): add advanced patient search functionality

- Implement search by name, ID, and date range
- Add search result pagination
- Include search filters for medical conditions"

# Push and create PR
git push origin feature/patient-search
```

When the feature is merged to main, Release Please will:
- Detect the `feat` commit
- Bump the minor version (e.g., 1.0.0 → 1.1.0)
- Add the feature to the changelog
- Create a release PR for review