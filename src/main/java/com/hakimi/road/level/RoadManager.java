package com.hakimi.road.level;

import com.hakimi.road.util.GameConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/**
 * 道路管理器
 * 负责管理道路的弯曲、转向和路段生成
 */
public class RoadManager {
    private static final Logger logger = LogManager.getLogger(RoadManager.class);

    public enum SegmentType {
        STRAIGHT,
        CURVE_LEFT,
        CURVE_RIGHT,
        TURN_LEFT_90,
        TURN_RIGHT_90
    }

    public enum TurnResult {
        NONE, // 未在转向窗口或无操作
        SUCCESS, // 成功转向
        FAIL, // 转向失败（太早或太晚）
        WRONG_DIRECTION // 方向错误
    }

    private SegmentType currentSegment;
    private double currentCurvature;
    private double targetCurvature;
    private int segmentDistanceRemaining;
    private Random random;

    // 转向相关
    private boolean isTurnActive; // 是否处于需要玩家操作的急转弯状态
    private boolean turnCompleted; // 当前转弯是否已完成

    public RoadManager() {
        this.random = new Random();
        this.currentSegment = SegmentType.STRAIGHT;
        this.segmentDistanceRemaining = GameConfig.ROAD_SEGMENT_LENGTH;
        this.currentCurvature = 0;
        this.targetCurvature = 0;
        logger.info("RoadManager initialized");
    }

    /**
     * 更新道路状态
     * 
     * @param speed         当前游戏速度
     * @param totalDistance 总奔跑距离
     */
    public void update(int speed, int totalDistance) {
        segmentDistanceRemaining -= speed;

        // 平滑过渡弯曲度
        if (Math.abs(currentCurvature - targetCurvature) > 0.01) {
            double delta = (targetCurvature - currentCurvature) * 0.1;
            currentCurvature += delta;
        } else {
            currentCurvature = targetCurvature;
        }

        // 检查路段结束
        if (segmentDistanceRemaining <= 0) {
            generateNextSegment(totalDistance);
        }
    }

    /**
     * 生成下一个路段
     * 根据游戏进度调整难度
     */
    private void generateNextSegment(int totalDistance) {
        // 如果当前是急转弯，必须接直道
        if (currentSegment == SegmentType.TURN_LEFT_90 || currentSegment == SegmentType.TURN_RIGHT_90) {
            setSegment(SegmentType.STRAIGHT, totalDistance);
        } else {
            // 根据距离动态调整生成概率和路段长度
            int roll = random.nextInt(100);

            // 简单难度 (前2000米) - 主要是直道
            if (totalDistance < 2000) {
                if (roll < 70) {
                    setSegment(SegmentType.STRAIGHT, totalDistance);
                } else if (roll < 85) {
                    setSegment(SegmentType.CURVE_LEFT, totalDistance);
                } else {
                    setSegment(SegmentType.CURVE_RIGHT, totalDistance);
                }
            }
            // 中等难度 (2000-5000米) - 引入急转弯，曲线变多
            else if (totalDistance < 5000) {
                if (roll < 50) {
                    setSegment(SegmentType.STRAIGHT, totalDistance);
                } else if (roll < 70) {
                    setSegment(SegmentType.CURVE_LEFT, totalDistance);
                } else if (roll < 90) {
                    setSegment(SegmentType.CURVE_RIGHT, totalDistance);
                } else if (roll < 95) {
                    setSegment(SegmentType.TURN_LEFT_90, totalDistance);
                } else {
                    setSegment(SegmentType.TURN_RIGHT_90, totalDistance);
                }
            }
            // 困难难度 (>5000米) - 更多急转弯和曲线
            else {
                if (roll < 30) {
                    setSegment(SegmentType.STRAIGHT, totalDistance);
                } else if (roll < 60) {
                    setSegment(SegmentType.CURVE_LEFT, totalDistance);
                } else if (roll < 90) {
                    setSegment(SegmentType.CURVE_RIGHT, totalDistance);
                } else if (roll < 95) {
                    setSegment(SegmentType.TURN_LEFT_90, totalDistance);
                } else {
                    setSegment(SegmentType.TURN_RIGHT_90, totalDistance);
                }
            }
        }

        logger.debug("Next segment generated: " + currentSegment);
    }

