package oop.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable , KeyListener {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 450;

    private Thread thread;
    private final int FPS = 30;

    private BufferedImage image;
    private Graphics2D g;
    private long createTimer, createDelay;

    private boolean inGame;
    private long hiScore;
    private int score;
    private final String HI_SCORE_FILE = "scores.txt";

    public static Background background;
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

        background = new  Background();

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
        score = 0;
        inGame = true;
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
        checkPlayerCollideEnemy();
    }

    private void gameRender() {
        if (inGame) {
            g.setColor(Color.white);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(Color.black);
            g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
            g.drawString("Score : " + score, 10, 20);

            background.render(g);

            player.render(g);

            // Bullet render.
            for (int i = 0; i < arrows.size(); i++) {
                arrows.get(i).render(g);
            }

            // Enmemy render.
            for (int i = 0; i < enemies.size(); i++) {
                enemies.get(i).render(g);
            }
        } else {
            drawGameOverScreen();
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
            if (inGame) {
                player.setFiring(false);
            } else {
                player = new Player();
                score = 0;
                enemies.clear();
                inGame = true;
            }

        }
    }

    private void createEnemies() {
        long elapsed = (System.nanoTime() - createTimer) / 1_000_000;
        if (elapsed > createDelay) {
            int y = (int)(Math.random() * HEIGHT);

            if (y < Background.MIN_Y + 10) { // 160 px margin is reserved above the character for background
                y = Background.MIN_Y + 10;
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

                        score++;
                    }
                }
            }
        }
    }

    private void checkPlayerCollideEnemy() {
        Rectangle pRect = player.getBounds();
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy =  enemies.get(i);
            Rectangle eRect = enemy.getBounds();

            if (pRect.intersects(eRect)) {
                inGame = false;
                getHiScore();
                writeHiScore();
                getHiScore();

                arrows.clear();
                enemies.clear();
            }
        }
    }

    private void writeHiScore() {
        if (score > hiScore) {
            File file = new File(HI_SCORE_FILE);
            try(BufferedWriter buffWriter = new BufferedWriter(new FileWriter(file))) {
                buffWriter.write("HiScore:" + score);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getHiScore() {
        File file = new File(HI_SCORE_FILE);
        try(BufferedReader buffReader = new BufferedReader(new FileReader(file))) {
            String[] value = buffReader.readLine().split(":");
            hiScore = Integer.parseInt(value[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawGameOverScreen() {
        g.setColor(Color.white);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Scribble Fighting
        g.setColor(Color.black);
        g.setFont(new Font("Century Gothic", Font.BOLD, 26));
        String strSF = "Scribble Fighting";
        int lengthMIG = (int)g.getFontMetrics().getStringBounds(strSF, g).getWidth();
        g.drawString(strSF, (WIDTH - lengthMIG) / 2, 40);

        // Game Over
        g.setFont(new Font("Century Gothic", Font.BOLD, 24));
        String strGameOver = "Game Over";
        int lengthGameOver = (int)g.getFontMetrics().getStringBounds(strGameOver, g).getWidth();
        g.drawString(strGameOver, (WIDTH - lengthGameOver) / 2, 120);

        // High Score
        g.setFont(new Font("Century Gothic", Font.BOLD, 18));
        String strHS = "High Score: " + hiScore;
        int lengthHS = (int)g.getFontMetrics().getStringBounds(strHS, g).getWidth();
        g.drawString(strHS, (WIDTH - lengthHS) / 2, 150);

        // Score
        g.setFont(new Font("Century Gothic", Font.BOLD, 18));
        String strScore = "Score: " + score;
        int lengthScore = (int)g.getFontMetrics().getStringBounds(strScore, g).getWidth();
        g.drawString(strScore, (WIDTH - lengthScore) / 2, 180);

        // Press spacebar to replay
        g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
        String strReplay = "Press spacebar to replay.";
        int lengthReplay = (int)g.getFontMetrics().getStringBounds(strReplay, g).getWidth();
        g.drawString(strReplay, (WIDTH - lengthReplay) / 2, HEIGHT - 20);
    }
}
