package player2;

import battlecode.common.*;

public class AmplifierStrategy {
    static void runAmplifier(RobotController rc) throws GameActionException {
        scanIslands(rc);
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
        // move to furtherst corner and run away from enemies then move randomly
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            Communication.updateIslandInfo(rc, id);
        }
    }
}
