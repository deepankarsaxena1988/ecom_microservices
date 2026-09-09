FROM mysql

ENV MYSQL_ROOT_PASSWORD=root123
ENV MYSQL_DATABASE=ecomAccountAddressesDB
ENV MYSQL_USER=ecomUser
ENV MYSQL_PASSWORD=root123

COPY ./ecomAccountAddressesDB.sql /docker-entrypoint-initdb.d/
