package com.hakimi.road.engine;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.screen.Screen;
import com.hakimi.road.entity.Chaser;
import com.hakimi.road.entity.Item;
import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Player;
import com.hakimi.road.system.Achievement;
import com.hakimi.road.system.AchievementManager;
import com.hakimi.road.system.CollisionSystem;
import com.hakimi.road.system.ScoreSystem;
import com.hakimi.road.ui.NotificationSystem;
import com.hakimi.road.util.GameConfig;
import com.hakimi.road.util.SaveManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 游戏引擎
 * 负责游戏逻辑更新
 */
public class GameEngine {
    private static final Logger logger = LogManager.getLogger(GameEngine.class);
    private Screen screen;
    private Random random;
    private CollisionSystem collisionSystem;
    private ScoreSystem scoreSystem;

    private Player player;
    private Chaser chaser;
    private List<Obstacle> obstacles;
    private List<Item> items;
    private int gameSpeed;
    private int hitCount;
    private int chaserVisibleTimer;
    private static final int CHASER_VISIBLE_DURATION = 40;
    private boolean caughtByChaser;
    private boolean chaserAwakened;
    private int tickCounter;

    public enum GameState {
        MENU, PLAYING, GAME_OVER, PAUSED, SETTINGS, SAVE_MENU, LOAD_MENU
    }

    private GameState gameState;

    private NotificationSystem notificationSystem;

    public GameEngine(Screen screen) {
        this.screen = screen;
        this.random = new Random();
        this.collisionSystem = new CollisionSystem();
        this.scoreSystem = new ScoreSystem();
        this.player = new Player();
        this.obstacles = new ArrayList<>();
        this.items = new ArrayList<>();
        this.chaser = new Chaser();
        this.gameSpeed = GameConfig.BASE_GAME_SPEED;
        this.hitCount = 0;
        this.chaserVisibleTimer = 0;
        this.chaserAwakened = false;
        this.caughtByChaser = false;
        this.tickCounter = 0;
        this.gameState = GameState.MENU;

        // 初始化通知系统和成就管理器
        this.notificationSystem = new NotificationSystem();
        AchievementManager.getInstance().setNotificationSystem(notificationSystem);
        logger.info("GameEngine初始化完成");
    }

    /**
     * 更新游戏状态
     */
    public void update() throws IOException {
        // 更新通知系统
        notificationSystem.update();

        if (gameState != GameState.PLAYING) {
            return;
        }

        // 更新玩家状态 (每一帧都更新，保证动画流畅)
        player.update();

        // 逻辑更新 (每两帧更新一次，保持原有游戏速度平衡)
        tickCounter++;
        if (tickCounter % 2 != 0) {
            return;
        }

        // 更新分数和距离
        scoreSystem.update(gameSpeed);

        // 检查距离成就
        checkDistanceAchievements();

        // 检查分数成就
        if (scoreSystem.getScore() >= 5000) {
            AchievementManager.getInstance().unlockAchievement(Achievement.MASTER);
        }

        // 更新游戏速度
        gameSpeed = GameConfig.BASE_GAME_SPEED + scoreSystem.getScore() / GameConfig.SPEED_INCREASE_INTERVAL;

        // 生成新障碍物
        if (random.nextInt(GameConfig.OBSTACLE_SPAWN_RATE) < gameSpeed) {
            int lane = random.nextInt(GameConfig.ROAD_WIDTH);
            int type = random.nextInt(GameConfig.OBSTACLE_TYPES);
            obstacles.add(new Obstacle(lane, 0, type));
        }

        // 生成道具 (5% 概率)
        if (random.nextInt(100) < 5) {
            int lane = random.nextInt(GameConfig.ROAD_WIDTH);
            // 简单检查该车道顶部是否有障碍物，避免重叠
            boolean occupied = false;
            for (Obstacle o : obstacles) {
                if (o.getLane() == lane && o.getY() < 5) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                Item.ItemType type = (random.nextInt(10) == 0) ? Item.ItemType.HAGEN_ABILITY : Item.ItemType.DRIED_FISH;
                items.add(new Item(lane, 0, type));
            }
        }

        // 移动障碍物
        TerminalSize size = screen.getTerminalSize();
        List<Obstacle> obstaclesToRemove = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            obstacle.move(gameSpeed);
            if (obstacle.isOutOfScreen(size.getRows())) {
                obstaclesToRemove.add(obstacle);
                scoreSystem.obstacleAvoided();
            }
        }
        obstacles.removeAll(obstaclesToRemove);

