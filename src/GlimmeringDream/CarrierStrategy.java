package GlimmeringDream;

import java.util.ArrayList;

import battlecode.common.*;
import battlecode.world.Island;

public class CarrierStrategy {
    
    static MapLocation hqLoc;
    static MapLocation wellLoc;
    static ArrayList <MapLocation> wellLocs = new ArrayList<MapLocation>();
    static MapLocation islandLoc;

    static boolean anchorMode = false;
    static int numHeadquarters = 0;
    static boolean mana = false;
    static boolean enemy = false;
    static MapLocation movingto;
    static int movingtocount = 0;
    static ArrayList <MapLocation> blacklist = new ArrayList<MapLocation>();
    static boolean runHome = false;

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        enemy = false;
        //if(hqLoc == null) 
        //scanHQ(rc);
        scanWells(rc);
        if(wellLoc == null && wellLocs.size() > 0){
            wellLoc = wellLocs.get(RobotPlayer.rng.nextInt(wellLocs.size()));
            int attempts = 0;
            while(blacklist.contains(wellLoc) && attempts < 5){
                attempts += 1;
                wellLoc = wellLocs.get(RobotPlayer.rng.nextInt(wellLocs.size()));
            }
            movingtocount = 0;
        }
        if(!mana){
            for(WellInfo well: rc.senseNearbyWells(rc.getLocation(), -1)){
                if(well.getResourceType() == ResourceType.MANA){
                    mana = true;
                    break;
                }
            }
        }
        scanIslands(rc);
        scan(rc);
        if(RobotPlayer.turnCount > 2){
            MapLocation res = Communication.getClosestHQ(rc);
            if(res != null)
                hqLoc = res;
        }

        if(rc.getAnchor() != null){
            anchorMode =true;
            if(rc.canPlaceAnchor()){
                rc.placeAnchor();
            }
        }
        if (enemy){
            enemy(rc);
        }
        if(runHome){
            Pathing.moveTowards(rc, hqLoc);
            wellLoc = null;
        }

        if(wellLoc != null && rc.canCollectResource(wellLoc, -1)){ 
            rc.collectResource(wellLoc, -1);
        }

        //Transfer resource to headquarters
        depositResource(rc, ResourceType.ADAMANTIUM);
        depositResource(rc, ResourceType.MANA);
        if(islandLoc != null){
            if(rc.canTakeAnchor(hqLoc, Anchor.STANDARD)) {
                rc.takeAnchor(hqLoc, Anchor.STANDARD);
                anchorMode = true;
            }
            if(rc.canTakeAnchor(hqLoc, Anchor.ACCELERATING)) {
                rc.takeAnchor(hqLoc, Anchor.ACCELERATING);
                anchorMode = true;
            }
        }

        //Collect from well if close and inventory not full
        

        
        //no resources -> look for well
        healingStrat(rc);
        if(anchorMode) {
            anchorStrat(rc);
        } else {
            int total = getTotalResources(rc);
            if(total < GameConstants.CARRIER_CAPACITY-1) {
                //move towards well or search for well
                if(wellLoc == null) Pathing.moveTowards(rc, RobotPlayer.center);
                else if(!rc.getLocation().isAdjacentTo(wellLoc)){
                    Pathing.moveTowards(rc, wellLoc);
                    movingtocount += 1;
                    if(movingtocount > 30){
                        blacklist.add(wellLoc);
                        wellLoc = null;
                    }
                }else{
                    // for (Direction currentDirection: RobotPlayer.directions) {
                    //     MapLocation sensing = rc.getLocation().add(currentDirection);
                    //     if(!rc.canSenseLocation(sensing)) continue;
                    //     MapInfo movetile = rc.senseMapInfo(sensing);
                    //     if (rc.canSenseLocation(sensing)&& sensing.isAdjacentTo(wellLoc) && rc.canMove(currentDirection) && !rc.canSenseRobotAtLocation(sensing) && (!(movetile.getCurrentDirection() == currentDirection.opposite()) || movetile.getCurrentDirection() == Direction.CENTER)) {
                    //         rc.move(currentDirection);
                    //     }
                    // }
                }
            }
            if(total >= GameConstants.CARRIER_CAPACITY-1) {
                //move towards HQ
                Pathing.moveTowards(rc, hqLoc);
            }
        }

        // Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        // if (rc.canMove(dir)) {
        //     rc.move(dir);
        // }
        
