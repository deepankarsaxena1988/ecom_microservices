delete from users;
delete from roles;
delete from authorities;

insert into users (id,user_name,password,active)
value((select next_val from hibernate_sequence),'user1','pass1','A');


update  hibernate_sequence set next_val=next_val+1;

insert into users (id,user_name,password,active)
value((select next_val from hibernate_sequence),'user2','pass2','A');  

update  hibernate_sequence set next_val=next_val+1;


insert into roles (id,role_name) value((select next_val from hibernate_sequence),'role1');

update  hibernate_sequence set next_val=next_val+1;

insert into roles (id,role_name) value((select next_val from hibernate_sequence),'role2');

update  hibernate_sequence set next_val=next_val+1;

insert into Authorities (user_id,role_id)
value((select id from users where user_name='user1'), (select id from roles where role_name='role1'));

insert into Authorities (user_id,role_id)
value((select id from users where user_name='user2'), (select id from roles where role_name='role2'));


