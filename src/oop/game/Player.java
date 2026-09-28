package oop.game;

import javax.swing.*;
import java.awt.*;

public class Player {
    private final Image playerImage;
    private final Image bowImage;
    private final Image arrowImage;
    private int x, y, width, height, speed, dy;
    private boolean up, down;
    private boolean firing;
    private long firingTimer;
    private final long firingDelay;

    private int arrowX, arrowDx;
    private final int arrowSpeed, arrowXMin, arrowXMax;

    public boolean isReadyToFire;

    public Player() {
        ImageIcon playerIcon = new ImageIcon("res/character_rectangle_green.png");
        playerImage = playerIcon.getImage();

        ImageIcon bowIcon = new ImageIcon("res/item_bow.png");
        bowImage = bowIcon.getImage();

        ImageIcon arrowIcon = new ImageIcon("res/item_arrow.png");
        arrowImage = arrowIcon.getImage();

        width = playerImage.getWidth(null);
        height = playerImage.getHeight(null);
        x = 20;
        y = (GamePanel.HEIGHT - height) / 2;
        dy = 0;
        speed = 5;

        arrowX = 75;
        arrowSpeed = 1;
        arrowDx = 0;
        arrowXMin = 55;
        arrowXMax = 75;

        isReadyToFire = true;
        firing = false;
        firingTimer = System.nanoTime();
        firingDelay = 1000;
    }

    public void setUp(boolean b) {
        up = b;
    }
    public void setDown(boolean b) {
        down = b;
    }
    public void setFiring(boolean b) {
        firing = b;
    }

    public void update() {
        // update player
        if (up) {
            dy = -speed;
        }
        if (down) {
            dy = speed;
        }
        y += dy;
        dy = 0;

        if (y < 0) {
            y = 0;
        }
        if ((y + height) > GamePanel.HEIGHT) {
            y = GamePanel.HEIGHT - height;
        }

        // update arrow
        // Prevent firing again until the arrow resets to its starting position.
        long elapsed = (System.nanoTime() - firingTimer) / 1_000_000;
        if (elapsed > firingDelay) {  // control show/hide player arrow when ready
            isReadyToFire = true;
        }
        if (firing && (arrowDx < 0 || arrowDx == 0)) {
            // When the spacebar is press/hole.
            // the player's arrow moves from right to left
            arrowDx = -arrowSpeed;
        } else {
            // When the spacebar is released,
            // the player's arrow moves from left to right,
            // but `isReadyToFire` controls whether the arrow is displayed and player can fire.
            arrowDx = arrowSpeed;

            if (isReadyToFire && arrowX < arrowXMax) {
                int maxForce = arrowXMax - arrowXMin;
                int force = arrowXMax - arrowX;

                float percentage = ((float) force / maxForce) * 100;
                int speed = Math.round(Arrow.MAX_SPEED * (percentage / 100));
                GamePanel.arrows.add(new Arrow(arrowX, y, speed));

                isReadyToFire = false;
                firingTimer =  System.nanoTime();
            }

        }
        arrowX += arrowDx;

        // Prevent arrow out of bow area
        if (arrowX < arrowXMin) {
            arrowX = arrowXMin;
        }
        if (arrowX > arrowXMax) {
            arrowX = arrowXMax;
            arrowDx = 0;
        }

        // Prevent character out of area
        if (y < Background.MIN_Y + 10) {
            y = Background.MIN_Y + 10;
        }
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(playerImage, x, y, null);
        g2d.drawImage(bowImage, x + 40, y, null);

        if (isReadyToFire) {
            g2d.drawImage(arrowImage, arrowX, y, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
