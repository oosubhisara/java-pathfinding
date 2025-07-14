package com.oosubhisara.walker;

import java.io.File;
import java.nio.file.Paths;

import com.oosubhisara.algorithm.Bfs;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PathFindingApp extends Application {
    private static PathFindingApp appInstance;
    
    private FileChooser fileChooser;
    private Stage stage;
    private Controller controller;
    private Grid grid;
    private String mapFileName;
    private Bfs.Node pathToTarget;
    private Bfs.Node currentNode;
    private Timeline timeline;
    private int moveDelay;
    
    public static PathFindingApp getInstance() {
        return appInstance;
    }

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("App:Init");
        appInstance = this;

        this.fileChooser = new FileChooser();
        this.fileChooser.setInitialDirectory(new File("."));
        this.fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Map file (*.map)", "*.map")
        );

    }

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("App:Start");
        this.stage = stage;

        System.out.println("App: load FXML");
        System.out.println(getClass().getResource("MainScene.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("MainScene.fxml"));
        // Controller initialized

        this.grid = new Grid(20, 20, 32, this.controller.getGraphicsContext());
        this.controller.setGrid(grid);
        this.grid.ResetCells();
        this.grid.draw();
        
        this.moveDelay = 100;

        stage.setTitle("Path Finding");
        System.out.println("App: stage.setScene");
        stage.setScene(new Scene(root, 660, 800));
        stage.setMinHeight(800);
        System.out.println("App: after stage.setScene");
        stage.show();
    } 
    
    public void setController(Controller controller) {
        this.controller = controller;
    }
    
    public Grid getGrid() {
        return this.grid;
    }
    
    public void setMoveDelay(int delay) {
        this.moveDelay = delay;
    }
   
    public void openMap() {
        File file = this.fileChooser.showOpenDialog(stage);
        if (file == null) return;

        String fileName;
        if (!file.getName().contains(".")) {
            fileName = file.getAbsolutePath() + ".map";
        } else {
            fileName = file.getAbsolutePath();
        }

        this.grid.load(fileName);
        this.mapFileName = fileName;
        this.stage.setTitle(String.format("Path Finding - %s", 
                Paths.get(mapFileName).getFileName()));
        System.out.println(String.format("%s Loaded.", fileName));
    }
    
    public void saveMap() {
        if (this.mapFileName.isEmpty()) {
            saveMapAs();
        } else {
            this.grid.save(this.mapFileName);
            System.out.println(String.format("%s Saved.", mapFileName));
        }
    }

    public void saveMapAs() {
        File file = this.fileChooser.showSaveDialog(stage);
        if (file == null) return;
        
        if (!file.getName().contains(".")) {
            this.mapFileName = file.getAbsolutePath() + ".map";
        } else {
            this.mapFileName = file.getAbsolutePath();
        }

        this.grid.save(mapFileName);
        this.stage.setTitle(String.format("Path Finding - %s", 
                Paths.get(mapFileName).getFileName()));
        System.out.println(String.format("%s Saved.", mapFileName));
    }
    
    private boolean checkMove(Grid.Position start, Grid.Position target) {
        if (start == null) {
            System.out.println("Cannot move! Starting position is missing");
            return false;
        } else if (target == null) {
            System.out.println("Cannot move! Target is missing");
            return false;
        } 
        return true;
    }
    
    public boolean findPath() {
        Grid.Position start = this.grid.getStart();
        Grid.Position target = this.grid.getTarget();
        if (!checkMove(start, target)) return false;

        Bfs.Node[][] nodes = grid.getPathNodes();

        Bfs pathFinder = new Bfs(nodes);
        Bfs.Node startNode = nodes[start.row][start.column];
        Bfs.Node targetNode = nodes[target.row][target.column];
        this.pathToTarget = pathFinder.findPath(startNode, targetNode, false);
        
        return true;
    }
    
    public void walk() {
        if (this.timeline != null) {
            this.timeline.stop();
        }
        this.currentNode = this.pathToTarget;
        this.timeline = new Timeline(new KeyFrame(
                Duration.millis(this.moveDelay), e -> drawPath()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();
    }
    
    private void drawPath() {
        if (this.currentNode != null) {
            Grid.Position position = new Grid.Position(
                    this.currentNode.row, this.currentNode.column);
            this.grid.setStart(position);
            this.grid.refresh();

            if (this.currentNode.previous != null) {
                this.grid.drawPath(new Grid.Position(
                       this.currentNode.previous.row, 
                       this.currentNode.previous.column));
            }

            this.currentNode = this.currentNode.next;
        } else {
            this.timeline.stop();
        }
    }
    
}
