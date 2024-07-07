user ecom;
drop table if exists users;
drop table if exists roles;
drop table if exists authorities;

create table users(id int ,user_name varchar(50),password varchar(100),active char(1)); 

create table authorities(user_id int, role_id int);

create table roles(id int, role_name varchar(50));