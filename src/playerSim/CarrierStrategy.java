package playerSim;

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

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        if(hqLoc == null) scanHQ(rc);
        scanWells(rc);
        if(wellLoc == null && wellLocs.size() > 0) wellLoc = wellLocs.get(RobotPlayer.rng.nextInt(wellLocs.size()));
        scanIslands(rc);
        RobotPlayer.scan(rc);

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
        if(wellLoc != null && rc.canCollectResource(wellLoc, -1)) rc.collectResource(wellLoc, -1);

        
        //no resources -> look for well
        if(anchorMode) {
            if(islandLoc == null){ 
                for (int i = Communication.STARTING_ISLAND_IDX; i < Communication.STARTING_ISLAND_IDX + GameConstants.MAX_NUMBER_ISLANDS; i++) {
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
            }
            if(islandLoc != null){
                Pathing.moveTowards(rc, islandLoc); 
            }
            

            if(rc.canPlaceAnchor() && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == Team.NEUTRAL) {
                rc.placeAnchor();
                anchorMode = false;
                islandLoc = null;
            }else if(rc.senseIsland(rc.getLocation()) != -1 && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) != Team.NEUTRAL){
                if(rc.canAttack(rc.getLocation().add(RobotPlayer.directions[0]))){
                    rc.attack(rc.getLocation().add(RobotPlayer.directions[0]));
                    anchorMode = false;
                }
                
            }
            // Direction moveDir = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
            // if (rc.canMove(moveDir)) {
            //     rc.move(moveDir);
            // }
        }
        else {
            int total = getTotalResources(rc);
            if(total == 0) {
                //move towards well or search for well
                if(wellLoc == null) RobotPlayer.moveRandom(rc);
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
                }
            }
        };
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
