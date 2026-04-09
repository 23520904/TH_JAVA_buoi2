import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class FlappyBird extends JFrame implements KeyListener {    
    
    class Bird {
        private Image img;
        double x, y, width = 34, height = 24;
        private double velocityY = 0;
        private static final double GRAVITY = 0.5;
        private static final double FLAP_STRENGTH = -10;

        public Bird(String path, double initX, double initY ){
            this.img = new ImageIcon(path).getImage();
            this.x = initX;
            this.y = initY;
        }

        public void move() {
            velocityY += GRAVITY;
            y += velocityY;

            
            if (y <= 0) {
                y = 0;
                velocityY = 0; 
            }

            
            if (y >= HEIGHT - HEIGHT/5.5) {
                y = HEIGHT - HEIGHT/5.5;
            }
        }

        public void flap() {
            velocityY += FLAP_STRENGTH;
        }

        public void draw(Graphics g) {
            g.drawImage(img, (int)x, (int)y, (int)width, (int)height, null);
        }
    }

    class Pipe {
        int x, y, width = 64, height = 512;
        Image img;
        boolean passed = false;

        Pipe(Image img){ 
            this.img = img;
        }
        void draw(Graphics g) { 
            g.drawImage(img, x, y, width, height, null); 
        }
    }

    private void placePipes() {
        int pipeHeight = 512;
        int openingSpace = HEIGHT / 4;
        int randomPipeY = (int) (0 - pipeHeight / 4 - Math.random() * (pipeHeight / 2));

        Pipe topPipe = new Pipe(topPipeImg);

        topPipe.x = WIDTH; 
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.x = WIDTH;
        bottomPipe.y = randomPipeY + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    
    public FlappyBird()  {
        setTitle("Flappy Bird");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        addKeyListener(this);

        bgImg = new ImageIcon("flappybirdbg.png").getImage();
        topPipeImg = new ImageIcon("toppipe.png").getImage();
        bottomPipeImg = new ImageIcon("bottompipe.png").getImage();
        bird = new Bird("flappybird.png", WIDTH / 8, HEIGHT / 2);
        pipes = new ArrayList<>();
        
        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), null);
                bird.draw(g);
                for (Pipe p : pipes) {
                    p.draw(g);
                }

                
                g.setColor(Color.WHITE);

                
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                if (gameOver) {
                    Font bigFont = new Font("Impact", Font.PLAIN, 38);
                    g.setFont(bigFont);
                    FontMetrics metricsBig = g.getFontMetrics(bigFont);

                    String gameOverText = "GAME OVER";
                    
                    int goX = (panelWidth - metricsBig.stringWidth(gameOverText)) / 2;
                    int goY = panelHeight / 3 - 30; 

                    g.drawString(gameOverText, Math.max(0, goX), goY);

                    String scoreText = "SCORE: " + (int) score;
                    int scoreX = (panelWidth - metricsBig.stringWidth(scoreText)) / 2;
                    int scoreY = goY + 55; 
                    g.drawString(scoreText, Math.max(0, scoreX), scoreY);

                    
                    Font smallFont = new Font("Tahoma", Font.BOLD, 16);
                    g.setFont(smallFont);
                    FontMetrics metricsSmall = g.getFontMetrics(smallFont);

                    String restartText = "Press SPACE or ENTER to Restart";
                    int restartX = (panelWidth - metricsSmall.stringWidth(restartText)) / 2;
                    int restartY = scoreY + 45;
                    g.drawString(restartText, Math.max(0, restartX), restartY);

                } else {
                    Font inGameFont = new Font("Impact", Font.PLAIN, 40);
                    g.setFont(inGameFont);
                    FontMetrics metrics = g.getFontMetrics(inGameFont);
                    
                    String currentScore = String.valueOf((int) score);
                    int scoreX = (panelWidth - metrics.stringWidth(currentScore)) / 2;

                    g.drawString(currentScore, Math.max(0, scoreX), 70); 
                }
            }
        };
        add(gamePanel);

        gameLoop = new Timer(1000 / 60, e -> {
            update();
            gamePanel.repaint();
        });
        gameLoop.start();
        
        pipeSpawner = new Timer(1500, e -> placePipes());
        pipeSpawner.start();
    } 

    private void restartGame() {
        bird.y = HEIGHT / 2;
        bird.velocityY = 0;  
        pipes.clear();       
        score = 0;       
        gameOver = false;    
        gameLoop.start();    
        pipeSpawner.start();
    }

    private void update() {
        if (gameOver) return;
        bird.move();

        Rectangle birdRect = new Rectangle((int)bird.x, (int)bird.y, (int)bird.width, (int)bird.height);

        for (int i = 0; i < pipes.size(); i++) {
            Pipe p = pipes.get(i);
            p.x -= 4;

            if (!p.passed && bird.x > p.x + p.width) {
                p.passed = true;
                score += 0.5; 
            }

            Rectangle pipeRect = new Rectangle(p.x, p.y, p.width, p.height);

            if (birdRect.intersects(pipeRect)) {
                gameOver = true;
            }

            if (p.x + p.width < 0) {
                pipes.remove(i);
                i--;
            }
        }

        if (bird.y >= HEIGHT - HEIGHT/5.5 || bird.y < 0) {
            gameOver = true;
        }

        if (gameOver) {
            gameLoop.stop();
            pipeSpawner.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (gameOver) {
                restartGame();
            } else {
                bird.flap();
            }
        }
    }

    private static final int WIDTH = 360;
    private static final int HEIGHT = 640;
    private Bird bird;
    private Image topPipeImg, bottomPipeImg;
    private Image bgImg;
    private JPanel gamePanel;
    private boolean gameOver = false;
    private double score = 0;
    private ArrayList<Pipe> pipes;
    private Timer gameLoop;
    private Timer pipeSpawner;

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new FlappyBird().setVisible(true));
    }
}