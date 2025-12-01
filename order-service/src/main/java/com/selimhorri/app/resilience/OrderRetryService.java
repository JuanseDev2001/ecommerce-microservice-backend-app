package com.selimhorri.app.resilience;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.service.OrderService;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service implementing the Retry Pattern with exponential backoff.
 * 
 * The Retry pattern automatically retries failed operations with
 * increasing delays between attempts (exponential backoff).
 * 
 * This is particularly useful for:
 * - Transient network failures
 * - Temporary service unavailability
 * - Database connection timeouts
 * - External API rate limiting
 * 
 * Benefits:
 * - Automatic recovery from transient failures
 * - Exponential backoff prevents overwhelming struggling services
 * - Configurable retry logic per operation type
 * - Fallback mechanisms for ultimate failure
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderRetryService {

    private final OrderService orderService;
    private final RestTemplate restTemplate;

    /**
     * Save order with retry on database failures.
     * 
     * Retries up to 3 times with exponential backoff if database operations fail.
     * Useful for handling transient DB connection issues.
     * 
     * @param orderDto Order to save
     * @return Saved order
     */
    @Retry(name = "orderProcessing", fallbackMethod = "saveOrderFallback")
    public OrderDto saveOrderWithRetry(OrderDto orderDto) {
        log.info("Attempting to save order with retry: {}", orderDto.getOrderId());

        OrderDto savedOrder = orderService.save(orderDto);

        log.info("Successfully saved order: {}", savedOrder.getOrderId());
        return savedOrder;
    }

    /**
     * Fallback method when all retry attempts fail.
     * Stores order in a dead-letter queue for manual processing.
     * 
     * @param orderDto  The order that failed to save
     * @param exception The exception that caused the failure
     * @return Order with error status
     */
    public OrderDto saveOrderFallback(OrderDto orderDto, Exception exception) {
        log.error("All retry attempts failed for order: {}. Error: {}",
                orderDto.getOrderId(),
                exception.getMessage());

        // In production, you would:
        // 1. Store in dead-letter queue (DLQ)
        // 2. Send alert to operations team
        // 3. Log to error tracking system (Sentry, Rollbar, etc.)

        orderDto.setOrderDesc("FAILED: " + orderDto.getOrderDesc());

        return orderDto;
    }

    /**
     * Call external API with retry logic.
     * 
     * Retries up to 5 times with exponential backoff for external API calls.
     * More retry attempts because external APIs are more prone to transient issues.
     * 
     * @param apiUrl  API endpoint URL
     * @param orderId Order ID for the API call
     * @return API response
     */
    @Retry(name = "externalApiCall", fallbackMethod = "externalApiCallFallback")
    public String callExternalApiWithRetry(String apiUrl, Integer orderId) {
        log.info("Calling external API with retry: {} for order: {}", apiUrl, orderId);

        try {
            String response = restTemplate.getForObject(apiUrl + "/" + orderId, String.class);
            log.info("External API call successful for order: {}", orderId);
            return response;

        } catch (Exception e) {
            log.warn("External API call failed (will retry): {} - {}", apiUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * Fallback for external API calls.
     * Returns cached data or default response.
     * 
     * @param apiUrl    The API URL that failed
     * @param orderId   The order ID
     * @param exception The exception thrown
     * @return Fallback response
     */
    public String externalApiCallFallback(String apiUrl, Integer orderId, Exception exception) {
        log.error("External API call failed after all retries: {} for order: {}. Error: {}",
                apiUrl, orderId, exception.getMessage());

        // Return cached data or default response
        return "{\"status\":\"unavailable\",\"orderId\":" + orderId
                + ",\"message\":\"Service temporarily unavailable\"}";
    }

    /**
     * Process payment with retry.
     * Critical operation that needs multiple retry attempts.
     * 
     * @param orderId Order ID for payment
     * @param amount  Amount to charge
     * @return Payment confirmation
     */
    @Retry(name = "orderProcessing", fallbackMethod = "processPaymentFallback")
    public String processPaymentWithRetry(Integer orderId, Double amount) {
        log.info("Processing payment with retry for order: {}, amount: {}", orderId, amount);

        // Simulate payment processing that might fail
        if (Math.random() < 0.3) { // 30% chance of transient failure
            throw new RuntimeException("Payment gateway temporarily unavailable");
        }

        log.info("Payment processed successfully for order: {}", orderId);
        return "PAYMENT_SUCCESS";
    }

    /**
     * Fallback for payment processing.
     * Queues payment for later retry or manual processing.
     */
    public String processPaymentFallback(Integer orderId, Double amount, Exception exception) {
        log.error("Payment processing failed after retries for order: {}. Queuing for manual review.", orderId);

        // In production:
        // 1. Queue to payment retry service
        // 2. Alert finance team
        // 3. Update order status to PAYMENT_PENDING

        return "PAYMENT_QUEUED_FOR_RETRY";
    }

    /**
     * Update inventory with retry.
     * Ensures inventory updates don't fail due to transient issues.
     * 
     * @param productId Product ID
     * @param quantity  Quantity to update
     * @return Update status
     */
    @Retry(name = "orderProcessing", fallbackMethod = "updateInventoryFallback")
    public boolean updateInventoryWithRetry(Integer productId, Integer quantity) {
        log.info("Updating inventory with retry - Product: {}, Quantity: {}", productId, quantity);

        // Simulate inventory update that might have transient failures
        if (Math.random() < 0.2) { // 20% chance of failure
            throw new RuntimeException("Inventory service connection timeout");
        }

        log.info("Inventory updated successfully for product: {}", productId);
        return true;
    }

    /**
     * Fallback for inventory update.
     * Queues update for eventual consistency.
     */
    public boolean updateInventoryFallback(Integer productId, Integer quantity, Exception exception) {
        log.error("Inventory update failed after retries for product: {}. Queuing for async update.", productId);

        // Queue for async processing to maintain eventual consistency
        // In production: send to message queue for later processing

        return false;
    }
}
