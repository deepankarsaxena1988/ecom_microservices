SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

CREATE DATABASE IF NOT EXISTS `ecomCheckoutDB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ecomCheckoutDB`;

CREATE TABLE `ecom_payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `razorpay_order_id` VARCHAR(255),
  `razorpay_payment_id` VARCHAR(255),
  `amount` BIGINT NOT NULL,
  `currency` VARCHAR(10) DEFAULT 'INR',
  `status` VARCHAR(50),
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_razorpay_order_id (razorpay_order_id),
  UNIQUE KEY uq_razorpay_payment_id (razorpay_payment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;
