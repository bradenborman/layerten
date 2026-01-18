@echo off
REM Set Railway PostgreSQL environment variables
REM Use the PUBLIC connection details (ballast.proxy.rlwy.net)

set PGHOST=ballast.proxy.rlwy.net
set PGPORT=23506
set PGDATABASE=railway
set PGUSER=postgres
set PGPASSWORD=HoSgelKxednixXrJrrVxnIDOGTuSbTXd

REM Run the migration test
gradlew :server:test --tests "com.layerten.migration.FlywayMigrationTest"
