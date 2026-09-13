package com.ecom.checkout.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CheckoutHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public CheckoutHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("service", "ecomCheckOutService")
                    .withDetail("database", connection.getMetaData().getURL())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("service", "ecomCheckOutService")
                    .withDetail("database", "unavailable")
                    .build();
        }
    }
}
