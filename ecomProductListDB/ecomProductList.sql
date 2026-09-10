SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

CREATE DATABASE IF NOT EXISTS `ecomProdDB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ecomProdDB`;

CREATE TABLE `product_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `product_seq` (`next_val`)
VALUES (1);

CREATE TABLE `ecom_product_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ecom_product` (
  `product_rating` decimal(38,2) DEFAULT NULL,
  `id` bigint NOT NULL,
  `domain` varchar(255) DEFAULT NULL,
  `product_code` varchar(255) DEFAULT NULL,
  `product_desc_detail` varchar(255) DEFAULT NULL,
  `product_desc_heading` varchar(255) DEFAULT NULL,
  `product_img` varchar(255) DEFAULT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `product_price` decimal(10,2) NOT NULL DEFAULT '0.00',
  `product_category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ecom_product_category` (`product_category_id`),
  CONSTRAINT `fk_ecom_product_category` FOREIGN KEY (`product_category_id`) REFERENCES `ecom_product_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ecom_product_review_id_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `ecom_product_review_id_seq` (`next_val`)
VALUES (1);

CREATE TABLE `ecom_product_review` (
  `id` bigint NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `review_user_id` bigint DEFAULT NULL,
  `product_review_comment` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlr9u6f1iqp7c560djt5bd9092` (`product_id`),
  CONSTRAINT `FKlr9u6f1iqp7c560djt5bd9092` FOREIGN KEY (`product_id`) REFERENCES `ecom_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

COMMIT;
