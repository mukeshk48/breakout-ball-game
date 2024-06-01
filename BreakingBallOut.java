import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;

public class BreakingBallOut extends JPanel implements ActionListener {
    private static final int BALL_SIZE = 20;
    private static final int PADDLE_SPEED = 20;
    private static final double BALL_SPEED_INCREMENT = 0.5;
    private static final int BRICK_OFFSET_X = 30;
    private static final int BRICK_OFFSET_Y = 50;
    private static final int SCORE_BRICK = 20;
    private static final int SCORE_PADDLE = 10;
    private static final int SCORE_LEVEL_COMPLETE = 50;
    private static final int LEVEL_CAP = 100;
    private static final int OBSTACLE_WIDTH = 50;
    private static final int OBSTACLE_HEIGHT = 20;
    private boolean flag = true;

    private int ballX, ballY;
    private double ballXVelocity = 1.00, ballYVelocity = -1.00;
    private int paddleX, paddleY;
    private int paddleWidth, paddleHeight;
    private int brickRows = 3, brickCols = 7;
    private int brickWidth, brickHeight;
    private int[][] bricks;
    private Timer timer;
    private int score = 0;
    private int level = 1;
    private boolean gameRunning = true;
    private boolean levelCompleted = false;
    private boolean isPaused = false;
    private boolean gameStarted = false;
    private List<Rectangle> movingObstacles = new ArrayList<>();

    private Clip brickBreakSound, paddleHitSound, powerUpSound;

    private Color[] brickColors = {
        Color.RED, Color.ORANGE, Color.CYAN, Color.GREEN, Color.BLUE,
        Color.YELLOW, Color.MAGENTA, Color.PINK, Color.LIGHT_GRAY, Color.WHITE
    };

    public BreakingBallOut(int frameWidth, int frameHeight) {
        ballX = frameWidth / 2 - BALL_SIZE / 2;
        ballY = frameHeight / 2 - BALL_SIZE / 2;
        paddleWidth = frameWidth / 5;
        paddleHeight = frameHeight / 50;
        paddleX = frameWidth / 2 - paddleWidth / 2;
        paddleY = frameHeight - frameHeight / 10;
        brickWidth = (frameWidth - 2 * BRICK_OFFSET_X) / brickCols;
        brickHeight = frameHeight / 25;

        timer = new Timer(5, this);
        timer.start();
        addKeyListener(new MyKeyAdapter());
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        initBricks(1);
        loadSounds();
    }

