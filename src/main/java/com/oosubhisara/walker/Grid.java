package com.oosubhisara.walker;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.oosubhisara.algorithm.Bfs;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Grid {
    public static class Position {
        public int row, column;

        public Position(int row, int column) {
            this.row = row;
            this.column = column;
        }
        
        public String toString() {
            return String.format("%d, %d", this.row, this.column);
        }
    }

    public static final char TILE_EMPTY = '.';
    public static final char TILE_START = 'A';
    public static final char TILE_TARGET = 'B';
    public static final char TILE_WALL = 'W';
    
    private static final Color BG_COLOR = Color.BLACK;
    private static final Color FLOOR_COLOR = Color.GRAY;
    private static final Color START_COLOR = Color.BLUE;
    private static final Color TARGET_COLOR = Color.RED;
    private static final Color WALL_COLOR = Color.LIGHTGRAY;
    private static final Color TILE_TEXT_COLOR = Color.WHITE;

    private GraphicsContext g;
    private int numRows;
    private int numColumns;
    private int cellSize;
    private List<char[]> cells; 
    private Grid.Position start;
    private Grid.Position target;
    private List<Grid.Position> dirtyCells;
    private List<Grid.Position> pathCells;
    private Bfs.Node[][] nodes;
    private boolean needRedraw;
    
    public Grid(int numRows, int numColumns, int cellSize, GraphicsContext g) {
        super();
        this.g = g;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.cellSize = cellSize;
        this.start = null;
        this.target = null;
        this.dirtyCells = new ArrayList<Grid.Position>();
        this.pathCells = new ArrayList<Grid.Position>();
        this.needRedraw = true;
        this.cells = new ArrayList<char[]>();
        this.ResetCells();
    }
    
    public int getNumRows() {
        return this.numRows;
    }

    public int getNumColumns() {
        return this.numColumns;
    }
    
    public Grid.Position getStart() {
        return this.start;
    }
    
    public void setStart(Grid.Position position) {
        char oldTile = getAt(position);

        if (oldTile == TILE_EMPTY) {
            // Clear previous start tile
            if (this.start != null) {
                setAt(this.start, Grid.TILE_EMPTY);
            }

            setAt(position, Grid.TILE_START);
            this.start = new Grid.Position(position.row, position.column);
        }
    }
    
    public Grid.Position getTarget() {
        return this.target;
    }

    public void setTarget(Grid.Position position) {
        char oldTile = getAt(position);

        if (oldTile == TILE_EMPTY) {
            // Clear previous target tile
            if (this.target != null) {
                setAt(this.target, Grid.TILE_EMPTY);
            }

            setAt(position, Grid.TILE_TARGET);
            this.target = new Grid.Position(position.row, position.column);
        }
    }

    public void setWall(Grid.Position position) {
        char oldTile = getAt(position);
        if (oldTile != Grid.TILE_START && oldTile != Grid.TILE_TARGET) {
            setAt(position, Grid.TILE_WALL);
        }
    }

    public void setEmpty(Grid.Position position) {
        char oldTile = getAt(position);
        if (oldTile != Grid.TILE_START && oldTile != Grid.TILE_TARGET) {
            setAt(position, Grid.TILE_EMPTY);
        }
    }

    public char getAt(Grid.Position position) {
        return this.cells.get(position.row)[position.column];
    }
    
    public String getTileNameAt(Grid.Position position) {
       char tile = getAt(position);
       String tileName;

       switch (tile) {
           case '.': tileName = "Empty"; break;
           case 'W': tileName = "Wall"; break;
           case 'A': tileName = "A"; break;
           case 'B': tileName = "B"; break;
           default: tileName = "Undefined";
       }
       return tileName;
    }

    private void setAt(Grid.Position position, char tile) {
        this.cells.get(position.row)[position.column] = tile;
        this.dirtyCells.add(position);
    }

    public void ResetCells() {
        this.cells.clear();
        
        for (int row=0; row < this.numRows; row++) {
            char[] newRow = new char[this.numColumns];
            for (int col=0; col < this.numColumns; col++) {
                newRow[col] = Grid.TILE_EMPTY;
            }
            this.cells.add(newRow);
        }

        this.nodes = new Bfs.Node[this.numRows][this.numColumns];
    }
    
    public int getCellSize() {
        return this.cellSize;
    }
    
    public Point toPoint(Grid.Position position) {
        return new Point(position.column * cellSize + cellSize / 2, 
                position.row * cellSize + cellSize / 2); 
    }

    public Grid.Position fromPoint(Point point) {
        return new Grid.Position(
                (int) (point.y / this.cellSize), 
                (int) (point.x / this.cellSize)); 
    }
    
    public void markFullRefresh() {
        this.needRedraw = true;
    }
    
    public void drawPath(Grid.Position position) {
        final int SIZE = (int) (this.cellSize * 0.5);
        Point point = toPoint(position);
        this.g.setFill(Color.ORANGE);
        this.g.fillRect(point.x - SIZE / 2, point.y - SIZE / 2, SIZE, SIZE);
        this.pathCells.add(position);
    }
    
    public void clearPath() {
        for (Grid.Position position : this.pathCells) {
            drawTileAt(position);
        }
        
        this.pathCells.clear();
    }
    
    public void draw() {
        this.g.setFill(BG_COLOR);
        this.g.fillRect(0, 0, 
                this.numColumns * this.cellSize, this.numRows * this.cellSize);

        for (int row = 0; row < this.getNumRows(); row++) {
            for (int col = 0; col < this.getNumColumns(); col++) {
                drawTileAt(new Grid.Position(row, col));
            }
        }

        this.dirtyCells.clear();
        this.needRedraw = false;
    }
    
    public void refresh() {
        if (this.needRedraw) {
            draw();
        } else {
            for (Grid.Position position : this.dirtyCells) {
                drawTileAt(position);
            }
            
            this.dirtyCells.clear();
        }
    }
    
    private void drawTileAt(Grid.Position position) {
        char tile = this.getAt(position);
        Point point = toPoint(position);
        
        if (tile == TILE_EMPTY) {
            drawTile(point, FLOOR_COLOR);
        } else if (tile == TILE_START) {
            drawTileWithSymbol(tile, point,
                    TILE_TEXT_COLOR, START_COLOR);
        } else if (tile == TILE_TARGET) {
            drawTileWithSymbol(tile, point,
                    TILE_TEXT_COLOR, TARGET_COLOR);
        } else if (tile == TILE_WALL) {
            drawTile(point, WALL_COLOR);
        }
    }
    
    public void load(String fileName) {
        try (BufferedReader reader = new BufferedReader (
                new FileReader(fileName))) {
            for (int row = 0; row < this.getNumRows(); row++) {
                reader.read(this.cells.get(row));
                reader.read();

                // Find start tile
                String line = new String(this.cells.get(row));
                int startColumn = line.indexOf(TILE_START);
                if (startColumn != -1) 
                    this.start = new Grid.Position(row, startColumn);
                
                // Find target tile
                int targetColumn = line.indexOf(TILE_TARGET);
                if (targetColumn != -1) 
                    this.target = new Grid.Position(row, targetColumn);
            }
        } catch (IOException e) {
            
        }
        
        if (this.start != null)
            System.out.println(this.start.toString());
        if (this.target != null)
            System.out.println(this.target.toString());
        
        draw();
    }

    public void save(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(fileName))) {
            for (int row = 0; row < this.getNumRows(); row++) {
                writer.write(new String(this.cells.get(row)) + "\n");
            }
        } catch (IOException e) {
            
        }
    }
    
    private void drawTileWithSymbol(char tile, 
                                  Point point,
                                  Color textColor, Color color) {
        drawTile(point, color);

        this.g.setFont(Font.font("Sans", 18));
        this.g.setFill(textColor);
        this.g.setTextAlign(TextAlignment.CENTER);
        this.g.setTextBaseline(VPos.CENTER);

        this.g.fillText(Character.toString(tile), point.x, point.y); 
    }
    
    private void drawTile(Point point,  Color color) {
        final int TILE_SIZE = (int) (this.cellSize * 0.95);
        this.g.setFill(color);
        this.g.fillRect(point.x - TILE_SIZE / 2, point.y - TILE_SIZE / 2,
                TILE_SIZE, TILE_SIZE);
    }
    

    public Bfs.Node[][] getPathNodes() {
        for (int row = 0; row < this.numRows; row++) {
            for (int column = 0; column < this.numColumns; column++) {
                char tile = getAt(new Grid.Position(row, column));
                this.nodes[row][column] = new Bfs.Node(
                        row, column, tile != TILE_WALL);
            }
        }

        return this.nodes; 
    }
}
