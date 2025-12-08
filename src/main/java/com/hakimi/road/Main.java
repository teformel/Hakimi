package com.hakimi.road;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;
import com.googlecode.lanterna.input.KeyType;
import com.hakimi.road.engine.GameEngine;
import com.hakimi.road.engine.RenderEngine;
import com.hakimi.road.system.InputSystem;
import com.hakimi.road.util.GameConfig;
import com.hakimi.road.util.SaveManager;
import com.hakimi.road.util.SettingsManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * 主程序入口
 * 负责初始化游戏并运行主循环
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private Screen screen;
    private GameEngine gameEngine;
    private RenderEngine renderEngine;
    private InputSystem inputSystem;

    // 设置界面状态
    private int settingsSelectedOption = 0;

    // 存档菜单状态
    private int saveMenuSelectedIndex = 0;
    private String saveInputName = "";
    private boolean isInputtingSaveName = false;
    private GameEngine.GameState stateBeforeSaveMenu = null;

    // 读档菜单状态
    private int loadMenuSelectedIndex = 0;

    public static void main(String[] args) {
        Main game = new Main();
        try {
            logger.info("Starting Hakimi Road...");
            game.run();
        } catch (IOException | InterruptedException e) {
            logger.error("Game crashed: ", e);
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
        // 设置默认终端大小
        TerminalSize size = new TerminalSize(GameConfig.TERMINAL_WIDTH, GameConfig.TERMINAL_HEIGHT);
        terminalFactory.setInitialTerminalSize(size);

        // 检测操作系统，在 Linux/WSL2 环境中使用 ANSI 终端，在 Windows 上使用 Swing 终端
        String osName = System.getProperty("os.name", "").toLowerCase();
        boolean isWindows = osName.contains("windows");
        boolean isLinux = osName.contains("linux") || osName.contains("unix");

        if (isWindows) {
            // 在 Windows 上强制使用 Swing 终端，避免使用 javaw 和 stty.exe 的问题
            System.setProperty("java.awt.headless", "false");
            try {
                // 创建 SwingTerminal
                SwingTerminalFrame terminal = terminalFactory.createSwingTerminal();
                // 在 EDT 线程上设置终端可见，确保窗口正确初始化
                SwingUtilities.invokeAndWait(() -> {
                    terminal.setVisible(true);
                });
                screen = new TerminalScreen(terminal);
            } catch (Exception e) {
                throw new IOException("无法初始化 Swing 终端", e);
            }
        } else {
            // 在 Linux/WSL2 环境中使用 ANSI 终端（不依赖 X11）
            screen = terminalFactory.createScreen();
        }

        screen.setCursorPosition(null);
        screen.startScreen();
        screen.doResizeIfNecessary();

        // 在Linux/ANSI终端中，确保画面从顶部开始显示
        // 清空屏幕并移动到顶部
        screen.clear();
        // 在Linux环境中，添加额外的清屏操作，确保显示从顶部开始
        if (isLinux) {
            // 输出ANSI转义序列，将光标移动到左上角并清屏
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
        screen.refresh();
    }

    /**
     * 初始化游戏组件
     */
    private void initializeGame() {
        gameEngine = new GameEngine(screen);
        renderEngine = new RenderEngine(screen);
        renderEngine.setNotificationSystem(gameEngine.getNotificationSystem());
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

        GameEngine.GameState state = gameEngine.getGameState();

        // 根据游戏状态处理输入
        switch (state) {
            case MENU:
                // 在主菜单时，ESC退出游戏
                if (inputSystem.isExitPressed(key)) {
                    System.exit(0);
                } else {
                    handleMenuInput(key);
                }
                break;
            case PLAYING:
                // 检查ESC返回菜单
                if (inputSystem.isExitPressed(key)) {
                    gameEngine.returnToMenu();
                } else if (inputSystem.isPausePressed(key)) {
                    // 检查暂停
                    gameEngine.togglePause();
                } else if (isSaveKey(key)) {
                    // 游戏中按S保存
                    stateBeforeSaveMenu = GameEngine.GameState.PLAYING;
                    gameEngine.enterSaveMenu();
                    saveInputName = "";
                    isInputtingSaveName = true;
                } else {
                    // 处理玩家输入（移动、跳跃、滑铲）
                    inputSystem.processInput(gameEngine.getPlayer(), key);
                }
                break;
            case PAUSED:
                // 暂停状态下可以继续游戏、保存或返回菜单
                if (inputSystem.isExitPressed(key)) {
                    gameEngine.returnToMenu();
                } else if (inputSystem.isPausePressed(key)) {
                    gameEngine.togglePause();
                } else if (isSaveKey(key)) {
                    stateBeforeSaveMenu = GameEngine.GameState.PAUSED;
                    gameEngine.enterSaveMenu();
                    saveInputName = "";
                    isInputtingSaveName = true;
                }
                break;
            case GAME_OVER:
                if (inputSystem.isExitPressed(key)) {
                    gameEngine.returnToMenu();
                } else if (inputSystem.isEnterPressed(key)) {
                    gameEngine.startGame();
                }
                break;
            case SETTINGS:
                handleSettingsInput(key);
                break;
            case SAVE_MENU:
                handleSaveMenuInput(key);
                break;
            case LOAD_MENU:
                handleLoadMenuInput(key);
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
                        gameEngine.getItems(),
                        gameEngine.isChaserVisible(),
                        gameEngine.getScoreSystem().getScore(),
                        gameEngine.getScoreSystem().getDistance(),
                        gameEngine.getGameSpeed(),
                        width,
                        height);
                // 如果暂停，显示暂停提示
                if (state == GameEngine.GameState.PAUSED) {
                    String pauseText = "游戏暂停 - 按 P 继续，按 S 保存";
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
                        height);
                break;
            case SETTINGS:
                renderEngine.renderSettings(width, height, settingsSelectedOption);
                break;
            case SAVE_MENU:
                renderEngine.renderSaveMenu(width, height, saveMenuSelectedIndex, saveInputName);
                break;
            case LOAD_MENU:
                renderEngine.renderLoadMenu(width, height, loadMenuSelectedIndex);
                break;
        }

        screen.refresh();
    }

    /**
     * 处理菜单输入
     */
    private void handleMenuInput(com.googlecode.lanterna.input.KeyStroke key) {
        if (inputSystem.isEnterPressed(key)) {
            gameEngine.startGame();
        } else if (isSettingsKey(key)) {
            gameEngine.enterSettings();
            settingsSelectedOption = 0;
        } else if (isLoadKey(key)) {
            gameEngine.enterLoadMenu();
            loadMenuSelectedIndex = 0;
        }
    }

    /**
     * 处理设置界面输入
     */
    private void handleSettingsInput(com.googlecode.lanterna.input.KeyStroke key) {
        SettingsManager settings = SettingsManager.getInstance();

        if (key.getKeyType() == KeyType.ArrowUp) {
            settingsSelectedOption = Math.max(0, settingsSelectedOption - 1);
        } else if (key.getKeyType() == KeyType.ArrowDown) {
            int maxOptions = 6;
            settingsSelectedOption = Math.min(maxOptions - 1, settingsSelectedOption + 1);
        } else if (key.getKeyType() == KeyType.ArrowLeft) {
            // 减少数值
            switch (settingsSelectedOption) {
                case 0: // 基础游戏速度
                    if (settings.getBaseGameSpeed() > 1) {
                        settings.setBaseGameSpeed(settings.getBaseGameSpeed() - 1);
                        settings.saveSettings();
                    }
                    break;
                case 1: // 障碍物生成频率
                    if (settings.getObstacleSpawnRate() > 1) {
                        settings.setObstacleSpawnRate(settings.getObstacleSpawnRate() - 1);
                        settings.saveSettings();
                    }
                    break;
                case 2: // 速度增加间隔
                    if (settings.getSpeedIncreaseInterval() > 10) {
                        settings.setSpeedIncreaseInterval(settings.getSpeedIncreaseInterval() - 10);
                        settings.saveSettings();
                    }
                    break;
                case 3: // 游戏循环延迟
                    if (settings.getGameLoopDelayMs() > 50) {
                        settings.setGameLoopDelayMs(settings.getGameLoopDelayMs() - 10);
                        settings.saveSettings();
                    }
                    break;
            }
        } else if (key.getKeyType() == KeyType.ArrowRight) {
            // 增加数值
            switch (settingsSelectedOption) {
                case 0: // 基础游戏速度
                    if (settings.getBaseGameSpeed() < 10) {
                        settings.setBaseGameSpeed(settings.getBaseGameSpeed() + 1);
                        settings.saveSettings();
                    }
                    break;
                case 1: // 障碍物生成频率
                    if (settings.getObstacleSpawnRate() < 50) {
                        settings.setObstacleSpawnRate(settings.getObstacleSpawnRate() + 1);
                        settings.saveSettings();
                    }
                    break;
                case 2: // 速度增加间隔
                    if (settings.getSpeedIncreaseInterval() < 200) {
                        settings.setSpeedIncreaseInterval(settings.getSpeedIncreaseInterval() + 10);
                        settings.saveSettings();
                    }
                    break;
                case 3: // 游戏循环延迟
                    if (settings.getGameLoopDelayMs() < 500) {
                        settings.setGameLoopDelayMs(settings.getGameLoopDelayMs() + 10);
                        settings.saveSettings();
                    }
                    break;
            }
        } else if (inputSystem.isEnterPressed(key)) {
            if (settingsSelectedOption == 4) {
                // 重置为默认值
                settings.resetToDefaults();
            } else if (settingsSelectedOption == 5) {
                // 返回菜单
                gameEngine.returnToMenu();
            }
        } else if (inputSystem.isExitPressed(key)) {
            gameEngine.returnToMenu();
        }
    }

    /**
     * 处理存档菜单输入
     */
    private void handleSaveMenuInput(com.googlecode.lanterna.input.KeyStroke key) {
        if (isInputtingSaveName) {
            if (key.getKeyType() == KeyType.Enter) {
                // 保存游戏
                if (!saveInputName.trim().isEmpty()) {
                    boolean success = gameEngine.saveGame(saveInputName.trim());
                    if (success) {
                        // 返回到之前的状态
                        if (stateBeforeSaveMenu != null) {
                            gameEngine.setGameState(stateBeforeSaveMenu);
                            stateBeforeSaveMenu = null;
                        } else {
                            gameEngine.returnToMenu();
                        }
                        saveInputName = "";
                        isInputtingSaveName = false;
                    }
                }
            } else if (key.getKeyType() == KeyType.Backspace) {
                // 删除字符
                if (saveInputName.length() > 0) {
                    saveInputName = saveInputName.substring(0, saveInputName.length() - 1);
                }
            } else if (key.getKeyType() == KeyType.Character && key.getCharacter() != null) {
                char c = key.getCharacter();
                if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                    if (saveInputName.length() < 20) {
                        saveInputName += c;
                    }
                }
            } else if (inputSystem.isExitPressed(key)) {
                // 返回到之前的状态
                if (stateBeforeSaveMenu != null) {
                    gameEngine.setGameState(stateBeforeSaveMenu);
                    stateBeforeSaveMenu = null;
                } else {
                    gameEngine.returnToMenu();
                }
                saveInputName = "";
                isInputtingSaveName = false;
            }
        } else {
            List<String> saves = SaveManager.getInstance().getSaveList();
            if (key.getKeyType() == KeyType.ArrowUp) {
                saveMenuSelectedIndex = Math.max(0, saveMenuSelectedIndex - 1);
            } else if (key.getKeyType() == KeyType.ArrowDown) {
                saveMenuSelectedIndex = Math.min(saves.size() - 1, saveMenuSelectedIndex + 1);
            } else if (inputSystem.isEnterPressed(key)) {
                isInputtingSaveName = true;
                saveInputName = "";
            } else if (inputSystem.isExitPressed(key)) {
                gameEngine.returnToMenu();
            }
        }
    }

    /**
     * 处理读档菜单输入
     */
    private void handleLoadMenuInput(com.googlecode.lanterna.input.KeyStroke key) {
        List<String> saves = SaveManager.getInstance().getSaveList();

        if (saves.isEmpty()) {
            if (inputSystem.isExitPressed(key)) {
                gameEngine.returnToMenu();
            }
            return;
        }

        if (key.getKeyType() == KeyType.ArrowUp) {
            loadMenuSelectedIndex = Math.max(0, loadMenuSelectedIndex - 1);
        } else if (key.getKeyType() == KeyType.ArrowDown) {
            loadMenuSelectedIndex = Math.min(saves.size() - 1, loadMenuSelectedIndex + 1);
        } else if (inputSystem.isEnterPressed(key)) {
            // 加载游戏
            if (loadMenuSelectedIndex >= 0 && loadMenuSelectedIndex < saves.size()) {
                String saveName = saves.get(loadMenuSelectedIndex);
                gameEngine.loadGame(saveName);
            }
        } else if (key.getKeyType() == KeyType.Character &&
                (key.getCharacter() == 'd' || key.getCharacter() == 'D')) {
            // 删除存档
            if (loadMenuSelectedIndex >= 0 && loadMenuSelectedIndex < saves.size()) {
                String saveName = saves.get(loadMenuSelectedIndex);
                SaveManager.getInstance().deleteSave(saveName);
            }
        } else if (inputSystem.isExitPressed(key)) {
            gameEngine.returnToMenu();
        }
    }

    /**
     * 检查是否是设置键（S）
     */
    private boolean isSettingsKey(com.googlecode.lanterna.input.KeyStroke key) {
        return key != null &&
                key.getKeyType() == KeyType.Character &&
                key.getCharacter() != null &&
                (key.getCharacter() == 's' || key.getCharacter() == 'S');
    }

    /**
     * 检查是否是保存键（S）
     */
    private boolean isSaveKey(com.googlecode.lanterna.input.KeyStroke key) {
        return isSettingsKey(key);
    }

    /**
     * 检查是否是加载键（L）
     */
    private boolean isLoadKey(com.googlecode.lanterna.input.KeyStroke key) {
        return key != null &&
                key.getKeyType() == KeyType.Character &&
                key.getCharacter() != null &&
                (key.getCharacter() == 'l' || key.getCharacter() == 'L');
    }
}