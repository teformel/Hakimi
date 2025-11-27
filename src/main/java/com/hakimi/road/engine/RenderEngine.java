package com.hakimi.road.engine;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Chaser;
import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;
import com.hakimi.road.util.GameConfig;

import java.io.IOException;
import java.util.List;

/**
 * 渲染引擎
 * 负责所有游戏画面的渲染
 */
public class RenderEngine {
    private Screen screen;
    
    public RenderEngine(Screen screen) {
        this.screen = screen;
    }
    
    /**
     * 渲染菜单界面
     */
    public void renderMenu(int width, int height) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);
        
        // 游戏说明文本
        String[] instructions = {
                "游戏说明：",
                "- 使用 ← → 键左右移动哈基米",
                "- 使用 ↑ 或 空格 跳跃（躲避低障碍）",
                "- 使用 ↓ 滑铲（躲避高障碍）",
                "- 躲避障碍物，跑得越远分数越高",
                "- 速度会随距离增加而变快",
                "",
                "按 Enter 开始奔跑！"
        };
        
        // 标题
        String title = "哈基米的南北路";
        
        // 计算内容块的最大宽度（包括标题、说明文字、哈基米）
        int hakimiWidth = 9; // 哈基米宽度
        int maxContentWidth = title.length();
        for (String instruction : instructions) {
            if (instruction.length() > maxContentWidth) {
                maxContentWidth = instruction.length();
            }
        }
        if (hakimiWidth > maxContentWidth) {
            maxContentWidth = hakimiWidth;
        }
        
        // 计算内容块的起始x坐标，使整个块居中
        int contentStartX = width / 2 - maxContentWidth / 2;
        
        // 计算垂直居中位置
        int hakimiHeight = 3; // 哈基米高度
        int totalContentHeight = hakimiHeight + 2 + 1 + instructions.length; // 哈基米 + 间距(2) + 标题(1) + 说明文字
        int contentStartY = height / 2 - totalContentHeight / 2;
        
        // 绘制哈基米（在顶部，左对齐）
        int hakimiX = contentStartX;
        int hakimiY = contentStartY;
        renderHakimi(tg, hakimiX, hakimiY, false, 0);
        
        // 标题（在哈基米下方，左对齐）
        int titleY = hakimiY + hakimiHeight + 2; // 哈基米高度 + 间距
        tg.putString(contentStartX, titleY, title);
        
        // 游戏说明（在标题下方，左对齐）
        int instructionStartY = titleY + 2;
        for (int i = 0; i < instructions.length; i++) {
            tg.putString(contentStartX, instructionStartY + i, instructions[i]);
        }
        
        screen.refresh();
    }
    
    /**
     * 渲染游戏界面
     */
    public void renderGame(Player player, Chaser chaser, List<Obstacle> obstacles,
                          boolean showChaser, int score, int distance, int gameSpeed,
                          int width, int height) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);
        
        // 绘制道路
        drawRoad(tg, width, height, distance);
        
        // 绘制障碍物
        for (Obstacle obstacle : obstacles) {
            int obstacleRow = Math.max(0, Math.min(height - 2, obstacle.getY()));
            int laneX = GameConfig.calculateLaneX(width, height, obstacle.getLane(), obstacleRow);
            drawObstacle(tg, laneX, obstacleRow, obstacle.getType());
        }

        int playerY = player.calculateY(height);
        int playerRow = Math.max(0, Math.min(height - 2, playerY));
        int playerX = GameConfig.calculateLaneX(width, height, player.getLane(), playerRow);

        // 绘制追逐者
        if (showChaser && chaser != null) {
            int chaserRow = Math.max(GameConfig.HORIZON_OFFSET + 1,
                    Math.min(playerRow - 5, height - 4));
            int chaserX = GameConfig.calculateLaneX(width, height, player.getLane(), chaserRow);
            renderChaser(tg, chaserX - 3, chaserRow - 3, chaser.getAnimationFrame());
        }

        // 绘制玩家（在追逐者之后，确保位于前景）
        // 根据玩家位置计算透视效果
        float depthFactor = calculateDepthFactor(height, playerRow);
        renderHakimi3D(tg, playerX, playerRow, player, true, distance, depthFactor);
        
        // 绘制HUD
        tg.putString(2, 1, "分数: " + score);
        tg.putString(2, 2, "距离: " + distance);
        tg.putString(2, 3, "速度: " + gameSpeed);
        
        // 绘制车道指示器
        for (int i = 0; i < GameConfig.ROAD_WIDTH; i++) {
            String indicator = (i == player.getLane()) ? "[★]" : "[ ]";
            int laneX = GameConfig.calculateLaneX(width, height, i, height - 1);
            tg.putString(laneX - 1, height - 1, indicator);
        }
        
        screen.refresh();
    }
    
    /**
     * 渲染游戏结束界面
     */
    public void renderGameOver(int score, int distance, boolean caughtByChaser,
                               int width, int height) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);
        
        String gameOver = "游戏结束!";
        tg.putString(width / 2 - gameOver.length() / 2, height / 2 - 2, gameOver);
        
        String scoreText = "最终分数: " + score;
        tg.putString(width / 2 - scoreText.length() / 2, height / 2, scoreText);
        
        String distanceText = "奔跑距离: " + distance;
        tg.putString(width / 2 - distanceText.length() / 2, height / 2 + 1, distanceText);
        
        String restart = "按 Enter 重新开始";
        tg.putString(width / 2 - restart.length() / 2, height / 2 + 3, restart);
        
        if (caughtByChaser) {
            String[] caughtScene = {
                    "   /\\_/\\     ____  ",
                    "  ( x x )   ( >< ) ",
                    "  /  ^  \\   /||||\\ ",
                    " / |===| \\ /  ||  \\",
                    "/  |   |  \\\\  ||  /",
                    "哈基米被怪物抓住了！"
            };
            for (int i = 0; i < caughtScene.length; i++) {
                tg.putString(width / 2 - caughtScene[i].length() / 2, height / 2 - 8 + i, caughtScene[i]);
            }
        } else {
            // 绘制沮丧的哈基米
            String[] sadHakimi = {
                    "  /\\_/\\  ",
                    " ( T.T ) ",
                    "  /   \\  ",
                    " 趴下了... "
            };
            
            for (int i = 0; i < sadHakimi.length; i++) {
                tg.putString(width / 2 - 4, height / 2 - 6 + i, sadHakimi[i]);
            }
        }
        
        screen.refresh();
    }
    
    /**
     * 绘制道路
     */
    private void drawRoad(TextGraphics tg, int width, int height, int distance) {
        // 地平线
        int horizonY = GameConfig.HORIZON_OFFSET;
        for (int x = 0; x < width; x++) {
            tg.putString(x, horizonY, "¯");
        }
        
        for (int y = horizonY + 1; y < height; y++) {
            int roadWidth = GameConfig.getRoadWidthAtRow(height, y);
            int roadLeft = GameConfig.getRoadLeftAtRow(width, height, y);
            int roadRight = Math.min(width - 1, roadLeft + roadWidth);
            int clampedLeft = Math.max(0, roadLeft);
            int clampedRight = Math.max(clampedLeft + 1, Math.min(width - 1, roadRight));
            
            tg.putString(clampedLeft, y, "/");
            tg.putString(clampedRight, y, "\\");
            
            // 车道分隔线（伪3D透视）
            for (int i = 1; i < GameConfig.ROAD_WIDTH; i++) {
                int prevLane = GameConfig.calculateLaneX(width, height, i - 1, y);
                int nextLane = GameConfig.calculateLaneX(width, height, i, y);
                int laneDivider = (prevLane + nextLane) / 2;
                if ((y + distance) % 4 < 2 && laneDivider > clampedLeft && laneDivider < clampedRight) {
                    tg.putString(laneDivider, y, "|");
                }
            }
            
            // 地面纹理
            if ((y + distance) % 6 < 3) {
                for (int fillX = clampedLeft + 1; fillX < clampedRight; fillX += 2) {
                    tg.putString(fillX, y, ".");
                }
            }
        }
    }
    
    /**
     * 绘制障碍物
     */
    private void drawObstacle(TextGraphics tg, int x, int y, int type) {
        if (type == 0) {
            // 障碍物类型1：石头（低障碍）
            String[] rock = {
                    " ███ ",
                    "█████",
                    " ███ "
            };
            for (int i = 0; i < rock.length; i++) {
                if (y + i >= 0) {
                    tg.putString(x - 2, y + i, rock[i]);
                }
            }
        } else {
            // 障碍物类型2：栅栏（高障碍）
            String[] fence = {
                    " ▄▄▄ ",
                    " ███ ",
                    " ▀▀▀ "
            };
            for (int i = 0; i < fence.length; i++) {
                if (y + i >= 0) {
                    tg.putString(x - 2, y + i, fence[i]);
                }
            }
        }
    }
    
    /**
     * 计算深度因子（用于伪3D透视缩放）
     * @param screenHeight 屏幕高度
     * @param row 当前行
     * @return 深度因子，1.0表示最近（底部），0.5表示最远（地平线）
     */
    private float calculateDepthFactor(int screenHeight, int row) {
        int horizonY = GameConfig.HORIZON_OFFSET;
        if (row <= horizonY) {
            return 0.5f; // 最远
        }
        float depth = (float) (row - horizonY) / (screenHeight - horizonY);
        return 0.5f + depth * 0.5f; // 0.5到1.0之间
    }
    
    /**
     * 绘制哈基米（伪3D版本，支持透视缩放）
     */
    private void renderHakimi3D(TextGraphics tg, int x, int y, Player player, 
                               boolean isRunning, int animationSeed, float depthFactor) {
        String[] hakimi;
        
        if (isRunning) {
            String[][] runningFrames = new String[][]{
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "   /   \\   "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "  /     \\  "
                    }
            };
            int frameIndex = Math.abs((animationSeed / GameConfig.ANIMATION_FRAME_INTERVAL) % runningFrames.length);
            hakimi = runningFrames[frameIndex];
        } else {
            // 静止状态的哈基米
            hakimi = new String[]{
                    "   /\\_/\\   ",
                    "  ( ^ ^ )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        }
        
        // 根据深度因子和跳跃状态调整绘制
        float scale = depthFactor;
        if (player.isJumping()) {
            // 跳跃时稍微缩小（因为向上移动，离地平线更近）
            float jumpProgress = player.getJumpProgress();
            scale = depthFactor * (1.0f - jumpProgress * 0.2f); // 跳跃时缩小最多20%
        }
        
        // 计算缩放后的位置
        int offsetX = (int) ((hakimi[0].length() / 2) * (1.0f - scale));
        int drawX = x - offsetX;
        
        // 绘制每一行，根据缩放跳过某些行
        int skipLines = scale < 0.7f ? 1 : 0; // 如果太小，跳过一些行
        int lineIndex = 0;
        for (int i = 0; i < hakimi.length; i++) {
            if (skipLines > 0 && i % 2 == 1 && scale < 0.7f) {
                continue; // 跳过某些行以模拟缩小
            }
            int drawY = y + lineIndex;
            if (drawY >= 0 && drawY < screen.getTerminalSize().getRows()) {
                String line = hakimi[i];
                // 如果缩放较小，截取中间部分
                if (scale < 0.8f && line.length() > 5) {
                    int start = (line.length() - 5) / 2;
                    line = line.substring(start, start + 5);
                }
                tg.putString(Math.max(0, Math.min(screen.getTerminalSize().getColumns() - line.length(), drawX)), drawY, line);
            }
            lineIndex++;
        }
    }
    
    /**
     * 绘制哈基米（保留原方法用于菜单等非3D场景）
     */
    private void renderHakimi(TextGraphics tg, int x, int y, boolean isRunning, int animationSeed) {
        String[] hakimi;
        
        if (isRunning) {
            String[][] runningFrames = new String[][]{
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "   /   \\   "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "  /     \\  "
                    }
            };
            int frameIndex = Math.abs((animationSeed / GameConfig.ANIMATION_FRAME_INTERVAL) % runningFrames.length);
            hakimi = runningFrames[frameIndex];
        } else {
            // 静止状态的哈基米
            hakimi = new String[]{
                    "   /\\_/\\   ",
                    "  ( ^ ^ )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        }
        
        for (int i = 0; i < hakimi.length; i++) {
            int drawY = y + i;
            if (drawY >= 0 && drawY < screen.getTerminalSize().getRows()) {
                tg.putString(Math.max(0, Math.min(screen.getTerminalSize().getColumns() - hakimi[i].length(), x)), drawY, hakimi[i]);
            }
        }
    }
    
    private void renderChaser(TextGraphics tg, int x, int y, int frame) {
        String[][] chaserFrames = new String[][]{
                {
                        "   ____   ",
                        "  ( >< )  ",
                        "  /||||\\  ",
                        " /  ||  \\ ",
                        "/   ||   \\",
                        "   /  \\   "
                },
                {
                        "   ____   ",
                        "  ( >< )  ",
                        "  /||||\\  ",
                        " /  ||  \\ ",
                        "/   ||   \\",
                        "  /    \\  "
                }
        };
        String[] sprite = chaserFrames[Math.abs(frame % chaserFrames.length)];
        for (int i = 0; i < sprite.length; i++) {
            int drawY = y + i;
            if (drawY >= 0 && drawY < screen.getTerminalSize().getRows()) {
                tg.putString(Math.max(0, Math.min(screen.getTerminalSize().getColumns() - sprite[i].length(), x)), drawY, sprite[i]);
            }
        }
    }
}

