package com.hakimi.road.engine;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
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
        renderHakimi(tg, hakimiX, hakimiY, false);
        
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
    public void renderGame(Player player, List<Obstacle> obstacles, 
                          int score, int distance, int gameSpeed, int width, int height) throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);
        
        // 计算车道位置（根据屏幕宽度动态计算）
        int[] roadLanes = GameConfig.calculateRoadLanes(width);
        
        // 绘制道路
        drawRoad(tg, width, height, distance);
        
        // 绘制障碍物
        for (Obstacle obstacle : obstacles) {
            drawObstacle(tg, roadLanes[obstacle.getLane()], obstacle.getY(), obstacle.getType());
        }
        
        // 绘制玩家
        int playerY = player.calculateY(height);
        renderHakimi(tg, roadLanes[player.getLane()] - 2, playerY, true);
        
        // 绘制HUD
        tg.putString(2, 1, "分数: " + score);
        tg.putString(2, 2, "距离: " + distance);
        tg.putString(2, 3, "速度: " + gameSpeed);
        
        // 绘制车道指示器
        for (int i = 0; i < GameConfig.ROAD_WIDTH; i++) {
            String indicator = (i == player.getLane()) ? "[★]" : "[ ]";
            tg.putString(roadLanes[i] - 1, height - 1, indicator);
        }
        
        screen.refresh();
    }
    
    /**
     * 渲染游戏结束界面
     */
    public void renderGameOver(int score, int distance, int width, int height) throws IOException {
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
        
        screen.refresh();
    }
    
    /**
     * 绘制道路
     */
    private void drawRoad(TextGraphics tg, int width, int height, int distance) {
        int roadLeft = GameConfig.ROAD_MARGIN;
        int roadRight = width - GameConfig.ROAD_MARGIN;
        
        // 绘制道路边界
        for (int y = 0; y < height; y++) {
            tg.putString(roadLeft, y, "│");
            tg.putString(roadRight, y, "│");
        }
        
        // 计算车道位置
        int[] roadLanes = GameConfig.calculateRoadLanes(width);
        
        // 绘制车道分隔线（虚线）
        for (int y = 0; y < height; y += 2) {
            for (int i = 1; i < GameConfig.ROAD_WIDTH; i++) {
                // 在车道之间绘制分隔线（使用车道位置的中点）
                int laneDivider = (roadLanes[i - 1] + roadLanes[i]) / 2;
                tg.putString(laneDivider, y, "·");
            }
        }
        
        // 绘制道路背景（移动效果）
        for (int y = 0; y < height; y++) {
            if ((y + distance) % 4 == 0) {
                tg.putString(roadLeft + 1, y, "─");
                tg.putString(roadRight - 1, y, "─");
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
     * 绘制哈基米
     */
    private void renderHakimi(TextGraphics tg, int x, int y, boolean isRunning) {
        String[] hakimi;
        
        if (isRunning) {
            // 奔跑状态的哈基米（两帧动画交替）
            // 这里可以根据距离计算动画帧
            hakimi = new String[]{
                    "  /\\_/\\  ",
                    " ( o.o ) ",
                    " /     \\ "
            };
        } else {
            // 静止状态的哈基米
            hakimi = new String[]{
                    "  /\\_/\\  ",
                    " ( ^.^ ) ",
                    " /     \\ "
            };
        }
        
        for (int i = 0; i < hakimi.length; i++) {
            tg.putString(x, y + i, hakimi[i]);
        }
    }
}

