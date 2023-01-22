package playerSim;

import java.util.HashMap;

import javax.naming.CommunicationException;

import battlecode.common.*;

public class LauncherStrategy {

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
        priority.put(RobotType.LAUNCHER, 4);
        priority.put(RobotType.HEADQUARTERS, 6);
        priority.put(RobotType.AMPLIFIER, 3);
    }
    static MapLocation islandLoc;
    static boolean toCenter = true;
    


    static void runLauncher(RobotController rc) throws GameActionException {
        MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
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
        RobotPlayer.scan(rc);
        RobotPlayer.squad(rc);
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

        RobotPlayer.moveSquad(rc);
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
            if(enemy.getTeam() == rc.getTeam() ){
                if(enemy.getType() == RobotType.AMPLIFIER && bestTarget != 6){
                    target = enemy;
                }
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
        //     for (int i = Communication.STARTING_ISLAND_IDX; i < Communication.STARTING_ISLAND_IDX + GameConstants.MAX_NUMBER_ISLANDS; i++) {
        //         MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
        //         double farDistance = 1.0;
        //         if (islandNearestLoc != null) {
        //             float dist = rc.getLocation().distanceSquaredTo(islandNearestLoc);
        //             //if(Communication.readTeamHoldingIsland(rc, i) == rc.getTeam().opponent()){ 
        //             if(rc.getLocation() != islandNearestLoc && dist > farDistance) {
        //                 islandLoc = islandNearestLoc;
        //                 farDistance = dist;
        //             }
        //         }
        //     }
        // }
        // if(islandLoc == null) {
        //     scanIslands(rc);
        // }
        // if(islandLoc != null){

        //     MapLocation robotLocation = rc.getLocation();
        //     //Pathing.moveTowards(rc, enemyLocation);
        //     // Direction moveDir = robotLocation.directionTo(islandLoc);
        //     // if (rc.canMove(moveDir)) {
        //     //     rc.move(moveDir);
        //     // }

        //     Pathing.moveTowards(rc, islandLoc);
        //     if(rc.getLocation() == islandLoc || rc.canSenseRobotAtLocation(islandLoc)){
        //         islandLoc = null; //expesive potenially
        //     } 
        // }


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


        if(rc.getLocation().distanceSquaredTo(center) < 6){
            toCenter = false;
        }else{
            if(Communication.headquarterLocs[0] != null && rc.getLocation().distanceSquaredTo(Communication.headquarterLocs[0]) < 6){
                toCenter =true;
            }
        }

        if(toCenter){
            Pathing.moveTowards(rc, center);
        }else{
            Pathing.moveTowards(rc, Communication.headquarterLocs[0]);
        }
        // Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        // if (rc.canMove(dir)) {
        //     rc.move(dir);
        // }


        Communication.tryWriteMessages(rc);
    }
    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            if(rc.senseTeamOccupyingIsland(id) != rc.getTeam()) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if(locs.length > 0) {
                    islandLoc = locs[0];
                    break;
                }
            }
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
}
