package com.selimhorri.app.resource;

import com.selimhorri.app.config.FeatureToggle;
import com.selimhorri.app.config.FeatureToggleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing feature toggles.
 * 
 * Provides endpoints to:
 * - View all feature toggles
 * - Check if specific feature is enabled
 * - Enable/disable features
 * - Update rollout percentage
 * 
 * This allows operations teams to control feature rollout without redeployment.
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@RestController
@RequestMapping("/actuator/features")
@RequiredArgsConstructor
@Slf4j
public class FeatureToggleController {

    private final FeatureToggleService featureToggleService;

    /**
     * Get all configured feature toggles
     * 
     * GET /actuator/features
     * 
     * @return Map of all feature toggles with their configuration
     */
    @GetMapping
    public ResponseEntity<Map<String, FeatureToggle>> getAllToggles() {
        log.info("Fetching all feature toggles");
        return ResponseEntity.ok(featureToggleService.getAllToggles());
    }

    /**
     * Get a specific feature toggle
     * 
     * GET /actuator/features/{featureName}
     * 
     * @param featureName Name of the feature
     * @return Feature toggle configuration
     */
    @GetMapping("/{featureName}")
    public ResponseEntity<FeatureToggle> getToggle(@PathVariable String featureName) {
        log.info("Fetching feature toggle: {}", featureName);

        Map<String, FeatureToggle> toggles = featureToggleService.getAllToggles();
        FeatureToggle toggle = toggles.get(featureName);

        if (toggle == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toggle);
    }

    /**
     * Check if a feature is enabled
     * 
     * GET /actuator/features/{featureName}/enabled
     * 
     * @param featureName Name of the feature
     * @param userId      Optional user ID for user-specific check
     * @return Status indicating if feature is enabled
     */
    @GetMapping("/{featureName}/enabled")
    public ResponseEntity<Map<String, Object>> isFeatureEnabled(
            @PathVariable String featureName,
            @RequestParam(required = false) String userId) {

        Map<String, Object> response = new HashMap<>();
        response.put("feature", featureName);

        if (userId != null) {
            boolean enabled = featureToggleService.isEnabledForUser(featureName, userId);
            response.put("enabled", enabled);
            response.put("userId", userId);
            log.info("Feature '{}' enabled check for user {}: {}", featureName, userId, enabled);
        } else {
            boolean enabled = featureToggleService.isEnabled(featureName);
            response.put("enabled", enabled);
            log.info("Feature '{}' enabled check (global): {}", featureName, enabled);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update a feature toggle
     * 
     * PUT /actuator/features/{featureName}
     * 
     * @param featureName Name of the feature
     * @param toggle      New toggle configuration
     * @return Updated toggle
     */
    @PutMapping("/{featureName}")
    public ResponseEntity<FeatureToggle> updateToggle(
            @PathVariable String featureName,
            @RequestBody FeatureToggle toggle) {

        log.info("Updating feature toggle '{}': enabled={}, rollout={}%",
                featureName, toggle.isEnabled(), toggle.getRolloutPercentage());

        featureToggleService.updateToggle(featureName, toggle);

        return ResponseEntity.ok(toggle);
    }

    /**
     * Enable a feature completely (100% rollout)
     * 
     * POST /actuator/features/{featureName}/enable
     * 
     * @param featureName Name of the feature
     * @return Success message
     */
    @PostMapping("/{featureName}/enable")
    public ResponseEntity<Map<String, String>> enableFeature(@PathVariable String featureName) {
        log.info("Enabling feature: {}", featureName);

        featureToggleService.enableFeature(featureName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Feature '" + featureName + "' enabled successfully");
        response.put("status", "enabled");

        return ResponseEntity.ok(response);
    }

    /**
     * Disable a feature completely
     * 
     * POST /actuator/features/{featureName}/disable
     * 
     * @param featureName Name of the feature
     * @return Success message
     */
    @PostMapping("/{featureName}/disable")
    public ResponseEntity<Map<String, String>> disableFeature(@PathVariable String featureName) {
        log.info("Disabling feature: {}", featureName);

        featureToggleService.disableFeature(featureName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Feature '" + featureName + "' disabled successfully");
        response.put("status", "disabled");

        return ResponseEntity.ok(response);
    }

    /**
     * Set rollout percentage for gradual release
     * 
     * PUT /actuator/features/{featureName}/rollout
     * 
     * @param featureName Name of the feature
     * @param percentage  Rollout percentage (0-100)
     * @return Success message with new percentage
     */
    @PutMapping("/{featureName}/rollout")
    public ResponseEntity<Map<String, Object>> setRolloutPercentage(
            @PathVariable String featureName,
            @RequestParam int percentage) {

        if (percentage < 0 || percentage > 100) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Percentage must be between 0 and 100");
            return ResponseEntity.badRequest().body(error);
        }

        log.info("Setting rollout percentage for '{}' to {}%", featureName, percentage);

        featureToggleService.setRolloutPercentage(featureName, percentage);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Rollout percentage updated successfully");
        response.put("feature", featureName);
        response.put("rolloutPercentage", percentage);

        return ResponseEntity.ok(response);
    }
}
