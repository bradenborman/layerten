# Database Configuration

This document explains the database configuration for the LayerTen application, covering both local development and Railway deployment scenarios.

## Requirements

This configuration satisfies:
- **Requirement 12.3**: Backend reads database connection details from environment variables (DATABASE_URL or individual DB_* variables)
- **Requirement 13.1**: Backend reads DATABASE_URL from Railway's PostgreSQL plugin environment variable

## Configuration Overview

The `application.yml` file is configured to support two deployment scenarios:

### 1. Railway Deployment (Production)

Railway provides a `DATABASE_URL` environment variable when you add a PostgreSQL plugin. 

**Important Note about Railway's DATABASE_URL:**

Railway provides `DATABASE_URL` in the format:
```
postgresql://user:password@host:port/database
```

However, Spring Boot expects JDBC format:
```
jdbc:postgresql://host:port/database
```

**Solution:** Railway also provides individual connection variables that we can use:
- `PGHOST` - Database host
- `PGPORT` - Database port  
- `PGDATABASE` - Database name
- `PGUSER` - Database username
- `PGPASSWORD` - Database password

**Recommended Railway Configuration:**

Set these environment variables in your Railway service:
```bash
DB_HOST=${PGHOST}
DB_PORT=${PGPORT}
DB_NAME=${PGDATABASE}
DB_USER=${PGUSER}
DB_PASSWORD=${PGPASSWORD}
```

Alternatively, if you want to use `DATABASE_URL` directly, you can set:
```bash
DATABASE_URL=jdbc:${DATABASE_URL#postgresql:}
```

This converts Railway's format to JDBC format by replacing `postgresql:` with `jdbc:postgresql:`.

### 2. Local Development

For local development, the configuration falls back to individual environment variables:

```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:layerten}}
    username: ${DB_USER:layerten}
    password: ${DB_PASSWORD:layerten}
```

**Environment Variables (Optional):**
- `DB_HOST` - Database host (default: `localhost`)
- `DB_PORT` - Database port (default: `5432`)
- `DB_NAME` - Database name (default: `layerten`)
- `DB_USER` - Database username (default: `layerten`)
- `DB_PASSWORD` - Database password (default: `layerten`)

**Default Configuration:**
If no environment variables are set, the application connects to:
- Host: `localhost`
- Port: `5432`
- Database: `layerten`
- Username: `layerten`
- Password: `layerten`

This matches the PostgreSQL instance started by `docker-compose.yml`.

## Flyway Migrations

Flyway is configured to run database migrations automatically on application startup:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

**Configuration Details:**
- `enabled: true` - Flyway runs on startup
- `baseline-on-migrate: true` - Allows Flyway to work with existing databases
- `locations: classpath:db/migration` - Migration scripts location

**Migration Files:**
- Located in: `server/src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`
- Example: `V1__Create_core_tables.sql`

## Local Development Setup

### Using Docker Compose (Recommended)

1. Start PostgreSQL:
   ```bash
   docker-compose up -d
   ```

2. Run the application:
   ```bash
   cd server
   ./gradlew bootRun
   ```

The application will automatically:
- Connect to PostgreSQL at `localhost:5432`
- Use database `layerten` with credentials `layerten/layerten`
- Run Flyway migrations to create tables

### Using Custom Database

If you have your own PostgreSQL instance:

1. Set environment variables:
   ```bash
   export DB_HOST=your-host
   export DB_PORT=5432
   export DB_NAME=your-database
   export DB_USER=your-username
   export DB_PASSWORD=your-password
   ```

2. Run the application:
   ```bash
   cd server
   ./gradlew bootRun
   ```

## Railway Deployment Setup

### Option 1: Using Railway's PostgreSQL Variables (Recommended)

1. Create a new Railway project
2. Add a PostgreSQL plugin to your project
3. In your service settings, add these environment variables:
   ```
   DB_HOST=${PGHOST}
   DB_PORT=${PGPORT}
   DB_NAME=${PGDATABASE}
   DB_USER=${PGUSER}
   DB_PASSWORD=${PGPASSWORD}
   ```
4. Deploy your application

### Option 2: Using DATABASE_URL Directly

1. Create a new Railway project
2. Add a PostgreSQL plugin to your project
3. In your service settings, add this environment variable:
   ```
   DATABASE_URL=jdbc:${DATABASE_URL#postgresql:}
   ```
   This converts Railway's `postgresql://` format to Spring Boot's `jdbc:postgresql://` format.
4. Deploy your application

**Note:** The username and password are embedded in the URL, so separate `DB_USER` and `DB_PASSWORD` variables are not needed with this approach.

## Verification

### Check Database Connection

When the application starts, you should see logs indicating:
```
Flyway Community Edition ... by Redgate
Database: jdbc:postgresql://...
Successfully validated 1 migration
Creating Schema History table ...
Current version of schema "public": << Empty Schema >>
Migrating schema "public" to version "1 - Create core tables"
Successfully applied 1 migration
```

### Test Database Configuration

Run the configuration test:
```bash
cd server
./gradlew test --tests DatabaseConfigurationTest
```

## Troubleshooting

### Connection Refused

**Problem:** `Connection refused` error when starting the application

**Solution:**
- Ensure PostgreSQL is running: `docker-compose ps`
- Check the database is accessible: `psql -h localhost -U layerten -d layerten`
- Verify environment variables are set correctly

### Flyway Migration Errors

**Problem:** Flyway fails to run migrations

**Solution:**
- Check migration files are in `server/src/main/resources/db/migration/`
- Verify migration file naming follows `V{version}__{description}.sql` pattern
- Check database user has CREATE TABLE permissions
- Review Flyway logs for specific error messages

### Railway Deployment Issues

**Problem:** Application can't connect to Railway database

**Solution:**
- Verify PostgreSQL plugin is added to your Railway project
- Check that you've set up the environment variables correctly (see Railway Deployment Setup above)
- Ensure the application is using the correct Railway service
- Review Railway deployment logs for connection errors
- Verify the `DATABASE_URL` format conversion is correct

### Railway DATABASE_URL Format Issues

**Problem:** Error like "No suitable driver found for postgresql://..."

**Solution:**
This means the `DATABASE_URL` is in Railway's format (`postgresql://`) instead of JDBC format (`jdbc:postgresql://`).

Use one of these solutions:
1. Use Railway's individual variables (`PGHOST`, `PGPORT`, etc.) mapped to `DB_*` variables
2. Convert the URL format: `DATABASE_URL=jdbc:${DATABASE_URL#postgresql:}`

## Environment Variable Priority

The configuration uses the following priority (highest to lowest):

1. `DATABASE_URL` - If set, this is used for the connection URL
2. Individual variables (`DB_HOST`, `DB_PORT`, `DB_NAME`) - Used to construct the URL if `DATABASE_URL` is not set
3. Default values (for local development) - Used if no environment variables are set

For username and password:
1. `DB_USER` and `DB_PASSWORD` - Explicit credentials
2. Default values (`layerten`/`layerten`) - For local development

This ensures seamless operation in both Railway (production) and local development environments.

## Summary

**For Local Development:**
- No configuration needed! Just run `docker-compose up -d` and start the application.

**For Railway Deployment:**
- Add PostgreSQL plugin
- Set environment variables to map Railway's `PG*` variables to `DB_*` variables
- Deploy and the application will automatically connect and run migrations

The configuration is designed to work out-of-the-box in both environments with minimal setup.