    private void setSegment(SegmentType type, int totalDistance) {
        this.currentSegment = type;

        // 动态调整路段长度
        // 早期曲线短一些，让玩家更容易适应
        int baseLength = GameConfig.ROAD_SEGMENT_LENGTH;

        if (type == SegmentType.CURVE_LEFT || type == SegmentType.CURVE_RIGHT) {
            if (totalDistance < 2000) {
                this.segmentDistanceRemaining = baseLength / 2; // 早期弯道长度减半
            } else {
                this.segmentDistanceRemaining = baseLength;
            }
        } else {
            this.segmentDistanceRemaining = baseLength;
        }

        this.turnCompleted = false;

        // 设置目标弯曲度
        switch (type) {
            case STRAIGHT:
                targetCurvature = 0;
                isTurnActive = false;
                break;
            case CURVE_LEFT:
                targetCurvature = -GameConfig.MAX_CURVATURE;
                // 早期弯曲度小一点
                if (totalDistance < 2000)
                    targetCurvature *= 0.5;
                isTurnActive = false;
                break;
            case CURVE_RIGHT:
                targetCurvature = GameConfig.MAX_CURVATURE;
                if (totalDistance < 2000)
                    targetCurvature *= 0.5;
                isTurnActive = false;
                break;
            case TURN_LEFT_90:
                // 急转弯预告，先稍微弯曲
                targetCurvature = -GameConfig.MAX_CURVATURE * 1.5;
                isTurnActive = true;
                break;
            case TURN_RIGHT_90:
                targetCurvature = GameConfig.MAX_CURVATURE * 1.5;
                isTurnActive = true;
                break;
        }
    }

    /**
     * 检查转向操作
     * 
     * @param inputDirection -1: Left, 1: Right, 0: None
     */
    public TurnResult checkTurn(int inputDirection) {
        if (!isTurnActive || turnCompleted) {
            return TurnResult.NONE;
        }

        // 检查是否在转向窗口内 (路段即将结束时)
        boolean inWindow = segmentDistanceRemaining <= GameConfig.TURN_WINDOW_TOLERANCE;

        if (inWindow) {
            if (inputDirection == 0)
                return TurnResult.NONE;

            if (currentSegment == SegmentType.TURN_LEFT_90) {
                if (inputDirection == -1) {
                    completeTurn();
                    return TurnResult.SUCCESS;
                } else {
                    return TurnResult.WRONG_DIRECTION;
                }
            } else if (currentSegment == SegmentType.TURN_RIGHT_90) {
                if (inputDirection == 1) {
                    completeTurn();
                    return TurnResult.SUCCESS;
                } else {
                    return TurnResult.WRONG_DIRECTION;
                }
            }
        } else {
            // 还没到窗口就按了
            if (inputDirection != 0) {
                // 可以选择忽略或者稍微惩罚，这里暂时忽略
                return TurnResult.NONE;
            }
        }

        return TurnResult.NONE;
    }

    /**
     * 检查是否错过了转向 (用于自动判定失败)
     */
    public boolean checkMissedTurn() {
        if (isTurnActive && !turnCompleted && segmentDistanceRemaining <= 0) {
            return true;
        }
        return false;
    }

    private void completeTurn() {
        turnCompleted = true;
        isTurnActive = false;
        // 视觉上瞬间回正，或者直接切到下一段直道
        currentCurvature = 0;
        targetCurvature = 0;
        logger.info("Turn completed successfully");
    }

    public double getCurrentCurvature() {
        return currentCurvature;
    }

    public SegmentType getCurrentSegment() {
        return currentSegment;
    }

    public boolean isTurnActive() {
        return isTurnActive;
    }

    public int getSegmentDistanceRemaining() {
        return segmentDistanceRemaining;
    }

    // For manual override (testing)
    public void forceSegment(SegmentType type) {
        setSegment(type, 5000); // Assume hard mode for forced segments
    }

    public void setSegmentDistanceRemaining(int distance) {
        this.segmentDistanceRemaining = distance;
    }
}
