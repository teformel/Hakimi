package com.hakimi.road.system;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Player;

import java.io.IOException;

/**
 * 输入处理系统
 * 负责处理玩家输入
 */
public class InputSystem {
    private Screen screen;
    private KeyStroke lastKey; // 缓存最后一次按键
    
    public InputSystem(Screen screen) {
        this.screen = screen;
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
     * @param player 玩家对象
     * @param key 按键事件（如果为null则轮询新的输入）
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
                        player.moveToLane(player.getLane() - 1);
                    }
                    break;
                case ArrowRight:
                    if (player.getLane() < 2) {
                        player.moveToLane(player.getLane() + 1);
                    }
                    break;
                case ArrowUp:
                    // 上箭头跳跃
                    player.jump();
                    break;
                case ArrowDown:
                    // 下箭头滑铲
                    player.slide();
                    break;
                case Character:
                    // 空格键跳跃
                    if (key.getCharacter() != null && key.getCharacter() == ' ') {
                        player.jump();
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

