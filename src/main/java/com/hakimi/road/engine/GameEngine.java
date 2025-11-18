package com.hakimi.road.engine;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Chaser;
import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;
import com.hakimi.road.system.CollisionSystem;
import com.hakimi.road.system.ScoreSystem;
import com.hakimi.road.util.GameConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏引擎
 * 负责游戏逻辑更新
 */
public class GameEngine {
    private Screen screen;
    private Random random;
    private CollisionSystem collisionSystem;
    private ScoreSystem scoreSystem;
    
    private Player player;
    private Chaser chaser;
    private List<Obstacle> obstacles;
    private int gameSpeed;
    private int hitCount;
    private int chaserVisibleTimer;
    private static final int CHASER_VISIBLE_DURATION = 40;
    private boolean caughtByChaser;
    private boolean chaserAwakened;
    
    public enum GameState {
        MENU, PLAYING, GAME_OVER, PAUSED
    }
    
    private GameState gameState;
    
    public GameEngine(Screen screen) {
        this.screen = screen;
        this.random = new Random();
        this.collisionSystem = new CollisionSystem();
        this.scoreSystem = new ScoreSystem();
        this.player = new Player();
        this.obstacles = new ArrayList<>();
        this.chaser = new Chaser();
        this.gameSpeed = GameConfig.BASE_GAME_SPEED;
        this.hitCount = 0;
        this.chaserVisibleTimer = 0;
        this.chaserAwakened = false;
        this.caughtByChaser = false;
        this.gameState = GameState.MENU;
    }
    
    /**
     * 更新游戏状态
     */
    public void update() throws IOException {
        if (gameState != GameState.PLAYING) {
            return;
        }
        
        // 更新玩家状态
        player.update();
        
        // 更新分数和距离
        scoreSystem.update(gameSpeed);
        
        // 更新游戏速度
        gameSpeed = GameConfig.BASE_GAME_SPEED + scoreSystem.getScore() / GameConfig.SPEED_INCREASE_INTERVAL;
        
        // 生成新障碍物
        if (random.nextInt(GameConfig.OBSTACLE_SPAWN_RATE) < gameSpeed) {
            int lane = random.nextInt(GameConfig.ROAD_WIDTH);
            int type = random.nextInt(GameConfig.OBSTACLE_TYPES);
            obstacles.add(new Obstacle(lane, 0, type));
        }
        
        // 移动障碍物
        TerminalSize size = screen.getTerminalSize();
        List<Obstacle> obstaclesToRemove = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            obstacle.move(gameSpeed);
            
            // 移除超出屏幕的障碍物并加分
            if (obstacle.isOutOfScreen(size.getRows())) {
                obstaclesToRemove.add(obstacle);
                scoreSystem.obstacleAvoided();
            }
        }
        obstacles.removeAll(obstaclesToRemove);
        
        // 更新追逐者
        int playerY = player.calculateY(size.getRows());
        chaser.update(playerY, gameSpeed);
        
        if (chaserVisibleTimer > 0) {
            chaserVisibleTimer--;
        }
        
        // 碰撞检测
        if (collisionSystem.checkCollision(player, obstacles, size.getRows())) {
            handlePlayerHit();
        }
    }
    
    /**
     * 开始游戏
     */
    public void startGame() {
        gameState = GameState.PLAYING;
        player = new Player();
        obstacles.clear();
        scoreSystem.reset();
        hitCount = 0;
        chaserVisibleTimer = 0;
        chaserAwakened = false;
        caughtByChaser = false;
        gameSpeed = GameConfig.BASE_GAME_SPEED;
        TerminalSize size = screen.getTerminalSize();
        chaser.reset(player.calculateY(size.getRows()));
    }
    
    /**
     * 重置游戏
     */
    public void resetGame() {
        gameState = GameState.MENU;
        player = new Player();
        obstacles.clear();
        scoreSystem.reset();
        hitCount = 0;
        chaserVisibleTimer = 0;
        chaserAwakened = false;
        caughtByChaser = false;
        gameSpeed = GameConfig.BASE_GAME_SPEED;
        TerminalSize size = screen.getTerminalSize();
        chaser.reset(player.calculateY(size.getRows()));
    }
    
    /**
     * 切换暂停状态
     */
    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
        }
    }
    
    // Getters
    public GameState getGameState() {
        return gameState;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public List<Obstacle> getObstacles() {
        return obstacles;
    }
    
    public ScoreSystem getScoreSystem() {
        return scoreSystem;
    }
    
    public int getGameSpeed() {
        return gameSpeed;
    }
    
    public Chaser getChaser() {
        return chaser;
    }
    
    public boolean isChaserVisible() {
        return chaserAwakened && chaserVisibleTimer > 0 && gameState == GameState.PLAYING;
    }
    
    public boolean isCaughtByChaser() {
        return caughtByChaser;
    }
    
    private void handlePlayerHit() {
        hitCount++;
        chaserAwakened = true;
        chaserVisibleTimer = CHASER_VISIBLE_DURATION;
        if (hitCount >= 3) {
            caughtByChaser = true;
            gameState = GameState.GAME_OVER;
        }
    }
}

