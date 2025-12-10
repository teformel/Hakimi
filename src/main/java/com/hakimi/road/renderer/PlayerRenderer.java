package com.hakimi.road.renderer;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.hakimi.road.entity.Chaser;
import com.hakimi.road.entity.Player;
import com.hakimi.road.util.GameConfig;

/**
 * 玩家渲染器
 * 负责渲染哈基米和追逐者
 */
public class PlayerRenderer {

    public void renderPlayer(TextGraphics tg, int width, int height, Player player, int distance) {
        int playerY = player.calculateY(height);
        int playerRow = Math.max(0, Math.min(height - 2, playerY));

        int verticalOffset = player.getVerticalOffset();
        int renderRow = Math.max(0, Math.min(height - 2, playerRow - verticalOffset));
        int playerX = GameConfig.calculateLaneX(width, height, player.getLane(), playerRow);

        float depthFactor = calculateDepthFactor(height, playerRow);
        renderHakimi3D(tg, width, height, playerX, renderRow, player, true, distance, depthFactor);
    }

    public void renderChaser(TextGraphics tg, int width, int height, Player player, Chaser chaser) {
        if (chaser == null)
            return;

        int playerY = player.calculateY(height);
        int playerRow = Math.max(0, Math.min(height - 2, playerY));

        int chaserRow = Math.max(GameConfig.HORIZON_OFFSET + 1,
                Math.min(playerRow - 5, height - 4));
        int chaserX = GameConfig.calculateLaneX(width, height, player.getLane(), chaserRow);

        renderChaserSprite(tg, width, height, chaserX - 3, chaserRow - 3, chaser.getAnimationFrame());
    }

    /**
     * 渲染菜单用的哈基米 (2D)
     */
    public void renderMenuHakimi(TextGraphics tg, int width, int height, int x, int y) {
        renderHakimi2D(tg, width, height, x, y, false, 0);
    }

    private float calculateDepthFactor(int screenHeight, int row) {
        int horizonY = GameConfig.HORIZON_OFFSET;
        if (row <= horizonY) {
            return 0.5f;
        }
        float depth = (float) (row - horizonY) / (screenHeight - horizonY);
        return 0.5f + depth * 0.5f;
    }

    private void renderHakimi3D(TextGraphics tg, int width, int height, int x, int y, Player player,
            boolean isRunning, int animationSeed, float depthFactor) {
        String[] hakimi;

        if (player.isJumping()) {
            hakimi = new String[] {
                    "   /\\_/\\   ",
                    "  ( > < )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        } else if (player.isSliding()) {
            hakimi = new String[] {
                    "           ",
                    "           ",
                    "   /\\_/\\   ",
                    "  ( - - )  ",
                    " /|=====|\\ ",
                    "/_|_____|_\\"
            };
        } else if (isRunning) {
            String[][] runningFrames = new String[][] {
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "   /   \\   "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "  //   \\\\  "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            "  /     \\  "
                    },
                    {
                            "   /\\_/\\   ",
                            "  ( o o )  ",
                            "   \\ ^ /   ",
                            "  /|===|\\  ",
                            " /_|   |_\\ ",
                            " //     \\\\ "
                    }
            };
            int frameIndex = Math.abs((animationSeed / GameConfig.ANIMATION_FRAME_INTERVAL) % runningFrames.length);
            hakimi = runningFrames[frameIndex];
        } else {
            hakimi = new String[] {
                    "   /\\_/\\   ",
                    "  ( ^ ^ )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        }

        float scale = depthFactor;
        tg.setForegroundColor(TextColor.ANSI.YELLOW);

        int skipLines = scale < 0.7f ? 1 : 0;
        int lineIndex = 0;
        for (int i = 0; i < hakimi.length; i++) {
            if (skipLines > 0 && i % 2 == 1 && scale < 0.7f) {
                continue;
            }
            int drawY = y + lineIndex;
            if (drawY >= 0 && drawY < height) {
                String line = hakimi[i];
                if (scale < 0.8f && line.length() > 5) {
                    int start = (line.length() - 5) / 2;
                    line = line.substring(start, start + 5);
                }
                int lineDrawX = x - line.length() / 2;
                tg.putString(Math.max(0, Math.min(width - line.length(), lineDrawX)),
                        drawY, line);
            }
            lineIndex++;
        }
        tg.setForegroundColor(TextColor.ANSI.WHITE);
    }

    private void renderHakimi2D(TextGraphics tg, int width, int height, int x, int y, boolean isRunning,
            int animationSeed) {
        String[] hakimi;

        if (isRunning) {
            // Simplify logic for menu, usually static
            hakimi = new String[] {
                    "   /\\_/\\   ",
                    "  ( o o )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /   \\   "
            };
        } else {
            hakimi = new String[] {
                    "   /\\_/\\   ",
                    "  ( ^ ^ )  ",
                    "   \\ ^ /   ",
                    "  /|===|\\  ",
                    " /_|   |_\\ ",
                    "   /___\\   "
            };
        }

        for (int i = 0; i < hakimi.length; i++) {
            int drawY = y + i;
            if (drawY >= 0 && drawY < height) {
                tg.putString(Math.max(0, Math.min(width - hakimi[i].length(), x)),
                        drawY, hakimi[i]);
            }
        }
    }

    private void renderChaserSprite(TextGraphics tg, int width, int height, int x, int y, int frame) {
        String[][] chaserFrames = new String[][] {
                {
                        "   ____   ",
                        "  ( >< )  ",
                        "  /||||\\  ",
                        " /  ||  \\ ",
                        " /   ||   \\", // Fixed alignment
                        "   /  \\   "
                },
                {
                        "   ____   ",
                        "  ( >< )  ",
                        "  /||||\\  ",
                        " /  ||  \\ ",
                        "/   ||   \\",
                        "  /    \\  "
                }
        };
        String[] sprite = chaserFrames[Math.abs(frame % chaserFrames.length)];
        for (int i = 0; i < sprite.length; i++) {
            int drawY = y + i;
            if (drawY >= 0 && drawY < height) {
                tg.putString(Math.max(0, Math.min(width - sprite[i].length(), x)),
                        drawY, sprite[i]);
            }
        }
    }
}
