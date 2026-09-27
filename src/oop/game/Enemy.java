package oop.game;

import javax.swing.*;
import java.awt.*;

public class Enemy {
    private final Image enemyImage;
    private int x, y, width, height, speed, dx;

    private final int barWidth, barHeight, maxHp;
    private int barX, barY, hp;

    public Enemy(int x, int y) {
        ImageIcon enemyIcon = new ImageIcon("res/character_rectangle_red.png");
        enemyImage = enemyIcon.getImage();

        width = enemyImage.getWidth(null);
        height = enemyImage.getHeight(null);
        this.x = x;
        this.y = y;
        speed = 3;
        dx = 0;

        barWidth = 50;
        barHeight = 6;
        maxHp = 100;
        hp = 100;
    }

    public boolean update() {
        dx = speed;

        x -= dx;
        barX = x + (width - barWidth) / 2;
        barY = y - 10;

        dx = 0;

        // if enemy out of screen
        if (x < (-width + 30)) {
            return true;
        }

        return false;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(enemyImage, x + width, y, -width, height, null);

        // create hp bar
        g2d.setColor(Color.RED);
        g2d.drawRect(barX, barY, barWidth, barHeight);
        int currentHpWidth = (int) ((double) hp / maxHp * barWidth);
        g2d.fillRect(barX, barY, currentHpWidth, barHeight);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean calculateHp(int hp) {
        this.hp -= hp;
        return this.hp <= 0;
    }
}
