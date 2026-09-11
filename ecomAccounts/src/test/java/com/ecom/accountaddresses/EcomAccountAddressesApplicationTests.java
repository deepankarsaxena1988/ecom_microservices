package com.ecom.accountaddresses;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:mysql://host.docker.internal:3312/ecomAccountsDB?useSSL=false&allowPublicKeyRetrieval=true&useTimezone=true&serverTimezone=UTC",
    "spring.datasource.username=root",
    "spring.datasource.password=root123",
    "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false"
})
class EcomAccountAddressesApplicationTests {

    @Test
    void contextLoads() {
    }

}
