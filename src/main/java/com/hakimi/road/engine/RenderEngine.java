package com.hakimi.road.engine;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Chaser;
import com.hakimi.road.entity.Item;
import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;
import com.hakimi.road.util.GameConfig;
import com.hakimi.road.util.SaveManager;
import com.hakimi.road.util.SettingsManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 渲染引擎
 * 负责所有游戏画面的渲染
 */
public class RenderEngine {
    private Screen screen;
    private com.hakimi.road.ui.NotificationSystem notificationSystem;

    public RenderEngine(Screen screen) {
        this.screen = screen;
    }

    public void setNotificationSystem(com.hakimi.road.ui.NotificationSystem notificationSystem) {
        this.notificationSystem = notificationSystem;
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
                "按 Enter 开始游戏",
                "按 S 进入设置",
                "按 L 加载存档"
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

        // 从顶部开始显示，留出少量边距，确保所有内容都在可见区域内
        // 不再使用垂直居中，避免在Linux终端中内容超出可见区域
        int hakimiHeight = 3; // 哈基米高度
        int topMargin = 2; // 顶部边距
        int contentStartY = topMargin;

        // 计算总内容高度，如果超出屏幕，则调整起始位置
        int totalContentHeight = hakimiHeight + 2 + 1 + instructions.length; // 哈基米 + 间距(2) + 标题(1) + 说明文字
        if (contentStartY + totalContentHeight > height) {
            // 如果内容超出屏幕，从顶部开始，但确保最后一行可见
            contentStartY = Math.max(0, height - totalContentHeight - 1);
        }

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
            int drawY = instructionStartY + i;
            // 确保不超出屏幕范围
            if (drawY >= 0 && drawY < height) {
                tg.putString(contentStartX, drawY, instructions[i]);
            }
        }

