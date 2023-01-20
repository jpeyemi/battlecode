package playerSim;

import battlecode.common.*;

public class AmplifierStrategy {
    static void runAmplifier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        scanIslands(rc);
        RobotPlayer.scan(rc);
        Direction moveDir = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
        if (rc.canMove(moveDir)) {
            rc.move(moveDir);
        }
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
