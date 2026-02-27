-- Chèn role 
INSERT IGNORE INTO role (id, name, description) VALUES (1, 'ADMIN', 'Quản trị viên hệ thống');
INSERT IGNORE INTO role (id, name, description) VALUES (2, 'MEMBER', 'Người dùng thông thường');

-- Chèn role_class
INSERT IGNORE INTO role_class (id, name, description) VALUES (1, 'ADMIN', 'Chủ sở hữu lớp học');
INSERT IGNORE INTO role_class (id, name, description) VALUES (2, 'MEMBER', 'Thành viên lớp học');

-- Chèn Admin (Lưu ý: đổi username -> user_name, thêm country và status)
INSERT IGNORE INTO users (id, password, created_at, email, user_name, full_name, role_id, country, status) 
VALUES (4, 
        '$2a$10$LO4/UoH/b3jVOSY833KvG.MngmZH3KltGBN12xN6Ekpx25xNIhv8C', 
        '2025-05-13 10:42:26', 
        'admin@gmail.com', 
        'admin', 
        'admin', 
        1, 
        'Vietnam', 
        1);