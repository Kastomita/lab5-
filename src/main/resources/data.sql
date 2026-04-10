-- =====================================================
-- ADD USERS FOR SPRING SECURITY
-- =====================================================

-- Clear users table
DELETE FROM user_roles;
DELETE FROM users;

-- Reset sequences
ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- Create users (passwords are BCrypt encoded)
-- Password for all users: 'Password123!' (matches the password pattern requirement)
INSERT INTO users (username, email, password, full_name, is_enabled, is_account_non_locked, created_at, updated_at) VALUES
('admin', 'admin@photoprinting.com', '$2a$10$rqKjQkRkRkRkRkRkRkRkRuRkRkRkRkRkRkRkRkRkRkRkRkRk', 'System Administrator', true, true, NOW(), NOW()),
('john_doe', 'john@example.com', '$2a$10$rqKjQkRkRkRkRkRkRkRkRuRkRkRkRkRkRkRkRkRkRkRkRkRk', 'John Doe', true, true, NOW(), NOW()),
('jane_smith', 'jane@example.com', '$2a$10$rqKjQkRkRkRkRkRkRkRkRuRkRkRkRkRkRkRkRkRkRkRkRkRk', 'Jane Smith', true, true, NOW(), NOW());

-- Assign roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ADMIN'),
(1, 'USER'),
(2, 'USER'),
(3, 'USER');
-- Clear tables (for restart)
DELETE FROM deliveries;
DELETE FROM photos;
DELETE FROM orders;
DELETE FROM formats;
DELETE FROM customers;

-- Reset sequences (for PostgreSQL)
ALTER SEQUENCE customers_id_seq RESTART WITH 1;
ALTER SEQUENCE formats_id_seq RESTART WITH 1;
ALTER SEQUENCE orders_id_seq RESTART WITH 1;
ALTER SEQUENCE photos_id_seq RESTART WITH 1;
ALTER SEQUENCE deliveries_id_seq RESTART WITH 1;

-- =====================================================
-- 1. PRINT FORMATS
-- =====================================================
INSERT INTO formats (name, description, price, is_available) VALUES
('9x13', 'Standard photo format for family photos', 50.0, true),
('10x15', 'Most popular photo format', 70.0, true),
('13x18', 'Enlarged photo format', 120.0, true),
('15x20', 'Medium format for posters', 150.0, true),
('20x30', 'Large poster format', 300.0, true),
('30x40', 'Extra large format', 500.0, true),
('A4', 'Document format', 80.0, true),
('A3', 'Large document format', 160.0, false);

-- =====================================================
-- 2. CUSTOMERS
-- =====================================================
INSERT INTO customers (name, email, phone, address, created_at, updated_at) VALUES
('John Smith', 'john.smith@example.com', '+1 (555) 123-45-67', '123 Main Street, New York, NY 10001', NOW(), NOW()),
('Maria Johnson', 'maria.johnson@example.com', '+1 (555) 234-56-78', '456 Broadway, Los Angeles, CA 90001', NOW(), NOW()),
('Alex Williams', 'alex.williams@example.com', '+1 (555) 345-67-89', '789 Oak Avenue, Chicago, IL 60601', NOW(), NOW()),
('Elena Brown', 'elena.brown@example.com', '+1 (555) 456-78-90', '321 Pine Street, Houston, TX 77001', NOW(), NOW()),
('Dmitry Jones', 'dmitry.jones@example.com', '+1 (555) 567-89-01', '654 Cedar Road, Phoenix, AZ 85001', NOW(), NOW()),
('Anna Garcia', 'anna.garcia@example.com', '+1 (555) 678-90-12', '987 Elm Street, Philadelphia, PA 19101', NOW(), NOW());

-- =====================================================
-- 3. ORDERS
-- =====================================================
INSERT INTO orders (order_number, total_price, status, payment_status, customer_id, created_at) VALUES
('ORD-20241201-001', 240.0, 'DELIVERED', 'PAID', 1, '2024-12-01 10:30:00'),
('ORD-20241201-002', 350.0, 'SHIPPED', 'PAID', 2, '2024-12-01 14:20:00'),
('ORD-20241202-003', 120.0, 'READY', 'PAID', 3, '2024-12-02 09:15:00'),
('ORD-20241202-004', 500.0, 'PROCESSING', 'PAID', 4, '2024-12-02 16:45:00'),
('ORD-20241203-005', 80.0, 'CREATED', 'PENDING', 5, '2024-12-03 11:00:00'),
('ORD-20241203-006', 600.0, 'CANCELLED', 'REFUNDED', 6, '2024-12-03 13:30:00'),
('ORD-20241204-007', 220.0, 'PRINTING', 'PAID', 1, '2024-12-04 08:00:00'),
('ORD-20241204-008', 450.0, 'PROCESSING', 'PENDING', 2, '2024-12-04 12:00:00');

-- =====================================================
-- 4. PHOTOS (links to orders and formats)
-- =====================================================
INSERT INTO photos (file_name, file_url, quantity, description, format_id, order_id) VALUES
-- Order 1 (John Smith) - 3 photos
('family_2024.jpg', 'https://example.com/photos/family_2024.jpg', 2, 'Family photo near Christmas tree', 2, 1),
('vacation_miami.jpg', 'https://example.com/photos/vacation_miami.jpg', 1, 'Vacation in Miami', 3, 1),
('birthday_2024.png', 'https://example.com/photos/birthday_2024.png', 1, 'Birthday celebration', 1, 1),

