-- sample user adding.
INSERT INTO users (id, username, email)
VALUES (
    'f7c962b7-0cc3-4f2d-8b3a-df0c5a9b128e',
    'viasl',
    'visal@zohomail.in'
)
ON CONFLICT (id) DO NOTHING;

-- dummy data feeding

INSERT INTO health_goals (
    id,
    user_id,
    title,
    description,
    target,
    unit,
    start_date,
    end_date,
    status
)
VALUES
(
    uuid_generate_v4(),
    'f7c962b7-0cc3-4f2d-8b3a-df0c5a9b128e',
    'Lose Weight',
    'Target to lose 5 kg in 3 months',
    5,
    'kg',
    '2025-10-21',
    '2026-01-21',
    'ACTIVE'
),
(
    uuid_generate_v4(),
    'f7c962b7-0cc3-4f2d-8b3a-df0c5a9b128e',
    'Run 5k Daily',
    'Daily running target for stamina',
    5,
    'km',
    '2025-10-21',
    '2025-11-21',
    'ACTIVE'
);
