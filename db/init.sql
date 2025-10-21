-- Enable UUID support for Postgres
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Optional seed data
INSERT INTO users (id, username, email)
VALUES (uuid_generate_v4(), 'visal', 'visal@zohomail.in')
ON CONFLICT DO NOTHING;
