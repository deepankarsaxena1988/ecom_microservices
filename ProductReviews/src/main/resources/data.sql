delete from product_list;

insert into product_list (id,product_name,product_img,product_desc_heading,product_desc_detail)
value(1,'Product 1','prodct1.jpg','Product 1 Heading','Product 1 Description');

insert into product_list (id,product_name,product_img,product_desc_heading,product_desc_detail)
value(2,'Product 2','prodct2.jpg','Product 2 Heading','Product 2 Description');


--review table---
create table product_review
 (id long, product_id long,review_id long);

create table reviews(id long,comment varchar(500));  