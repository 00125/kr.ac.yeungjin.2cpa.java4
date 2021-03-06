package org.game.map;
 
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import org.game.CanvasView;
import org.game.DrawableObject;
import org.game.Game;
import org.game.geom.Circle;
import org.game.geom.Rect;
import org.game.object.Ghost;
import org.game.object.Player;
import org.game.math.Line2D;
import org.game.math.Point2D;
import org.game.map.TileMap;
import org.game.map.TileNode;
import org.game.math.Matrix2D;
import org.game.object.Wall;
import org.game.util.IntersectionUtil;

public class Map implements DrawableObject {

    public static final boolean DEBUG = true;
    public static final int MAP_WIDTH = 800;
    public static final int MAP_HEIGHT = 600;
    
    public static final int TILE_COLUMNS = (int) (MAP_WIDTH / 11.0);
    public static final int TILE_ROWS = (int) (MAP_HEIGHT / 11.0);
    
    private List<Wall> wall = new ArrayList();
    private TileMap tiles = new TileMap(TILE_COLUMNS, TILE_ROWS);
    
    private Player player;
    private List<Ghost> mobs = new ArrayList<>();
    
    public Map() { 
        // 기본적으로 맵의 테두리를 만들고...
        // 북쪽
        wall.add(new Wall(0, 0, MAP_WIDTH - 10, 10));
        
        // 동쪽
        wall.add(new Wall(MAP_WIDTH - 10, 0, 10, MAP_HEIGHT));
        
        // 남쪽
        wall.add(new Wall(0, MAP_HEIGHT - 10, MAP_WIDTH, 10));
        
        // 서쪽
        wall.add(new Wall(0, 0, 10, MAP_HEIGHT - 10));
        
        // 장애물 1
        wall.add(new Wall(300, 200, 10, 400));
        
        Wall w = new Wall(507, 0, 10, 500);
        
        w.transform(Matrix2D.rotate(Math.toRadians(30)));
        
        // 장애물 2
        wall.add(w);
        
        Ghost m;
        
        m = new Ghost(this);
        m.getPosition().setX(50);
        m.getPosition().setY(50);
        mobs.add(m);
        
        m = new Ghost(this);
        m.getPosition().setX(760);
        m.getPosition().setY(560);
        mobs.add(m);
        
        m = new Ghost(this);
        m.getPosition().setX(50);
        m.getPosition().setY(550);
        mobs.add(m);
        
        for(Line2D l : getWall2()) {
            Point2D t1 = getTileIndexByPoint2D(l.getX1(), l.getY1());
            Point2D t2 = getTileIndexByPoint2D(l.getX2(), l.getY2());

            for (Point2D p : IntersectionUtil.getBresenhamLines(t1.getX(), t1.getY(), t2.getX(), t2.getY())) {
                if (tiles.isWithin(p.getX(), p.getY())) {
                    tiles.getNode(p.getX(), p.getY()).setNotWalkable();
                }
            }
        }
    }
    
    public void setPlayer(Player p) {
        this.player = p;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int getTileWidth() {
        return MAP_WIDTH / TILE_COLUMNS;
    }
    
    public int getTileHeight() {
        return MAP_HEIGHT / TILE_ROWS;
    }
    
    public Point2D getTileIndexByPoint2D(Point2D p) {
        return getTileIndexByPoint2D(p.getX(), p.getY());
    }
    
    public Point2D getTileIndexByPoint2D(int x, int y) {
        return new Point2D((int) (x / getTileWidth())
                         , (int) (y / getTileHeight()));
    }

    public List<Wall> getWall() {
        return wall;
    }
    
    public List<Line2D> getWall2() {
        List<Line2D> l = new ArrayList<>();
        
        for(Wall w : wall) {
            
            Point2D p1 = w.getVertex().get(w.getVertex().size() - 1);
            Point2D p2 = w.getVertex().get(0);

            l.add(new Line2D(p1.getX(), p1.getY(), p2.getX(), p2.getY()));
                
            for(int n = 1; n < w.getVertex().size(); ++n) {
                p1 = w.getVertex().get(n - 1);
                p2 = w.getVertex().get(n);
                
                l.add(new Line2D(p1.getX(), p1.getY(), p2.getX(), p2.getY()));
            }
        }
        
        return l;
    }
    
//    public List<Line2D> getObstacle() { 
//        List<Line2D> l = new ArrayList<>(getWall());
//        
//        return l;
//    }
    
    public List<Point2D> getPath(Point2D p1, Point2D p2) {
        return getPath(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
    
    public List<Point2D> getPath(int x1, int y1, int x2, int y2) {
        Point2D p1 = getTileIndexByPoint2D(x1, y1);
        Point2D p2 = getTileIndexByPoint2D(x2, y2);
        
        List<Point2D> l = new ArrayList<>();
        
        for(TileNode n : tiles.getPath(p1.getX(), p1.getY(), p2.getX(), p2.getY())) {
            l.add(new  Point2D(n.getX() * getTileWidth() + (getTileWidth() / 2) , n.getY() * getTileHeight() + (getTileHeight() / 2)));
        }
        
        return l;
    }
    

    @Override
    public void draw(CanvasView c, Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
         
        if (Game.DEBUG && Map.DEBUG) { 
            g2d.setColor(new Color(255, 0, 0, (int) (255 * 0.20)));

            for (int y = 0; y < tiles.getRows(); ++y) {
                for (int x = 0; x < tiles.getColumns(); ++x) {

                    TileNode n = tiles.getNode(x, y);

                    if (n.canWalk()) {
                        g2d.drawRect(getTileWidth() * x, getTileHeight() * y, getTileWidth(), getTileHeight());
                    }
                    else {
                        g2d.fillRect(getTileWidth() * x, getTileHeight() * y, getTileWidth(), getTileHeight());
                    }
                }
            }
        }

        for (Wall w : wall) {
            w.draw(c, g2d);
        } 
        
        for(Ghost g : mobs) {
            g.draw(c, g2d);
        }
    }

}
