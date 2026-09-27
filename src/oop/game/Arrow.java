package oop.game;

import javax.swing.*;
import java.awt.*;

public class Arrow {
    private Image arrowImage;
    private int x, y, width, height, speed, dx, dy;
    public static int MAX_SPEED = 10;

    public Arrow(int x, int y, int speed) {
        ImageIcon arrowIcon = new ImageIcon("res/item_arrow.png");
        arrowImage = arrowIcon.getImage();

        width = arrowImage.getWidth(null);
        height = arrowImage.getHeight(null);
        this.x = x;
        this.y = y;
        dx = 0;
        dy = 0;
        this.speed = speed;
    }

    public boolean update() {
        dx = speed;
        dy = 0;

        x += dx;
        y += dy;

        dx = 0;
        dy = 0;

        // if bullet out of left screen
        if (x > GamePanel.WIDTH) {
            return true;
        }
        return false;
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(arrowImage, x, y, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getSpeed() { return speed; }


}
