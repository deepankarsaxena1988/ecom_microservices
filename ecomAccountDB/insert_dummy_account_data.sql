USE `ecomAccountsDB`;

-- Insert dummy account details.
INSERT INTO `account_details` (`user_id`, `first_name`, `last_name`, `email`, `phone_number`, `date_of_birth`, `gender`) VALUES
  (101, 'Aarav', 'Sharma', 'aarav.sharma@example.com', '+91-9876543210', '1992-05-14', 'Male'),
  (102, 'Meera', 'Patel', 'meera.patel@example.com', '+91-9876543211', '1995-08-21', 'Female'),
  (103, 'Rohit', 'Verma', 'rohit.verma@example.com', '+91-9876543212', '1989-11-30', 'Male');

-- Insert dummy addresses; each address is linked to the corresponding account detail via `acnt_id`.
INSERT INTO `account_addresses` (`acnt_id`, `user_id`, `address_type`, `full_name`, `mobile_number`, `address_line1`, `address_line2`, `landmark`, `city`, `state`, `postal_code`, `country`, `is_default`)
SELECT `id`, `user_id`, 'HOME', CONCAT(`first_name`, ' ', `last_name`), `phone_number`, '12, MG Road', 'Near City Center', 'Metro Station', 'Bengaluru', 'Karnataka', '560001', 'India', b'1'
FROM `account_details`
WHERE `email` = 'aarav.sharma@example.com';

INSERT INTO `account_addresses` (`acnt_id`, `user_id`, `address_type`, `full_name`, `mobile_number`, `address_line1`, `address_line2`, `landmark`, `city`, `state`, `postal_code`, `country`, `is_default`)
SELECT `id`, `user_id`, 'WORK', CONCAT(`first_name`, ' ', `last_name`), `phone_number`, '45, Corporate Park', 'Floor 4', 'Tech Park', 'Hyderabad', 'Telangana', '500032', 'India', b'0'
FROM `account_details`
WHERE `email` = 'meera.patel@example.com';

INSERT INTO `account_addresses` (`acnt_id`, `user_id`, `address_type`, `full_name`, `mobile_number`, `address_line1`, `address_line2`, `landmark`, `city`, `state`, `postal_code`, `country`, `is_default`)
SELECT `id`, `user_id`, 'HOME', CONCAT(`first_name`, ' ', `last_name`), `phone_number`, '88, Lake View Apartments', 'Tower B', 'Garden Lake', 'Pune', 'Maharashtra', '411001', 'India', b'1'
FROM `account_details`
WHERE `email` = 'rohit.verma@example.com';

SELECT * FROM `account_details`;
SELECT * FROM `account_addresses`;
