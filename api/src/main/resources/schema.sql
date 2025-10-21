-- ============================================
-- Database: healthgoal
-- Schema: public
-- ============================================

-- Enable UUID extension (needed for UUID primary keys)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- ============================================
-- HEALTH GOALS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS health_goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    title VARCHAR(255),
    description TEXT,
    target INTEGER,
    unit VARCHAR(50),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),

    CONSTRAINT fk_health_goal_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- ============================================
-- INDEXES
-- ============================================

-- Faster lookups for user's goals
CREATE INDEX IF NOT EXISTS idx_health_goals_user_id
    ON health_goals(user_id);

-- Optional index on status for filtering
CREATE INDEX IF NOT EXISTS idx_health_goals_status
    ON health_goals(status);

-- ============================================
-- ENUM-LIKE CONSTRAINTS (for status)
-- ============================================

ALTER TABLE health_goals
    ADD CONSTRAINT chk_health_goal_status
    CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED'));
