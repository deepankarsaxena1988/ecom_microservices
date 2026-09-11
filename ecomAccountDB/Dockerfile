FROM mysql

ENV MYSQL_ROOT_PASSWORD=root123
ENV MYSQL_DATABASE=ecomAccountsDB
ENV MYSQL_USER=ecomUser
ENV MYSQL_PASSWORD=root123

COPY ./ecomAccountDB.sql /docker-entrypoint-initdb.d/
