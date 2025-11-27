package com.hakimi.road;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.hakimi.road.engine.GameEngine;
import com.hakimi.road.engine.RenderEngine;
import com.hakimi.road.system.InputSystem;
import com.hakimi.road.util.GameConfig;

import java.io.IOException;
import javax.swing.SwingUtilities;

/**
 * 主程序入口
 * 负责初始化游戏并运行主循环
 */
public class Main {
    private Screen screen;
    private GameEngine gameEngine;
    private RenderEngine renderEngine;
    private InputSystem inputSystem;
    
    public static void main(String[] args) {
        Main game = new Main();
        try {
            game.run();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 运行游戏主循环
     */
    public void run() throws IOException, InterruptedException {
        setupScreen();
        initializeGame();
        
        // 主游戏循环
        while (true) {
            handleInput();
            gameEngine.update();
            render();
            Thread.sleep(GameConfig.GAME_LOOP_DELAY_MS);
        }
    }
    
    /**
     * 初始化屏幕
     */
    private void setupScreen() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        // 在 Windows 上强制使用 Swing 终端，避免使用 javaw 和 stty.exe 的问题
        System.setProperty("java.awt.headless", "false");
        // 设置默认终端大小
        TerminalSize size = new TerminalSize(GameConfig.TERMINAL_WIDTH, GameConfig.TERMINAL_HEIGHT);
        terminalFactory.setInitialTerminalSize(size);
        // 创建 SwingTerminal
        SwingTerminalFrame terminal = terminalFactory.createSwingTerminal();
        // 在 EDT 线程上设置终端可见，确保窗口正确初始化
        try {
            SwingUtilities.invokeAndWait(() -> {
                terminal.setVisible(true);
            });
        } catch (Exception e) {
            throw new IOException("无法初始化 Swing 终端", e);
        }
        screen = new TerminalScreen(terminal);
        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();
    }
    
    /**
     * 初始化游戏组件
     */
    private void initializeGame() {
        gameEngine = new GameEngine(screen);
        renderEngine = new RenderEngine(screen);
        inputSystem = new InputSystem(screen);
    }
    
    /**
     * 处理输入
     */
    private void handleInput() throws IOException {
        // 只轮询一次输入
        com.googlecode.lanterna.input.KeyStroke key = inputSystem.pollInput();
        
        if (key == null) {
            return; // 没有输入
        }
        
        // 检查退出（所有状态都可以退出）
        if (inputSystem.isExitPressed(key)) {
            System.exit(0);
        }
        
        GameEngine.GameState state = gameEngine.getGameState();
        
        // 根据游戏状态处理输入
        switch (state) {
            case MENU:
                if (inputSystem.isEnterPressed(key)) {
                    gameEngine.startGame();
                }
                break;
            case PLAYING:
                // 检查暂停
                if (inputSystem.isPausePressed(key)) {
                    gameEngine.togglePause();
                } else {
                    // 处理玩家输入（移动、跳跃、滑铲）
                    inputSystem.processInput(gameEngine.getPlayer(), key);
                }
                break;
            case PAUSED:
                // 暂停状态下可以继续游戏
                if (inputSystem.isPausePressed(key)) {
                    gameEngine.togglePause();
                }
                break;
            case GAME_OVER:
                if (inputSystem.isEnterPressed(key)) {
                    gameEngine.startGame();
                }
                break;
        }
    }
    
    /**
     * 渲染游戏画面
     */
    private void render() throws IOException {
        GameEngine.GameState state = gameEngine.getGameState();
        TerminalSize size = screen.getTerminalSize();
        int width = size.getColumns();
        int height = size.getRows();
        
        switch (state) {
            case MENU:
                renderEngine.renderMenu(width, height);
                break;
            case PLAYING:
            case PAUSED:
                renderEngine.renderGame(
                    gameEngine.getPlayer(),
                    gameEngine.getChaser(),
                    gameEngine.getObstacles(),
                    gameEngine.isChaserVisible(),
                    gameEngine.getScoreSystem().getScore(),
                    gameEngine.getScoreSystem().getDistance(),
                    gameEngine.getGameSpeed(),
                    width,
                    height
                );
                // 如果暂停，显示暂停提示
                if (state == GameEngine.GameState.PAUSED) {
                    String pauseText = "游戏暂停 - 按 P 继续";
                    int x = width / 2 - pauseText.length() / 2;
                    int y = height / 2;
                    screen.newTextGraphics().putString(x, y, pauseText);
                }
                break;
            case GAME_OVER:
                renderEngine.renderGameOver(
                    gameEngine.getScoreSystem().getScore(),
                    gameEngine.getScoreSystem().getDistance(),
                    gameEngine.isCaughtByChaser(),
                    width,
                    height
                );
                break;
        }
        
        screen.refresh();
    }
}