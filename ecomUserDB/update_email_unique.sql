USE `ecomUserDB`;
ALTER TABLE `users` ADD CONSTRAINT unique_email UNIQUE (email);
