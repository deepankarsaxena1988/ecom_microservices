USE `ecomUserDB`;

INSERT INTO `users` (`id`,`user_name`, `password`, `email`) VALUES
(1,'john_doe', 'password123', 'john@example.com'),
(2,'jane_smith', 'securePass!', 'jane.smith@ecom.com'),
(3,'admin_user', 'admin@2026', 'admin@ecom.com'),
(4,'test_user', 'test1234', 'test@test.com'),
(101, 'Aarav', 'Sharma', 'aarav.sharma@example.com'),
(102, 'Meera', 'Patel', 'meera.patel@example.com'),
(103, 'Rohit', 'Verma', 'rohit.verma@example.com');

SELECT * FROM `users`;
