package GlimmeringDream;

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
        priority.put(RobotType.LAUNCHER, 3);
        priority.put(RobotType.HEADQUARTERS, 6);
        priority.put(RobotType.AMPLIFIER, 4);
    }
    static MapLocation islandLoc;
    static boolean survey = false;
    static boolean followStep = true;
    


    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }

        for(MapLocation ehq : Communication.eheadquarterLocs){
            if(ehq != null && rc.getLocation().distanceSquaredTo(ehq) < RobotType.HEADQUARTERS.actionRadiusSquared){
                Direction moveDir = rc.getLocation().directionTo(ehq).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
                Communication.tryWriteMessages(rc);
                return;
            }
        }
        followStep = true;
        avoidHqKillRadius(rc);
        scanIslands(rc);
        scanWells(rc);
        Communication.clearObsoleteEnemies(rc);
        attackStrat(rc);
        MapLocation [] clouds = rc.senseNearbyCloudLocations();
        if(clouds.length > 0){
            MapLocation cloud = clouds[RobotPlayer.rng.nextInt(clouds.length)];
            if(rc.canAttack(cloud))rc.attack(cloud);
        }
       
        healingStrat(rc);
        moveToEnemies(rc);
        islandDef(rc);
        if(followStep) explore(rc);
        Communication.tryWriteMessages(rc);


        
    }
    static void attackStrat(RobotController rc) throws GameActionException{
        RobotInfo [] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
        RobotInfo [] allies = rc.senseNearbyRobots(-1,rc.getTeam());
        double x = 0.0;
        double y = 0.0;
        int total = 0;
        if(enemies.length == 0) return;
        if(!targetAndAttack(rc, enemies)) return;
        for(RobotInfo enemy: enemies){
            if(enemy.getType() == RobotType.CARRIER) continue;
            total+=1;
            MapLocation away = rc.getLocation().add(rc.getLocation().directionTo(enemy.getLocation()).opposite());
            x += (away.x);
            y += (away.y);
        }
        for(RobotInfo ally: allies){
            if(ally.getType() == RobotType.CARRIER) continue;
            total+=1;
            MapLocation to = rc.getLocation().add(rc.getLocation().directionTo(ally.getLocation()));
            x += (to.x);
            y += (to.y);
        }
        x/=total;
        y/=total;
        MapLocation escape_regroup = new MapLocation((int)Math.round(x), (int)Math.round(y));
        Pathing.moveTowards(rc, escape_regroup);
        //targetAndAttack(rc, rc.senseNearbyRobots(-1,rc.getTeam().opponent()));
    }

    static boolean targetAndAttack(RobotController rc, RobotInfo [] enemies) throws GameActionException{
        if(enemies.length == 0) return true;
        RobotInfo target = null;
        int lowestHealth = 1000;
        int smallestDistance = 1000;
        int bestTarget = 6;
        for (RobotInfo enemy: enemies){
            if(enemy.getType() == RobotType.HEADQUARTERS){
                Communication.addEHq(enemy, rc);
                Direction moveDir = rc.getLocation().directionTo(enemy.getLocation()).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                    followStep = false;
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
                bestTarget = priority.get(enemyType);
            }
            else if (enemyDistance < smallestDistance){
                target = enemy;
                smallestDistance = enemyDistance;
                bestTarget = priority.get(enemyType);
            }
        }
        if(target == null) return false;
        if (rc.canAttack(target.getLocation())){
            rc.attack(target.getLocation());
            return true;
        }else{
            Pathing.moveTowards(rc, target.getLocation());
            if (rc.canAttack(target.getLocation())){
                rc.attack(target.getLocation());
                return false;
            }
            //return true;
        }
        return false;

    }

    static void islandDef(RobotController rc) throws GameActionException{
        if(islandLoc == null) {
            double lowDistance = 1000;
            for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS; i++) {
                MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
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
            if(rc.canSenseLocation(islandLoc) && rc.senseTeamOccupyingIsland(rc.senseIsland(islandLoc)) != rc.getTeam().opponent()){
                islandLoc = null;
            }else{
            Pathing.moveTowards(rc, islandLoc);
            }
        }
    }

    static void healingStrat (RobotController rc) throws GameActionException{
        if(rc.getHealth() > RobotType.LAUNCHER.getMaxHealth()/2) return;
        MapLocation healthIsland = null;
        int distance = 1000;
        for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS; i++) {
            MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
            if (islandNearestLoc != null) {
                int dist = rc.getLocation().distanceSquaredTo(islandNearestLoc);
                if(Communication.readTeamHoldingIsland(rc, i) == rc.getTeam()){ 
                    if(rc.getLocation() != islandNearestLoc && dist < distance) {
                        healthIsland = islandNearestLoc;
                        distance = dist;
                    }
                }
            }
        }
        if(healthIsland != null)
            Pathing.moveTowards(rc, healthIsland);
    }

    static void explore(RobotController rc) throws GameActionException{
        if(rc.getLocation().distanceSquaredTo(RobotPlayer.center) < 6){
            //RobotPlayer.toCenter = false;
            if((survey && scanAmp(rc)) && rc.senseNearbyRobots(-1,rc.getTeam()).length > 15){
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
            if (leaderPos != null && rc.getLocation().distanceSquaredTo(leaderPos) > 2){
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

    static void moveToEnemies(RobotController rc) throws GameActionException{
        if(rc.senseNearbyRobots(-1,rc.getTeam()).length < 4) return;
        MapLocation enemyLocation= Communication.getClosestEnemy(rc);
        
        if(enemyLocation != null){
            Pathing.moveTowards(rc, enemyLocation);
            //rc.setIndicatorString(enemyLocation.toString());
        }
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            if(rc.senseTeamOccupyingIsland(id) == rc.getTeam().opponent()) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if(locs.length > 0) {
                    islandLoc = locs[0];
                }
            }
            Communication.updateIslandInfo(rc, id);
        }
    }

    static void avoidHqKillRadius(RobotController rc) throws GameActionException{
        for(MapLocation ehq : Communication.eheadquarterLocs){
            if(ehq != null && rc.getLocation().distanceSquaredTo(ehq) < RobotType.LAUNCHER.actionRadiusSquared){
                Direction moveDir = rc.getLocation().directionTo(ehq).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                    return;
                }
                //Clock.yield();
            }
        }
        for(RobotInfo enemy : rc.senseNearbyRobots(-1,rc.getTeam().opponent()))
            if(enemy.getType() == RobotType.HEADQUARTERS){
                MapLocation enemyLocation = enemy.getLocation();
                if(rc.getLocation().distanceSquaredTo(enemyLocation) < RobotType.LAUNCHER.actionRadiusSquared){
                    Direction moveDir = rc.getLocation().directionTo(enemyLocation).opposite();
                    if (rc.canMove(moveDir)) {
                        rc.move(moveDir);
                        followStep = false;
                        return;
                    }
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
