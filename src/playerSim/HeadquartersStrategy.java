package playerSim;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


class HeadquartersStrategy{
    static boolean anchorTime = true;
    static int anchorCooldown = 0;
    static int anchorMaxCooldown = 20;

    static void runHeadquarters(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 1) {
            Communication.addHeadquarter(rc);
        } else if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        
        buildStrat(rc);
        anchorStrat(rc);
        Communication.tryWriteMessages(rc);

    }

    static void buildStrat(RobotController rc) throws GameActionException{
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        int carrierCount = 0;
        for (RobotInfo r : robots){
            if(r.getType() == RobotType.CARRIER){
                carrierCount++;
            }
        }
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        
        if (RobotPlayer.turnCount % 50 == 19){
            rc.setIndicatorString("Trying to build a Amplifier");
            if (rc.canBuildRobot(RobotType.AMPLIFIER, newLoc)) {
                rc.buildRobot(RobotType.AMPLIFIER, newLoc);
            }
        }
        if(carrierCount < 4){
            if (RobotPlayer.rng.nextBoolean()) {
                // Let's try to build a carrier.
                rc.setIndicatorString("Trying to build a carrier");
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                }
            } else {
                // Let's try to build a launcher.
                rc.setIndicatorString("Trying to build a launcher");
                if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                    rc.buildRobot(RobotType.LAUNCHER, newLoc);
                }
            }
        }else if(rc.getNumAnchors(Anchor.STANDARD) > 1){
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
    }

    static void anchorStrat(RobotController rc) throws GameActionException{
        if (anchorCooldown > 0){
            anchorCooldown --;
            if(anchorCooldown == 0) anchorTime = true;
        }

        if (rc.canBuildAnchor(Anchor.STANDARD) && isIsland(rc) && anchorTime) {
            // If we can build an anchor do it!
            anchorTime = false;
            anchorCooldown = anchorMaxCooldown;
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building anchor! " + rc.getNumAnchors(Anchor.STANDARD));
        }
    }

    static boolean isIsland(RobotController rc) throws GameActionException{
        for (int i = Communication.STARTING_ISLAND_IDX; i < Communication.STARTING_ISLAND_IDX + GameConstants.MAX_NUMBER_ISLANDS; i++) {
            if(Communication.readTeamHoldingIsland(rc, i) == Team.NEUTRAL){
                return(true);
            }
        }
        return (false);
    }
}