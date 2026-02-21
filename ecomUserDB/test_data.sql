USE `ecomUserDB`;

INSERT INTO `users` (`user_name`, `password`, `email`) VALUES 
('john_doe', 'password123', 'john@example.com'),
('jane_smith', 'securePass!', 'jane.smith@ecom.com'),
('admin_user', 'admin@2026', 'admin@ecom.com'),
('test_user', 'test1234', 'test@test.com');

SELECT * FROM `users`;
