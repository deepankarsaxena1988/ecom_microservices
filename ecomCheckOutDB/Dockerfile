FROM mysql
ENV MYSQL_ROOT_PASSWORD=root123
ENV MYSQL_DATABASE=ecomCheckoutDB
ENV MYSQL_USER=ecomUser
ENV MYSQL_PASSWORD=root123
# Place initialization SQL into the docker-entrypoint init dir so mysql initializes DB on first run
COPY ./ecomCheckOutDB.sql /docker-entrypoint-initdb.d/