        // 移动道具
        List<Item> itemsToRemove = new ArrayList<>();
        for (Item item : items) {
            item.move(gameSpeed);
            if (item.isOutOfScreen(size.getRows())) {
                itemsToRemove.add(item);
            }
        }
        items.removeAll(itemsToRemove);

        // 更新追逐者
        int playerY = player.calculateY(size.getRows());
        chaser.update(playerY, gameSpeed);

        if (chaserVisibleTimer > 0) {
            chaserVisibleTimer--;
            // 检查幸存者成就（简单模拟：如果追逐者出现且计时器快结束时还活着）
            if (chaserVisibleTimer == 1 && !caughtByChaser) {
                AchievementManager.getInstance().unlockAchievement(Achievement.SURVIVOR);
            }
        }

        // 道具收集检测
        Item collectedItem = collisionSystem.checkItemCollision(player, items, size.getRows());
        if (collectedItem != null) {
            items.remove(collectedItem);
            if (collectedItem.getType() == Item.ItemType.DRIED_FISH) {
                player.addDriedFish(1);
                // 可以加一点分数
                scoreSystem.addScore(100);
            } else if (collectedItem.getType() == Item.ItemType.HAGEN_ABILITY) {
                player.setHagenAbility(true);
                notificationSystem.addNotification("获得哈根之力!", "被人抓住时自动触发", "★", 3000,
                        com.googlecode.lanterna.TextColor.ANSI.YELLOW);
            }
        }

