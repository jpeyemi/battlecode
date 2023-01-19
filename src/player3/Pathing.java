package player3;

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
    static Path currentPath = null;
    static void bugZero(RobotController rc, MapLocation target) throws GameActionException {
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
    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation().equals(target)) {
            return;
        }
        if (!rc.isActionReady()) {
            return;
        }
        //System.out.println("" + rc.getLocation()+ ", " +  closetToTarget(rc, target));
        if (currentPath == null) {
            currentPath = new Path(pathing(rc.getLocation(), closetToTarget(rc, target), rc), rc);
        }
        if (currentPath == null) {
            bugZero(rc, target);
        } else if (currentPath.moveOnPath()) {
            return;
        } else {
            bugZero(rc, target);
        }

        
        // Direction d = rc.getLocation().directionTo(target);
        // if (rc.canMove(d)) {
        //     rc.move(d);
        //     currentDirection = null; // there is no obstacle we're going around
        // } else {
        //     // Going around some obstacle: can't move towards d because there's an obstacle there
        //     // Idea: keep the obstacle on our right hand

        //     if (currentDirection == null) {
        //         currentDirection = d;
        //     }
        //     // Try to move in a way that keeps the obstacle on our right
        //     for (int i = 0; i < 8; i++) {
        //         if (rc.canMove(currentDirection)) {
        //             rc.move(currentDirection);
        //             currentDirection = currentDirection.rotateRight();
        //             break;
        //         } else {
        //             currentDirection = currentDirection.rotateLeft();
        //         }
        //     }
        // }
    }
    // A Star

    static class Node implements Comparable<Node>{
        private int x;
        private int y;
        private int h;
        private int g;
        private int f;
        private MapLocation loc;

        public Node(MapLocation loc) {
            x = loc.x;
            y = loc.y;
            g = Integer.MAX_VALUE;
            this.loc = loc;

        }
        public int compareTo(Node o) {
            return this.g + this.h - o.g - o.h;
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
        public String toString() {
            return "" + loc;
        }
        public boolean equals(Node o) {
            return o.loc.equals(this.loc);
        }
    }
    
    static ArrayList<MapLocation> pathing(MapLocation start, MapLocation end, RobotController robot) throws GameActionException{
        return aStar(new Node(start), new Node(end), robot);
    }

    static ArrayList<Node> getNeighbors(Node node, RobotController rc) throws GameActionException {
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
    static ArrayList<MapLocation> aStar(Node start, Node end, RobotController rc) throws GameActionException{
        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashMap<Node, Integer> closedList = new HashMap<>();
        Map<Node, Node> parent = new HashMap<>();
        parent.put(start, null);
        
        start.setG(0);

        openList.add(start);
  
        while(!openList.isEmpty()) {
            // System.out.println(i);
            Node currentNode = openList.poll();

            if (currentNode.equals(end)) {
                System.out.println("Goal state reached!");
                return reconstructPath(parent, end);
            }
            // Add the current node to the closed list
            closedList.put(currentNode, currentNode.getG());

            // Iterate through the current node's neighbors
            for (Node neighbor : getNeighbors(currentNode, rc)) {
                neighbor.setH(getDistance(neighbor, end));
                // System.out.println(neighbor);
                // Check if the neighbor is already in the closed list
                if (closedList.containsKey(neighbor)) {
                    continue;
                }
                // Calculate the new g-value for the neighbor
                int newG = currentNode.getG() + getDistance(currentNode, neighbor);
                // System.out.println("" + neighbor + " G: "  + neighbor.g);
                // Check if the new g-value is less than the neighbor's current g-value
                if (newG < neighbor.getG()) {
                    // Update the neighbor's g-value and f-value
                    neighbor.setG(newG);
                    neighbor.setF(neighbor.getG() + neighbor.getH());
                    
                    // Add the neighbor to the open list
                    openList.add(neighbor);
                    
                }
                parent.put(neighbor, currentNode);  
            }
            System.out.println(openList);
        }
        System.out.print(parent);
        return null;
    }
    static ArrayList<MapLocation> reconstructPath(Map<Node, Node> cameFrom, Node end) {
        ArrayList<MapLocation> path = new ArrayList<>();
        Node current = end;

        while (current != null) {
            path.add(current.loc);
            current = cameFrom.get(current);
        }

        Collections.reverse(path);
        System.out.println(path);
        return path;
    }

    static MapLocation closetToTarget(RobotController rc, MapLocation target) {
        if (rc.canSenseLocation(target)) {
            return target;
        }
        // int radius = rc.getType().visionRadiusSquared;
        Direction dir = rc.getLocation().directionTo(target);
        MapLocation current = rc.getLocation();
        while (rc.canSenseLocation(current)) {
            current = current.add(dir);
        }
        return current.add(dir.opposite());
    }
}