        screen.refresh();
    }

    /**
     * 渲染游戏界面
     */
    public void renderGame(Player player, Chaser chaser, List<Obstacle> obstacles, List<Item> items,
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

        // 绘制道具
        for (Item item : items) {
            int itemRow = Math.max(0, Math.min(height - 2, item.getY()));
            int laneX = GameConfig.calculateLaneX(width, height, item.getLane(), itemRow);
            drawItem(tg, laneX, itemRow, item.getType());
        }

        int playerY = player.calculateY(height);
        int playerRow = Math.max(0, Math.min(height - 2, playerY));
        // 跳跃时保持在同一深度（不改变y值），通过垂直偏移显示跳跃高度
        // 垂直偏移用于在渲染时向上移动玩家，但不改变深度计算
        int verticalOffset = player.getVerticalOffset();
        // 保持playerRow不变（保持深度），只在渲染时应用垂直偏移
        int renderRow = Math.max(0, Math.min(height - 2, playerRow - verticalOffset));
        // 使用原始playerRow计算x坐标（保持透视正确），但用renderRow渲染（显示跳跃高度）
        int playerX = GameConfig.calculateLaneX(width, height, player.getLane(), playerRow);

        // 绘制追逐者
        if (showChaser && chaser != null) {
            int chaserRow = Math.max(GameConfig.HORIZON_OFFSET + 1,
                    Math.min(playerRow - 5, height - 4));
            int chaserX = GameConfig.calculateLaneX(width, height, player.getLane(), chaserRow);
            renderChaser(tg, chaserX - 3, chaserRow - 3, chaser.getAnimationFrame());
        }

        // 绘制玩家（在追逐者之后，确保位于前景）
        // 根据玩家位置计算透视效果（使用原始playerRow计算深度，保持在同一深度）
        // 跳跃时保持在同一深度，让玩家看起来是垂直向上跳，而不是向地平线移动
        float depthFactor = calculateDepthFactor(height, playerRow);
        renderHakimi3D(tg, playerX, renderRow, player, true, distance, depthFactor);

        // 绘制HUD（放在屏幕右侧，不占用跑道空间）
        int hudX = width - 20;
        tg.putString(hudX, 1, "分数: " + score);
        tg.putString(hudX, 2, "距离: " + distance);
        tg.putString(hudX, 3, "速度: " + gameSpeed);
        tg.putString(hudX, 4, "小鱼干: " + player.getDriedFishCount());

        // 绘制血量
        tg.putString(hudX, 5, "血量: ");
        tg.setForegroundColor(TextColor.ANSI.RED);
        StringBuilder hearts = new StringBuilder();
        for (int i = 0; i < player.getMaxHealth(); i++) {
            if (i < player.getHealth()) {
                hearts.append("❤");
            } else {
                hearts.append("♡");
            }
        }
        tg.putString(hudX + 6, 5, hearts.toString());
        tg.setForegroundColor(TextColor.ANSI.WHITE);

        if (player.hasHagenAbility()) {
            tg.setForegroundColor(TextColor.ANSI.YELLOW);
            tg.putString(hudX, 7, "★ 哈根之力 ★");
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        }

        // 绘制车道指示器（放在底部，不占用跑道空间）
        for (int i = 0; i < GameConfig.ROAD_WIDTH; i++) {
            String indicator = (i == player.getLane()) ? "[★]" : "[ ]";
            int laneX = GameConfig.calculateLaneX(width, height, i, height - 1);
            tg.putString(laneX - 1, height - 1, indicator);
        }

        // 渲染通知
        if (notificationSystem != null) {
            notificationSystem.render(tg, width, height);
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

        // 计算内容总高度，确保所有内容都在可见区域内
        int sceneHeight = caughtByChaser ? 6 : 4;
        int textHeight = 4; // 游戏结束文本 + 分数 + 距离 + 重启提示
        int totalHeight = sceneHeight + textHeight + 2; // 场景 + 文本 + 间距

        // 从顶部开始显示，留出边距，但如果内容太多则调整位置确保可见
        int topMargin = 2;
        int startY = topMargin;
        if (startY + totalHeight > height) {
            // 如果内容超出屏幕，调整起始位置确保最后一行可见
            startY = Math.max(0, height - totalHeight - 1);
        }

        int sceneStartY = startY;
        int textStartY = sceneStartY + sceneHeight + 2;

        // 绘制场景
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
                int drawY = sceneStartY + i;
                if (drawY >= 0 && drawY < height) {
                    tg.putString(width / 2 - caughtScene[i].length() / 2, drawY, caughtScene[i]);
                }
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
                int drawY = sceneStartY + i;
                if (drawY >= 0 && drawY < height) {
                    tg.putString(width / 2 - 4, drawY, sadHakimi[i]);
                }
            }
        }

        // 绘制文本信息
        String gameOver = "游戏结束!";
        int gameOverY = textStartY;
        if (gameOverY >= 0 && gameOverY < height) {
            tg.putString(width / 2 - gameOver.length() / 2, gameOverY, gameOver);
        }

        String scoreText = "最终分数: " + score;
        int scoreY = gameOverY + 1;
        if (scoreY >= 0 && scoreY < height) {
            tg.putString(width / 2 - scoreText.length() / 2, scoreY, scoreText);
        }

        String distanceText = "奔跑距离: " + distance;
        int distanceY = scoreY + 1;
        if (distanceY >= 0 && distanceY < height) {
            tg.putString(width / 2 - distanceText.length() / 2, distanceY, distanceText);
        }

        String restart = "按 Enter 重新开始";
        int restartY = distanceY + 2;
        if (restartY >= 0 && restartY < height) {
            tg.putString(width / 2 - restart.length() / 2, restartY, restart);
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
            // 障碍物类型1：石头（低障碍，需要跳跃）
            // 设计：底部厚重，看起来像地上的障碍
            String[] rock = {
                    "   ▄   ",
                    "  ███  ",
                    " █████ "
            };
            for (int i = 0; i < rock.length; i++) {
                if (y + i >= 0) {
                    tg.putString(x - 3, y + i, rock[i]);
                }
            }
        } else {
            // 障碍物类型2：无人机/悬挂物（高障碍，需要滑铲）
            // 设计：顶部有结构，底部悬空，暗示可以钻过去
            String[] highObstacle = {
                    "▀▀▀▀▀▀▀",
                    " \\ | / ",
                    "  [o]  "
            };
            for (int i = 0; i < highObstacle.length; i++) {
                if (y + i >= 0) {
                    tg.putString(x - 3, y + i, highObstacle[i]);
                }
            }
        }
    }

    /**
     * 绘制道具
     */
    private void drawItem(TextGraphics tg, int x, int y, Item.ItemType type) {
        if (type == Item.ItemType.DRIED_FISH) {
            tg.setForegroundColor(TextColor.ANSI.CYAN);
            if (y >= 0)
                tg.putString(x - 1, y, "><>");
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        } else if (type == Item.ItemType.HAGEN_ABILITY) {
            tg.setForegroundColor(TextColor.ANSI.YELLOW);
            String[] hagen = {
                    " /|\\ ",
                    "([★])",
                    " \\|/ "
            };
            for (int i = 0; i < hagen.length; i++) {
                if (y + i >= 0) {
                    tg.putString(x - 2, y + i, hagen[i]);
                }
            }
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        }
    }

    /**
     * 计算深度因子（用于伪3D透视缩放）
     * 
     * @param screenHeight 屏幕高度
     * @param row          当前行
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

        if (player.isJumping()) {
            // 跳跃状态
            hakimi = new String[] {
                    "   /\\_/\\   ",
                    "  ( > < )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        } else if (player.isSliding()) {
            // 滑铲状态
            hakimi = new String[] {
                    "           ",
                    "           ",
                    "   /\\_/\\   ",
                    "  ( - - )  ",
                    " /|=====|\\ ",
                    "/_|_____|_\\"
            };
        } else if (isRunning) {
            String[][] runningFrames = new String[][] {
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
                            "  //   \\\\  "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "  /     \\  "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            " //     \\\\ "
                    }
            };
            int frameIndex = Math.abs((animationSeed / GameConfig.ANIMATION_FRAME_INTERVAL) % runningFrames.length);
            hakimi = runningFrames[frameIndex];
        } else {
            // 静止状态的哈基米
            hakimi = new String[] {
                    "   /\\_/\\   ",
                    "  ( ^ ^ )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        }

        // 根据深度因子调整绘制
        // 跳跃时保持正常大小，让玩家看起来是垂直向上跳，而不是向地平线移动
        float scale = depthFactor;
        // 移除跳跃时的缩放效果，让跳跃看起来更自然（向上跳而不是向前跳）

        // 绘制每一行，根据缩放跳过某些行
        // x 是车道中心位置，对于每一行，将该行的中心对齐到这个位置
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
                // 将每一行的中心对齐到车道中心位置 x
                int lineDrawX = x - line.length() / 2;
                tg.putString(Math.max(0, Math.min(screen.getTerminalSize().getColumns() - line.length(), lineDrawX)),
                        drawY, line);
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
            String[][] runningFrames = new String[][] {
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
            hakimi = new String[] {
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
                tg.putString(Math.max(0, Math.min(screen.getTerminalSize().getColumns() - hakimi[i].length(), x)),
                        drawY, hakimi[i]);
            }
        }
    }

    private void renderChaser(TextGraphics tg, int x, int y, int frame) {
        String[][] chaserFrames = new String[][] {
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
                tg.putString(Math.max(0, Math.min(screen.getTerminalSize().getColumns() - sprite[i].length(), x)),
                        drawY, sprite[i]);
            }
        }
    }

    /**
     * 渲染设置界面
     */
    public void renderSettings(int width, int height, int selectedOption) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);

        SettingsManager settings = SettingsManager.getInstance();

        String title = "设置";
        tg.putString(width / 2 - title.length() / 2, 3, title);

        String[] options = {
                "基础游戏速度: " + settings.getBaseGameSpeed(),
                "障碍物生成频率: " + settings.getObstacleSpawnRate(),
                "速度增加间隔: " + settings.getSpeedIncreaseInterval(),
                "游戏循环延迟(ms): " + settings.getGameLoopDelayMs(),
                "显示模式: " + getDisplayModeName(settings.getDisplayMode()) + " (需重启)",
                "重置为默认值",
                "返回菜单"
        };

        int startY = height / 2 - options.length / 2;
        for (int i = 0; i < options.length; i++) {
            int x = width / 2 - options[i].length() / 2;
            int y = startY + i;

            if (i == selectedOption) {
                tg.setForegroundColor(TextColor.ANSI.YELLOW);
                tg.putString(x - 2, y, "> ");
                tg.putString(x, y, options[i]);
            } else {
                tg.setForegroundColor(TextColor.ANSI.WHITE);
                tg.putString(x, y, options[i]);
            }
        }

        String hint = "使用 ↑↓ 选择，←→ 调整数值，Enter 确认，Esc 返回";
        tg.setForegroundColor(TextColor.ANSI.CYAN);
        tg.putString(width / 2 - hint.length() / 2, height - 2, hint);

        screen.refresh();
    }

    /**
     * 渲染存档菜单
     */
    public void renderSaveMenu(int width, int height, int selectedIndex, String inputName) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);

        String title = "保存游戏";
        tg.putString(width / 2 - title.length() / 2, 3, title);

        List<String> saves = SaveManager.getInstance().getSaveList();

        int startY = 6;
        int maxVisible = height - startY - 5;
        int displayStart = Math.max(0, selectedIndex - maxVisible / 2);
        int displayEnd = Math.min(saves.size(), displayStart + maxVisible);

        // 显示存档列表
        for (int i = displayStart; i < displayEnd; i++) {
            int y = startY + (i - displayStart);
            String saveName = saves.get(i);
            long timestamp = SaveManager.getInstance().getSaveTimestamp(saveName);
            String timeStr = timestamp > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp))
                    : "未知时间";

            String saveText = String.format("%d. %s - %s", i + 1, saveName, timeStr);

            if (i == selectedIndex) {
                tg.setForegroundColor(TextColor.ANSI.YELLOW);
                tg.putString(2, y, "> " + saveText);
            } else {
                tg.setForegroundColor(TextColor.ANSI.WHITE);
                tg.putString(4, y, saveText);
            }
        }

        // 显示输入框
        String inputLabel = "存档名称: ";
        tg.setForegroundColor(TextColor.ANSI.CYAN);
        tg.putString(2, height - 4, inputLabel);
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.putString(2 + inputLabel.length(), height - 4, inputName + "_");

        String hint = "输入存档名称后按 Enter 保存，Esc 返回";
        tg.setForegroundColor(TextColor.ANSI.CYAN);
        tg.putString(width / 2 - hint.length() / 2, height - 2, hint);

        screen.refresh();
    }

    /**
     * 渲染读档菜单
     */
    public void renderLoadMenu(int width, int height, int selectedIndex) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);

        String title = "加载游戏";
        tg.putString(width / 2 - title.length() / 2, 3, title);

        List<String> saves = SaveManager.getInstance().getSaveList();

        if (saves.isEmpty()) {
            String noSaves = "没有找到存档";
            tg.putString(width / 2 - noSaves.length() / 2, height / 2, noSaves);
        } else {
            int startY = 6;
            int maxVisible = height - startY - 5;
            int displayStart = Math.max(0, selectedIndex - maxVisible / 2);
            int displayEnd = Math.min(saves.size(), displayStart + maxVisible);

            // 显示存档列表
            for (int i = displayStart; i < displayEnd; i++) {
                int y = startY + (i - displayStart);
                String saveName = saves.get(i);
                long timestamp = SaveManager.getInstance().getSaveTimestamp(saveName);
                String timeStr = timestamp > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp))
                        : "未知时间";

                String saveText = String.format("%d. %s - %s", i + 1, saveName, timeStr);

                if (i == selectedIndex) {
                    tg.setForegroundColor(TextColor.ANSI.YELLOW);
                    tg.putString(2, y, "> " + saveText);
                } else {
                    tg.setForegroundColor(TextColor.ANSI.WHITE);
                    tg.putString(4, y, saveText);
                }
            }
        }

        String hint = "使用 ↑↓ 选择，Enter 加载，D 删除，Esc 返回";
        tg.setForegroundColor(TextColor.ANSI.CYAN);
        tg.putString(width / 2 - hint.length() / 2, height - 2, hint);

        screen.refresh();
    }

    private String getDisplayModeName(int mode) {
        switch (mode) {
            case 0:
                return "自动";
            case 1:
                return "窗口";
            case 2:
                return "终端";
            default:
                return "未知";
        }
    }
}
