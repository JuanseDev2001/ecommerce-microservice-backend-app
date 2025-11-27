package com.selimhorri.app.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing feature toggles (feature flags).
 * 
 * Feature toggles allow features to be enabled/disabled dynamically without
 * redeployment, enabling:
 * - Gradual rollouts (canary releases)
 * - A/B testing
 * - Kill switches for problematic features
 * - Environment-specific features
 * 
 * Configuration is externalized in application.yml and can be refreshed
 * dynamically using Spring Cloud Config refresh endpoint.
 * 
 * Usage example:
 * 
 * <pre>
 * if (featureToggleService.isEnabled("new-checkout-flow")) {
 *     return newCheckoutFlow();
 * } else {
 *     return legacyCheckoutFlow();
 * }
 * </pre>
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@Service
@ConfigurationProperties(prefix = "features")
@RefreshScope
@Data
@Slf4j
public class FeatureToggleService {

    /**
     * Map of feature name to feature toggle configuration
     */
    private Map<String, FeatureToggle> toggles = new HashMap<>();

    /**
     * Current environment (dev, stage, prod)
     */
    private String environment = "dev";

    @PostConstruct
    public void init() {
        log.info("Feature Toggle Service initialized with {} features", toggles.size());
        toggles.forEach((name, toggle) -> {
            log.info("Feature '{}': enabled={}, rollout={}%",
                    name, toggle.isEnabled(), toggle.getRolloutPercentage());
        });
    }

    /**
     * Check if a feature is enabled globally.
     * 
     * @param featureName The name of the feature
     * @return true if feature is enabled
     */
    public boolean isEnabled(String featureName) {
        FeatureToggle toggle = toggles.get(featureName);

        if (toggle == null) {
            log.warn("Feature toggle '{}' not found. Returning false by default.", featureName);
            return false;
        }

        boolean enabledForEnv = toggle.isEnabledForEnvironment(environment);
        boolean globallyEnabled = toggle.isEnabled();

        log.debug("Feature '{}' check: globallyEnabled={}, enabledForEnv={}",
                featureName, globallyEnabled, enabledForEnv);

        return globallyEnabled && enabledForEnv;
    }

    /**
     * Check if a feature is enabled for a specific user.
     * 
     * Takes into account:
     * - Global enabled flag
     * - Environment enablement
     * - User-specific enablement
     * - Rollout percentage (consistent hashing)
     * 
     * @param featureName The name of the feature
     * @param userId      The user ID to check
     * @return true if feature is enabled for this user
     */
    public boolean isEnabledForUser(String featureName, String userId) {
        FeatureToggle toggle = toggles.get(featureName);

        if (toggle == null) {
            log.warn("Feature toggle '{}' not found for user {}. Returning false.", featureName, userId);
            return false;
        }

        if (!toggle.isEnabledForEnvironment(environment)) {
            log.debug("Feature '{}' not enabled for environment '{}'", featureName, environment);
            return false;
        }

        boolean enabledForUser = toggle.isEnabledForUser(userId);

        log.debug("Feature '{}' check for user {}: enabled={}", featureName, userId, enabledForUser);

        return enabledForUser;
    }

    /**
     * Get all feature toggles and their current state
     * 
     * @return Map of all feature toggles
     */
    public Map<String, FeatureToggle> getAllToggles() {
        return new HashMap<>(toggles);
    }

    /**
     * Update a feature toggle at runtime.
     * 
     * Note: This only updates the in-memory state. For persistent changes,
     * update the configuration source (application.yml or Config Server).
     * 
     * @param featureName Name of the feature
     * @param toggle      New toggle configuration
     */
    public void updateToggle(String featureName, FeatureToggle toggle) {
        log.info("Updating feature toggle '{}': enabled={}, rollout={}%",
                featureName, toggle.isEnabled(), toggle.getRolloutPercentage());

        toggle.setLastModified(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        toggles.put(featureName, toggle);
    }

    /**
     * Enable a feature for all users
     * 
     * @param featureName Name of the feature
     */
    public void enableFeature(String featureName) {
        FeatureToggle toggle = toggles.getOrDefault(featureName, new FeatureToggle());
        toggle.setEnabled(true);
        toggle.setRolloutPercentage(100);
        updateToggle(featureName, toggle);
    }

    /**
     * Disable a feature for all users
     * 
     * @param featureName Name of the feature
     */
    public void disableFeature(String featureName) {
        FeatureToggle toggle = toggles.getOrDefault(featureName, new FeatureToggle());
        toggle.setEnabled(false);
        updateToggle(featureName, toggle);
    }

    /**
     * Set rollout percentage for gradual release
     * 
     * @param featureName Name of the feature
     * @param percentage  Percentage of users (0-100)
     */
    public void setRolloutPercentage(String featureName, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }

        FeatureToggle toggle = toggles.getOrDefault(featureName, new FeatureToggle());
        toggle.setEnabled(true);
        toggle.setRolloutPercentage(percentage);
        updateToggle(featureName, toggle);

        log.info("Feature '{}' rollout set to {}%", featureName, percentage);
    }

    /**
     * Get the current rollout percentage for a feature
     * 
     * @param featureName Name of the feature
     * @return Rollout percentage (0-100), or 0 if feature doesn't exist
     */
    public int getRolloutPercentage(String featureName) {
        FeatureToggle toggle = toggles.get(featureName);
        return toggle != null ? toggle.getRolloutPercentage() : 0;
    }

    /**
     * Add a user to the explicit enable list for a feature
     * 
     * @param featureName Name of the feature
     * @param userId      User ID to enable
     */
    public void enableForUser(String featureName, String userId) {
        FeatureToggle toggle = toggles.getOrDefault(featureName, new FeatureToggle());
        if (!toggle.getEnabledUsers().contains(userId)) {
            toggle.getEnabledUsers().add(userId);
            updateToggle(featureName, toggle);
            log.info("Feature '{}' explicitly enabled for user: {}", featureName, userId);
        }
    }

    /**
     * Check if a feature exists in configuration
     * 
     * @param featureName Name of the feature
     * @return true if feature exists
     */
    public boolean featureExists(String featureName) {
        return toggles.containsKey(featureName);
    }
}
