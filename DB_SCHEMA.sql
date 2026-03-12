-- ================================================================
--  MediNear v2 – PostgreSQL Schema
--
--  Run this ONCE in pgAdmin before starting Spring Boot.
--  Spring Boot (ddl-auto=update) will auto-create/update tables,
--  but this script adds the performance indexes Hibernate cannot
--  create (e.g. GIN for full-text, partial indexes).
-- ================================================================

-- 1. Create database (run as superuser)
-- CREATE DATABASE "MedNear";
-- \c MedNear

-- ── Core tables (Hibernate creates these, shown for reference) ──

CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(120) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS stores (
    store_id   BIGSERIAL    PRIMARY KEY,
    store_name VARCHAR(200) NOT NULL,
    owner_id   BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address    VARCHAR(500) NOT NULL,
    latitude   DOUBLE PRECISION NOT NULL,
    longitude  DOUBLE PRECISION NOT NULL,
    phone      VARCHAR(20)  NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS medicines (
    medicine_id           BIGSERIAL    PRIMARY KEY,
    medicine_name         VARCHAR(255) NOT NULL UNIQUE,
    category              VARCHAR(100),
    is_prescription_required BOOLEAN  DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS inventory (
    inventory_id BIGSERIAL PRIMARY KEY,
    store_id     BIGINT    NOT NULL REFERENCES stores(store_id)   ON DELETE CASCADE,
    medicine_id  BIGINT    NOT NULL REFERENCES medicines(medicine_id) ON DELETE CASCADE,
    quantity     INTEGER   NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    last_updated TIMESTAMP DEFAULT NOW(),
    UNIQUE (store_id, medicine_id)
);

-- ================================================================
--  Performance Indexes
--  (Hibernate @Index creates B-tree indexes on startup;
--   these extra ones must be created manually)
-- ================================================================

-- Search query: medicine ILIKE filter (covers the JOIN + filter)
CREATE INDEX IF NOT EXISTS idx_medicines_name
    ON medicines (medicine_name);

-- GIN index for full-text autocomplete at scale (optional but fast)
-- CREATE INDEX IF NOT EXISTS idx_medicines_name_gin
--     ON medicines USING gin (to_tsvector('english', medicine_name));

-- Haversine: partial index on active stores only
CREATE INDEX IF NOT EXISTS idx_stores_active_lat_lng
    ON stores (latitude, longitude)
    WHERE is_active = true;

-- Inventory: composite covering index for the Haversine JOIN
CREATE INDEX IF NOT EXISTS idx_inventory_store_medicine_qty
    ON inventory (store_id, medicine_id, quantity)
    WHERE quantity > 0;

-- ================================================================
--  Sample Data for Testing
-- ================================================================

-- Test owner (password = "test1234")
INSERT INTO users (name, email, password, role) VALUES
('Pharmacy Owner', 'owner@test.com',
 '$2a$10$7QhYbhbzLEVjxJt7Kh2fteHfQGPeQsBTJBMhJHiAZNxf2bPuJcXGu', 'OWNER')
ON CONFLICT (email) DO NOTHING;

-- Test customer (password = "test1234")
INSERT INTO users (name, email, password, role) VALUES
('Test Customer', 'customer@test.com',
 '$2a$10$7QhYbhbzLEVjxJt7Kh2fteHfQGPeQsBTJBMhJHiAZNxf2bPuJcXGu', 'CUSTOMER')
ON CONFLICT (email) DO NOTHING;

-- Sample medicines
INSERT INTO medicines (medicine_name, category) VALUES
('PARACETAMOL 500MG',  'Analgesic'),
('IBUPROFEN 400MG',    'Anti-inflammatory'),
('AMOXICILLIN 250MG',  'Antibiotic'),
('CETIRIZINE 10MG',    'Antihistamine'),
('METFORMIN 500MG',    'Antidiabetic'),
('OMEPRAZOLE 20MG',    'Antacid'),
('AZITHROMYCIN 500MG', 'Antibiotic'),
('PANTOPRAZOLE 40MG',  'Antacid')
ON CONFLICT (medicine_name) DO NOTHING;

-- ================================================================
--  Useful Queries for pgAdmin
-- ================================================================

-- View all stock across all stores:
-- SELECT u.name AS owner, s.store_name, m.medicine_name,
--        i.quantity, i.last_updated
-- FROM inventory i
-- JOIN stores s    ON s.store_id    = i.store_id
-- JOIN medicines m ON m.medicine_id = i.medicine_id
-- JOIN users u     ON u.id          = s.owner_id
-- ORDER BY s.store_name, m.medicine_name;

-- Explain the Haversine search query:
-- EXPLAIN ANALYSE
-- SELECT s.store_name,
--        6371 * ACOS(LEAST(1.0,
--            COS(RADIANS(17.385)) * COS(RADIANS(s.latitude))
--          * COS(RADIANS(s.longitude) - RADIANS(78.487))
--          + SIN(RADIANS(17.385)) * SIN(RADIANS(s.latitude))
--        )) AS distance_km
-- FROM stores s
-- JOIN inventory i  ON i.store_id    = s.store_id
-- JOIN medicines m  ON m.medicine_id = i.medicine_id
-- WHERE m.medicine_name ILIKE '%paracetamol%'
--   AND i.quantity > 0 AND s.is_active = true
-- ORDER BY distance_km;
