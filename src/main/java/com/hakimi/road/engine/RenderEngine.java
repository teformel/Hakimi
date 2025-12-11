package com.hakimi.road.engine;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Chaser;
import com.hakimi.road.entity.Item;
import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;
import com.hakimi.road.entity.Scenery;
import com.hakimi.road.level.Level;
import com.hakimi.road.renderer.EntityRenderer;
import com.hakimi.road.renderer.HudRenderer;
import com.hakimi.road.renderer.PlayerRenderer;
import com.hakimi.road.renderer.RoadRenderer;

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

    private final RoadRenderer roadRenderer;
    private final EntityRenderer entityRenderer;
    private final PlayerRenderer playerRenderer;
    private final HudRenderer hudRenderer;

    public RenderEngine(Screen screen) {
        this.screen = screen;
        this.roadRenderer = new RoadRenderer();
        this.entityRenderer = new EntityRenderer();
        this.playerRenderer = new PlayerRenderer();
        this.hudRenderer = new HudRenderer();
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
                "- 使用 A / D 键在急转弯处转向",
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
        playerRenderer.renderMenuHakimi(tg, width, height, hakimiX, hakimiY);

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
    public void renderGame(Level level, Player player, Chaser chaser, List<Obstacle> obstacles, List<Item> items,
            List<Scenery> sceneryList,
            boolean showChaser, int score, int distance, int gameSpeed, double curvature,
            int width, int height) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        tg.setForegroundColor(TextColor.ANSI.WHITE);

        // 绘制道路
        roadRenderer.render(tg, width, height, distance, level, curvature);

        // 绘制障碍物
        for (Obstacle obstacle : obstacles) {
            entityRenderer.renderObstacle(tg, width, height, obstacle, level, curvature);
        }

        // 绘制道具
        for (Item item : items) {
            entityRenderer.renderItem(tg, width, height, item, curvature);
        }

        // 绘制风景
        for (Scenery scenery : sceneryList) {
            entityRenderer.renderScenery(tg, width, height, scenery, curvature);
        }

        // 绘制玩家
        playerRenderer.renderPlayer(tg, width, height, player, distance);

        // 绘制追逐者
        if (showChaser && chaser != null) {
            playerRenderer.renderChaser(tg, width, height, player, chaser);
        }

        // 绘制HUD
        hudRenderer.renderHud(tg, width, height, player, score, distance, gameSpeed);

        // 渲染通知
        if (notificationSystem != null) {
            hudRenderer.renderNotifications(tg, width, height, notificationSystem);
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
