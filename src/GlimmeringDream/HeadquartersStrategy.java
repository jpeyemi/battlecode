package GlimmeringDream;

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
    static int anchorMaxCooldown = 40;
    static int openIsland = -1;
    static boolean saving = false;
    static boolean spawning = false;

    static void runHeadquarters(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 1) {
            Communication.addHeadquarter(rc);
            scanWells(rc);
        } else if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        RobotInfo[] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
        if(enemies.length > 0){
            Communication.reportEnemy(rc,enemies[0].getLocation());
            if(enemies.length > 15){
                counterStrat(rc);
                return;
            }
        }
        //RobotPlayer.scan(rc);

        //Communication.checkSquad(rc);
        if(RobotPlayer.turnCount > 200){
            anchorStrat(rc);
        }
        buildStrat(rc);
        Communication.tryWriteMessages(rc);

    }


    static void counterStrat(RobotController rc) throws GameActionException{
        if(!saving && !spawning){
            saving = true;
        }
        if(rc.getResourceAmount(ResourceType.MANA) > 10 * RobotType.LAUNCHER.buildCostMana){
            spawning = true;
            saving = false;
        }
        if(spawning){
            Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            MapLocation newLoc = rc.getLocation().add(dir);
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
        if(rc.getResourceAmount(ResourceType.MANA) < 10){
            spawning = false;
        }
    }

    static void buildStrat(RobotController rc) throws GameActionException{
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);//.add(dir);
        
        if (RobotPlayer.turnCount % 70 == 20){
            rc.setIndicatorString("Trying to build a Amplifier");
            if (rc.canBuildRobot(RobotType.AMPLIFIER, newLoc)) {
                rc.buildRobot(RobotType.AMPLIFIER, newLoc);
            }
        }
        
        //if(carrierCount < 10){
        if(RobotPlayer.turnCount < 200){
            boolean buildAll = true;
            while(buildAll){
                if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                    rc.buildRobot(RobotType.CARRIER, newLoc);
                    //newLoc = rc.getLocation().add(dir).add(RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)]);
                }else{
                    buildAll = false;
                }
            }
            if(RobotPlayer.turnCount < 10){
                buildAll = true;
                while(buildAll){
                    if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                        rc.buildRobot(RobotType.LAUNCHER, newLoc);
                        //newLoc = rc.getLocation().add(dir).add(RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)]);
                    }else{
                        buildAll = false;
                    }
                }
            }
            
        }
        //else if(carrierCount > 10){
        // else if(RobotPlayer.turnCount > 50){

        //     boolean buildAll = true;
        //     // while(buildAll && rc.getResourceAmount(ResourceType.MANA)> 40){
        //     //     if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
        //     //         rc.buildRobot(RobotType.LAUNCHER, newLoc);
        //     //     }else{
        //     //         buildAll = false;
        //     //     }
        //     // }
        //     // buildAll = true;
        //     // while(buildAll  && rc.getResourceAmount(ResourceType.ADAMANTIUM) > 60){
        //         if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
        //             rc.buildRobot(RobotType.CARRIER, newLoc);
        //         }//else{
        //     //         buildAll = false;
        //     //     }
        //     // }
            
        // }else {
        //     rc.setIndicatorString("Trying to build a launcher");
        //     if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
        //         rc.buildRobot(RobotType.LAUNCHER, newLoc);
        //     }
        // }


        boolean buildAll = true;
        while(buildAll && rc.getResourceAmount(ResourceType.ADAMANTIUM) > 120){
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
                // dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
                // newLoc = rc.getLocation().add(dir).add(RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)]);
            }else{
                buildAll = false;
            }
        }
        if(RobotPlayer.turnCount > 100){
            if (rc.getResourceAmount(ResourceType.MANA) < 3 * RobotType.LAUNCHER.buildCostMana)
            return;
        }

        if (rc.getResourceAmount(ResourceType.MANA) < 1 * RobotType.LAUNCHER.buildCostMana)
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
                newLoc = rc.getLocation().add(dir);//.add(dir);
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

        if (rc.canBuildAnchor(Anchor.STANDARD) && anchorTime && rc.getNumAnchors(Anchor.STANDARD) < 1) {
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

    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        for(WellInfo well : wells){
            Communication.addWell(well, rc);
        }
    }
}