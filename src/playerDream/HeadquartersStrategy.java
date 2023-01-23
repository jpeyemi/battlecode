package playerDream;

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
    static int openIsland = -1;

    static void runHeadquarters(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 1) {
            Communication.addHeadquarter(rc);
        } else if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        Communication.checkSquad(rc);
        if(RobotPlayer.turnCount > 0){
            anchorStrat(rc);
        }
        buildStrat(rc);
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
        
        if (RobotPlayer.turnCount % 100 == 50){
            rc.setIndicatorString("Trying to build a Amplifier");
            if (rc.canBuildRobot(RobotType.AMPLIFIER, newLoc)) {
                rc.buildRobot(RobotType.AMPLIFIER, newLoc);
            }
        }
        
        if(carrierCount < 10){
            boolean buildAll = true;
            while(buildAll){
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                }else{
                    buildAll = false;
                }
            }
            // buildAll = true;
            // while(buildAll){
            //     if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
            //         rc.buildRobot(RobotType.LAUNCHER, newLoc);
            //     }else{
            //         buildAll = false;
            //     }
            // }
        }
        else if(carrierCount > 10){

            boolean buildAll = true;
            while(buildAll && rc.getResourceAmount(ResourceType.MANA)> 40){
                if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                    rc.buildRobot(RobotType.LAUNCHER, newLoc);
                }else{
                    buildAll = false;
                }
            }
            // buildAll = true;
            // while(buildAll  && rc.getResourceAmount(ResourceType.ADAMANTIUM)> 40){
            //     if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
            //         rc.buildRobot(RobotType.CARRIER, newLoc);
            //     }else{
            //         buildAll = false;
            //     }
            // }
            
        }else {
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
        if (rc.getResourceAmount(ResourceType.MANA) < 5 * RobotType.LAUNCHER.buildCostMana)
            return;
        int attempts = 0;
        int numPlaced = 0;
        while (numPlaced != 5 && attempts != 30){
            attempts++;
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
                numPlaced++;
            }
            else{
                dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
                newLoc = rc.getLocation().add(dir);
            }
        }
        rc.setIndicatorString("Trying to build a launcher");
        

       

    }

    static void anchorStrat(RobotController rc) throws GameActionException{
        if (anchorCooldown > 0){
            anchorCooldown --;
            if(anchorCooldown == 0) anchorTime = true;
        }

        //int isl = isIsland(rc);

        if (rc.canBuildAnchor(Anchor.STANDARD) && anchorTime) {
            // If we can build an anchor do it!
            anchorTime = false;
            anchorCooldown = anchorMaxCooldown;
            rc.buildAnchor(Anchor.STANDARD);
            rc.setIndicatorString("Building anchor! " + rc.getNumAnchors(Anchor.STANDARD));
            //openIsland = isl;
        }
        // if(openIsland != isl){
        //     if (rc.canBuildAnchor(Anchor.STANDARD) && anchorTime) {
        //         // If we can build an anchor do it!
        //         anchorTime = false;
        //         anchorCooldown = anchorMaxCooldown;
        //         rc.buildAnchor(Anchor.STANDARD);
        //         rc.setIndicatorString("Building anchor! " + rc.getNumAnchors(Anchor.STANDARD));
        //         openIsland = isl;
        //     }
        // }


        
    }

    static int isIsland(RobotController rc) throws GameActionException{
        for (int i = Communication.STARTING_ISLAND_IDX; i < Communication.STARTING_ISLAND_IDX + GameConstants.MAX_NUMBER_ISLANDS; i++) {
            if(Communication.readTeamHoldingIsland(rc, i) == Team.NEUTRAL){
                return(i);
            }
        }
        return (-1);
    }
}