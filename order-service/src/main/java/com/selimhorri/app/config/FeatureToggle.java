package com.selimhorri.app.config;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a feature toggle configuration.
 * 
 * A feature toggle (also known as feature flag) allows features to be
 * enabled or disabled dynamically without redeploying the application.
 * 
 * Supports:
 * - Simple on/off toggle
 * - Gradual rollout by percentage
 * - User-specific enablement
 * - Environment-specific enablement
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@Data
public class FeatureToggle {

    /**
     * Whether the feature is enabled globally
     */
    private boolean enabled = false;

    /**
     * Percentage of users who should see this feature (0-100)
     * Used for gradual rollout / canary releases
     */
    private int rolloutPercentage = 100;

    /**
     * Specific user IDs that should have this feature enabled
     * regardless of rolloutPercentage
     */
    private List<String> enabledUsers = new ArrayList<>();

    /**
     * Environments where this feature should be enabled
     * e.g., ["dev", "stage"] but not "prod"
     */
    private List<String> enabledEnvironments = new ArrayList<>();

    /**
     * Description of what this feature does
     */
    private String description;

    /**
     * When this feature was last modified
     */
    private String lastModified;

    /**
     * Check if a specific user should have this feature enabled.
     * 
     * Uses consistent hashing to determine if user is in rollout percentage.
     * Same user will always get the same result for a given percentage.
     * 
     * @param userId The user ID to check
     * @return true if feature should be enabled for this user
     */
    public boolean isEnabledForUser(String userId) {
        if (!enabled) {
            return false;
        }

        // Explicitly enabled users always get the feature
        if (enabledUsers.contains(userId)) {
            return true;
        }

        // If rollout is 100%, everyone gets it
        if (rolloutPercentage >= 100) {
            return true;
        }

        // If rollout is 0%, nobody gets it (except explicit users)
        if (rolloutPercentage <= 0) {
            return false;
        }

        // Use consistent hashing to determine if user is in rollout percentage
        int hash = Math.abs(userId.hashCode());
        return (hash % 100) < rolloutPercentage;
    }

    /**
     * Check if feature is enabled for a specific environment
     * 
     * @param environment The environment to check (dev, stage, prod, etc.)
     * @return true if feature is enabled for this environment
     */
    public boolean isEnabledForEnvironment(String environment) {
        if (!enabled) {
            return false;
        }

        // If no environments specified, enabled for all
        if (enabledEnvironments.isEmpty()) {
            return true;
        }

        return enabledEnvironments.contains(environment);
    }

    /**
     * Create a disabled feature toggle
     */
    public static FeatureToggle disabled() {
        FeatureToggle toggle = new FeatureToggle();
        toggle.setEnabled(false);
        return toggle;
    }

    /**
     * Create a fully enabled feature toggle
     */
    public static FeatureToggle fullyEnabled() {
        FeatureToggle toggle = new FeatureToggle();
        toggle.setEnabled(true);
        toggle.setRolloutPercentage(100);
        return toggle;
    }

    /**
     * Create a feature toggle with specific rollout percentage
     */
    public static FeatureToggle withRollout(int percentage) {
        FeatureToggle toggle = new FeatureToggle();
        toggle.setEnabled(true);
        toggle.setRolloutPercentage(Math.max(0, Math.min(100, percentage)));
        return toggle;
    }
}
