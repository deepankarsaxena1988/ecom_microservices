FROM mysql
ENV MYSQL_ROOT_PASSWORD=root@123
ENV MYSQL_DATABASE=ecomUserDB
ENV MYSQL_USER=ecomUser
ENV MYSQL_PASSWORD=root@123
COPY ./ecomUserDB.sql /docker-entrypoint-initdb.d/
