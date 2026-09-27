package oop.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable , KeyListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 300;

    private Thread thread;
    private final int FPS = 30;

    private BufferedImage image;
    private Graphics2D g;
    private long createTimer, createDelay;

    public static Player player;
    public static ArrayList<Arrow> arrows;
    public static ArrayList<Enemy> enemies;

    public GamePanel() {
        super();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
    }

    @Override
    public void run() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g = (Graphics2D)image.getGraphics();

        player = new Player();

        arrows = new ArrayList<>();

        enemies = new ArrayList<>();

        createTimer = System.nanoTime();
        createDelay = 2000;

        long startTime;
        long elapsed;
        long waitTime;

        long targetTime = 1_000 / FPS;

        while(true) {
            startTime = System.nanoTime();

            gameUpdate();
            gameRender();
            gameDraw();

            elapsed = (System.nanoTime() - startTime) / 1_000_000;
            waitTime = targetTime - elapsed;

            try {
                Thread.sleep(waitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addNotify() {
        super.addNotify();
        if(thread == null) {
            thread = new Thread(this);
            thread.start();
        }
        addKeyListener(this);
    }

    private void gameUpdate() {
        player.update();

        // Bullet update.
        for (int i = 0; i < arrows.size(); i++) {
            boolean removeBullet = arrows.get(i).update();
            if (removeBullet) {
                arrows.remove(i);
                i--;
            }
        }

        // Enemy update
        createEnemies();
        for (int i = 0; i < enemies.size(); i++) {
            boolean removeEnemy =  enemies.get(i).update();
            if (removeEnemy) {
                enemies.remove(i);
                i--;
            }
        }

        // Collision Detection.
        checkArrowCollideEnemy();
    }

    private void gameRender() {
        g.setColor(Color.white);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        player.render(g);

        // Bullet render.
        for (int i = 0; i < arrows.size(); i++) {
            arrows.get(i).render(g);
        }

        // Enmemy render.
        for (int i = 0; i < enemies.size(); i++) {
            enemies.get(i).render(g);
        }
    }

    private void gameDraw() {
        Graphics g2 = this.getGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if(keyCode == KeyEvent.VK_UP) {
            player.setUp(true);
        }

        if (keyCode == KeyEvent.VK_DOWN) {
            player.setDown(true);
        }

        if (keyCode == KeyEvent.VK_SPACE) {
            player.setFiring(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if(keyCode == KeyEvent.VK_UP) {
            player.setUp(false);
        }

        if (keyCode == KeyEvent.VK_DOWN) {
            player.setDown(false);
        }

        if (keyCode == KeyEvent.VK_SPACE) {
            player.setFiring(false);
        }
    }

    private void createEnemies() {
        long elapsed = (System.nanoTime() - createTimer) / 1_000_000;
        if (elapsed > createDelay) {
            int y = (int)(Math.random() * HEIGHT);

            if (y < 10) { // 10 px margin is reserved above the character for the HP bar
                y = 10;
            } else if (y > HEIGHT - 64) { // 64 is the character height; keep the whole character inside the screen
                y = HEIGHT - 64;
            }

            enemies.add(new Enemy(WIDTH - 20, y));
            createTimer = System.nanoTime();
        }
    }

    private void checkArrowCollideEnemy() {
        for (int i = 0; i < arrows.size(); i++) {
            Arrow arrow = arrows.get(i);
            Rectangle aRect = arrow.getBounds();

            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);
                Rectangle eRect = enemy.getBounds();

                if (aRect.intersects(eRect)) {
                    arrows.remove(i);
                    i--;

                    int arrowSpeed = arrow.getSpeed();
                    int percentage = Math.round(((float) arrowSpeed / Arrow.MAX_SPEED) * 100);

                    boolean result = enemy.calculateHp(percentage);
                    if (result) {
                        enemies.remove(j);
                        j--;
                    }
                }
            }
        }
    }
}
