package player3;

import battlecode.common.*;
import java.util.ArrayList;


public class Path {
    
    static ArrayList<MapLocation> path;
    static int index;
    static RobotController rc;
    static boolean strongPath; 

    public Path(ArrayList<MapLocation> inputPath, RobotController robot) {
        rc = robot;
        path = inputPath;
        index = 0;
    }

    public boolean moveOnPath() throws GameActionException {
        if (index == path.size() - 1) return false;
        MapLocation current = path.get(index);
        MapLocation nextLocation = path.get(index + 1);
        Direction dir = current.directionTo(nextLocation);

        if (rc.canMove(dir)) {
            rc.move(dir);
            index++;
            return true;
        }
        return false;

    }
}