        Communication.tryWriteMessages(rc);
    }

    static void healingStrat (RobotController rc) throws GameActionException{
        if(rc.getHealth() > RobotType.CARRIER.getMaxHealth()/3) return;
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

    static void anchorStrat(RobotController rc) throws GameActionException{
        if(islandLoc == null){ 
            for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS; i++) {
                if(rc.readSharedArray(i+Communication.STARTING_ISLAND_IDX) == 0){
                    continue;
                }
                MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
                float lowestDistance = 10000;
                if (islandNearestLoc != null) {
                    float dist = rc.getLocation().distanceSquaredTo(islandNearestLoc);
                    if(Communication.readTeamHoldingIsland(rc, i) == Team.NEUTRAL && dist < lowestDistance){
                        islandLoc = islandNearestLoc;
                        lowestDistance = dist;
                    }
                }
            }
        } else Pathing.moveTowards(rc, islandLoc); 
        if(islandLoc != null){ 
            Pathing.moveTowards(rc, islandLoc); 
        }
        

        if(rc.canPlaceAnchor() && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == Team.NEUTRAL) {
            rc.placeAnchor();
            anchorMode = false;
            islandLoc = null;
        } else if(rc.senseIsland(rc.getLocation()) != -1 && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == rc.getTeam()){
            islandLoc = null;
        }
        // } else if(rc.senseIsland(rc.getLocation()) != -1 && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) != Team.NEUTRAL){
        //     if(rc.canAttack(rc.getLocation().add(RobotPlayer.directions[0]))){
        //         rc.attack(rc.getLocation().add(RobotPlayer.directions[0]));
        //         anchorMode = false;
        //     }
            
        // }
        // Direction moveDir = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
        // if (rc.canMove(moveDir)) {
        //     rc.move(moveDir);
        // }
    }

    static void enemy(RobotController rc) throws GameActionException{
        RobotInfo[] enemies = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
        RobotInfo target = null;
        double x = 0.0;
        double y = 0.0;
        int total = enemies.length;
        int lowestHealth = 1000;
        int smallestDistance = 100;
        for(RobotInfo enemy: enemies){
            MapLocation away = rc.getLocation().add(rc.getLocation().directionTo(enemy.getLocation()).opposite());
            x += (away.x/total);
            y += (away.y/total);
            int enemyHealth = enemy.getHealth();
            int enemyDistance = enemy.getLocation().distanceSquaredTo(rc.getLocation());
            RobotType enemyType = enemy.getType();
            if (enemyType == RobotType.LAUNCHER){
                if (enemyHealth < lowestHealth){
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
        MapLocation escape = new MapLocation((int)Math.round(x), (int)Math.round(y));
        if(target != null && rc.canAttack(target.getLocation())){
            rc.attack(target.getLocation());
        }
        Pathing.moveTowards(rc, escape);

        wellLoc = null;

        
    }


    static void scanHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.HEADQUARTERS) {
                hqLoc = robot.getLocation();
                if(rc.getLocation().distanceSquaredTo(hqLoc) < RobotType.CARRIER.actionRadiusSquared) runHome = false;
                break;
            }
        }
    }

    static void scan(RobotController rc) throws GameActionException {
        Communication.clearObsoleteEnemies(rc);
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getTeam() == rc.getTeam().opponent()){
                Communication.reportEnemy(rc, robot.getLocation());
                if(robot.getType() == RobotType.HEADQUARTERS) {
                    Communication.addEHq(robot, rc);
                    continue;
                }else if(robot.getType() != RobotType.CARRIER){
                    enemy = true;
                }
                
            }else{
                if(RobotPlayer.turnCount == 1 && robot.getType() == RobotType.HEADQUARTERS)
                    hqLoc = robot.getLocation();
            }
        }
    }


    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length > 0) {
            for(WellInfo well : wells){
                if(!wellLocs.contains(well.getMapLocation())){
                    wellLocs.add(well.getMapLocation());
                    if(well.getResourceType() == ResourceType.MANA){
                        Communication.addWell(well, rc);
                        wellLocs.add(well.getMapLocation());
                        //wellLocs.add(well.getMapLocation());
                    }
                }
            }
        }
        MapLocation cwell = Communication.getNearestWell(rc);
        if(cwell != null){
            wellLocs.add(cwell);
            wellLocs.add(cwell);
            //wellLocs.add(cwell);
        }
    }

    static void depositResource(RobotController rc, ResourceType type) throws GameActionException {
        int amount = rc.getResourceAmount(type);
        if(amount > 0) {
            if(rc.canTransferResource(hqLoc, type, amount)) {
                rc.transferResource(hqLoc, type, amount);
                wellLoc = null;
            }
        }
    }

    static int getTotalResources(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) 
            + rc.getResourceAmount(ResourceType.MANA) 
            + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        islandLoc = null;
        for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS; i++) {
            // if(rc.readSharedArray(i+Communication.STARTING_ISLAND_IDX) == 0){
            //     continue;
            // }
            MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
            //System.out.println("read"+islandNearestLoc);
            //System.out.println("read"+islandNearestLoc);
            float lowestDistance = 10000;
            if (islandNearestLoc != null) {
                float dist = rc.getLocation().distanceSquaredTo(islandNearestLoc);
                if(Communication.readTeamHoldingIsland(rc, i) == Team.NEUTRAL && dist < lowestDistance){
                    islandLoc = islandNearestLoc;
                    lowestDistance = dist;
                }
            }
        }
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            if(rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if(locs.length > 0) {
                    islandLoc = locs[0];
                    break;
                }
            }
            Communication.updateIslandInfo(rc, id);
        }
        
    }
}
