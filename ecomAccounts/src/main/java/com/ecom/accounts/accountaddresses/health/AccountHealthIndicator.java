package com.ecom.accountaddresses.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AccountHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public AccountHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("service", "ecomAccounts")
                    .withDetail("database", connection.getMetaData().getURL())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("service", "ecomAccounts")
                    .withDetail("database", "unavailable")
                    .build();
        }
    }
}
