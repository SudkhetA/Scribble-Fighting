package oop.game;

import javax.swing.*;
import java.awt.*;

public class Background {
    private final Image fenceImage;
    private final Image cloudImage;

    public static int MIN_Y = 150;

    public Background() {
        ImageIcon fenceIcon = new ImageIcon("res/tile_fence.png");
        fenceImage = fenceIcon.getImage();

        ImageIcon cloudIcon = new ImageIcon("res/background_cloudA.png");
        cloudImage = cloudIcon.getImage();
    }

    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Fence
        int amount = Math.round((float) GamePanel.WIDTH / fenceImage.getWidth(null));
        for (int i = 0; i < amount; i++) {
            g2d.drawImage(fenceImage, i * fenceImage.getWidth(null), MIN_Y, null);
        }

        // Cloud
        g2d.drawImage(cloudImage, 70, 70, null);
        g2d.drawImage(cloudImage, 500, 30, null);
    }
}
