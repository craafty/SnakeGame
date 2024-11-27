import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class Snake extends Canvas implements KeyListener, Runnable {
    private static final int MAP_COLS = 16;
    private static final int MAP_ROWS = 16;
    private static final int[][] map = new int[MAP_ROWS][MAP_COLS];
    private static final int SQUARE_SIZE = 32;
    private static final int HEIGHT = (map.length) * SQUARE_SIZE+39;
    private static final int WIDTH = (map[0].length) * SQUARE_SIZE+16;
    private static final int SCOREBOARD_HEIGHT = 60;
    private static final int SCOREBOARD_WIDTH = WIDTH;
    private static final boolean PORTAL_WALLS = false;
    private int movesPerSecond;
    private int partsPerApple;
    private final ArrayList<MapPoint> snake;
    private final ArrayList<MapPoint> apples;
    BufferedReader reader;
    private int highScore;
    private char dir;
    private boolean gameOver;
    private boolean gameStart;
    private int score = 0;
    public Snake() {
        snake = new ArrayList<>();
        apples = new ArrayList<>();

        init();

        setBackground(Color.black);
        this.addKeyListener(this);
        setVisible(true);
    }
    private void init() {
        try {
            reader = new BufferedReader(new FileReader("Info/score.txt"));
            highScore = Integer.parseInt(reader.readLine());
        }catch (Exception e) {
            e.printStackTrace();
        }

        //restart thread
        new Thread(this).start();

        //reset map
        for(int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                map[r][c] = 0;
            }
        }
        gameOver = false;
        gameStart = false;
        dir = 'd';
        score = 0;
        partsPerApple = 1;
        movesPerSecond = 6;
        snake.clear();
        apples.clear();

        snake.add(new MapPoint(map.length/2, map[0].length/2));
        spawnApple();

        updateMap();
    }

    @Override
    public void update(Graphics g) {
        if (gameStart && !gameOver) {updateSnake();}
        drawGame(g);
    }

    private void drawGame(Graphics g) {
        BufferedImage back = (BufferedImage)(createImage(getWidth(), getHeight()));
        Graphics2D g2D = back.createGraphics();

        //scoreboard
        g2D.setColor(new Color(70, 70, 70));
        g2D.fillRect(0, 0, SCOREBOARD_WIDTH, SCOREBOARD_HEIGHT);
        g2D.setFont(new Font("Arial", Font.PLAIN, 20));
        if (highScore >= score) {
            g2D.setColor(Color.magenta);
            g2D.drawString("HIGH SCORE: "+ highScore, 5, 20);
        }else {
            g2D.setColor(Color.yellow);
            g2D.drawString("HIGH SCORE: "+ score, 5, 20);
        }
        g2D.setColor(Color.green);
        g2D.drawString("CURRENT SCORE: "+ score, 5, 50);

        //background
        g2D.setColor(Color.black);
        g2D.fillRect(0, SCOREBOARD_HEIGHT, WIDTH, HEIGHT);

        for(int r = 0; r < map.length; r++) {
            for (int c = 0; c < map[0].length; c++) {
                if (map[r][c] == 1) {
                    g2D.setColor(Color.green);
                }else if(map[r][c] == 2) {
                    g2D.setColor(Color.red);
                }else {
                    g2D.setColor(Color.black);
                }
                g2D.fillRect(c*SQUARE_SIZE+1, r*SQUARE_SIZE+1+SCOREBOARD_HEIGHT,
                        SQUARE_SIZE-1, SQUARE_SIZE-1);
            }
        }

        if (!gameStart) {
            Rectangle rect = new Rectangle(0, SCOREBOARD_HEIGHT, WIDTH, HEIGHT);
            Font font = new Font("Arial", Font.BOLD, 30);
            String text = "Press Any Key to Start";
            FontMetrics metrics = g2D.getFontMetrics(font);
            int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
            int y = rect.y + ((rect.height - metrics.getHeight()) / 50) + metrics.getAscent();
            g2D.setColor(Color.white);
            g2D.setFont(font);
            g2D.drawString(text, x, y);
        }
        if (gameOver) {
            Rectangle rect = new Rectangle(0, SCOREBOARD_HEIGHT, WIDTH, HEIGHT);
            Font font = new Font("Arial", Font.BOLD, 30);
            String text = "GAME OVER! (press r to reset)";
            FontMetrics metrics = g2D.getFontMetrics(font);
            int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
            int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
            g2D.setColor(Color.red);
            g2D.setFont(font);
            g2D.drawString(text, x, y);
        }

        ((Graphics2D)g).drawImage(back, null, 0, 0);
    }
    private void updateMap() {
        //reset map
        for(int r = 0; r < map.length; r++) {
            for(int c = 0; c < map[0].length; c++) {
                map[r][c] = 0;
            }
        }
        //place apples
        for(MapPoint a : apples) {
            map[a.r][a.c] = 2;
        }
        //place snake
        for(MapPoint s : snake) {
            map[s.r][s.c] = 1;
        }
    }
    private void updateSnake() {
        switch (dir) {
            case 'a' -> moveSnake(0, -1);
            case 'd' -> moveSnake(0, 1);
            case 'w' -> moveSnake(-1, 0);
            case 's' -> moveSnake(1, 0);
        }
    }
    void moveSnake(int moveR, int moveC) {
        for (int i = snake.size()-1; i >= 0; i--) {
            MapPoint s = snake.get(i);
            if (i == 0) {
                //if out of bounds
                if(s.r+moveR>=map.length || s.r+moveR<0 ||
                        s.c+moveC>=map[0].length || s.c+moveC<0) {
                    if (!PORTAL_WALLS) {
                        gameOver = true;
                        System.out.println("out of bounds");
                        break;
                    }else {
                        if (s.r+moveR>=map.length) {
                            s.move(0, s.c);
                        }else if (s.r+moveR<0) {
                            s.move(map.length-1, s.c);
                        }else if (s.c+moveC>=map[0].length) {
                            s.move(s.r, 0);
                        }else if (s.c+moveC<0) {
                            s.move(s.r, map[0].length-1);
                        }
                    }
                }
                //if next point open: move
                else if (map[s.r+moveR][s.c+moveC] == 0) {
                    s.move(s.r+moveR, s.c+moveC);
                    updateMap();
                }
                //if next point snake: die
                else if(map[s.r+moveR][s.c+moveC] == 1) {
                    gameOver = true;
                    System.out.println("collision");
                    break;
                }
                //if next point apple: move, grow, and remove apple
                else if(map[s.r+moveR][s.c+moveC] == 2) {
                    apples.removeIf(a -> a.r == s.r+moveR && a.c == s.c+moveC);
                    score++;

                    if (score % 5 == 0 && score!=0) {
                        //partsPerApple++;
                        //movesPerSecond++;
                        //spawnApple();
                    }

                    MapPoint lastPart = snake.get(snake.size()-1);
                    for (int j = 0; j < partsPerApple; j++) {
                        snake.add(new MapPoint(lastPart.r, lastPart.c));}
                    s.move(s.r+moveR, s.c+moveC);
                    spawnApple();
                    updateMap();
                }
            }else {
                MapPoint prevPart = snake.get(i-1);
                s.move(prevPart.r, prevPart.c);
                updateMap();
            }
        }
    }

    private void spawnApple() {
        int r = (int)(Math.random() * map.length);
        int c = (int)(Math.random() * map[0].length);
        while (true) {
            if (map[r][c] == 0) {
                apples.add(new MapPoint(r, c));
                break;
            }
            r = (int)(Math.random() * map.length);
            c = (int)(Math.random() * map[0].length);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!gameStart) {gameStart=true;}

        if ((e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT)
                && dir != 'd') {dir = 'a';}
        else if ((e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT)
                && dir != 'a') {dir = 'd';}
        else if ((e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP)
                && dir != 's') {dir = 'w';}
        else if ((e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN)
                && dir != 'w') {dir = 's';}
        else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {init();}
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {
        try
        {
            while(!gameOver)
            {
                Thread.currentThread().sleep(1000/movesPerSecond);

                repaint();
            }
            repaint();

            //high score change
            if (score > highScore) {
                BufferedWriter writer = new BufferedWriter(new FileWriter("Info/score.txt"));
                writer.write(String.valueOf(score));
                writer.close();
            }
            reader.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Snake Game");

        frame.setSize(WIDTH, HEIGHT+SCOREBOARD_HEIGHT);

        Snake game = new Snake();
        (game).setFocusable(true);

        frame.getContentPane().add(game);

        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    class MapPoint {
        int r;
        int c;
        public MapPoint(int r, int c) {
            this.r = r;
            this.c = c;
        }
        public void move(int r, int c) {
            this.r=r;
            this.c=c;
        }

        @Override
        public String toString() {
            return "(" + r + ", " + c + ")";
        }
    }
}