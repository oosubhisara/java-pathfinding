package com.oosubhisara.walker;

import java.awt.Point;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

public class Controller {

    @FXML
    private ToggleGroup tileGroup;
    @FXML
    private RadioButton buttonA;
    @FXML
    private RadioButton buttonB;
    @FXML
    private RadioButton buttonWall;
    @FXML
    private ToggleButton buttonAutoMove;
    @FXML
    private Button buttonMove;
    @FXML
    private Canvas canvas;
    @FXML
    private Label statusBar;
    
    private Grid grid;
    private int tileIndex = 1;
    private PathFindingApp app;
    
    public Controller() {
        super();
        System.out.println("Controller:Construtor");
        Global.controller = this; 
        this.app = PathFindingApp.getInstance();
        this.app.setController(this);
    }
    
    @FXML
    protected void initialize() {
        System.out.println("Controller:Initialize");
        buttonA.getStyleClass().remove("radio-button");
        buttonA.getStyleClass().add("toggle-button");
        buttonB.getStyleClass().remove("radio-button");
        buttonB.getStyleClass().add("toggle-button");
        buttonWall.getStyleClass().remove("radio-button");
        buttonWall.getStyleClass().add("toggle-button");
    }
    
    public GraphicsContext getGraphicsContext() {
       return this.canvas.getGraphicsContext2D(); 
    }
    
    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    @FXML
    private void onNew(ActionEvent e) {
        System.out.println("New map");
    }

    @FXML
    private void onLoad(ActionEvent e) {
        this.app.openMap();

        if (buttonAutoMove.isSelected()) {
            findPath();
        }
    }

    @FXML
    private void onSave(ActionEvent e) {
        this.app.saveMap();
    }

    @FXML
    private void onSaveAs(ActionEvent e) {
        this.app.saveMapAs();
    }

    @FXML
    private void onExit(ActionEvent e) {
        Platform.exit();
    }
    
    @FXML
    private void onDelay(ActionEvent e) {
        RadioMenuItem menuItem = (RadioMenuItem) e.getSource();
        int delay = Integer.parseInt(menuItem.getText().replace(" ms", ""));
        System.out.println("Delay: " + delay);
        this.app.setMoveDelay(delay);
    }
    
    @FXML
    private void onAutoMoveButton(ActionEvent e) {
        if (buttonAutoMove.isSelected()) {
            findPath();
        }
    }

    @FXML
    private void onMoveButton(ActionEvent e) {
        findPath();
    }

    @FXML
    private void onTileButton(ActionEvent e) {
        RadioButton button = (RadioButton) e.getSource();
        this.tileIndex = this.tileGroup.getToggles().indexOf(button) + 1;
        System.out.println("Tile Index: " + this.tileIndex);
    }
    
    @FXML
    private void onCanvasMouseMoved(MouseEvent e) {
        Grid.Position position = this.grid.fromPoint(
                new Point((int) e.getX(), (int) e.getY()));
        statusBar.setText(String.format("Position: %d, %d    Tile: %s", 
                                        position.row, position.column, 
                                        this.grid.getTileNameAt(position)));
    }
    
    @FXML
    private void onCanvasClicked(MouseEvent e) {
        drawOnCanvas(new Point((int) e.getX(), (int) e.getY()),
                     e.getButton().ordinal());
    }

    @FXML
    private void onCanvasMouseDragged(MouseEvent e) {
        drawOnCanvas(new Point((int) e.getX(), (int) e.getY()),
                     e.getButton().ordinal());
    }
    
    
    private void drawOnCanvas(Point point, int mouseButton) {
        System.out.println("Tile: " + this.tileIndex);
        System.out.println("MouseButton: " + mouseButton);
        Grid.Position position = this.grid.fromPoint(point);
        
        if (position.column < 0 || 
                position.column >= this.grid.getNumColumns() ||
                position.row < 0 
                || position.row >= this.grid.getNumRows()) {
            return;
        }

        System.out.println("Click: " + position.row + ", " + 
                           position.column); 
        
        int tile = this.tileIndex;
        if (mouseButton == 3) tile = 0;
            
        switch (tile) {
            case 0:
                this.grid.setEmpty(position); break;
            case 1:
                this.grid.setStart(position); break;
            case 2:
                this.grid.setTarget(position); break;
            case 3:
                this.grid.setWall(position); break;
            default: 
                throw new ArrayIndexOutOfBoundsException(
                        "Tile index out of bound.");
        }
       
        this.grid.clearPath();
        this.grid.refresh();

        if (buttonAutoMove.isSelected()) {
            findPath();
        }
    }
    
    private void findPath() {
        if (this.app.findPath()) {
            this.app.walk();
        }
    }

   
}
