INSERT INTO station_heads (full_name, email, password_hash, badge_number, phone_number, role, created_at)
VALUES (
    'Station Head Admin',
    'stationhead@cms.gov',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnESKKyB2yS',
    'SH-001',
    '9876543210',
    'STATION_HEAD',
    NOW()
) ON DUPLICATE KEY UPDATE email = email;
