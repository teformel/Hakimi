package com.hakimi.road.level;

import com.hakimi.road.util.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RoadManagerTest {

    private RoadManager roadManager;

    @BeforeEach
    public void setUp() {
        roadManager = new RoadManager();
    }

    @Test
    public void testInitialState() {
        assertEquals(RoadManager.SegmentType.STRAIGHT, roadManager.getCurrentSegment());
        assertEquals(0.0, roadManager.getCurrentCurvature(), 0.001);
    }

    @Test
    public void testCurvatureUpdate() {
        roadManager.forceSegment(RoadManager.SegmentType.CURVE_LEFT);
        // Initial curvature is 0, target is negative
        roadManager.update(10, 5000); // 5000 distance for standard curve behavior
        assertTrue(roadManager.getCurrentCurvature() < 0);
    }

    @Test
    public void testTurnLogicSuccessLeft() {
        roadManager.forceSegment(RoadManager.SegmentType.TURN_LEFT_90);
        // Set distance inside window
        roadManager.setSegmentDistanceRemaining(GameConfig.TURN_WINDOW_TOLERANCE - 10);

        RoadManager.TurnResult result = roadManager.checkTurn(-1); // Left input
        assertEquals(RoadManager.TurnResult.SUCCESS, result);
    }

    @Test
    public void testTurnLogicSuccessRight() {
        roadManager.forceSegment(RoadManager.SegmentType.TURN_RIGHT_90);
        // Set distance inside window
        roadManager.setSegmentDistanceRemaining(GameConfig.TURN_WINDOW_TOLERANCE - 10);

        RoadManager.TurnResult result = roadManager.checkTurn(1); // Right input
        assertEquals(RoadManager.TurnResult.SUCCESS, result);
    }

    @Test
    public void testTurnLogicWrongDirection() {
        roadManager.forceSegment(RoadManager.SegmentType.TURN_LEFT_90);
        roadManager.setSegmentDistanceRemaining(GameConfig.TURN_WINDOW_TOLERANCE - 10);

        RoadManager.TurnResult result = roadManager.checkTurn(1); // Right input
        assertEquals(RoadManager.TurnResult.WRONG_DIRECTION, result);
    }

    @Test
    public void testTurnLogicTooEarly() {
        roadManager.forceSegment(RoadManager.SegmentType.TURN_LEFT_90);
        roadManager.setSegmentDistanceRemaining(GameConfig.TURN_WINDOW_TOLERANCE + 100);

        RoadManager.TurnResult result = roadManager.checkTurn(-1);
        assertEquals(RoadManager.TurnResult.NONE, result);
    }

    @Test
    public void testMissedTurn() {
        roadManager.forceSegment(RoadManager.SegmentType.TURN_LEFT_90);
        roadManager.setSegmentDistanceRemaining(0);

        assertTrue(roadManager.checkMissedTurn());
    }
}
