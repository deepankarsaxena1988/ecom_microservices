SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";
CREATE DATABASE IF NOT EXISTS `ecomProductDB` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ecomProductDB`;


CREATE TABLE `product_seq` (
  `next_val` bigint(20) DEFAULT NULL
);
INSERT INTO `product_seq` (`next_val`)
VALUES
 (1);

create table ecom_product
 (id long, product_code varchar(100), product_name varchar(50),product_img varchar(50),product_desc_heading  varchar(50),product_desc_detail  varchar(50));
 


alter table  ecom_product add product_rating DECIMAL(2,1) default 0.0;

-- appending file for create product review table
-- from root user execute below command 
SET GLOBAL log_bin_trust_function_creators = 1;

-- from db user execute below
mysql> delimiter //

mysql> CREATE FUNCTION get_ecomProductReviewID_next_sequence() RETURNS bigint
BEGIN
    INSERT INTO ecom_product_review_id_seq VALUES (NULL);
    RETURN LAST_INSERT_ID();
END //

-- if needed grant permission from root user , sometime execute primivilidges are not present with current user for a function/procedure (db object)
mysql> GRANT EXECUTE ON FUNCTION ecomProdDB.get_ecomProductReviewID_next_sequence TO 'ecomUser'@'%';--'ecomUser'@'localhost'
mysql>SELECT LAST_INSERT_ID();

mysql> select get_next_sequence();-- for calling Function from command line
-- mysql> call PROCEDURE_NAME for calling procedure from cammand line

mysql> SHOW FUNCTION STATUS WHERE db = 'ecomProdDB';

CREATE TABLE `ecom_product_review_id_seq` (
  `next_val` bigint(20) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY(next_val)
);




INSERT INTO `ecom_product_review_id_seq` (`next_val`)
VALUES
 (1);
 
create table ecom_product_review 
(id long, product_id long, review_user_id long , product_review_comment varchar(250));



COMMIT;


