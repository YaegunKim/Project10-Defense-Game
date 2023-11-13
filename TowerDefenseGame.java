import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TowerDefenseGame extends Frame {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int ENEMY_WIDTH = 80;
    private static final int ENEMY_HEIGHT = 120;
    private static final int BOSS_SIZE_MULTIPLIER = 3;
    private static final int BOSS_HEALTH_MULTIPLIER = 3;

    private List<Tower> towers;
    private List<Projectile> projectiles;
    private List<Enemy> enemies;
    private BossEnemy bossEnemy;
    private boolean isBossSpawned;
    private boolean hasSpawnedBoss;

    public TowerDefenseGame() {
        setTitle("Tower Defense Game");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                addTower(mouseX, mouseY);
                repaint();
            }
        });

        towers = new ArrayList<>();
        projectiles = new ArrayList<>();
        enemies = new ArrayList<>();
        bossEnemy = null;
        isBossSpawned = false;
        hasSpawnedBoss = false;

        enemies.add(new Enemy(WINDOW_WIDTH, WINDOW_HEIGHT / 2));

        gameLoop();
    }

    public void gameLoop() {
        while (true) {
        // Spawn boss enemy
        if (!hasSpawnedBoss && enemies.isEmpty()) {
            spawnBoss();
            hasSpawnedBoss = true;
        }

            // Update towers
            for (Tower tower : towers) {
                // Check if tower can attack an enemy
                // Check if tower can attack an enemy
if (tower.canAttack()) {
    Enemy target = tower.findTarget(enemies, isBossSpawned, bossEnemy);
    if (target != null) {
        shoot(tower.getX(), tower.getY(), target.getX(), target.getY());
    }
}

            }

            // Update projectiles
            Iterator<Projectile> projectileIterator = projectiles.iterator();
            while (projectileIterator.hasNext()) {
                Projectile projectile = projectileIterator.next();
                projectile.move();
                if (projectile.getX() > WINDOW_WIDTH) {
                    projectileIterator.remove();
                }
            }

            // Update enemies
            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                enemy.move();

                // Check for collision between enemies and projectiles
                Iterator<Projectile> collisionIterator = projectiles.iterator();
                while (collisionIterator.hasNext()) {
                    Projectile projectile = collisionIterator.next();
                    if (Math.abs(enemy.getX() - projectile.getX()) < 10 && Math.abs(enemy.getY() - projectile.getY()) < 10) {
                        // Enemy destroyed
                        enemy.takeDamage(projectile.getDamage());
                        if (enemy.isDestroyed()) {
                            collisionIterator.remove();
                            enemyIterator.remove();
                        }
                        break;
                    }
                }

                if (enemy.getX() < 0) {
                    // Enemy reached the end
                    // Handle game over or other logic here
                    enemyIterator.remove();
                }
            }

            // Update boss enemy
            if (isBossSpawned) {
                bossEnemy.move();

                // Check for collision between boss enemy and projectiles
                Iterator<Projectile> collisionIterator = projectiles.iterator();
                while (collisionIterator.hasNext()) {
                    Projectile projectile = collisionIterator.next();
                    if (Math.abs(bossEnemy.getX() - projectile.getX()) < 10 && Math.abs(bossEnemy.getY() - projectile.getY()) < 10) {
                        // Boss enemy takes damage
                        bossEnemy.takeDamage(projectile.getDamage());
                        if (bossEnemy.isDestroyed()) {
                            collisionIterator.remove();
                            isBossSpawned = false;
                        }
                        break;
                    }
                }
            }

            repaint();

            try {
                Thread.sleep(10); // Adjust the game speed as needed
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addTower(int x, int y) {
        towers.add(new Tower(x, y));
    }

    public void shoot(int towerX, int towerY, int targetX, int targetY) {
        projectiles.add(new Projectile(towerX, towerY, targetX, targetY));
    }

    public void spawnBoss() {
        int bossX = WINDOW_WIDTH;
        int bossY = WINDOW_HEIGHT / 2;
        int bossHealth = bossEnemy != null ? bossEnemy.getHealth() : 0;
        bossEnemy = new BossEnemy(bossX, bossY, bossHealth);
        isBossSpawned = true;
    }

    public void paint(Graphics g) {
        super.paint(g);

        // Draw towers
        for (Tower tower : towers) {
            drawTurret(g, tower.getX(), tower.getY());
        }

        // Draw projectiles
        for (Projectile projectile : projectiles) {
            drawProjectile(g, projectile.getX(), projectile.getY());
        }

        // Draw enemies
        for (Enemy enemy : enemies) {
            if (enemy instanceof OakEnemy) {
                drawOak(g, enemy.getX(), enemy.getY());
            } else {
                drawRectangle(g, enemy.getX(), enemy.getY(), ENEMY_WIDTH, ENEMY_HEIGHT, Color.RED);
            }
        }

        // Draw boss enemy
        if (isBossSpawned && bossEnemy != null) {
            int bossSize = ENEMY_WIDTH * BOSS_SIZE_MULTIPLIER;
            int bossHealthBarWidth = bossSize;
            int bossHealthBarHeight = 10;
            int bossHealthBarX = bossEnemy.getX() - bossSize / 2;
            int bossHealthBarY = bossEnemy.getY() + ENEMY_HEIGHT;

            drawRectangle(g, bossHealthBarX, bossHealthBarY, bossHealthBarWidth, bossHealthBarHeight, Color.BLACK);
            int remainingHealthWidth = (int) ((double) bossEnemy.getHealth() / bossEnemy.getMaxHealth() * bossHealthBarWidth);
            drawRectangle(g, bossHealthBarX, bossHealthBarY, remainingHealthWidth, bossHealthBarHeight, Color.GREEN);

            drawRectangle(g, bossEnemy.getX() - bossSize / 2, bossEnemy.getY() - bossSize / 2, bossSize, bossSize, Color.ORANGE);
        }

        // Draw victory text
        if (!isBossSpawned && enemies.isEmpty()) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fontMetrics = g.getFontMetrics();
            String victoryText = "VICTORY!";
            int textWidth = fontMetrics.stringWidth(victoryText);
            int textX = (WINDOW_WIDTH - textWidth) / 2;
            int textY = WINDOW_HEIGHT / 2;
            g.drawString(victoryText, textX, textY);
        }
    }
        
    private void drawRectangle(Graphics g, int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    private void drawOak(Graphics g, int x, int y) {
        // Draw oak-shaped enemy
        g.setColor(Color.GREEN);
        g.fillOval(x - ENEMY_WIDTH / 2, y - ENEMY_HEIGHT / 2, ENEMY_WIDTH, ENEMY_HEIGHT);
    }

    private void drawTurret(Graphics g, int x, int y) {
        // Draw turret-shaped tower
        g.setColor(Color.BLUE);
        g.fillRect(x - 10, y - 10, 20, 20);
    }

    private void drawProjectile(Graphics g, int x, int y) {
        // Draw projectile
        g.setColor(Color.GREEN);
        g.fillOval(x - 5, y - 5, 10, 10);
    }
        

    public static void main(String[] args) {
        new TowerDefenseGame();
    }

    private static class Tower {
        private static final int ATTACK_RANGE = 100;
        private static final int ATTACK_COOLDOWN = 1000; // in milliseconds

        private int x;
        private int y;
        private long lastAttackTime;

        public Tower(int startX, int startY) {
            x = startX;
            y = startY;
            lastAttackTime = 0;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean canAttack() {
            long currentTime = System.currentTimeMillis();
            return currentTime - lastAttackTime >= ATTACK_COOLDOWN;
        }

public Enemy findTarget(List<Enemy> enemies, boolean isBossSpawned, BossEnemy bossEnemy) {
    for (Enemy enemy : enemies) {
        int distanceX = Math.abs(enemy.getX() - x);
        int distanceY = Math.abs(enemy.getY() - y);
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        if (distance <= ATTACK_RANGE) {
            return enemy;
        }
    }
    
    // Check if boss enemy is within range
    if (isBossSpawned && bossEnemy != null) {
        int distanceX = Math.abs(bossEnemy.getX() - x);
        int distanceY = Math.abs(bossEnemy.getY() - y);
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        if (distance <= ATTACK_RANGE) {
            return bossEnemy;
        }
    }
    
    return null;
}


    }

    private static class Projectile {
        private static final int SPEED = 5;
        private static final int DAMAGE = 1;

        private int x;
        private int y;
        private int targetX;
        private int targetY;
        private double direction;

        public Projectile(int startX, int startY, int targetX, int targetY) {
            x = startX;
            y = startY;
            this.targetX = targetX;
            this.targetY = targetY;

            double angle = Math.atan2(targetY - y, targetX - x);
            direction = angle;
        }

        public void move() {
            x += SPEED * Math.cos(direction);
            y += SPEED * Math.sin(direction);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getDamage() {
            return DAMAGE;
        }
    }

    private static class Enemy {
        private static final int SPEED = 2;
        protected static final int MAX_HEALTH = 1;

        protected int x;
        protected int y;
        protected int health;

        public Enemy(int startX, int startY) {
            x = startX;
            y = startY;
            health = MAX_HEALTH;
        }

        public void move() {
            x -= SPEED;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void takeDamage(int damage) {
            health -= damage;
        }

        public boolean isDestroyed() {
            return health <= 0;
        }
    }

    private static class BossEnemy extends Enemy {
        public BossEnemy(int startX, int startY, int health) {
            super(startX, startY);
            this.health = health * BOSS_HEALTH_MULTIPLIER;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getHealth() {
            return health;
        }

        public int getMaxHealth() {
            return MAX_HEALTH * BOSS_HEALTH_MULTIPLIER;
        }
    }
}
