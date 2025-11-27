package com.selimhorri.app.resilience;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.service.OrderService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service implementing the Bulkhead Pattern for order processing.
 * 
 * The Bulkhead pattern isolates resources for different types of operations,
 * preventing one type of workload from consuming all available resources
 * and affecting other operations.
 * 
 * This implementation provides:
 * - Semaphore-based bulkh AAAºead for synchronous order processing
 * - Thread pool-based bulkhead for asynchronous order processing
 * - Fallback mechanisms when bulkhead is full
 * - Metrics and monitoring through Resilience4j
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderBulkheadService {

    private final OrderService orderService;

    /**
     * Process order with semaphore-based bulkhead protection.
     * 
     * This limits concurrent order processing to prevent resource exhaustion.
     * When the limit is reached, the fallback method is called.
     * 
     * @param orderDto The order to process
     * @return Processed order
     */
    @Bulkhead(name = "orderProcessing", fallbackMethod = "orderProcessingFallback", type = Type.SEMAPHORE)
    public OrderDto processOrder(OrderDto orderDto) {
        log.info("Processing order with bulkhead protection: {}", orderDto.getOrderId());

        try {
            // Simulate processing time
            Thread.sleep(100);

            // Actual order processing
            OrderDto savedOrder = orderService.save(orderDto);

            log.info("Successfully processed order: {}", savedOrder.getOrderId());
            return savedOrder;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Order processing interrupted: {}", orderDto.getOrderId());
            throw new RuntimeException("Order processing interrupted", e);
        }
    }

    /**
     * Fallback method when bulkhead is full.
     * 
     * This provides graceful degradation by queueing the order
     * for later processing instead of rejecting it outright.
     * 
     * @param orderDto  The order that couldn't be processed
     * @param exception The exception that triggered the fallback
     * @return Order with pending status
     */
    public OrderDto orderProcessingFallback(OrderDto orderDto, Exception exception) {
        log.error("Bulkhead full for order processing. Order queued for retry: {}. Reason: {}",
                orderDto.getOrderId(),
                exception.getMessage());

        // Set order description to indicate pending retry status
        orderDto.setOrderDesc("PENDING_RETRY: " + orderDto.getOrderDesc());

        // In a real implementation, you would:
        // 1. Queue to message broker (RabbitMQ, Kafka)
        // 2. Store in retry queue database
        // 3. Schedule for later processing

        return orderDto;
    }

    /**
     * Process order asynchronously with thread pool bulkhead.
     * 
     * This uses a dedicated thread pool for async processing,
     * preventing blocking operations from affecting the main thread pool.
     * 
     * @param orderDto The order to process
     * @return Processed order
     */
    @Bulkhead(name = "orderAsyncProcessing", fallbackMethod = "orderAsyncProcessingFallback", type = Type.THREADPOOL)
    public OrderDto processOrderAsync(OrderDto orderDto) {
        log.info("Processing order asynchronously with thread pool bulkhead: {}", orderDto.getOrderId());

        try {
            // Simulate async processing
            Thread.sleep(200);

            OrderDto savedOrder = orderService.save(orderDto);

            log.info("Successfully processed order async: {}", savedOrder.getOrderId());
            return savedOrder;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async order processing interrupted: {}", orderDto.getOrderId());
            throw new RuntimeException("Async order processing interrupted", e);
        }
    }

    /**
     * Fallback for async processing when thread pool is full.
     * 
     * @param orderDto  The order that couldn't be processed
     * @param exception The exception that triggered the fallback
     * @return Order with queued status
     */
    public OrderDto orderAsyncProcessingFallback(OrderDto orderDto, Exception exception) {
        log.error("Thread pool bulkhead full for async order processing. Order queued: {}. Reason: {}",
                orderDto.getOrderId(),
                exception.getMessage());

        orderDto.setOrderDesc("QUEUED_FOR_ASYNC: " + orderDto.getOrderDesc());

        return orderDto;
    }

    /**
     * High priority order processing with dedicated bulkhead.
     * 
     * This ensures that high-priority orders have dedicated resources
     * and are not affected by regular order processing load.
     * 
     * @param orderDto High priority order
     * @return Processed order
     */
    @Bulkhead(name = "highPriorityOrders", fallbackMethod = "highPriorityFallback", type = Type.SEMAPHORE)
    public OrderDto processHighPriorityOrder(OrderDto orderDto) {
        log.info("Processing HIGH PRIORITY order: {}", orderDto.getOrderId());

        // Fast-track processing for high priority orders
        // In a real implementation, you would add a priority field to OrderDto
        orderDto.setOrderDesc("HIGH_PRIORITY: " + orderDto.getOrderDesc());
        OrderDto savedOrder = orderService.save(orderDto);

        return savedOrder;
    }

    /**
     * Fallback for high priority orders.
     * High priority orders should rarely hit this, but when they do,
     * we need to handle them specially.
     */
    public OrderDto highPriorityFallback(OrderDto orderDto, Exception exception) {
        log.error("CRITICAL: High priority order bulkhead full: {}. Immediate escalation needed!",
                orderDto.getOrderId());

        // In production:
        // 1. Alert operations team
        // 2. Auto-scale if in cloud
        // 3. Queue to highest priority queue

        orderDto.setOrderDesc("ESCALATED: " + orderDto.getOrderDesc());
        return orderDto;
    }
}
