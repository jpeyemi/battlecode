package player3;

import battlecode.common.*;
import java.util.ArrayList;


public class Path {
    
    static ArrayList<MapLocation> path;
    static RobotController rc;
    private Path parent;

    public Path(ArrayList<MapLocation> inputPath, RobotController robot) {
        rc = robot;
        path = inputPath;
    }

    public int getCurrentIndex() {
        MapLocation robotLocation = rc.getLocation();
        for (int i = 0; i < path.size(); i++) {
            MapLocation loc = path.get(i);
            System.out.println("" + loc + " " + robotLocation);
            if (robotLocation.equals( loc)) {
                return i;
            }
        }
        return -1; 
    }

    public boolean moveOnPath() throws GameActionException {
        int index = getCurrentIndex();
        System.out.println(index);
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
            Path correction = new Path(Pathing.pathing(rc.getLocation(), closestLocation, rc ), rc);
            // System.out.println(correction);
            correction.parent = this;
            return correction;
        } else {
            return this.parent.pathCorrection();
        }
    }
}