    private void loadSounds() {
        try {
            brickBreakSound = AudioSystem.getClip();
            brickBreakSound.open(AudioSystem.getAudioInputStream(new File("sounds/brickBreak.wav")));

            paddleHitSound = AudioSystem.getClip();
            paddleHitSound.open(AudioSystem.getAudioInputStream(new File("sounds/paddleHit.wav")));

            powerUpSound = AudioSystem.getClip();
            powerUpSound.open(AudioSystem.getAudioInputStream(new File("sounds/powerUp.wav")));
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void initBricks(int level) {
        bricks = new int[brickRows][brickCols];
        switch (level) {
            case 1:
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        bricks[i][j] = 1;
                    }
                }
                break;
            case 2:
            
            
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        if (i % 2 == 0 || j % 2 == 0) {
                            bricks[i][j] = 1;
                        }
                    }
                }
                break;
            case 3:
 
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        if (i == j||i>0) {
                            bricks[i][j] = 1;
                        }
                    }
                }
                break;
            case 4:
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        if ((i + j) % 3 == 0) {
                            bricks[i][j] = 1;
                        }
                    }
                }
                break;
            case 5:
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        if (i == brickRows - 1 || j == brickCols - 1) {
                            bricks[i][j] = 1;
                        }
                    }
                }
                break;
            case 6:
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        if ((i + j) % 2 == 0 && i != brickRows - 1 && j != brickCols - 1) {
                            bricks[i][j] = 1;
                        }
                    }
                }
                break;
            case 7:
                Random random = new Random();
                for (int i = 0; i < 3; i++) {
                    int obstacleX = random.nextInt(getWidth() - OBSTACLE_WIDTH);
                    int obstacleY = random.nextInt(getHeight() / 2);
                    movingObstacles.add(new Rectangle(obstacleX, obstacleY, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));
                }
                break;
            default:
                random = new Random();
                for (int i = 0; i < brickRows; i++) {
                    for (int j = 0; j < brickCols; j++) {
                        bricks[i][j] = random.nextInt(2);
                    }
                }
                break;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        g.setColor(Color.BLUE);
        g.fillRect(paddleX, paddleY, paddleWidth, paddleHeight);

        for (int i = 0; i < brickRows; i++) {
            for (int j = 0; j < brickCols; j++) {
                if (bricks[i][j] == 1) {
                    g.setColor(brickColors[level - 1]);
                    g.fillRect(j * brickWidth + BRICK_OFFSET_X, i * brickHeight + BRICK_OFFSET_Y, brickWidth, brickHeight);
                }
            }
        }

        for (Rectangle obstacle : movingObstacles) {
            g.setColor(Color.YELLOW);
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);

        g.drawString("Level: " + level, getWidth() - 100, 30);

        if (!gameRunning && !levelCompleted && score > 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over", getWidth() / 2 - 100, getHeight() / 2 - 30);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press 'R' to Restart or 'E' to Exit and 'P' to pause/resume", getWidth() / 2 - 150, getHeight() / 2 + 20);
        }

        if (levelCompleted) {
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press 'N' for Next Level, 'R' to Restart or 'E' to Exit", getWidth() / 2 - 150, getHeight() / 2 + 20);
        }

        if (isPaused && gameRunning) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Paused", getWidth() / 2 - 100, getHeight() / 2 - 30);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press 'P' to Resume", getWidth() / 2 - 100, getHeight() / 2 + 20);
        }

        if (!gameRunning && !levelCompleted && flag) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Press 'R' to Start the Game", getWidth() / 2 - 150, getHeight() / 2);
            g.setFont(new Font("Arial", Font.BOLD,20));
            g.drawString("Press 'P' to Pause/Resume and 'E' to Exit", getWidth() / 2 - 150, getHeight() / 2 + 30);
            flag = false;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!isPaused && gameRunning) {
            gameStarted = true;
            ballX += ballXVelocity;
            ballY += ballYVelocity;

            if (ballX <= 0 || ballX >= getWidth() - BALL_SIZE) {
                ballXVelocity = -ballXVelocity;
            }
            if (ballY <= 0) {
                ballYVelocity = -ballYVelocity;
            }
            if (ballY >= getHeight() - BALL_SIZE) {
                gameRunning = false;
            }

            Rectangle ballRect = new Rectangle(ballX, ballY, BALL_SIZE, BALL_SIZE);
            Rectangle paddleRect = new Rectangle(paddleX, paddleY, paddleWidth, paddleHeight);

            if (ballRect.intersects(paddleRect)) {
                ballYVelocity = -ballYVelocity;
                paddleHitSound.start();
                paddleHitSound.setFramePosition(0);
            }

            for (int i = 0; i < brickRows; i++) {
                for (int j = 0; j < brickCols; j++) {
                    if (bricks[i][j] == 1) {
                        int brickX = j * brickWidth + BRICK_OFFSET_X;
                        int brickY = i * brickHeight + BRICK_OFFSET_Y;
                        Rectangle brickRect = new Rectangle(brickX, brickY, brickWidth, brickHeight);

                        if (ballRect.intersects(brickRect)) {
                            bricks[i][j] = 0;
                            ballYVelocity = -ballYVelocity;
                            score += SCORE_BRICK;
                            brickBreakSound.start();
                            brickBreakSound.setFramePosition(0);

                            boolean levelComplete = true;
                            for (int[] row : bricks) {
                                for (int brick : row) {
                                    if (brick == 1) {
                                        levelComplete = false;
                                        break;
                                    }
                                }
                                if (!levelComplete) {
                                    break;
                                }
                            }
                            if (levelComplete) {
                                levelCompleted = true;
                                gameRunning = false;
                            }
                            break;
                        }
                    }
                }
            }

            for (Rectangle obstacle : movingObstacles) {
                obstacle.y += level;
                if (obstacle.y >= getHeight()) {
                    obstacle.y = 0;
                    obstacle.x = new Random().nextInt(getWidth() - OBSTACLE_WIDTH);
                }
                if (ballRect.intersects(obstacle)) {
                    ballYVelocity = -ballYVelocity;
                }
            }

            repaint();
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT && paddleX > 0) {
                paddleX -= PADDLE_SPEED;
            }
            if (key == KeyEvent.VK_RIGHT && paddleX < getWidth() - paddleWidth) {
                paddleX += PADDLE_SPEED;
            }
            if (key == KeyEvent.VK_R) {
                gameRunning = true;
                levelCompleted = false;
                isPaused = false;
                gameStarted = true;
                ballX = getWidth() / 2 - BALL_SIZE / 2;
                ballY = getHeight() / 2 - BALL_SIZE / 2;
                ballXVelocity = 2;
                ballYVelocity = -2;
                score = 0;
                level = 1;
                movingObstacles.clear();
                initBricks(1);
            }
            if (key == KeyEvent.VK_N && levelCompleted) {
                level++;
                if (level > 7) {
                    level = 1;
                }
                levelCompleted = false;
                isPaused = false;
                ballX = getWidth() / 2 - BALL_SIZE / 2;
                ballY = getHeight() / 2 - BALL_SIZE / 2;
                ballXVelocity = Math.abs(ballXVelocity) +  (double)(BALL_SPEED_INCREMENT * 1.0);
                ballYVelocity = -(Math.abs(ballYVelocity) + (double) (BALL_SPEED_INCREMENT * 1.0));
                movingObstacles.clear();
                initBricks(level);
                gameRunning = true;
            }
           
           
            if (key == KeyEvent.VK_P) {
                isPaused = !isPaused;
            }
            if (key == KeyEvent.VK_E) {
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Breaking Ball Out");
        BreakingBallOut game = new BreakingBallOut(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(game);
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Welcome to Breakout Ball Game"));
}
    }

