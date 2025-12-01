package com.selimhorri.app.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking business metrics related to orders
 */
@Service
@Slf4j
public class OrderMetricsService {

    private final Counter ordersCreatedCounter;
    private final Counter ordersCompletedCounter;
    private final Counter ordersCancelledCounter;
    private final Counter ordersFailedCounter;
    private final AtomicLong totalOrderAmount;
    private final AtomicInteger activeOrders;
    private final Timer orderProcessingTimer;

    public OrderMetricsService(MeterRegistry meterRegistry) {
        // Counter for orders created
        this.ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Total number of orders created")
                .tag("service", "order-service")
                .register(meterRegistry);

        // Counter for orders completed
        this.ordersCompletedCounter = Counter.builder("orders.completed.total")
                .description("Total number of orders completed successfully")
                .tag("service", "order-service")
                .register(meterRegistry);

        // Counter for orders cancelled
        this.ordersCancelledCounter = Counter.builder("orders.cancelled.total")
                .description("Total number of orders cancelled")
                .tag("service", "order-service")
                .register(meterRegistry);

        // Counter for orders failed
        this.ordersFailedCounter = Counter.builder("orders.failed.total")
                .description("Total number of orders that failed")
                .tag("service", "order-service")
                .register(meterRegistry);

        // Gauge for total order amount
        this.totalOrderAmount = new AtomicLong(0);
        Gauge.builder("orders.amount.total", totalOrderAmount, AtomicLong::get)
                .description("Total amount of all orders")
                .tag("service", "order-service")
                .register(meterRegistry);

        // Gauge for active orders
        this.activeOrders = new AtomicInteger(0);
        Gauge.builder("orders.active.count", activeOrders, AtomicInteger::get)
                .description("Number of active orders being processed")
                .tag("service", "order-service")
                .register(meterRegistry);

        // Timer for order processing time
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
                .description("Time taken to process an order")
                .tag("service", "order-service")
                .register(meterRegistry);
    }

    /**
     * Record that an order was created
     */
    public void recordOrderCreated() {
        ordersCreatedCounter.increment();
        activeOrders.incrementAndGet();
        log.debug("Order created metric recorded");
    }

    /**
     * Record that an order was created with amount
     */
    public void recordOrderCreated(double amount) {
        ordersCreatedCounter.increment();
        totalOrderAmount.addAndGet((long) amount);
        activeOrders.incrementAndGet();
        log.debug("Order created with amount {} metric recorded", amount);
    }

    /**
     * Record that an order was completed
     */
    public void recordOrderCompleted() {
        ordersCompletedCounter.increment();
        activeOrders.decrementAndGet();
        log.debug("Order completed metric recorded");
    }

    /**
     * Record that an order was cancelled
     */
    public void recordOrderCancelled() {
        ordersCancelledCounter.increment();
        activeOrders.decrementAndGet();
        log.debug("Order cancelled metric recorded");
    }

    /**
     * Record that an order failed
     */
    public void recordOrderFailed() {
        ordersFailedCounter.increment();
        activeOrders.decrementAndGet();
        log.debug("Order failed metric recorded");
    }

    /**
     * Get timer for recording order processing time
     */
    public Timer.Sample startOrderProcessing() {
        return Timer.start();
    }

    /**
     * Stop timer and record order processing time
     */
    public void stopOrderProcessing(Timer.Sample sample) {
        sample.stop(orderProcessingTimer);
    }
}
