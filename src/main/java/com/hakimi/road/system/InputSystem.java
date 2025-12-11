package com.hakimi.road.system;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Player;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 输入处理系统
 * 负责处理玩家输入
 */
public class InputSystem {
    private static final Logger logger = LogManager.getLogger(InputSystem.class);
    private Screen screen;
    private KeyStroke lastKey; // 缓存最后一次按键

    public InputSystem(Screen screen) {
        this.screen = screen;
        logger.debug("InputSystem初始化");
    }

    /**
     * 轮询输入，返回当前按键（如果存在）
     */
    public KeyStroke pollInput() throws IOException {
        lastKey = screen.pollInput();
        return lastKey;
    }

    /**
     * 获取最后一次按键
     */
    public KeyStroke getLastKey() {
        return lastKey;
    }

    /**
     * 处理输入并更新玩家状态
     * 
     * @param player 玩家对象
     * @param key    按键事件（如果为null则轮询新的输入）
     */
    public void processInput(Player player, KeyStroke key) throws IOException {
        // 如果没有传入按键，则轮询新的输入
        if (key == null) {
            key = pollInput();
        }

        if (key != null) {
            switch (key.getKeyType()) {
                case ArrowLeft:
                    if (player.getLane() > 0) {
                        logger.trace("处理输入: 左移");
                        player.moveToLane(player.getLane() - 1);
                    }
                    break;
                case ArrowRight:
                    if (player.getLane() < 2) {
                        logger.trace("处理输入: 右移");
                        player.moveToLane(player.getLane() + 1);
                    }
                    break;
                case ArrowUp:
                    // 上箭头跳跃
                    logger.trace("处理输入: 跳跃(上箭头)");
                    player.jump();
                    break;
                case ArrowDown:
                    // 下箭头滑铲
                    logger.trace("处理输入: 滑铲(下箭头)");
                    player.slide();
                    break;
                case Character:
                    if (key.getCharacter() != null && key.getCharacter() == ' ') {
                        logger.trace("处理输入: 跳跃(空格)");
                        player.jump();
                    } else if (key.getCharacter() != null && (key.getCharacter() == 'a' || key.getCharacter() == 'A')) {
                        // A controls
                        // We will handle this in GameEngine by checking last key or we can add a method
                        // here
                        // Actually, InputSystem processes input and updates Player.
                        // But turning is a GameEngine level concern (RoadManager).
                        // So we should expose if A or D was pressed.
                    }
                    break;
                default:
                    // 其他按键不做处理
                    break;
            }
        }
    }

    /**
     * 检查是否按下退出键
     */
    public boolean isExitPressed(KeyStroke key) {
        return key != null && key.getKeyType() == KeyType.Escape;
    }

    /**
     * 检查是否按下确认键（Enter）
     */
    public boolean isEnterPressed(KeyStroke key) {
        return key != null && key.getKeyType() == KeyType.Enter;
    }

    /**
     * 检查是否按下暂停键（P）
     */
    public boolean isPausePressed(KeyStroke key) {
        return key != null &&
                key.getKeyType() == KeyType.Character &&
                key.getCharacter() != null &&
                (key.getCharacter() == 'p' || key.getCharacter() == 'P');
    }
}
