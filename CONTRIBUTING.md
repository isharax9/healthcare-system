# Contributing to GlobeMed Healthcare Management System

Welcome to the GlobeMed Healthcare Management System! We appreciate your interest in contributing to this project. This guide will help you get started with development and contribution.

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- **Java Development Kit (JDK) 17+** - [Download here](https://adoptium.net/)
- **Apache Maven 3.9+** - [Download here](https://maven.apache.org/download.cgi)
- **MySQL Server 8.0+** - [Download here](https://dev.mysql.com/downloads/mysql/)
- **Git** - [Download here](https://git-scm.com/downloads)
- **IDE** (recommended: IntelliJ IDEA, Eclipse, or VS Code)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/isharax9/healthcare-system.git
   cd healthcare-system
   ```

2. **Set up the database**
   ```bash
   # Create the database
   mysql -u root -p -e "CREATE DATABASE globemed_db;"
   
   # Import the database schema
   mysql -u root -p globemed_db < globemed_db.sql
   ```

3. **Configure database connection**
   
   You can configure the database connection in several ways:

   **Option A: Environment Variables (Recommended)**
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/globemed_db"
   export DB_USERNAME="your_username"
   export DB_PASSWORD="your_password"
   ```

   **Option B: System Properties**
   ```bash
   mvn exec:java -Ddb.url="jdbc:mysql://localhost:3306/globemed_db" \
                 -Ddb.username="your_username" \
                 -Ddb.password="your_password"
   ```

   **Option C: Edit application.properties**
   Modify `src/main/resources/application.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/globemed_db
   db.username=your_username
   db.password=your_password
   ```

4. **Build and run the application**
   ```bash
   # Clean and compile
   mvn clean compile
   
   # Run tests
   mvn test
   
   # Run the application
   mvn exec:java -Dexec.mainClass="com.globemed.Main"
   ```

## 🛠️ Development Workflow

### Branching Strategy

- `main` - Production-ready code
- `develop` - Development branch for integration
- `feature/feature-name` - Feature development branches
- `bugfix/bug-description` - Bug fix branches
- `hotfix/urgent-fix` - Critical production fixes

### Making Changes

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Follow the coding standards (see below)
   - Write or update tests
   - Update documentation if needed

3. **Test your changes**
   ```bash
   # Run all tests
   mvn test
   
   # Run integration tests
   mvn verify -Pintegration-tests
   
   # Check code coverage
   mvn jacoco:report
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add new feature description"
   ```
   
   > 💡 **Tip**: Use [Conventional Commits](CONVENTIONAL_COMMITS.md) format for automatic releases and changelog generation.

5. **Push and create a pull request**
   ```bash
   git push origin feature/your-feature-name
   ```
   Then create a pull request through GitHub.

## 📝 Coding Standards

### Java Code Style

- **Java 17 features**: Use modern Java features like records, pattern matching, and enhanced switch expressions
- **Naming conventions**: Follow standard Java naming conventions
- **JavaDoc**: Add comprehensive JavaDoc comments for public methods and classes
- **Design patterns**: Utilize appropriate design patterns (already implemented: Decorator, Mediator, Chain of Responsibility, etc.)

### Code Example

```java
/**
 * Example service class demonstrating coding standards.
 * 
 * @author Your Name
 * @version 1.0
 * @since 1.0
 */
public class ExampleService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleService.class);
    
    /**
     * Processes patient data with validation.
     * 
     * @param patientData the patient data to process
     * @return processed result
     * @throws ValidationException if data is invalid
     */
    public ProcessResult processPatientData(PatientData patientData) {
        // Implementation here
    }
}
```

### Database Access

- Use the `DatabaseManager` class for all database connections
- Follow DAO pattern for data access
- Use proper exception handling
- Close resources properly (try-with-resources)

```java
public class PatientDAO {
    public List<Patient> findAll() throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM patients");
             ResultSet rs = stmt.executeQuery()) {
            
            // Process results
            return patients;
        }
    }
}
```

## 🧪 Testing Guidelines

### Unit Tests

- Write unit tests for all new functionality
- Use JUnit 5 for testing framework
- Mock external dependencies
- Aim for at least 80% code coverage

```java
@Test
void shouldCreatePatientSuccessfully() {
    // Given
    PatientData patientData = new PatientData("John", "Doe", "123-456-7890");
    
    // When
    Patient result = patientService.createPatient(patientData);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getFirstName()).isEqualTo("John");
}
```

### Integration Tests

- Test database interactions
- Test UI components
- Use test database for integration tests

## 🏗️ Architecture Guidelines

### Design Patterns Used

The system implements several design patterns:

- **Decorator Pattern**: Role-based permissions and user decorations
- **Mediator Pattern**: Appointment scheduling and conflict resolution
- **Chain of Responsibility**: Insurance claim processing
- **DAO Pattern**: Data access layer
- **Singleton Pattern**: Database connection management
- **Factory Pattern**: Object creation

### Adding New Features

When adding new features:

1. Follow existing architectural patterns
2. Create appropriate DAOs for data access
3. Implement proper validation
4. Add comprehensive logging
5. Write tests for new functionality
6. Update documentation

## 🐛 Bug Reports

When reporting bugs, please include:

- **Clear description** of the issue
- **Steps to reproduce** the problem
- **Expected vs actual behavior**
- **Environment details** (OS, Java version, MySQL version)
- **Screenshots** if applicable
- **Error logs** if relevant

## 🚀 Building and Deployment

### Local Development Build

```bash
# Standard build
mvn clean package

# Skip tests (for faster builds)
mvn clean package -DskipTests

# Create executable JAR
mvn clean package
# This creates: target/healthcare-system-1.0.0-executable.jar
```

### Cross-Platform Builds

The CI/CD pipeline automatically builds for multiple platforms:

- **Windows**: `.exe` installer
- **macOS**: `.dmg` installer and `.app` bundle
- **Linux**: `.deb`, `.rpm` packages and portable scripts

To trigger a release build, create a tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

> **Note**: The automated Release Please system has been temporarily disabled due to pipeline issues. Currently, only manual tag-based releases are supported.

### 🎯 Automated Release System (Temporarily Disabled)

> **Important**: The automated Release Please system has been temporarily disabled due to pipeline issues. 
> The information below is kept for reference and future re-enablement.

The project previously used **Release Please** for automated version management:

#### How It Worked
1. **Write commits** using [Conventional Commits](CONVENTIONAL_COMMITS.md) format
2. **Push to main** - Release Please analyzes your commits
3. **Review release PR** - Automatically created with version bump and changelog
4. **Merge release PR** - Triggers cross-platform builds and GitHub release

#### Version Bumping Rules (For Reference)
- `feat:` commits → **Minor** version bump (1.0.0 → 1.1.0)
- `fix:` commits → **Patch** version bump (1.0.0 → 1.0.1)
- `feat!:` or `BREAKING CHANGE:` → **Major** version bump (1.0.0 → 2.0.0)

#### To Re-enable (Future)
If you need to add automated releases:
1. Create a new `.github/workflows/release.yml` file with Release Please action
2. Configure push trigger for main branch
3. Test thoroughly before deployment

#### Changelog Generation (For Reference)
- **Features** - New functionality added
- **Bug Fixes** - Issues resolved
- **Performance Improvements** - Optimizations made
- **Documentation** - Docs updates
- **Code Refactoring** - Code improvements

#### Previous Release Flow Example
```bash
# This process is currently disabled
# Feature development
git commit -m "feat(patients): add advanced search with filters"
git commit -m "fix(database): resolve connection timeout issues"
git commit -m "docs: update API documentation"

# Push to main
git push origin main

# Release Please would create PR with:
# - Version: 1.0.0 → 1.1.0 (due to feat commit)
# - Changelog with all changes categorized
# - Updated version in pom.xml

# After merging release PR:
# - GitHub release created with tag v1.1.0
# - Cross-platform builds triggered
# - Artifacts uploaded to release
```

## 🔒 Security Considerations

- Never commit sensitive data (passwords, API keys)
- Use environment variables for configuration
- Validate all user inputs
- Follow SQL injection prevention practices
- Implement proper authentication and authorization

## 📋 Environment Variables Reference

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database connection URL | `jdbc:mysql://localhost:3306/globemed_db` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `NewPassword123!` |

## 📧 Communication

- **Issues**: Use GitHub Issues for bug reports and feature requests
- **Discussions**: Use GitHub Discussions for questions and ideas
- **Email**: Contact the maintainers at [project email]

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## 🙏 Acknowledgments

- Thanks to all contributors who help improve this project
- Special thanks to the healthcare professionals who provide domain expertise
- Built with love using Java, MySQL, and modern software engineering practices

---

**Happy Contributing! 🎉**

For any questions about contributing, feel free to open an issue or start a discussion.