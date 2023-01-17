package player2;

import battlecode.common.*;

public class BoosterStrategy {
    static void runBooster(RobotController rc) throws GameActionException {
        scanIslands(rc);
        // move to furtherst corner and run away from enemies then move randomly
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            Communication.updateIslandInfo(rc, id);
        }
    }
}
