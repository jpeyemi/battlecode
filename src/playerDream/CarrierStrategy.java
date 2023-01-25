package playerDream;

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

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        //if(hqLoc == null) 
        scanHQ(rc);
        scanWells(rc);
        if(wellLoc == null && wellLocs.size() > 0) wellLoc = wellLocs.get(RobotPlayer.rng.nextInt(wellLocs.size()));
        for(WellInfo well: rc.senseNearbyWells(hqLoc, numHeadquarters)){
            if(well.getResourceType() == ResourceType.MANA){
                mana = true;
                break;
            }
        }
        if(!mana && RobotPlayer.turnCount < 10)
        {
            // RobotInfo[] allies = rc.senseNearbyRobots(2, rc.getTeam());
            // int lowestID = rc.getID();
            // MapLocation leaderPos = null;
            // for (RobotInfo ally : allies){
            //     if (ally.getType() != RobotType.CARRIER)
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
            //     Direction moveDir = rc.getLocation().directionTo(hqLoc).opposite();
            //     if (rc.canMove(moveDir)) {
            //         rc.move(moveDir);
            //     }
            //     rc.setIndicatorString("I'm the leader!");
            // }
            
        }
        scanIslands(rc);
        RobotPlayer.scan(rc);

        if(rc.getAnchor() != null){
            anchorMode =true;
        }


        if(wellLoc != null && rc.canCollectResource(wellLoc, -1)) rc.collectResource(wellLoc, -1);

        //Transfer resource to headquarters
        depositResource(rc, ResourceType.ADAMANTIUM);
        depositResource(rc, ResourceType.MANA);
        if(rc.canTakeAnchor(hqLoc, Anchor.STANDARD)) {
            rc.takeAnchor(hqLoc, Anchor.STANDARD);
            anchorMode = true;
        }
        if(rc.canTakeAnchor(hqLoc, Anchor.ACCELERATING)) {
            rc.takeAnchor(hqLoc, Anchor.ACCELERATING);
            anchorMode = true;
        }

        //Collect from well if close and inventory not full
        

        
        //no resources -> look for well
        if(anchorMode) {
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
            } else if(rc.senseIsland(rc.getLocation()) != -1 && rc.getLocation().distanceSquaredTo(islandLoc) < 2 && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == rc.getTeam()){
                islandLoc = null;

            }
            // }else if(rc.senseIsland(rc.getLocation()) != -1 && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) != Team.NEUTRAL){
            //     if(rc.canAttack(rc.getLocation().add(RobotPlayer.directions[0]))){
            //         rc.attack(rc.getLocation().add(RobotPlayer.directions[0]));
            //         anchorMode = false;
            //     }
                
            // }
            // Direction moveDir = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
            // if (rc.canMove(moveDir)) {
            //     rc.move(moveDir);
            // }
        } else {
            int total = getTotalResources(rc);
            if(total == 0) {
                //move towards well or search for well
                if(wellLoc == null) Pathing.moveTowards(rc, RobotPlayer.center);
                else if(!rc.getLocation().isAdjacentTo(wellLoc)) Pathing.moveTowards(rc, wellLoc);
            }
            if(total == GameConstants.CARRIER_CAPACITY) {
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

    static void scanHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.HEADQUARTERS) {
                hqLoc = robot.getLocation();
                break;
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
            //wellLocs.add(cwell);
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
        for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS; i++) {
            // if(rc.readSharedArray(i+Communication.STARTING_ISLAND_IDX) == 0){
            //     continue;
            // }
            MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
            System.out.println("read"+islandNearestLoc);
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