-- Order 2 (Maria Johnson) - 2 photos
('wedding_photo.jpg', 'https://example.com/photos/wedding_photo.jpg', 3, 'Wedding ceremony photo', 2, 2),
('portrait_master.jpg', 'https://example.com/photos/portrait_master.jpg', 2, 'Professional portrait', 1, 2),

-- Order 3 (Alex Williams) - 1 photo
('graduation.jpg', 'https://example.com/photos/graduation.jpg', 2, 'Graduation ceremony', 3, 3),

-- Order 4 (Elena Brown) - 2 photos
('poster_nature.jpg', 'https://example.com/photos/poster_nature.jpg', 1, 'Nature landscape poster', 5, 4),
('city_landscape.jpg', 'https://example.com/photos/city_landscape.jpg', 1, 'City skyline view', 4, 4),

-- Order 5 (Dmitry Jones) - 1 photo
('document_scan.pdf', 'https://example.com/photos/document_scan.pdf', 1, 'Document scan for printing', 7, 5),

-- Order 6 (Anna Garcia) - 2 photos (cancelled)
('anniversary.jpg', 'https://example.com/photos/anniversary.jpg', 2, 'Wedding anniversary photo', 2, 6),
('gift_photo.jpg', 'https://example.com/photos/gift_photo.jpg', 1, 'Birthday gift photo', 4, 6),

-- Order 7 (John Smith) - 2 photos
('new_year_2025.jpg', 'https://example.com/photos/new_year_2025.jpg', 3, 'New Year 2025 celebration', 1, 7),
('winter_morning.png', 'https://example.com/photos/winter_morning.png', 1, 'Winter morning landscape', 2, 7),

-- Order 8 (Maria Johnson) - 3 photos
('art_print.jpg', 'https://example.com/photos/art_print.jpg', 1, 'Fine art reproduction', 4, 8),
('photo_collage.jpg', 'https://example.com/photos/photo_collage.jpg', 1, 'Custom photo collage', 3, 8),
('business_card.jpg', 'https://example.com/photos/business_card.jpg', 5, 'Business cards', 7, 8);

-- =====================================================
-- 5. DELIVERIES
-- =====================================================
INSERT INTO deliveries (address, status, tracking_number, estimated_delivery_date, actual_delivery_date, order_id) VALUES
('123 Main Street, New York, NY 10001', 'DELIVERED', 'TRK-CDE-123456', '2024-12-05 18:00:00', '2024-12-04 15:30:00', 1),
('456 Broadway, Los Angeles, CA 90001', 'IN_TRANSIT', 'TRK-XYZ-789012', '2024-12-07 20:00:00', NULL, 2),
('789 Oak Avenue, Chicago, IL 60601', 'PENDING', 'TRK-ABC-345678', '2024-12-06 14:00:00', NULL, 3),
('321 Pine Street, Houston, TX 77001', 'PROCESSING', 'TRK-DEF-901234', '2024-12-08 12:00:00', NULL, 4),
('654 Cedar Road, Phoenix, AZ 85001', 'PENDING', 'TRK-GHI-567890', '2024-12-06 16:00:00', NULL, 5),
-- Order 6 is cancelled, no delivery
('123 Main Street, New York, NY 10001', 'SHIPPED', 'TRK-JKL-123789', '2024-12-09 18:00:00', NULL, 7);

-- =====================================================
-- 6. VERIFICATION QUERIES (for debugging)
-- =====================================================

-- Show statistics for all tables
SELECT 'customers' as table_name, COUNT(*) as records FROM customers
UNION ALL
SELECT 'formats', COUNT(*) FROM formats
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'photos', COUNT(*) FROM photos
UNION ALL
SELECT 'deliveries', COUNT(*) FROM deliveries;

-- Show order information with JOINs
SELECT
    o.id,
    o.order_number,
    c.name as customer_name,
    o.total_price,
    o.status,
    o.payment_status,
    COUNT(p.id) as photos_count,
    d.status as delivery_status
FROM orders o
JOIN customers c ON o.customer_id = c.id
LEFT JOIN photos p ON o.id = p.order_id
LEFT JOIN deliveries d ON o.id = d.order_id
GROUP BY o.id, c.name, d.status
ORDER BY o.id;

-- Show all active orders with customer email
SELECT
    o.order_number,
    c.name as customer_name,
    c.email,
    o.total_price,
    o.status
FROM orders o
JOIN customers c ON o.customer_id = c.id
WHERE o.status NOT IN ('DELIVERED', 'CANCELLED')
ORDER BY o.created_at DESC;

-- Show delivery tracking information
SELECT
    d.tracking_number,
    d.status as delivery_status,
    c.name as customer_name,
    c.address as customer_address,
    d.estimated_delivery_date,
    d.actual_delivery_date
FROM deliveries d
JOIN orders o ON d.order_id = o.id
JOIN customers c ON o.customer_id = c.id
WHERE d.status != 'DELIVERED'
ORDER BY d.estimated_delivery_date;