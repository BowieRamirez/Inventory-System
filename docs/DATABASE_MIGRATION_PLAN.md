# 🗄️ Database Migration Plan - STI ProWear System

**Document Version:** 1.0  
**Date:** October 30, 2024  
**Project:** STI ProWear Inventory Management System  
**Current Version:** 2.0.0 (File-based storage)  
**Target:** Relational Database Migration

---

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Database Options Evaluation](#database-options-evaluation)
4. [Recommended Solution](#recommended-solution)
5. [Database Schema Design](#database-schema-design)
6. [Migration Strategy](#migration-strategy)
7. [Implementation Roadmap](#implementation-roadmap)
8. [Risk Assessment](#risk-assessment)
9. [Testing Strategy](#testing-strategy)
10. [Rollback Plan](#rollback-plan)

---

## 📊 Executive Summary

### Current Situation
The STI ProWear System currently uses **file-based storage** (CSV/TXT files) for data persistence:
- `items.txt` - Inventory items (242 records)
- `students.txt` - Student accounts (3 records)
- `reservations.txt` - Reservation data (2 records)
- `receipts.txt` - Receipt records
- `stock_logs.txt` - Stock audit trail

### Why Migrate?
**Critical Issues with Current Approach:**
- ❌ **No ACID Compliance** - Risk of data corruption during concurrent access
- ❌ **No Referential Integrity** - No foreign key constraints
- ❌ **Limited Query Capabilities** - Manual parsing required for complex queries
- ❌ **Poor Scalability** - Performance degrades with data growth
- ❌ **No Transaction Support** - Cannot rollback failed operations
- ❌ **Difficult Backup/Recovery** - Manual file management
- ❌ **No Concurrent Access Control** - Race conditions possible

### Recommended Solution
**PostgreSQL** with H2 as development/testing database

**Key Benefits:**
- ✅ Production-grade reliability
- ✅ Full ACID compliance
- ✅ Excellent Java 21 support
- ✅ Easy local development (H2)
- ✅ Seamless cloud deployment
- ✅ Strong community support
- ✅ Free and open-source

---

## 🔍 Current State Analysis

### Technology Stack
- **Language:** Java 21 (LTS)
- **Framework:** JavaFX 21.0.1
- **Build Tool:** Maven 3.9.5+
- **Current Storage:** File-based (CSV/TXT)
- **Data Access:** `FileStorage.java` utility class

### Current Data Model

#### 1. **Items** (`items.txt`)
```csv
ItemCode,ItemName,Course,Size,Quantity,Price
1001,IT/Eng Gray 3/4 Polo (Male),BSIT,S,47,450.00
```
**Fields:** ItemCode, ItemName, Course, Size, Quantity, Price

#### 2. **Students** (`students.txt`)
```csv
StudentID|Password|Course|FirstName|LastName|Gender|IsActive
02000284710|Ramirez123|BSIT|Bowie|Ramirez|Male|true
```
**Fields:** StudentID, Password, Course, FirstName, LastName, Gender, IsActive

#### 3. **Reservations** (`reservations.txt`)
```csv
ReservationID|StudentName|StudentID|Course|ItemCode|ItemName|Quantity|Price|Size|Status|IsPaid|PaymentMethod|CreatedAt|UpdatedAt|Notes
5001|Ramirez, Bowie|02000284710|BSIT|1001|IT/Eng Gray 3/4 Polo (Male)|1|450.0|XL|RETURNED - REFUNDED|true|CASH|2025-10-30 20:31:17|2025-10-30 20:33:04|Item returned within 10 days
```

#### 4. **Receipts** (`receipts.txt`)
Receipt generation data

#### 5. **Stock Logs** (`stock_logs.txt`)
Audit trail for inventory changes

### Current File Access Patterns
- **Read Operations:** Load entire file into memory
- **Write Operations:** Overwrite entire file
- **Search:** Linear scan through ArrayList
- **Concurrency:** None (potential data loss)

---

## 🔬 Database Options Evaluation

### Option 1: **PostgreSQL** ⭐ RECOMMENDED

#### Compatibility with Java 21
- ✅ **Fully Compatible** - PostgreSQL JDBC Driver supports Java 8+
- ✅ **Latest Driver:** `postgresql-42.7.4.jar` (October 2024)
- ✅ **Maven Dependency:**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.4</version>
</dependency>
```

#### Pros
- ✅ **Production-Ready** - Battle-tested, enterprise-grade
- ✅ **ACID Compliant** - Full transaction support
- ✅ **Rich Feature Set** - JSON support, full-text search, triggers
- ✅ **Excellent Performance** - Handles millions of records
- ✅ **Strong Community** - Extensive documentation and support
- ✅ **Cloud-Ready** - Easy deployment to AWS RDS, Azure, GCP, Heroku
- ✅ **Free & Open Source** - No licensing costs
- ✅ **Scalable** - Vertical and horizontal scaling options

#### Cons
- ⚠️ Requires separate server installation
- ⚠️ More complex setup than embedded databases
- ⚠️ Overkill for very small datasets (< 1000 records)

#### Setup Complexity: **Medium**
- Local: Install PostgreSQL server (5 minutes)
- Cloud: Use managed service (Supabase, AWS RDS, etc.)

---

### Option 2: **H2 Database** ⭐ RECOMMENDED FOR DEVELOPMENT

#### Compatibility with Java 21
- ✅ **Fully Compatible** - Requires Java 11+
- ✅ **Latest Version:** `2.3.232` (October 2024)
- ✅ **Maven Dependency:**
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
</dependency>
```

#### Pros
- ✅ **Zero Configuration** - Embedded, no server needed
- ✅ **Lightweight** - Small footprint (~2.5 MB)
- ✅ **Fast** - Excellent performance for small-medium datasets
- ✅ **PostgreSQL Compatibility Mode** - Easy migration path
- ✅ **Built-in Web Console** - Database management UI
- ✅ **Perfect for Development** - Quick setup, easy testing
- ✅ **In-Memory Mode** - Ultra-fast testing

#### Cons
- ⚠️ Not recommended for production (single-user focus)
- ⚠️ Limited concurrent access
- ⚠️ Smaller community than PostgreSQL

#### Setup Complexity: **Very Easy**
- Add Maven dependency → Done!

---

### Option 3: **MongoDB**

#### Compatibility with Java 21
- ✅ **Fully Compatible** - MongoDB Java Driver 4.11+ supports Java 21
- ✅ **Virtual Threads Support** - Optimized for Java 21 features
- ✅ **Maven Dependency:**
```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>5.2.1</version>
</dependency>
```

#### Pros
- ✅ **Schema Flexibility** - NoSQL, dynamic schemas
- ✅ **Horizontal Scaling** - Excellent for massive datasets
- ✅ **Cloud-Ready** - MongoDB Atlas (managed service)
- ✅ **JSON-Native** - Natural fit for modern apps

#### Cons
- ❌ **Overkill for This Project** - Structured data fits relational model better
- ❌ **No ACID Transactions** (in older versions)
- ❌ **Learning Curve** - Different query paradigm
- ❌ **More Complex** - Requires understanding of NoSQL concepts
- ❌ **Not Ideal for Inventory** - Relational data (items, students, reservations)

#### Verdict: **Not Recommended** for this use case

---

### Option 4: **Supabase**

#### Compatibility with Java 21
- ⚠️ **Limited Java Support** - No official Java SDK
- ⚠️ **Community Library:** `supabase-java` (unmaintained, last update 2022)
- ⚠️ **Workaround:** Use REST API directly (not ideal)

#### Pros
- ✅ **Backend-as-a-Service** - Hosted PostgreSQL + Auth + Storage
- ✅ **Free Tier** - Good for small projects
- ✅ **Real-time Subscriptions** - WebSocket support

#### Cons
- ❌ **No Official Java SDK** - Poor Java ecosystem support
- ❌ **REST API Only** - No native JDBC driver
- ❌ **Vendor Lock-in** - Tied to Supabase platform
- ❌ **Unnecessary Complexity** - We don't need auth/storage features

#### Verdict: **Not Recommended** - Use PostgreSQL directly instead

---

### Option 5: **PocketBase**

#### Compatibility with Java 21
- ❌ **No Official Java SDK** - Go-based backend
- ⚠️ **Community Library:** `pocketbase4j` (experimental, not production-ready)
- ⚠️ **REST API Only** - No native database access

#### Pros
- ✅ **All-in-One** - Database + Auth + File Storage
- ✅ **Single Binary** - Easy deployment
- ✅ **Built-in Admin UI**

#### Cons
- ❌ **Not Stable** - Pre-1.0 version (no backward compatibility guarantee)
- ❌ **Poor Java Support** - Designed for JavaScript/Dart
- ❌ **REST API Overhead** - Slower than direct database access
- ❌ **Limited Ecosystem** - Small community

#### Verdict: **Not Recommended** for Java projects

---

## 🏆 Recommended Solution

### **Dual-Database Strategy**

#### Development & Testing: **H2 Database**
- Embedded, zero-config setup
- Fast local development
- In-memory mode for unit tests
- PostgreSQL compatibility mode

#### Production: **PostgreSQL**
- Enterprise-grade reliability
- Cloud deployment ready
- Excellent performance
- Strong ecosystem

### Why This Approach?
1. **Easy Development** - Developers can run the app without installing PostgreSQL
2. **Fast Testing** - H2 in-memory mode for lightning-fast tests
3. **Production-Ready** - PostgreSQL for real deployments
4. **Seamless Migration** - H2's PostgreSQL mode ensures compatibility
5. **Cost-Effective** - Free for both development and production

---

## 🗂️ Database Schema Design

### Entity Relationship Diagram (ERD)

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│    STUDENTS     │         │   RESERVATIONS   │         │      ITEMS      │
├─────────────────┤         ├──────────────────┤         ├─────────────────┤
│ student_id (PK) │────┐    │ reservation_id   │    ┌────│ item_id (PK)    │
│ password        │    │    │   (PK)           │    │    │ item_code       │
│ first_name      │    └───<│ student_id (FK)  │    │    │ item_name       │
│ last_name       │         │ item_id (FK)     │>───┘    │ course          │
│ course          │         │ quantity         │         │ size            │
│ gender          │         │ total_price      │         │ quantity        │
│ is_active       │         │ status           │         │ price           │
│ created_at      │         │ is_paid          │         │ created_at      │
│ updated_at      │         │ payment_method   │         │ updated_at      │
└─────────────────┘         │ created_at       │         └─────────────────┘
                            │ updated_at       │
                            │ notes            │
                            └──────────────────┘
                                     │
                                     │
                                     ▼
                            ┌──────────────────┐
                            │     RECEIPTS     │
                            ├──────────────────┤
                            │ receipt_id (PK)  │
                            │ reservation_id   │
                            │   (FK)           │
                            │ student_name     │
                            │ items_summary    │
                            │ total_amount     │
                            │ payment_method   │
                            │ issued_at        │
                            └──────────────────┘

                            ┌──────────────────┐
                            │   STOCK_LOGS     │
                            ├──────────────────┤
                            │ log_id (PK)      │
                            │ item_id (FK)     │
                            │ action_type      │
                            │ quantity_change  │
                            │ performed_by     │
                            │ timestamp        │
                            │ notes            │
                            └──────────────────┘
```

### SQL Schema (PostgreSQL/H2 Compatible)

```sql
-- Table: students
CREATE TABLE students (
    student_id VARCHAR(20) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    course VARCHAR(50) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: items
CREATE TABLE items (
    item_id SERIAL PRIMARY KEY,
    item_code INTEGER NOT NULL,
    item_name VARCHAR(200) NOT NULL,
    course VARCHAR(50) NOT NULL,
    size VARCHAR(20) NOT NULL CHECK (size IN ('XS', 'S', 'M', 'L', 'XL', 'XXL', 'One Size')),
    quantity INTEGER NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (item_code, size)
);

-- Table: reservations
CREATE TABLE reservations (
    reservation_id SERIAL PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL REFERENCES students(student_id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL REFERENCES items(item_id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_paid BOOLEAN DEFAULT FALSE,
    payment_method VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Table: receipts
CREATE TABLE receipts (
    receipt_id INTEGER PRIMARY KEY,
    reservation_id INTEGER NOT NULL REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    student_name VARCHAR(200) NOT NULL,
    items_summary TEXT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: stock_logs
CREATE TABLE stock_logs (
    log_id SERIAL PRIMARY KEY,
    item_id INTEGER NOT NULL REFERENCES items(item_id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL,
    quantity_change INTEGER NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

-- Indexes for performance
CREATE INDEX idx_items_course ON items(course);
CREATE INDEX idx_items_code ON items(item_code);
CREATE INDEX idx_reservations_student ON reservations(student_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_stock_logs_item ON stock_logs(item_id);
CREATE INDEX idx_stock_logs_timestamp ON stock_logs(timestamp);
```

---

## 🚀 Migration Strategy

### Phase 1: Setup & Configuration (Week 1)

#### Step 1.1: Add Database Dependencies
Update `pom.xml`:
```xml
<!-- H2 Database (Development) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.3.232</version>
</dependency>

<!-- PostgreSQL JDBC Driver (Production) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.4</version>
</dependency>

<!-- HikariCP Connection Pool -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

#### Step 1.2: Create Database Configuration
Create `src/database/DatabaseConfig.java`:
- Database connection settings
- Environment-based configuration (dev/prod)
- Connection pooling setup

#### Step 1.3: Create Database Manager
Create `src/database/DatabaseManager.java`:
- Connection management
- Transaction support
- Query execution utilities

### Phase 2: Data Access Layer (Week 2)

#### Step 2.1: Create DAO Interfaces
- `StudentDAO.java`
- `ItemDAO.java`
- `ReservationDAO.java`
- `ReceiptDAO.java`
- `StockLogDAO.java`

#### Step 2.2: Implement DAO Classes
- `StudentDAOImpl.java`
- `ItemDAOImpl.java`
- `ReservationDAOImpl.java`
- `ReceiptDAOImpl.java`
- `StockLogDAOImpl.java`

### Phase 3: Data Migration (Week 3)

#### Step 3.1: Create Migration Scripts
- `V1__create_tables.sql`
- `V2__create_indexes.sql`
- `V3__migrate_data.sql`

#### Step 3.2: Build Data Migration Tool
Create `src/database/DataMigrationTool.java`:
- Read existing TXT files
- Parse and validate data
- Insert into database
- Verify data integrity

### Phase 4: Refactor Business Logic (Week 4)

#### Step 4.1: Update Manager Classes
- Refactor `InventoryManager.java` to use `ItemDAO`
- Refactor `ReservationManager.java` to use `ReservationDAO`
- Refactor `ReceiptManager.java` to use `ReceiptDAO`

#### Step 4.2: Remove File Storage Dependencies
- Keep `FileStorage.java` as backup/export utility
- Update all references to use database

### Phase 5: Testing & Validation (Week 5)

#### Step 5.1: Unit Tests
- Test all DAO methods
- Test transaction handling
- Test error scenarios

#### Step 5.2: Integration Tests
- Test complete workflows
- Test concurrent access
- Performance testing

#### Step 5.3: User Acceptance Testing
- Test all GUI features
- Verify data accuracy
- Test backup/restore

---

## ⚠️ Risk Assessment

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Data loss during migration | **High** | Low | Backup all files before migration |
| Performance degradation | Medium | Low | Use connection pooling, indexes |
| Compatibility issues | Medium | Low | Use H2 PostgreSQL mode for testing |
| Learning curve | Low | Medium | Provide training, documentation |
| Deployment complexity | Medium | Medium | Use Docker, managed services |

---

## ✅ Testing Strategy

### 1. Unit Tests
- Test each DAO method independently
- Mock database connections
- Test edge cases and error handling

### 2. Integration Tests
- Test complete CRUD operations
- Test transaction rollback
- Test concurrent access scenarios

### 3. Performance Tests
- Benchmark query performance
- Test with large datasets (10,000+ records)
- Compare with file-based performance

### 4. Migration Tests
- Test data migration accuracy
- Verify all records migrated correctly
- Test rollback procedures

---

## 🔄 Rollback Plan

### If Migration Fails:
1. **Keep Original Files** - Never delete `src/database/data/*.txt`
2. **Version Control** - Commit before migration
3. **Database Backup** - Export database before changes
4. **Feature Flag** - Toggle between file/database storage
5. **Gradual Rollout** - Test with subset of users first

### Rollback Steps:
```bash
# 1. Stop application
# 2. Restore from backup
git checkout HEAD~1

# 3. Restore database files
cp backup/database/data/* src/database/data/

# 4. Restart application with file storage
mvn javafx:run
```

---

## 📅 Implementation Timeline

| Phase | Duration | Tasks | Deliverables |
|-------|----------|-------|--------------|
| **Phase 1** | 1 week | Setup & Config | Database dependencies, config files |
| **Phase 2** | 1 week | DAO Layer | DAO interfaces and implementations |
| **Phase 3** | 1 week | Migration | Migration scripts, data transfer |
| **Phase 4** | 1 week | Refactoring | Updated business logic |
| **Phase 5** | 1 week | Testing | Test suite, documentation |
| **Total** | **5 weeks** | | Production-ready database system |

---

## 🎯 Success Criteria

- ✅ All existing features work with database
- ✅ No data loss during migration
- ✅ Performance equal or better than file storage
- ✅ All tests passing (unit, integration, E2E)
- ✅ Documentation complete
- ✅ Backup/restore procedures tested
- ✅ Team trained on new system

---

## 📚 Next Steps

1. **Review this plan** with the development team
2. **Get approval** from stakeholders
3. **Set up development environment** (PostgreSQL + H2)
4. **Create feature branch** (`feature/database-migration`)
5. **Begin Phase 1** implementation

---

## 📞 Support & Resources

### Documentation
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/documentation/)
- [H2 Database Documentation](http://h2database.com/html/main.html)
- [HikariCP Connection Pool](https://github.com/brettwooldridge/HikariCP)

### Community
- [PostgreSQL Mailing Lists](https://www.postgresql.org/list/)
- [Stack Overflow - PostgreSQL](https://stackoverflow.com/questions/tagged/postgresql)
- [Stack Overflow - H2](https://stackoverflow.com/questions/tagged/h2)

---

**Document Prepared By:** AI Assistant  
**Last Updated:** October 30, 2024  
**Status:** Draft - Awaiting Review

