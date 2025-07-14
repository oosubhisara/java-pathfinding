package com.oosubhisara.algorithm;

import java.util.LinkedList;
import java.util.Queue;

public class Bfs {
    public static class Node {
        public int row, column;
        public boolean visited;
        public boolean walkable;
        public Node previous;
        public Node next;
        
        public Node(int row, int column, boolean walkable) {
            this.row = row;
            this.column = column;
            this.walkable = walkable;
            this.visited = false;
            this.previous = null;
            this.next = null;
        }
        
        public String toString() {
            return String.format("%d, %d", this.row, this.column);
        }
        
        public int getlength() {
            int count = 0;
            Node node = this;

            while (node != null) {
                count++;
                node = node.next;
            }

            return count;
        }
    }
    
    private int numRows, numColumns;
    private Node[][] nodes;
    private Node start;
    private Node target;
    private Queue<Node> queue;
    
    public Bfs(Node[][] nodes) {
        this.numRows = nodes.length;
        this.numColumns = nodes[0].length;
        this.nodes = nodes;
        System.out.println("BFS: " + this.numRows + "x" + this.numColumns);
    }
    
    public Bfs.Node findPath(Node start, Node target, boolean includeTarget) {
        this.start = start;
        this.target = target;
        this.queue = new LinkedList<Node>();
        
        this.start.visited = true;
        queue.add(this.start);
        
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            visitNeighbors(node);
        }
        
        Bfs.Node lastNode = includeTarget? this.target : this.target.previous;
        relinkNodes(lastNode);

        System.out.println("Length of path: " + this.start.getlength());
        return this.start.next;
    }

    public void visitNeighbors(Node node) {
        final int[][] OFFSETS = { {-1, 0}, {0, 1}, {1, 0}, {0, -1} };

        for (int i = 0; i < 4; i++) {
            // Calculate position of the adjacent node
            int row = node.row + OFFSETS[i][0];
            int column = node.column + OFFSETS[i][1];
            
            // if row and column are out of bound then skip
            if (row < 0 || row >= this.numRows ||
                    column < 0 || column >= this.numColumns) {
                continue;
            }
            
            // Check if neighboring node is walkable
            Node neighborNode = this.nodes[row][column];
            if (!neighborNode.visited && neighborNode.walkable) {
                neighborNode.visited = true;
                neighborNode.previous = node;
                this.queue.add(neighborNode);
            }
        }
        
    }
    
    private void relinkNodes(Bfs.Node node) {
        while (node != null) {
            if (node.previous != null) {
                node.previous.next = node;
            }
            node = node.previous;
        }
    }
    
}
