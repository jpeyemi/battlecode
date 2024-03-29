package player2;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pathing {
    // Basic bug nav - Bug 0

    static Direction currentDirection = null;

    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation().equals(target)) {
            return;
        }
        if (!rc.isActionReady()) {
            return;
        }
        Direction d = rc.getLocation().directionTo(target);
        if (rc.canMove(d)) {
            rc.move(d);
            currentDirection = null; // there is no obstacle we're going around
        } else {
            // Going around some obstacle: can't move towards d because there's an obstacle there
            // Idea: keep the obstacle on our right hand

            if (currentDirection == null) {
                currentDirection = d;
            }
            // Try to move in a way that keeps the obstacle on our right
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection)) {
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else {
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
    }
    // A Star
    static ArrayList<MapLocation> path;
    static int nodeOn;
    static RobotController rc;

    public void pathing(MapLocation start, MapLocation end, RobotController robot) throws GameActionException{
        rc = robot;
        // path = aStar(new Node(start), new Node(end));
        nodeOn = 0;
    }
    static class Node {
        private int x;
        private int y;
        private int h;
        private int g;
        private int f;
        private MapLocation loc;

        public Node(MapLocation loc) {
            x = loc.x;
            y = loc.y;
            this.loc = loc;

        }
        public int getH() {
            return h;
        }
        public int getG() {
            return g;
        }
        public int getF() {
            return f;
        }
        public void setH(int h) {
            this.h = h;
        }
        public void setG(int g) {
            this.g = g;
        }
        public void setF(int f) {
            this.f = f;
        }
    }
    
    static ArrayList<Node> getNeighbors(Node node) throws GameActionException {
        ArrayList<Node> neighbors = new ArrayList<>();
        for (Direction dir : RobotPlayer.directions) {
            MapLocation newLoc = node.loc.add(dir);
            if (rc.canSenseLocation(newLoc) && rc.sensePassability(newLoc)) {
                // Will add additional conditions ex: currents, clouds, etc.
                neighbors.add(new Node(newLoc));
            }
        }
        
        return neighbors;
    }

    static int getDistance(Node a, Node b) throws GameActionException{
        MapLocation locA = a.loc;
        MapLocation locB = b.loc;
        return locA.distanceSquaredTo(locB);
    }
    static List<MapLocation> aStar(Node start, Node end) throws GameActionException{
        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashMap<Node, Integer> closedList = new HashMap<>();
        Map<Node, Node> parent = new HashMap<>();
        parent.put(start, null);
        
        start.setG(0);

        openList.add(start);

        while(!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (currentNode.equals(end)) {
                return reconstructPath(parent, end);
            }
            // Add the current node to the closed list
            closedList.put(currentNode, currentNode.getG());

            // Iterate through the current node's neighbors
            for (Node neighbor : getNeighbors(currentNode)) {
                // Check if the neighbor is already in the closed list
                if (closedList.containsKey(neighbor)) {
                    continue;
                }
                parent.put(neighbor, currentNode);
                // Calculate the new g-value for the neighbor
                int newG = currentNode.getG() + getDistance(currentNode, neighbor);

                // Check if the new g-value is less than the neighbor's current g-value
                if (newG < neighbor.getG()) {
                    // Update the neighbor's g-value and f-value
                    neighbor.setG(newG);
                    neighbor.setF(neighbor.getG() + neighbor.getH());

                    // Add the neighbor to the open list
                    openList.add(neighbor);
                }
            }
        }

        return null;
    }
    static List<MapLocation> reconstructPath(Map<Node, Node> cameFrom, Node current) {
        List<MapLocation> path = new ArrayList<>();
        path.add(current.loc);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current.loc);
        }

        Collections.reverse(path);
        return path;
    }
}


