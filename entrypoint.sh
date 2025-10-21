#!/bin/sh
set -e

echo "waiting for postgreSQL to be ready"

# Wait for Postgres to accept connections
until nc -z postgres 5432; do
  echo "postgres is unavailable - sleeping"
  sleep 2
done

echo "postgres is up and running!"

# Enable uuid-ossp extension if not already enabled
echo "enabling uuid-ossp extension..."
PGPASSWORD=$SPRING_DATASOURCE_PASSWORD psql -h postgres -U $SPRING_DATASOURCE_USERNAME -d healthgoal -c 'CREATE EXTENSION IF NOT EXISTS "uuid-ossp";' || true

echo "🚀 Starting HealthGoal API..."
exec java -jar /app/healthgoal-api.jar
