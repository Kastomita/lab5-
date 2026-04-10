-- =====================================================
-- ОЧИСТКА ТАБЛИЦ В ПРАВИЛЬНОМ ПОРЯДКЕ (сначала дочерние)
-- =====================================================

-- Удаляем данные из таблиц с внешними ключами сначала
DELETE FROM user_roles;
DELETE FROM user_sessions;
DELETE FROM deliveries;
DELETE FROM photos;
DELETE FROM orders;
DELETE FROM formats;
DELETE FROM customers;
DELETE FROM users;

-- Сбрасываем последовательности
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE customers_id_seq RESTART WITH 1;
ALTER SEQUENCE formats_id_seq RESTART WITH 1;
ALTER SEQUENCE orders_id_seq RESTART WITH 1;
ALTER SEQUENCE photos_id_seq RESTART WITH 1;
ALTER SEQUENCE deliveries_id_seq RESTART WITH 1;
ALTER SEQUENCE user_sessions_id_seq RESTART WITH 1;

-- =====================================================
-- ТАБЛИЦА ПОЛЬЗОВАТЕЛЕЙ (с правильными типами)
-- =====================================================

-- Удаляем старую таблицу user_roles если есть
DROP TABLE IF EXISTS user_roles CASCADE;

-- Создаем таблицу user_roles заново
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Добавляем колонку enabled с DEFAULT значением (чтобы не было NULL)
ALTER TABLE users ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE users ALTER COLUMN enabled SET NOT NULL;

-- Добавляем остальные колонки если их нет
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_non_locked BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS student_id VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- =====================================================
-- ВСТАВКА ТЕСТОВЫХ ПОЛЬЗОВАТЕЛЕЙ
-- =====================================================

