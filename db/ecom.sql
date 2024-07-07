CREATE TABLE `product_seq` (
  `next_val` bigint(20) DEFAULT NULL
);
INSERT INTO `product_seq` (`next_val`)
VALUES
 (1);
 
  create table ecom_product
 (id long, product_code varchar(100), product_name varchar(50),product_img varchar(50),product_desc_heading  varchar(50),product_desc_detail  varchar(50));
 
 
 use ecom;

select * from product;
select * from product_category;
select * from product_list;
create table product_list
 (id long, product_name varchar(50),product_img varchar(50),product_desc_heading  varchar(50),product_desc_detail  varchar(50));
 
 insert into product_list (id,product_name,product_img,product_desc_heading,product_desc_detail)
value(1,'Product 1','prodct1.jpg','Product 1 Heading','Product 1 Description');

insert into product_list (id,product_name,product_img,product_desc_heading,product_desc_detail)
value(2,'Product 2','prodct2.jpg','Product 2 Heading','Product 2 Description');


CREATE TABLE `product_seq` (
  `next_val` bigint(20) DEFAULT NULL
);
INSERT INTO `product_seq` (`next_val`)
VALUES
 (1);
 
 
 create table ecom_product
 (id long, product_code varchar(100), product_name varchar(50),product_img varchar(50),product_desc_heading  varchar(50),product_desc_detail  varchar(50));
 
 select * from ecom_product;
 
 delete from ecom_product;
 
 

 
 ALTER TABLE ecom_product
ADD domain varchar(200);
 
 update ecom_product set domain ='Http://localhost/ecom';
 
 insert into ecom_product (product_code,product_desc_detail,product_desc_heading,product_img,product_name,id) values (?,?,?,?,?,?);
 
 select next_val from product_seq;

delete from product_seq;