        // 障碍物碰撞检测
        if (!player.isInvincible()) {
            Obstacle hitObstacle = collisionSystem.getCollidedObstacle(player, obstacles, size.getRows());
            if (hitObstacle != null) {
                if (player.hasHagenAbility()) {
                    // 触发哈根能力
                    player.consumeHagen();
                    obstacles.remove(hitObstacle); // 移除障碍物，表示被哈走了

                    // 吓退追逐者
                    if (chaserAwakened) {
                        chaserAwakened = false;
                        chaserVisibleTimer = 0;
                    }

                    notificationSystem.addNotification("哈根!!!!!!", "吓退了敌人!", "⚡", 2000,
                            com.googlecode.lanterna.TextColor.ANSI.RED);
                } else {
                    handlePlayerHit();
                }
            }
        }
    }

    private void checkDistanceAchievements() {
        int distance = scoreSystem.getDistance();
        if (distance >= 10) {
            AchievementManager.getInstance().unlockAchievement(Achievement.FIRST_STEP);
        }
        if (distance >= 100) {
            AchievementManager.getInstance().unlockAchievement(Achievement.SPRINTER);
        }
        if (distance >= 1000) {
            AchievementManager.getInstance().unlockAchievement(Achievement.MARATHON);
        }
    }

    /**
     * 开始游戏
     */
    public void startGame() {
        logger.info("开始新游戏");
        gameState = GameState.PLAYING;
        player = new Player();
        obstacles.clear();
        items.clear();
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
        logger.info("重置游戏");
        gameState = GameState.MENU;
        player = new Player();
        obstacles.clear();
        items.clear();
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

    public List<Item> getItems() {
        return items;
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

    public NotificationSystem getNotificationSystem() {
        return notificationSystem;
    }

    /**
     * 进入设置界面
     */
    public void enterSettings() {
        gameState = GameState.SETTINGS;
    }

    /**
     * 返回菜单
     */
    public void returnToMenu() {
        gameState = GameState.MENU;
    }

    /**
     * 设置游戏状态（用于恢复状态）
     */
    public void setGameState(GameState state) {
        this.gameState = state;
    }

    /**
     * 进入存档菜单
     */
    public void enterSaveMenu() {
        gameState = GameState.SAVE_MENU;
    }

    /**
     * 进入读档菜单
     */
    public void enterLoadMenu() {
        gameState = GameState.LOAD_MENU;
    }

    /**
     * 保存游戏
     */
    public boolean saveGame(String saveName) {
        SaveManager.GameSaveData saveData = new SaveManager.GameSaveData();

        // 保存玩家数据
        saveData.playerLane = player.getLane();
        saveData.playerY = player.getY();
        saveData.playerState = player.getState().name();
        saveData.playerStateTimer = player.getStateTimer();
        saveData.driedFishCount = player.getDriedFishCount();
        saveData.hasHagenAbility = player.hasHagenAbility();
        // Saving health implicitly by reconstructing or we should add health to
        // SaveManager?
        // For now, let's keep it simple. If we want to save health, we need to update
        // SaveManager.
        // But since this is a new feature, maybe we should just reset health on load?
        // Actually, let's update SaveManager later if needed. For now, we assume
        // hitCount tracks lost health.
        // Wait, I should probably update SaveManager to support health if I want it
        // persisted perfectly.
        // But wait, hitCount is saved. I can reconstruct health from hitCount?
        // Yes, hitCount is saved. player.health = maxHealth - hitCount.
        // I will add logic in loadGame to sync health.

        // 保存追逐者数据
        saveData.chaserY = chaser.getY();
        saveData.chaserAnimationTick = chaser.getAnimationTick();

        // 保存游戏状态
        saveData.gameSpeed = gameSpeed;
        saveData.hitCount = hitCount;
        saveData.chaserVisibleTimer = chaserVisibleTimer;
        saveData.chaserAwakened = chaserAwakened;
        saveData.caughtByChaser = caughtByChaser;

        // 保存分数系统
        saveData.score = scoreSystem.getScore();
        saveData.distance = scoreSystem.getDistance();
        saveData.combo = scoreSystem.getCombo();

        // 保存障碍物
        saveData.obstacles = new ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            SaveManager.ObstacleData obsData = new SaveManager.ObstacleData();
            obsData.lane = obstacle.getLane();
            obsData.y = obstacle.getY();
            obsData.type = obstacle.getType();
            saveData.obstacles.add(obsData);
        }

        // 保存道具
        saveData.items = new ArrayList<>();
        for (Item item : items) {
            SaveManager.ItemData itemData = new SaveManager.ItemData();
            itemData.lane = item.getLane();
            itemData.y = item.getY();
            itemData.type = item.getType().name();
            saveData.items.add(itemData);
        }

        // 保存成就
        saveData.unlockedAchievements = AchievementManager.getInstance().getUnlockedAchievementIds();

        return SaveManager.getInstance().saveGame(saveName, saveData);
    }

    /**
     * 加载游戏
     */
    public boolean loadGame(String saveName) {
        SaveManager.GameSaveData saveData = SaveManager.getInstance().loadGame(saveName);
        if (saveData == null) {
            return false;
        }

        // 恢复玩家数据
        player.setLane(saveData.playerLane);
        player.setY(saveData.playerY);
        player.setStateFromString(saveData.playerState);
        player.setStateTimer(saveData.playerStateTimer);
        player.setDriedFishCount(saveData.driedFishCount);
        player.setHagenAbility(saveData.hasHagenAbility);

        // 恢复追逐者数据
        chaser.setY(saveData.chaserY);
        chaser.setAnimationTick(saveData.chaserAnimationTick);

        // 恢复游戏状态
        gameSpeed = saveData.gameSpeed;
        gameSpeed = saveData.gameSpeed;
        hitCount = saveData.hitCount;
        // Sync health with hitCount
        player.setHealth(player.getMaxHealth() - hitCount);
        if (player.getHealth() < 0)
            player.setHealth(0);
        chaserVisibleTimer = saveData.chaserVisibleTimer;
        chaserAwakened = saveData.chaserAwakened;
        caughtByChaser = saveData.caughtByChaser;

        // 恢复分数系统
        scoreSystem.setScore(saveData.score);
        scoreSystem.setDistance(saveData.distance);
        scoreSystem.setCombo(saveData.combo);

        // 恢复障碍物
        obstacles.clear();
        for (SaveManager.ObstacleData obsData : saveData.obstacles) {
            obstacles.add(new Obstacle(obsData.lane, obsData.y, obsData.type));
        }

        // 恢复道具
        items.clear();
        if (saveData.items != null) {
            for (SaveManager.ItemData itemData : saveData.items) {
                try {
                    Item.ItemType type = Item.ItemType.valueOf(itemData.type);
                    items.add(new Item(itemData.lane, itemData.y, type));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的道具类型
                }
            }
        }

        // 恢复成就
        AchievementManager.getInstance().mergeUnlockedAchievements(saveData.unlockedAchievements);

        // 恢复游戏状态
        if (caughtByChaser) {
            gameState = GameState.GAME_OVER;
        } else {
            gameState = GameState.PLAYING;
        }

        return true;
    }

    private void handlePlayerHit() {
        player.damage();
        hitCount = player.getMaxHealth() - player.getHealth();

        chaserAwakened = true;
        chaserVisibleTimer = CHASER_VISIBLE_DURATION;

        // 解锁受伤成就
        AchievementManager.getInstance().unlockAchievement(Achievement.OUCH);

        logger.warn("玩家受击: 剩余血量={}", player.getHealth());
        if (player.getHealth() <= 0) {
            caughtByChaser = true;
            gameState = GameState.GAME_OVER;
            logger.info("游戏结束: 血量耗尽");
        }
    }
}
