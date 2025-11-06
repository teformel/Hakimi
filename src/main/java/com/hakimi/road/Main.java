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

public class Main {
    // 游戏状态
    private enum GameState {
        MENU, PLAYING, GAME_OVER
    }

    private Screen screen;
    private GameState gameState = GameState.MENU;

    // 玩家位置
    private int playerX = 10;
    private int playerY = 15;
    private boolean isJumping = false;
    private int jumpCounter = 0;
    private final int jumpHeight = 5;

    // 障碍物
    private int obstacleX = 50;
    private int obstacleSpeed = 1;

    // 分数
    private int score = 0;

    public static void main(String[] args) {
        Main game = new Main();
        try {
            game.run();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() throws IOException, InterruptedException {
        // 初始化屏幕
        setupScreen();

        // 主游戏循环
        while (true) {
            // 处理输入
            handleInput();

            // 更新游戏状态
            updateGame();

            // 渲染画面
            render();

            // 控制帧率
            Thread.sleep(50); // 约20fps
        }
    }

    private void setupScreen() throws IOException {
        screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal());
        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();
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
                        gameState = GameState.PLAYING;
                    } else if (gameState == GameState.GAME_OVER) {
                        resetGame();
                        gameState = GameState.PLAYING;
                    }
                    break;
                case ArrowUp:
                case Character:
                    if (key.getKeyType() == KeyType.Character && key.getCharacter() == ' ') {
                        if (gameState == GameState.PLAYING && !isJumping) {
                            isJumping = true;
                            jumpCounter = 0;
                        }
                    }
                    break;
            }
        }
    }

    private void updateGame() {
        if (gameState != GameState.PLAYING) return;

        // 更新玩家跳跃
        if (isJumping) {
            if (jumpCounter < jumpHeight) {
                playerY--;
            } else if (jumpCounter < jumpHeight * 2) {
                playerY++;
            } else {
                isJumping = false;
            }
            jumpCounter++;
        }

        // 更新障碍物
        obstacleX -= obstacleSpeed;
        if (obstacleX < 0) {
            obstacleX = 80; // 屏幕宽度
            score++;
            obstacleSpeed = 1 + score / 5; // 随分数增加速度
        }

        // 碰撞检测
        if (obstacleX >= playerX && obstacleX <= playerX + 3 &&
                playerY >= 13 && playerY <= 15) {
            gameState = GameState.GAME_OVER;
        }
    }

    private void render() throws IOException {
        screen.clear();
        TextGraphics tg = screen.newTextGraphics();

        TerminalSize size = screen.getTerminalSize();
        int width = size.getColumns();
        int height = size.getRows();

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
        tg.putString(width / 2 - title.length() / 2, height / 2 - 3, title);

        // 菜单选项
        String startInfo = "按 Enter 开始游戏";
        tg.putString(width / 2 - startInfo.length() / 2, height / 2, startInfo);

        String controlInfo = "按空格键跳跃，ESC退出";
        tg.putString(width / 2 - controlInfo.length() / 2, height / 2 + 2, controlInfo);

        // 绘制简单的哈基米字符画
        String[] hakimiArt = {
                "  /\\_/\\  ",
                " ( o.o ) ",
                "  > ^ <  "
        };

        for (int i = 0; i < hakimiArt.length; i++) {
            tg.putString(width / 2 - 4, height / 2 - 6 + i, hakimiArt[i]);
        }
    }

    private void renderGame(TextGraphics tg, int width, int height) {
        // 绘制地面
        for (int x = 0; x < width; x++) {
            tg.putString(x, 16, "_");
        }

        // 绘制玩家（哈基米）
        String[] playerArt = isJumping ?
                new String[]{" /\\_/\\ ", "(-.-) ", "  |   "} :
                new String[]{" /\\_/\\ ", "(-.-) ", "/   \\ "};

        for (int i = 0; i < playerArt.length; i++) {
            tg.putString(playerX, playerY + i, playerArt[i]);
        }

        // 绘制障碍物
        String[] obstacleArt = {
                " ███ ",
                "█████",
                " ███ "
        };

        for (int i = 0; i < obstacleArt.length; i++) {
            tg.putString(obstacleX, 13 + i, obstacleArt[i]);
        }

        // 显示分数
        tg.putString(2, 1, "分数: " + score);
        tg.putString(2, 2, "速度: " + obstacleSpeed);
    }

    private void renderGameOver(TextGraphics tg, int width, int height) {
        String gameOver = "游戏结束!";
        tg.putString(width / 2 - gameOver.length() / 2, height / 2 - 2, gameOver);

        String scoreText = "最终分数: " + score;
        tg.putString(width / 2 - scoreText.length() / 2, height / 2, scoreText);

        String restart = "按 Enter 重新开始";
        tg.putString(width / 2 - restart.length() / 2, height / 2 + 2, restart);
    }

    private void resetGame() {
        playerX = 10;
        playerY = 15;
        obstacleX = 50;
        score = 0;
        obstacleSpeed = 1;
        isJumping = false;
    }
}