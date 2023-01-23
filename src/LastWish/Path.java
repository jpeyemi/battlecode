package LastWish;

import battlecode.common.*;
import LastWish.Pathing.Node;

import java.util.ArrayList;
import java.util.HashMap;


public class Path {
    
    static ArrayList<MapLocation> path;
    static RobotController rc;
    private Path parent;

    public Path(ArrayList<MapLocation> inputPath, RobotController robot) {
        rc = robot;
        path = inputPath;
        // System.out.println(path.size() + ", " + path.toString());
    }

    public int getCurrentIndex() {
        MapLocation robotLocation = rc.getLocation();
        for (int i = 0; i < path.size() - 1; i++) {
            MapLocation loc = path.get(i);
            // System.out.println("" + loc + " " + robotLocation);
            if (robotLocation.equals( loc)) {
                return i;
            }
        }
        return -1; 
    }

    public boolean moveOnPath() throws GameActionException {
        // System.out.println(this.path);
        int index = getCurrentIndex();
        if(path == null) return false;
        // System.out.println(index);
        if (index == -1) return false;
        MapLocation current = path.get(index);
        MapLocation nextLocation = path.get(index + 1);
        Direction dir = current.directionTo(nextLocation);

        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    public MapLocation getClosestLocationToPath() throws GameActionException {
        MapLocation robotLocation = rc.getLocation();
        if (path.contains(robotLocation)) {
            int index = getCurrentIndex();
            if (index >= path.size() - 2) {
                return null;
            } else {
                return path.get(index + 2);
            }
        }
        MapLocation closestLocation = path.get(0);
        int closestDist = Integer.MAX_VALUE;

        for (MapLocation loc: path) {
            if (closestDist > loc.distanceSquaredTo(robotLocation)) {
                closestDist = loc.distanceSquaredTo(robotLocation);
                closestLocation = loc;
            }
        }
        return closestLocation;

    }
    
    public Path pathCorrection() throws GameActionException {
        if (parent == null) {
            MapLocation closestLocation = getClosestLocationToPath();
            // System.out.println(closestLocation);
            HashMap<Node, Integer> visited = new HashMap<Node, Integer>();
            try {
                visited.put(new Node(path.get(getCurrentIndex() + 1)), Integer.MAX_VALUE);
            } catch(Exception e) {

            }
            Path correction = new Path(Pathing.pathing(rc.getLocation(), closestLocation, rc, visited), rc);
            // System.out.println(correction);
            correction.parent = this;
            return correction;
        } else {
            return this.parent.pathCorrection();
        }
    }
}