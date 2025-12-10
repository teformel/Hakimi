package com.hakimi.road.renderer;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.hakimi.road.entity.Item;
import com.hakimi.road.entity.Obstacle;
import com.hakimi.road.entity.Scenery;
import com.hakimi.road.level.Level;
import com.hakimi.road.util.GameConfig;

public class EntityRenderer {

    public void renderObstacle(TextGraphics tg, int width, int height, Obstacle obstacle, Level level) {
        int obstacleRow = Math.max(0, Math.min(height - 2, obstacle.getY()));
        int laneX = GameConfig.calculateLaneX(width, height, obstacle.getLane(), obstacleRow);
        drawObstacleSprite(tg, laneX, obstacleRow, obstacle.getType(), level);
    }

    public void renderItem(TextGraphics tg, int width, int height, Item item) {
        int itemRow = Math.max(0, Math.min(height - 2, item.getY()));
        int laneX = GameConfig.calculateLaneX(width, height, item.getLane(), itemRow);
        drawItemSprite(tg, laneX, itemRow, item.getType());
    }

    public void renderScenery(TextGraphics tg, int width, int height, Scenery scenery) {
        int row = Math.max(0, Math.min(height - 1, scenery.getY()));
        if (row < GameConfig.HORIZON_OFFSET + 1)
            return;

        int roadLeft = GameConfig.getRoadLeftAtRow(width, height, row);
        int roadWidth = GameConfig.getRoadWidthAtRow(height, row);

        int x;
        if (scenery.getSide() == -1) {
            x = roadLeft - 5;
        } else {
            x = roadLeft + roadWidth + 5;
        }

        x = Math.max(0, Math.min(width - 1, x));

        drawScenerySprite(tg, x, row, height, scenery);
    }

    private void drawObstacleSprite(TextGraphics tg, int x, int y, int type, Level level) {
        if (type == 0) {
            tg.setForegroundColor(TextColor.ANSI.BLACK_BRIGHT);
            String[] sprite;
            if (level.getObstacleStyle() == Level.ObstacleStyle.DESERT) {
                sprite = new String[] {
                        "   ̦   ",
                        " ψΨψ ",
                        "  |  "
                };
                tg.setForegroundColor(TextColor.ANSI.GREEN);
            } else if (level.getObstacleStyle() == Level.ObstacleStyle.CYBERPUNK) {
                sprite = new String[] {
                        " ╱ ╲ ",
                        " |=| ",
                        " ╲_╱ "
                };
                tg.setForegroundColor(TextColor.ANSI.BLUE);
            } else {
                sprite = new String[] {
                        "   ▄   ",
                        "  ███  ",
                        " █████ "
                };
            }

            drawSprite(tg, x, y, sprite);
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        } else {
            tg.setForegroundColor(TextColor.ANSI.RED);
            String[] sprite;

            if (level.getObstacleStyle() == Level.ObstacleStyle.DESERT) {
                sprite = new String[] {
                        " ^o^ ",
                        " / \\ ",
                        " v v "
                };
            } else if (level.getObstacleStyle() == Level.ObstacleStyle.CYBERPUNK) {
                sprite = new String[] {
                        " <O> ",
                        " /|\\ ",
                        "  v  "
                };
                tg.setForegroundColor(TextColor.ANSI.CYAN);
            } else {
                sprite = new String[] {
                        "▀▀▀▀▀▀▀",
                        " \\ | / ",
                        "  [o]  "
                };
            }

            drawSprite(tg, x, y, sprite);
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        }
    }

    private void drawItemSprite(TextGraphics tg, int x, int y, Item.ItemType type) {
        if (type == Item.ItemType.DRIED_FISH) {
            tg.setForegroundColor(TextColor.ANSI.CYAN);
            if (y >= 0)
                tg.putString(x - 1, y, "><>");
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        } else if (type == Item.ItemType.HAGEN_ABILITY) {
            tg.setForegroundColor(TextColor.ANSI.YELLOW);
            String[] hagen = {
                    " /|\\ ",
                    "([★])",
                    " \\|/ "
            };
            drawSprite(tg, x, y, hagen);
            tg.setForegroundColor(TextColor.ANSI.WHITE);
        }
    }

    private void drawScenerySprite(TextGraphics tg, int x, int row, int height, Scenery scenery) {
        boolean isFar = row < height / 2;

        tg.setForegroundColor(TextColor.ANSI.GREEN);
        if (isFar) {
            tg.putString(x, row, "^");
        } else {
            if (row + 1 < height) {
                tg.putString(x, row, " ^ ");
                tg.putString(x, row + 1, "/|\\");
            } else {
                tg.putString(x, row, "^");
            }
        }
        tg.setForegroundColor(TextColor.ANSI.WHITE);
    }

    private void drawSprite(TextGraphics tg, int centerX, int startY, String[] sprite) {
        for (int i = 0; i < sprite.length; i++) {
            if (startY + i >= 0) {
                String line = sprite[i];
                int drawX = centerX - line.length() / 2;
                tg.putString(drawX, startY + i, line);
            }
        }
    }
}
