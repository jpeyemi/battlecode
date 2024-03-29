package GlimmeringDream;

import java.util.HashMap;

import javax.naming.CommunicationException;

import battlecode.common.*;

public class LauncherStrategyold {

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */

    //private static final RobotType [] priority = new RobotType [] {RobotType.BOOSTER, RobotType.DESTABILIZER, RobotType.LAUNCHER, RobotType.CARRIER};
    public static HashMap<RobotType, Integer> priority;
    static {
        priority = new HashMap<RobotType, Integer>();
        priority.put(RobotType.DESTABILIZER, 1);
        priority.put(RobotType.BOOSTER, 2);
        priority.put(RobotType.CARRIER, 5);
        priority.put(RobotType.LAUNCHER, 3);
        priority.put(RobotType.HEADQUARTERS, 6);
        priority.put(RobotType.AMPLIFIER, 4);
    }
    static MapLocation islandLoc;
    static boolean survey = false;
    


    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        int lowestHealth = 100;
        int smallestDistance = 100;
        int bestTarget = 6;
        RobotInfo target = null;
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }

        for(MapLocation ehq : Communication.eheadquarterLocs){
            if(ehq != null && rc.getLocation().distanceSquaredTo(ehq) < RobotType.HEADQUARTERS.actionRadiusSquared){
                Direction moveDir = rc.getLocation().directionTo(ehq).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
                return;
            }
        }

        avoidHqKillRadius(rc);
        scanIslands(rc);
        scanWells(rc);
        // RobotPlayer.scan(rc);
        // RobotPlayer.squad(rc);
        // RobotPlayer.scan(rc);
        
        //Communication.clearObsoleteEnemies(rc);
        if (enemies.length > 0) {
            for (RobotInfo enemy: enemies){
                if(enemy.getType() == RobotType.HEADQUARTERS){
                    Communication.addEHq(enemy, rc);
                    continue;
                }
                Communication.reportEnemy(rc, enemy.location);
                int enemyHealth = enemy.getHealth();
                int enemyDistance = enemy.getLocation().distanceSquaredTo(rc.getLocation());
                RobotType enemyType = enemy.getType();
                if (priority.containsKey(enemyType) && priority.get(enemyType) < bestTarget){
                    target = enemy;
                    lowestHealth = enemyHealth;
                    smallestDistance = enemyDistance;
                    bestTarget = priority.get(enemyType);
                }
                else if (enemyHealth < lowestHealth){
                    target = enemy;
                    lowestHealth = enemyHealth;
                    smallestDistance = enemyDistance;
                    //bestTarget = priority.get(enemyType);
                }
                else if (enemyHealth == lowestHealth){
                    if (enemyDistance < smallestDistance){
                        target = enemy;
                        smallestDistance = enemyDistance;
                        //bestTarget = priority.get(enemyType);
                    }
                }
            }
        }
        

        if (target != null){
            if (rc.canAttack(target.getLocation()))
                rc.attack(target.getLocation());
        }
        Communication.tryWriteMessages(rc);
        // RobotPlayer.moveSquad(rc);
        //if(!RobotPlayer.leader) return;
        // if(Communication.eheadquarterLocs[0] != null){
        //     Pathing.moveTowards(rc, Communication.eheadquarterLocs[0]); // to be changed
        //     return;
        // }

        // if(RobotPlayer.follower) {
        //     Pathing.moveTowards(rc, RobotPlayer.following.getLocation());
        //     return;
        // }

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        for (RobotInfo enemy : visibleEnemies) {
            if(enemy.getType() == RobotType.HEADQUARTERS){
                Communication.addEHq(enemy, rc);
                target = enemy;
                break;
            }
            // if(enemy.getTeam() == rc.getTeam() ){
            //     if(enemy.getType() == RobotType.AMPLIFIER && bestTarget != 6){
            //         target = enemy;
            //     }
            //     continue;
            // }
            Communication.reportEnemy(rc, enemy.location);
            int enemyHealth = enemy.getHealth();
            int enemyDistance = enemy.getLocation().distanceSquaredTo(rc.getLocation());
            RobotType enemyType = enemy.getType();
            if (priority.containsKey(enemyType) && priority.get(enemyType) < bestTarget){
                target = enemy;
                lowestHealth = enemyHealth;
                smallestDistance = enemyDistance;
                bestTarget = priority.get(enemyType);
            }
            else if (enemyHealth < lowestHealth){
                target = enemy;
                lowestHealth = enemyHealth;
                smallestDistance = enemyDistance;
                //bestTarget = priority.get(enemyType);
            }
            else if (enemyHealth == lowestHealth){
                if (enemyDistance < smallestDistance){
                    target = enemy;
                    smallestDistance = enemyDistance;
                    //bestTarget = priority.get(enemyType);
                }
            }
        	// if (enemy.getType() != RobotType.HEADQUARTERS) {
        	// 	MapLocation enemyLocation = enemy.getLocation();
        	// 	MapLocation robotLocation = rc.getLocation();
            //     Pathing.moveTowards(rc, enemyLocation);
        	// 	Direction moveDir = robotLocation.directionTo(enemyLocation);
        	// 	if (rc.canMove(moveDir)) {
        	// 		rc.move(moveDir);
        	// 	}
        	// }
        }

        Communication.tryWriteMessages(rc);

        if(target != null){
            MapLocation enemyLocation = target.getLocation();
            if(target.getType() == RobotType.HEADQUARTERS){
                //enemyLocation = rc.getLocation();
                Direction moveDir = rc.getLocation().directionTo(enemyLocation).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
                return;
            }
            MapLocation robotLocation = rc.getLocation();
            //Pathing.moveTowards(rc, enemyLocation);
            Direction moveDir = robotLocation.directionTo(enemyLocation);
            if (rc.canMove(moveDir)) {
                rc.move(moveDir);
            }
            Clock.yield();
            return;
        } else {
            // RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
            // int lowestID = rc.getID();
            // MapLocation leaderPos = null;
            // for (RobotInfo ally : allies){
            //     if (ally.getType() != RobotType.LAUNCHER)
            //         continue;
            //     if (ally.getID() < lowestID){
            //         lowestID = ally.getID();
            //         leaderPos = ally.getLocation();
            //     }
            // }
            // if (leaderPos != null){
            //     Pathing.moveTowards(rc, leaderPos);
            //     rc.setIndicatorString("Following " + lowestID);
            //     return;
            // }
            // else{
            //     //MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
            //     //Pathing.moveTowards(rc, RobotPlayer.center);
            //     rc.setIndicatorString("I'm the leader!");
            // }
        }

        MapLocation enemyLocation= Communication.getClosestEnemy(rc);
        if(enemyLocation != null){
            MapLocation robotLocation = rc.getLocation();
            Pathing.moveTowards(rc, enemyLocation);
            rc.setIndicatorString(enemyLocation.toString());

            // Direction moveDir = robotLocation.directionTo(enemyLocation);
            // if (rc.canMove(moveDir)) {
            //     rc.move(moveDir);
            // }
        }

        // if(islandLoc == null) {
        //     scanIslands(rc);
        // }
        if(islandLoc == null) {
            for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS; i++) {
                MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
                double lowDistance = 1000;
                if (islandNearestLoc != null) {
                    float dist = rc.getLocation().distanceSquaredTo(islandNearestLoc);
                    if(Communication.readTeamHoldingIsland(rc, i) == rc.getTeam().opponent()){ 
                        if(rc.getLocation() != islandNearestLoc && dist < lowDistance) {
                            islandLoc = islandNearestLoc;
                            lowDistance = dist;
                        }
                    }
                }
            }
        }
        
        if(islandLoc != null){
            //System.out.println(islandLoc.toString());
            // MapLocation robotLocation = rc.getLocation();
            // Direction moveDir = robotLocation.directionTo(islandLoc);
            // if (rc.canMove(moveDir)) {
            //     rc.move(moveDir);
            // }

            Pathing.moveTowards(rc, islandLoc);
            if(rc.getLocation() == islandLoc || rc.canSenseRobotAtLocation(islandLoc)){
                islandLoc = null; //expesive potenially
            } 
        }


        // WellInfo[] wells = rc.senseNearbyWells();
        // if (wells.length > 0){
        //     MapLocation wellLoc = wells[0].getMapLocation();
        //     Pathing.moveTowards(rc, wellLoc);
        // }

        
        
        // Also try to move randomly.
        // Direction moveDir = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
        // if (rc.canMove(moveDir)) {
        //     rc.move(moveDir);
        // }

        if(rc.getLocation().distanceSquaredTo(RobotPlayer.center) < 6){
            //RobotPlayer.toCenter = false;
            if((survey && scanAmp(rc)) && rc.senseNearbyRobots(-1,rc.getTeam()).length > 30){
                RobotPlayer.toCenter =true;
                if(RobotPlayer.explore.size()>0){
                    RobotPlayer.center = RobotPlayer.explore.get(0);
                    RobotPlayer.explore.remove(0);
                }
            }
        }else if(RobotPlayer.myhq != null && rc.getLocation().distanceSquaredTo(RobotPlayer.myhq) < 3){
            if(RobotPlayer.toCenter == false){
                RobotPlayer.toCenter =true;
                survey = true;
                // if(RobotPlayer.explore.size()>0){
                //     RobotPlayer.center = RobotPlayer.explore.get(0);
                //     RobotPlayer.explore.remove(0);
                // }
            }
        }

        if(RobotPlayer.toCenter){

            RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
            int lowestID = rc.getID();
            MapLocation leaderPos = null;
            for (RobotInfo ally : allies){
                if (ally.getType() != RobotType.LAUNCHER)
                    continue;
                if (ally.getID() < lowestID){
                    lowestID = ally.getID();
                    leaderPos = ally.getLocation();
                }
            }
            if (leaderPos != null && rc.getLocation().distanceSquaredTo(leaderPos) > 9){
                Pathing.moveTowards(rc, leaderPos);
                rc.setIndicatorString("Following " + lowestID);
                return;
            }
            else{
                //MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
                //Pathing.moveTowards(rc, RobotPlayer.center);
                rc.setIndicatorString("I'm the leader!");
            }

            Pathing.moveTowards(rc, RobotPlayer.center);
        }else if(!scanAmp(rc)){
            Pathing.moveTowards(rc, RobotPlayer.myhq);
        }
        // Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        // if (rc.canMove(dir)) {
        //     rc.move(dir);
        // }


        
    }
    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            // if(rc.senseTeamOccupyingIsland(id) == rc.getTeam().opponent()) {
            //     MapLocation[] locs = rc.senseNearbyIslandLocations(id);
            //     if(locs.length > 0) {
            //         islandLoc = locs[0];
            //     }
            // }
            Communication.updateIslandInfo(rc, id);
        }
    }

    static void avoidHqKillRadius(RobotController rc) throws GameActionException{
        for(MapLocation ehq : Communication.eheadquarterLocs){
            if(ehq != null && rc.getLocation().distanceSquaredTo(ehq) < RobotType.HEADQUARTERS.actionRadiusSquared){
                Direction moveDir = rc.getLocation().directionTo(ehq).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
                Clock.yield();
            }
        }
    }

    static boolean scanAmp(RobotController rc) throws GameActionException { // scan should only be on recon bots ex: amp and launcher
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for(RobotInfo robot : robots) {
           if(robot.getType() == RobotType.AMPLIFIER) return true;
        }
        return false;
    }

    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length > 0) {
            for(WellInfo well: wells){
                if(well.getResourceType() == ResourceType.MANA){
                    Communication.addWell(well, rc);
                }
            }
        }
    }
}
