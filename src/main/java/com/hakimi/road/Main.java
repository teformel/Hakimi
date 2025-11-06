package com.hakimi.road;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.input.KeyStroke;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    // 游戏状态
    private enum GameState {
        MENU, PLAYING, GAME_OVER
    }

    // 游戏常量
    private static final int ROAD_WIDTH = 3; // 3条车道
    private static final int ROAD_LANES[] = {15, 30, 45}; // 车道x坐标
    private static final int PLAYER_START_LANE = 1; // 中间车道
    private static final int OBSTACLE_SPAWN_RATE = 10; // 障碍物生成频率

    private Screen screen;
    private GameState gameState = GameState.MENU;
    private Random random = new Random();

    // 玩家状态
    private int playerLane = PLAYER_START_LANE; // 当前所在车道(0,1,2)
    private int playerY; // 玩家y坐标（从底部向上）
    private int playerHeight = 3; // 玩家高度

    // 障碍物
    private List<Obstacle> obstacles = new ArrayList<>();
    private int gameSpeed = 1;

    // 游戏数据
    private int score = 0;
    private int distance = 0;

    // 障碍物类
    private class Obstacle {
        int lane; // 所在车道
        int y;    // y坐标
        int type; // 障碍物类型

        Obstacle(int lane, int y, int type) {
            this.lane = lane;
            this.y = y;
            this.type = type;
        }
    }

    public static void main(String[] args) {
        Main game = new Main();
        try {
            game.run();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException, InterruptedException {
        setupScreen();
        resetGame();

        // 主游戏循环
        while (true) {
            handleInput();
            updateGame();
            render();
            Thread.sleep(100); // 10fps
        }
    }

    private void setupScreen() throws IOException {
        screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal());
        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();
    }

    private void resetGame() {
        gameState = GameState.MENU;
        playerLane = PLAYER_START_LANE;
        obstacles.clear();
        score = 0;
        distance = 0;
        gameSpeed = 1;
    }

    private void startGame() {
        gameState = GameState.PLAYING;
        playerLane = PLAYER_START_LANE;
        obstacles.clear();
        score = 0;
        distance = 0;
        gameSpeed = 1;
    }

    private void handleInput() throws IOException {
        KeyStroke key = screen.pollInput();
        if (key != null) {
            switch (key.getKeyType()) {
                case Escape:
                    System.exit(0);
                    break;
                case Enter:
                    if (gameState == GameState.MENU) {
                        startGame();
                    } else if (gameState == GameState.GAME_OVER) {
                        startGame();
                    }
                    break;
                case ArrowLeft:
                    if (gameState == GameState.PLAYING && playerLane > 0) {
                        playerLane--;
                    }
                    break;
                case ArrowRight:
                    if (gameState == GameState.PLAYING && playerLane < ROAD_WIDTH - 1) {
                        playerLane++;
                    }
                    break;
            }
        }
    }

    private void updateGame() {
        if (gameState != GameState.PLAYING) return;

        // 增加距离和分数
        distance += gameSpeed;
        score = distance / 10;

        // 随距离增加游戏速度
        gameSpeed = 1 + score / 50;

        // 生成新障碍物
        if (random.nextInt(OBSTACLE_SPAWN_RATE) < gameSpeed) {
            int lane = random.nextInt(ROAD_WIDTH);
            obstacles.add(new Obstacle(lane, 0, random.nextInt(2)));
        }

        // 移动障碍物
        List<Obstacle> obstaclesToRemove = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            obstacle.y += gameSpeed;

            // 移除超出屏幕的障碍物并加分
            TerminalSize size = screen.getTerminalSize();
            if (obstacle.y > size.getRows()) {
                obstaclesToRemove.add(obstacle);
                score += 5; // 成功躲避加分
            }

            // 碰撞检测
            if (obstacle.lane == playerLane &&
                    obstacle.y >= size.getRows() - playerHeight - 2 &&
                    obstacle.y <= size.getRows() - 1) {
                gameState = GameState.GAME_OVER;
            }
        }
        obstacles.removeAll(obstaclesToRemove);
    }

    private void render() throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();
        TerminalSize size = screen.getTerminalSize();

        int width = size.getColumns();
        int height = size.getRows();
        playerY = height - playerHeight - 1; // 玩家在屏幕底部

        // 设置颜色
        tg.setForegroundColor(TextColor.ANSI.WHITE);
        tg.setBackgroundColor(TextColor.ANSI.BLACK);

        switch (gameState) {
            case MENU:
                renderMenu(tg, width, height);
                break;
            case PLAYING:
                renderGame(tg, width, height);
                break;
            case GAME_OVER:
                renderGameOver(tg, width, height);
                break;
        }

        screen.refresh();
    }

    private void renderMenu(TextGraphics tg, int width, int height) {
        // 标题
        String title = "哈基米的南北路";
        tg.putString(width / 2 - title.length() / 2, height / 2 - 5, title);

        // 游戏说明
        String[] instructions = {
                "游戏说明：",
                "- 使用 ← → 键左右移动哈基米",
                "- 躲避障碍物，跑得越远分数越高",
                "- 速度会随距离增加而变快",
                "",
                "按 Enter 开始奔跑！"
        };

        for (int i = 0; i < instructions.length; i++) {
            tg.putString(width / 2 - 15, height / 2 - 2 + i, instructions[i]);
        }

        // 绘制哈基米
        renderHakimi(tg, width / 2 - 2, height / 2 - 8, false);
    }

    private void renderGame(TextGraphics tg, int width, int height) {
        // 绘制道路和车道线
        drawRoad(tg, width, height);

        // 绘制障碍物
        for (Obstacle obstacle : obstacles) {
            drawObstacle(tg, ROAD_LANES[obstacle.lane], obstacle.y, obstacle.type);
        }

        // 绘制玩家
        renderHakimi(tg, ROAD_LANES[playerLane] - 2, playerY, true);

        // 绘制HUD
        tg.putString(2, 1, "分数: " + score);
        tg.putString(2, 2, "距离: " + distance);
        tg.putString(2, 3, "速度: " + gameSpeed);

        // 绘制车道指示器
        for (int i = 0; i < ROAD_WIDTH; i++) {
            String indicator = (i == playerLane) ? "[★]" : "[ ]";
            tg.putString(ROAD_LANES[i] - 1, height - 1, indicator);
        }
    }

    private void renderGameOver(TextGraphics tg, int width, int height) {
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
    }

    private void drawRoad(TextGraphics tg, int width, int height) {
        // 绘制道路边界
        for (int y = 0; y < height; y++) {
            tg.putString(5, y, "│");
            tg.putString(width - 5, y, "│");
        }

        // 绘制车道分隔线（虚线）
        for (int y = 0; y < height; y += 2) {
            for (int i = 1; i < ROAD_WIDTH; i++) {
                int laneDivider = 5 + (i * (width - 10)) / ROAD_WIDTH;
                tg.putString(laneDivider, y, "·");
            }
        }

        // 绘制道路背景（移动效果）
        for (int y = 0; y < height; y++) {
            if ((y + distance) % 4 == 0) {
                tg.putString(6, y, "─");
                tg.putString(width - 6, y, "─");
            }
        }
    }

    private void drawObstacle(TextGraphics tg, int x, int y, int type) {
        if (type == 0) {
            // 障碍物类型1：石头
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
            // 障碍物类型2：栅栏
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

    private void renderHakimi(TextGraphics tg, int x, int y, boolean isRunning) {
        String[] hakimi;

        if (isRunning) {
            // 奔跑状态的哈基米（两帧动画交替）
            if ((distance / 5) % 2 == 0) {
                hakimi = new String[]{
                        "  /\\_/\\  ",
                        " ( o.o ) ",
                        " /     \\ "
                };
            } else {
                hakimi = new String[]{
                        "  /\\_/\\  ",
                        " ( -.- ) ",
                        " /     \\ "
                };
            }
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