-- Пароль: Password123! (BCrypt hash)
INSERT INTO users (username, email, password, full_name, student_id, role, enabled, account_non_locked) VALUES
('admin', 'admin@photoprinting.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'System Administrator', 'ST-2024-000000', 'ADMIN', true, true),
('john_doe', 'john.doe@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'John Doe', 'ST-2024-001234', 'USER', true, true),
('maria_j', 'maria.j@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', 'Maria Johnson', 'ST-2024-001235', 'USER', true, true);

-- Вставка ролей
INSERT INTO user_roles (user_id, role) VALUES
((SELECT id FROM users WHERE username = 'admin'), 'ADMIN'),
((SELECT id FROM users WHERE username = 'admin'), 'USER'),
((SELECT id FROM users WHERE username = 'john_doe'), 'USER'),
((SELECT id FROM users WHERE username = 'maria_j'), 'USER');

-- =====================================================
-- ПРОДОЛЖЕНИЕ ВАШИХ ДАННЫХ (форматы, клиенты, заказы и т.д.)
-- =====================================================

-- 1. PRINT FORMATS
INSERT INTO formats (name, description, price, is_available) VALUES
('9x13', 'Standard photo format for family photos', 50.0, true),
('10x15', 'Most popular photo format', 70.0, true),
('13x18', 'Enlarged photo format', 120.0, true),
('15x20', 'Medium format for posters', 150.0, true),
('20x30', 'Large poster format', 300.0, true),
('30x40', 'Extra large format', 500.0, true),
('A4', 'Document format', 80.0, true),
('A3', 'Large document format', 160.0, false);

-- 2. CUSTOMERS
INSERT INTO customers (name, email, phone, address, created_at, updated_at) VALUES
('John Smith', 'john.smith@example.com', '+1 (555) 123-45-67', '123 Main Street, New York, NY 10001', NOW(), NOW()),
('Maria Johnson', 'maria.johnson@example.com', '+1 (555) 234-56-78', '456 Broadway, Los Angeles, CA 90001', NOW(), NOW()),
('Alex Williams', 'alex.williams@example.com', '+1 (555) 345-67-89', '789 Oak Avenue, Chicago, IL 60601', NOW(), NOW()),
('Elena Brown', 'elena.brown@example.com', '+1 (555) 456-78-90', '321 Pine Street, Houston, TX 77001', NOW(), NOW()),
('Dmitry Jones', 'dmitry.jones@example.com', '+1 (555) 567-89-01', '654 Cedar Road, Phoenix, AZ 85001', NOW(), NOW()),
('Anna Garcia', 'anna.garcia@example.com', '+1 (555) 678-90-12', '987 Elm Street, Philadelphia, PA 19101', NOW(), NOW());

-- 3. ORDERS
INSERT INTO orders (order_number, total_price, status, payment_status, customer_id, created_at) VALUES
('ORD-20241201-001', 240.0, 'DELIVERED', 'PAID', 1, '2024-12-01 10:30:00'),
('ORD-20241201-002', 350.0, 'SHIPPED', 'PAID', 2, '2024-12-01 14:20:00'),
('ORD-20241202-003', 120.0, 'READY', 'PAID', 3, '2024-12-02 09:15:00'),
('ORD-20241202-004', 500.0, 'PROCESSING', 'PAID', 4, '2024-12-02 16:45:00'),
('ORD-20241203-005', 80.0, 'CREATED', 'PENDING', 5, '2024-12-03 11:00:00'),
('ORD-20241203-006', 600.0, 'CANCELLED', 'REFUNDED', 6, '2024-12-03 13:30:00'),
('ORD-20241204-007', 220.0, 'PRINTING', 'PAID', 1, '2024-12-04 08:00:00'),
('ORD-20241204-008', 450.0, 'PROCESSING', 'PENDING', 2, '2024-12-04 12:00:00');

-- 4. PHOTOS
INSERT INTO photos (file_name, file_url, quantity, description, format_id, order_id) VALUES
('family_2024.jpg', 'https://example.com/photos/family_2024.jpg', 2, 'Family photo near Christmas tree', 2, 1),
('vacation_miami.jpg', 'https://example.com/photos/vacation_miami.jpg', 1, 'Vacation in Miami', 3, 1),
('birthday_2024.png', 'https://example.com/photos/birthday_2024.png', 1, 'Birthday celebration', 1, 1),
('wedding_photo.jpg', 'https://example.com/photos/wedding_photo.jpg', 3, 'Wedding ceremony photo', 2, 2),
('portrait_master.jpg', 'https://example.com/photos/portrait_master.jpg', 2, 'Professional portrait', 1, 2),
('graduation.jpg', 'https://example.com/photos/graduation.jpg', 2, 'Graduation ceremony', 3, 3),
('poster_nature.jpg', 'https://example.com/photos/poster_nature.jpg', 1, 'Nature landscape poster', 5, 4),
('city_landscape.jpg', 'https://example.com/photos/city_landscape.jpg', 1, 'City skyline view', 4, 4),
('document_scan.pdf', 'https://example.com/photos/document_scan.pdf', 1, 'Document scan for printing', 7, 5),
('anniversary.jpg', 'https://example.com/photos/anniversary.jpg', 2, 'Wedding anniversary photo', 2, 6),
('gift_photo.jpg', 'https://example.com/photos/gift_photo.jpg', 1, 'Birthday gift photo', 4, 6),
('new_year_2025.jpg', 'https://example.com/photos/new_year_2025.jpg', 3, 'New Year 2025 celebration', 1, 7),
('winter_morning.png', 'https://example.com/photos/winter_morning.png', 1, 'Winter morning landscape', 2, 7),
('art_print.jpg', 'https://example.com/photos/art_print.jpg', 1, 'Fine art reproduction', 4, 8),
('photo_collage.jpg', 'https://example.com/photos/photo_collage.jpg', 1, 'Custom photo collage', 3, 8),
('business_card.jpg', 'https://example.com/photos/business_card.jpg', 5, 'Business cards', 7, 8);

-- 5. DELIVERIES
INSERT INTO deliveries (address, status, tracking_number, estimated_delivery_date, actual_delivery_date, order_id) VALUES
('123 Main Street, New York, NY 10001', 'DELIVERED', 'TRK-CDE-123456', '2024-12-05 18:00:00', '2024-12-04 15:30:00', 1),
('456 Broadway, Los Angeles, CA 90001', 'IN_TRANSIT', 'TRK-XYZ-789012', '2024-12-07 20:00:00', NULL, 2),
('789 Oak Avenue, Chicago, IL 60601', 'PENDING', 'TRK-ABC-345678', '2024-12-06 14:00:00', NULL, 3),
('321 Pine Street, Houston, TX 77001', 'PROCESSING', 'TRK-DEF-901234', '2024-12-08 12:00:00', NULL, 4),
('654 Cedar Road, Phoenix, AZ 85001', 'PENDING', 'TRK-GHI-567890', '2024-12-06 16:00:00', NULL, 5),
('123 Main Street, New York, NY 10001', 'SHIPPED', 'TRK-JKL-123789', '2024-12-09 18:00:00', NULL, 7);

-- =====================================================
-- USER SESSIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token VARCHAR(512),
    refresh_token_hash VARCHAR(255) UNIQUE,
    access_token_id VARCHAR(255),
    user_agent TEXT,
    ip_address VARCHAR(45),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_sessions_refresh_token_hash ON user_sessions(refresh_token_hash);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_status ON user_sessions(status);

-- =====================================================
-- ВЕРИФИКАЦИЯ
-- =====================================================
SELECT 'users' as table_name, COUNT(*) as records FROM users
UNION ALL SELECT 'user_roles', COUNT(*) FROM user_roles
UNION ALL SELECT 'customers', COUNT(*) FROM customers
UNION ALL SELECT 'formats', COUNT(*) FROM formats
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'photos', COUNT(*) FROM photos
UNION ALL SELECT 'deliveries', COUNT(*) FROM deliveries
UNION ALL SELECT 'user_sessions', COUNT(*) FROM user_sessions;