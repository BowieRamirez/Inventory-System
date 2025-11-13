MySQL Setup and Migration

1) Create a database (example: merch_system)

USE your MySQL client to create DB:

CREATE DATABASE merch_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

2) Configure credentials

Option A: Environment variables (recommended)
- DB_URL=jdbc:mysql://localhost:3306/merch_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
- DB_USER=<your_user>
- DB_PASSWORD=<your_password>

Option B: Properties file
- Edit src/database/data/db.properties and set:
  url=jdbc:mysql://localhost:3306/merch_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  user=<your_user>
  password=<your_password>

3) Run migration (imports all existing TXT data)
- From project root:

mvn -q -DskipTests clean compile
mvn -q exec:java

The tool will:
- Create tables if missing
- Import students, staff, items, reservations, receipts, stock_logs, and system_config

4) Switch runtime to MySQL for signup
- When DB_* is set, the app uses MySQL for student signup (read/write).
- Other modules continue to read TXT until you migrate those controllers/DAOs.

Notes
- You can run the migration multiple times; it uses upsert where possible.
- To revert to TXT-only mode, unset the DB_* environment variables.
