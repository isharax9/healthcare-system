# Environment Configuration Guide

## Database Configuration Methods

The GlobeMed Healthcare System supports multiple ways to configure database connections for different deployment scenarios.

### Method 1: Environment Variables (Recommended for Production)

**Linux/macOS:**
```bash
export DB_URL="jdbc:mysql://your-server:3306/globemed_db"
export DB_USERNAME="your_username"
export DB_PASSWORD="your_secure_password"

# Run the application
java -jar healthcare-system-1.0-SNAPSHOT-executable.jar
```

**Windows Command Prompt:**
```cmd
set DB_URL=jdbc:mysql://your-server:3306/globemed_db
set DB_USERNAME=your_username
set DB_PASSWORD=your_secure_password

java -jar healthcare-system-1.0-SNAPSHOT-executable.jar
```

**Windows PowerShell:**
```powershell
$env:DB_URL="jdbc:mysql://your-server:3306/globemed_db"
$env:DB_USERNAME="your_username"
$env:DB_PASSWORD="your_secure_password"

java -jar healthcare-system-1.0-SNAPSHOT-executable.jar
```

### Method 2: System Properties

```bash
java -jar healthcare-system-1.0-SNAPSHOT-executable.jar \
     -Ddb.url="jdbc:mysql://your-server:3306/globemed_db" \
     -Ddb.username="your_username" \
     -Ddb.password="your_secure_password"
```

### Method 3: Using Launcher Scripts

**Linux/macOS:**
```bash
./run-globemed.sh
# Script will prompt for database configuration if not set
```

**Windows:**
```cmd
run-globemed.bat
# Script will prompt for database configuration if not set
```

### Method 4: Docker Deployment

```dockerfile
FROM openjdk:17-jre-slim

COPY healthcare-system-1.0-SNAPSHOT-executable.jar /app/app.jar

# Database configuration via environment variables
ENV DB_URL="jdbc:mysql://db:3306/globemed_db"
ENV DB_USERNAME="globemed_user"
ENV DB_PASSWORD="secure_password"

WORKDIR /app
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

### Method 5: Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: globemed-healthcare
spec:
  replicas: 3
  selector:
    matchLabels:
      app: globemed-healthcare
  template:
    metadata:
      labels:
        app: globemed-healthcare
    spec:
      containers:
      - name: healthcare-app
        image: globemed/healthcare-system:latest
        env:
        - name: DB_URL
          value: "jdbc:mysql://mysql-service:3306/globemed_db"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        ports:
        - containerPort: 8080
```

## Configuration Priority

The application uses the following priority order:

1. **Environment Variables** (highest priority)
   - `DB_URL`
   - `DB_USERNAME` 
   - `DB_PASSWORD`

2. **System Properties**
   - `db.url`
   - `db.username`
   - `db.password`

3. **Application Properties File** (lowest priority)
   - `src/main/resources/application.properties`

## Security Best Practices

1. **Never hardcode credentials** in source code
2. **Use environment variables** in production
3. **Use secrets management** in cloud deployments
4. **Restrict file permissions** on configuration files
5. **Use encrypted connections** (SSL/TLS) for database connections

## Connection URL Examples

### Local MySQL
```
jdbc:mysql://localhost:3306/globemed_db
```

### Remote MySQL with SSL
```
jdbc:mysql://db.example.com:3306/globemed_db?useSSL=true&verifyServerCertificate=true
```

### MySQL with Connection Pool Settings
```
jdbc:mysql://localhost:3306/globemed_db?useConnectionPooling=true&maxPoolSize=20
```

### Cloud Database (Amazon RDS)
```
jdbc:mysql://globemed-db.cluster-xyz.us-east-1.rds.amazonaws.com:3306/globemed_db?useSSL=true
```

## Troubleshooting

### Connection Issues
```bash
# Test database connectivity
java -cp healthcare-system-1.0-SNAPSHOT-executable.jar \
     com.globemed.db.DatabaseManager
```

### Debug Mode
```bash
# Enable debug logging
java -Djava.util.logging.level=FINE \
     -jar healthcare-system-1.0-SNAPSHOT-executable.jar
```

### Common Error Solutions

**"Communications link failure"**
- Check if MySQL server is running
- Verify hostname/port are correct
- Check firewall settings

**"Access denied for user"**
- Verify username and password
- Check user permissions in MySQL
- Ensure user can connect from the application host

**"Unknown database"**
- Verify database name is correct
- Create the database if it doesn't exist
- Import the schema using provided SQL files