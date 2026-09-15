package com.ecom.ordersummary.health;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class OrderSummaryHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public OrderSummaryHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("service", "ecomOrderSummary")
                    .withDetail("database", connection.getMetaData().getURL())
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("service", "ecomOrderSummary")
                    .withDetail("database", "unavailable")
                    .build();
        }
    }
}
