package com.selimhorri.app.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking business metrics related to payments
 */
@Service
@Slf4j
public class PaymentMetricsService {

    private final Counter paymentsProcessedCounter;
    private final Counter paymentsFailedCounter;
    private final Counter paymentsRefundedCounter;
    private final AtomicLong totalPaymentAmount;
    private final Timer paymentProcessingTimer;

    public PaymentMetricsService(MeterRegistry meterRegistry) {
        // Counter for payments processed
        this.paymentsProcessedCounter = Counter.builder("payments.processed.total")
                .description("Total number of payments processed successfully")
                .tag("service", "payment-service")
                .register(meterRegistry);

        // Counter for payments failed
        this.paymentsFailedCounter = Counter.builder("payments.failed.total")
                .description("Total number of payments that failed")
                .tag("service", "payment-service")
                .register(meterRegistry);

        // Counter for payments refunded
        this.paymentsRefundedCounter = Counter.builder("payments.refunded.total")
                .description("Total number of payments refunded")
                .tag("service", "payment-service")
                .register(meterRegistry);

        // Gauge for total payment amount
        this.totalPaymentAmount = new AtomicLong(0);
        Gauge.builder("payments.amount.total", totalPaymentAmount, AtomicLong::get)
                .description("Total amount of all payments processed")
                .tag("service", "payment-service")
                .register(meterRegistry);

        // Timer for payment processing time
        this.paymentProcessingTimer = Timer.builder("payments.processing.time")
                .description("Time taken to process a payment")
                .tag("service", "payment-service")
                .register(meterRegistry);
    }

    /**
     * Record that a payment was processed successfully
     */
    public void recordPaymentProcessed(double amount) {
        paymentsProcessedCounter.increment();
        totalPaymentAmount.addAndGet((long) amount);
        log.debug("Payment processed metric recorded with amount {}", amount);
    }

    /**
     * Record that a payment failed
     */
    public void recordPaymentFailed() {
        paymentsFailedCounter.increment();
        log.debug("Payment failed metric recorded");
    }

    /**
     * Record that a payment was refunded
     */
    public void recordPaymentRefunded(double amount) {
        paymentsRefundedCounter.increment();
        totalPaymentAmount.addAndGet(-((long) amount));
        log.debug("Payment refunded metric recorded with amount {}", amount);
    }

    /**
     * Get timer for recording payment processing time
     */
    public Timer.Sample startPaymentProcessing() {
        return Timer.start();
    }

    /**
     * Stop timer and record payment processing time
     */
    public void stopPaymentProcessing(Timer.Sample sample) {
        sample.stop(paymentProcessingTimer);
    }
}
