package com.selimhorri.app.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for tracking business metrics related to products
 */
@Service
@Slf4j
public class ProductMetricsService {

    private final Counter productsCreatedCounter;
    private final Counter productsViewedCounter;
    private final Counter productsUpdatedCounter;
    private final Counter productsDeletedCounter;
    private final AtomicInteger lowStockProducts;

    public ProductMetricsService(MeterRegistry meterRegistry) {
        // Counter for products created
        this.productsCreatedCounter = Counter.builder("products.created.total")
                .description("Total number of products created")
                .tag("service", "product-service")
                .register(meterRegistry);

        // Counter for product views
        this.productsViewedCounter = Counter.builder("products.views.total")
                .description("Total number of product views")
                .tag("service", "product-service")
                .register(meterRegistry);

        // Counter for products updated
        this.productsUpdatedCounter = Counter.builder("products.updated.total")
                .description("Total number of products updated")
                .tag("service", "product-service")
                .register(meterRegistry);

        // Counter for products deleted
        this.productsDeletedCounter = Counter.builder("products.deleted.total")
                .description("Total number of products deleted")
                .tag("service", "product-service")
                .register(meterRegistry);

        // Gauge for low stock products
        this.lowStockProducts = new AtomicInteger(0);
        Gauge.builder("products.stock.low", lowStockProducts, AtomicInteger::get)
                .description("Number of products with low stock levels")
                .tag("service", "product-service")
                .register(meterRegistry);
    }

    /**
     * Record that a product was created
     */
    public void recordProductCreated() {
        productsCreatedCounter.increment();
        log.debug("Product created metric recorded");
    }

    /**
     * Record that a product was viewed
     */
    public void recordProductViewed() {
        productsViewedCounter.increment();
        log.debug("Product view metric recorded");
    }

    /**
     * Record that a product was updated
     */
    public void recordProductUpdated() {
        productsUpdatedCounter.increment();
        log.debug("Product updated metric recorded");
    }

    /**
     * Record that a product was deleted
     */
    public void recordProductDeleted() {
        productsDeletedCounter.increment();
        log.debug("Product deleted metric recorded");
    }

    /**
     * Update low stock products count
     */
    public void updateLowStockCount(int count) {
        lowStockProducts.set(count);
        log.debug("Low stock products count updated to {}", count);
    }

    /**
     * Increment low stock products count
     */
    public void incrementLowStockCount() {
        lowStockProducts.incrementAndGet();
        log.debug("Low stock products count incremented");
    }

    /**
     * Decrement low stock products count
     */
    public void decrementLowStockCount() {
        lowStockProducts.decrementAndGet();
        log.debug("Low stock products count decremented");
    }
}
