package com.selimhorri.app.config;

import io.github.resilience4j.bulkhead.event.BulkheadEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallFinishedEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallPermittedEvent;
import io.github.resilience4j.bulkhead.event.BulkheadOnCallRejectedEvent;
import io.github.resilience4j.circuitbreaker.event.*;
import io.github.resilience4j.retry.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listeners for Resilience4j patterns.
 * 
 * Provides centralized logging and monitoring for:
 * - Circuit Breaker events
 * - Bulkhead events
 * - Retry events
 * 
 * These events are useful for:
 * - Debugging resilience issues
 * - Monitoring system health
 * - Alerting on pattern activations
 * - Metrics collection
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@Component
@Slf4j
public class ResilienceEventListener {

    // ========================================
    // Circuit Breaker Events
    // ========================================

    @EventListener
    public void onCircuitBreakerStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        log.warn("Circuit Breaker '{}' transitioned from {} to {}",
                event.getCircuitBreakerName(),
                event.getStateTransition().getFromState(),
                event.getStateTransition().getToState());

        // In production, you might:
        // - Send alert if circuit opens
        // - Update dashboard
        // - Trigger auto-scaling if needed
    }

    @EventListener
    public void onCircuitBreakerError(CircuitBreakerOnErrorEvent event) {
        log.error("Circuit Breaker '{}' recorded error: {}",
                event.getCircuitBreakerName(),
                event.getThrowable().getMessage());
    }

    @EventListener
    public void onCircuitBreakerSuccess(CircuitBreakerOnSuccessEvent event) {
        log.debug("Circuit Breaker '{}' recorded success. Duration: {}ms",
                event.getCircuitBreakerName(),
                event.getElapsedDuration().toMillis());
    }

    @EventListener
    public void onCircuitBreakerCallNotPermitted(CircuitBreakerOnCallNotPermittedEvent event) {
        log.warn("Circuit Breaker '{}' call not permitted - circuit is OPEN",
                event.getCircuitBreakerName());

        // Alert! Service is completely blocked
        // Consider scaling, failover, or manual intervention
    }

    // ========================================
    // Bulkhead Events
    // ========================================

    @EventListener
    public void onBulkheadCallRejected(BulkheadOnCallRejectedEvent event) {
        log.warn("Bulkhead '{}' rejected call - max concurrent calls reached",
                event.getBulkheadName());

        // This indicates system is at capacity
        // Consider:
        // - Auto-scaling
        // - Load shedding
        // - Alerting operations team
    }

    @EventListener
    public void onBulkheadCallPermitted(BulkheadOnCallPermittedEvent event) {
        log.debug("Bulkhead '{}' permitted call",
                event.getBulkheadName());
    }

    @EventListener
    public void onBulkheadCallFinished(BulkheadOnCallFinishedEvent event) {
        log.debug("Bulkhead '{}' call finished",
                event.getBulkheadName());
    }

    // ========================================
    // Retry Events
    // ========================================

    @EventListener
    public void onRetryAttempt(RetryOnRetryEvent event) {
        log.warn("Retry attempt {} for '{}'. Last exception: {}",
                event.getNumberOfRetryAttempts(),
                event.getName(),
                event.getLastThrowable().getMessage());

        // Track retry metrics
        // If too many retries, might indicate systematic issue
    }

    @EventListener
    public void onRetrySuccess(RetryOnSuccessEvent event) {
        log.info("Retry succeeded for '{}' after {} attempts",
                event.getName(),
                event.getNumberOfRetryAttempts());

        // Good to know - service recovered
        // But many retries might indicate unstable dependency
    }

    @EventListener
    public void onRetryError(RetryOnErrorEvent event) {
        log.error("Retry failed for '{}' after {} attempts. Final error: {}",
                event.getName(),
                event.getNumberOfRetryAttempts(),
                event.getLastThrowable().getMessage());

        // All retries exhausted - invoking fallback
        // Alert operations team
        // Check dependent service health
    }

    @EventListener
    public void onRetryIgnoredError(RetryOnIgnoredErrorEvent event) {
        log.info("Retry ignored error for '{}': {}",
                event.getName(),
                event.getLastThrowable().getMessage());

        // Error type is configured to not trigger retry
        // This is expected for certain error types
    }

    /**
     * Generic handler for any Bulkhead event
     * Use for custom metrics collection
     */
    @EventListener
    public void onBulkheadEvent(BulkheadEvent event) {
        // Custom metrics collection
        // Example: Send to Prometheus, Datadog, etc.
        log.trace("Bulkhead event: {} - {}",
                event.getBulkheadName(),
                event.getEventType());
    }

    /**
     * Generic handler for any Circuit Breaker event
     * Use for custom metrics collection
     */
    @EventListener
    public void onCircuitBreakerEvent(CircuitBreakerEvent event) {
        // Custom metrics collection
        log.trace("Circuit Breaker event: {} - {}",
                event.getCircuitBreakerName(),
                event.getEventType());
    }

    /**
     * Generic handler for any Retry event
     * Use for custom metrics collection
     */
    @EventListener
    public void onRetryEvent(RetryEvent event) {
        // Custom metrics collection
        log.trace("Retry event: {} - {}",
                event.getName(),
                event.getEventType());
    }
}
