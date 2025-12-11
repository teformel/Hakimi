package com.hakimi.road.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SettingsManagerTest {

    @Test
    public void testSingleton() {
        SettingsManager instance1 = SettingsManager.getInstance();
        SettingsManager instance2 = SettingsManager.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    public void testDefaultValues() {
        SettingsManager settings = SettingsManager.getInstance();
        // Reset to ensure defaults
        settings.resetToDefaults();

        assertEquals(1, settings.getBaseGameSpeed());
        assertEquals(10, settings.getObstacleSpawnRate());
        assertEquals(50, settings.getSpeedIncreaseInterval());
    }

    @Test
    public void testUpdateAndSave() {
        SettingsManager settings = SettingsManager.getInstance();
        int originalSpeed = settings.getBaseGameSpeed();

        settings.setBaseGameSpeed(originalSpeed + 1);
        assertEquals(originalSpeed + 1, settings.getBaseGameSpeed());

        // Restore
        settings.setBaseGameSpeed(originalSpeed);
    }
}
