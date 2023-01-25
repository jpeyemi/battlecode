package playerDream;

import java.util.List;

import java.util.ArrayList;

import battlecode.common.*;

class Message {
    public int idx;
    public int value;
    public int turnAdded;

    Message (int idx, int value, int turnAdded) {
        this.idx = idx;
        this.value = value;
        this.turnAdded = turnAdded;
    }
}

class Communication {

    private static final int OUTDATED_TURNS_AMOUNT = 30;
    private static final int AREA_RADIUS = RobotType.CARRIER.visionRadiusSquared;
    private static final int MAX_SQUADS = 3;
    private static final int MAX_WELLS = 6;
    // Maybe you want to change this based on exact amounts which you can get on turn 1
    static final int STARTING_ISLAND_IDX = 2*GameConstants.MAX_STARTING_HEADQUARTERS + 1;
    private static final int STARTING_LEADER_INDEX = GameConstants.MAX_NUMBER_ISLANDS + STARTING_ISLAND_IDX;
    private static final int STARTING_WELL_INDEX = GameConstants.MAX_NUMBER_ISLANDS + STARTING_ISLAND_IDX;
    private static final int STARTING_ENEMY_IDX = STARTING_LEADER_INDEX + MAX_WELLS;

    private static final int TOTAL_BITS = 16;
    private static final int MAPLOC_BITS = 12;
    private static final int TEAM_BITS = 1;
    private static final int HEALTH_BITS = 3;
    private static final int HEALTH_SIZE = (int) Math.ceil(Anchor.ACCELERATING.totalHealth / 8.0);
    private static final int TURN_CYCLE = 20;
    private static final int TURN_CYCLE_BITS = 1;


    private static List<Message> messagesQueue = new ArrayList<>();
    public static MapLocation[] headquarterLocs = new MapLocation[GameConstants.MAX_STARTING_HEADQUARTERS];
    public static MapLocation[] eheadquarterLocs = new MapLocation[GameConstants.MAX_STARTING_HEADQUARTERS];



    static void addHeadquarter(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, locationToInt(rc, me));
                break;
            }
        }
    }

    static void writeTurnCount(RobotController rc) throws GameActionException {
        if(rc.readSharedArray(GameConstants.MAX_STARTING_HEADQUARTERS + 1) != RobotPlayer.turnCount){
            rc.writeSharedArray(GameConstants.MAX_STARTING_HEADQUARTERS + 1, RobotPlayer.turnCount);
        }
    }

    static int readTurnCount(RobotController rc) throws GameActionException{
        return (rc.readSharedArray(GameConstants.MAX_STARTING_HEADQUARTERS + 1));
    }

    static void addEHq(RobotInfo ri, RobotController rc) throws GameActionException{
        MapLocation loc = ri.getLocation();
        for (int i = GameConstants.MAX_STARTING_HEADQUARTERS+1; i < 2*GameConstants.MAX_STARTING_HEADQUARTERS+1; i++) {
            int iloc = locationToInt(rc, loc);
            if (rc.readSharedArray(i) == iloc) break;
            if (rc.readSharedArray(i) == 0) {
                Message msg = new Message(i, locationToInt(rc, loc), RobotPlayer.turnCount);
                messagesQueue.add(msg);
                break;
            }
        }
    }
    static void addWell(WellInfo w, RobotController rc) throws GameActionException{
        MapLocation loc = w.getMapLocation();
        for (int i = STARTING_WELL_INDEX; i < MAX_WELLS + STARTING_WELL_INDEX; i++) {
            int iloc = locationToInt(rc, loc);
            if (rc.readSharedArray(i) == iloc) break;
            if (rc.readSharedArray(i) == 0) {
                Message msg = new Message(i, locationToInt(rc, loc), RobotPlayer.turnCount);
                messagesQueue.add(msg);
                break;
            }
        }
    }

    static MapLocation getNearestWell(RobotController rc) throws GameActionException {
        MapLocation answer = null;
        for (int i = STARTING_WELL_INDEX; i < STARTING_WELL_INDEX + MAX_WELLS; i++) {
            final int value;
            if(rc.readSharedArray(i) == 0 ){
                break;
            }
            try {
                value = rc.readSharedArray(i);
                final MapLocation m = intToLocation(rc, value);
                if (m != null && (answer == null || rc.getLocation().distanceSquaredTo(m) < rc.getLocation().distanceSquaredTo(answer))) {
                    answer = m;
                }
            } catch (GameActionException e) {
                continue;
            }
        }
        return answer;
    }



    static void readEHq(RobotController rc) throws GameActionException{
        for (int i = GameConstants.MAX_STARTING_HEADQUARTERS+1; i < 2*GameConstants.MAX_STARTING_HEADQUARTERS+1; i++) {
            eheadquarterLocs[i] = (intToLocation(rc, rc.readSharedArray(i)));
            if (rc.readSharedArray(i) == 0) {
                break;
            }
        }
    }

    static void updateHeadquarterInfo(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
                headquarterLocs[i] = (intToLocation(rc, rc.readSharedArray(i)));
                if (rc.readSharedArray(i) == 0) {
                    break;
                }
            }
        }
    }

    static void tryWriteMessages(RobotController rc) throws GameActionException {
        messagesQueue.removeIf(msg -> msg.turnAdded + OUTDATED_TURNS_AMOUNT < RobotPlayer.turnCount);
        // Can always write (0, 0), so just checks are we in range to write
        if (rc.canWriteSharedArray(0, 0)) {
            while (messagesQueue.size() > 0 ) {
                Message msg = messagesQueue.remove(0);
                if (rc.readSharedArray(msg.idx) != msg.value && rc.canWriteSharedArray(msg.idx, msg.value)) {
                    try{
                        rc.writeSharedArray(msg.idx, msg.value);
                    } catch (GameActionException e){
                        continue;
                    }
                }
            }
        }
    }

    static void updateIslandInfo(RobotController rc, int id) throws GameActionException {
        if (headquarterLocs[0] == null) {
            return;
        }
        MapLocation closestIslandLoc = null;
        int closestDistance = -1;
        MapLocation[] islandLocs = rc.senseNearbyIslandLocations(id);
        for (MapLocation loc : islandLocs) {
            int distance = headquarterLocs[0].distanceSquaredTo(loc);
            if (closestIslandLoc == null || distance < closestDistance) {
                closestDistance = distance;
                closestIslandLoc = loc;
            }
        }
        if(closestDistance == -1){
            return;
        }
        //System.out.println("writing"+closestIslandLoc.toString());
        // Remember reading is cheaper than writing so we don't want to write without knowing if it's helpful
        int idx = id + STARTING_ISLAND_IDX;
        int oldIslandValue = rc.readSharedArray(idx);
        int updatedIslandValue = bitPackIslandInfo(rc, id, closestIslandLoc);
        if (oldIslandValue != updatedIslandValue) {
            Message msg = new Message(idx, updatedIslandValue, RobotPlayer.turnCount);
            messagesQueue.add(msg);
        }
    }

    static int bitPackIslandInfo(RobotController rc, int islandId, MapLocation closestLoc) {
        int islandInt = locationToInt(rc, closestLoc);
        islandInt = islandInt << (TOTAL_BITS - MAPLOC_BITS);
        try {
            Team teamHolding = rc.senseTeamOccupyingIsland(islandId);
            islandInt += teamHolding.ordinal() << (TOTAL_BITS - MAPLOC_BITS - TEAM_BITS);
            int islandHealth = rc.senseAnchorPlantedHealth(islandId);
            int healthEncoding = (int) Math.ceil((double) islandHealth / HEALTH_SIZE);
            islandInt += healthEncoding;
            return islandInt;
        } catch (GameActionException e) {
            return islandInt;
        }
    }

    static int bitPackSquadInfo(RobotController rc, int turncycle, int leaderID, MapLocation loc){
        int TeamInt = locationToInt(rc, loc);
        TeamInt = TeamInt << (TOTAL_BITS - MAPLOC_BITS);
        TeamInt += turncycle << (TOTAL_BITS - MAPLOC_BITS - TEAM_BITS);
        int leaderLast = leaderID%10;
        if(leaderLast == 9){
            leaderLast = 0;
        }
        if(leaderLast == 8){
            leaderLast = 1;
        }
        TeamInt += leaderLast;
        return TeamInt;
    }

    static MapLocation readSquadLocation(RobotController rc, int squad) {
        try {
            int squadInt = rc.readSharedArray(squad);
            int idx = squadInt >> (HEALTH_BITS + TEAM_BITS);
            return intToLocation(rc, idx);
        } catch (GameActionException e) {} 
        return null;
    }

    static int readSquadCycle(RobotController rc, int squad) throws GameActionException{
        int squadInt = rc.readSharedArray(squad);
        int cycle = (squadInt >> HEALTH_BITS) % 0b1;
        return(cycle);
    }

    static boolean squadExists(RobotController rc, int squad) throws GameActionException{
        if(rc.readSharedArray(squad) == 0){
            return(false);
        }
        return(true);
    }

    static int readSquadLeader(RobotController rc, int squad) {
        try {
            int squadInt = rc.readSharedArray(squad);
            int healthMask = 0b111;
            int leaderLast = squadInt & healthMask;
            return(leaderLast);
        } catch (GameActionException e) {
            return -1;
        } 
    }


    static Team readTeamHoldingIsland(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int healthMask = 0b111;
            int health = islandInt & healthMask;
            int team = (islandInt >> HEALTH_BITS) % 0b1;
            if (health > 0) {
                return Team.values()[team];
            }
        } catch (GameActionException e) {} 
        return Team.NEUTRAL;
    }

    static MapLocation readIslandLocation(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int idx = islandInt >> (HEALTH_BITS + TEAM_BITS);
            //System.out.println("writing"+intToLocation(rc, idx).toString());
            return intToLocation(rc, idx);
        } catch (GameActionException e) {} 
        return null;
    }

    static int readMaxIslandHealth(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int healthMask = 0b111;
            int health = islandInt & healthMask;
            return health*HEALTH_SIZE;
        } catch (GameActionException e) {
            return -1;
        } 
    }


    static void clearObsoleteEnemies(RobotController rc) {
        for (int i = STARTING_ENEMY_IDX; i < GameConstants.SHARED_ARRAY_LENGTH; i++) {
            try {
                MapLocation mapLoc = intToLocation(rc, rc.readSharedArray(i));
                if (mapLoc == null) {
                    continue;
                }
                if (rc.canSenseLocation(mapLoc) && rc.senseNearbyRobots(mapLoc, AREA_RADIUS, rc.getTeam().opponent()).length == 0) {
                    Message msg = new Message(i, locationToInt(rc, null), RobotPlayer.turnCount);
                    messagesQueue.add(msg);
                }
            } catch (GameActionException e) {
                continue;
            }

        }
    }

    static void reportEnemy(RobotController rc, MapLocation enemy) {
        int slot = -1;
        for (int i = STARTING_ENEMY_IDX; i < GameConstants.SHARED_ARRAY_LENGTH; i++) {
            try {
                MapLocation prevEnemy = intToLocation(rc, rc.readSharedArray(i));
                if (prevEnemy == null) {
                    slot = i;
                    break;
                } else if (prevEnemy.distanceSquaredTo(enemy) < AREA_RADIUS) {
                    return;
                }
            } catch (GameActionException e) {
                continue;
            }
        }
        if (slot != -1) {
            Message msg = new Message(slot, locationToInt(rc, enemy), RobotPlayer.turnCount);
            messagesQueue.add(msg);
        }
    }

    static MapLocation getClosestEnemy(RobotController rc) throws GameActionException{
        MapLocation answer = null;
        for (int i = STARTING_ENEMY_IDX; i < GameConstants.SHARED_ARRAY_LENGTH; i++) {
            final int value;
            try {
                value = rc.readSharedArray(i);
                final MapLocation m = intToLocation(rc, value);
                if (m != null && (answer == null || rc.getLocation().distanceSquaredTo(m) < rc.getLocation().distanceSquaredTo(answer))) {
                    answer = m;
                }
            } catch (GameActionException e) {
                continue;
            }
        }
        return answer;
    }

    static int writeSquad(RobotController rc){
        int slot = -1;
        int message = -1;
        int numbers = -1;
        for (int i = STARTING_LEADER_INDEX; i < STARTING_LEADER_INDEX + 2*MAX_SQUADS; i+=2) {
            try {
                int turnCount = rc.readSharedArray(GameConstants.MAX_STARTING_HEADQUARTERS+1);
                int cycle = 0;
                if((turnCount+1) % TURN_CYCLE > TURN_CYCLE/2) cycle = 1;
                if(rc.readSharedArray(i) == 0){
                    message = bitPackSquadInfo(rc, cycle, rc.getID(), rc.getLocation());
                    slot = i;
                }
                //bitpack surrounding squad robots
            } catch (GameActionException e) {
                continue;
            }
        }
        if (slot != -1) {
            RobotPlayer.leader = true;
            Message msg = new Message(slot, message, RobotPlayer.turnCount);

            messagesQueue.add(msg);
            System.out.println("Squade made");
            RobotPlayer.squad = slot;
        }
        return(-1);
    }
    static void updateSquad(RobotController rc, int slot) throws GameActionException{
        int turnCount = rc.readSharedArray(GameConstants.MAX_STARTING_HEADQUARTERS+1);
        int cycle = 0;
        if((turnCount+1) % TURN_CYCLE > TURN_CYCLE/2) cycle = 1;
        if(cycle != readSquadCycle(rc, slot)){
            int message =  bitPackSquadInfo(rc, cycle, rc.getID(), rc.getLocation());
            Message msg = new Message(slot, message, RobotPlayer.turnCount);
            messagesQueue.add(msg);
        }
    }

    static void checkSquad(RobotController rc) throws GameActionException{
        int cycle = 0;
        if((readTurnCount(rc)) % TURN_CYCLE > TURN_CYCLE/2) cycle = 1;
        for (int i = STARTING_LEADER_INDEX; i < STARTING_LEADER_INDEX + MAX_SQUADS; i++) {
            if(readSquadCycle(rc, i) != cycle){
                rc.writeSharedArray(i,0);
            }
        }
    }

    static void joinSquad(RobotController rc, int ind){

    }

    static void joinSquad(RobotController rc) throws GameActionException{

        for (int i = STARTING_LEADER_INDEX; i < STARTING_LEADER_INDEX + 2*MAX_SQUADS; i+=2) {
            if(rc.readSharedArray(i) == 0 && rc.getID()%10 != 9 && rc.getID()%10 != 8 && rc.getType() == RobotType.LAUNCHER){
                writeSquad(rc);
                return;
            }
        }
        int team = RobotPlayer.rng.nextInt(MAX_SQUADS)*2;
        if(rc.readSharedArray(team + STARTING_LEADER_INDEX) != 0){
            RobotPlayer.squad = team + STARTING_LEADER_INDEX;
            rc.setIndicatorString("joined squad");
        }else{
            RobotPlayer.squad = -1;
        } 
    }

    private static int locationToInt(RobotController rc, MapLocation m) {
        if (m == null) {
            return 0;
        }
        return 1 + m.x + m.y * rc.getMapWidth();
    }

    private static MapLocation intToLocation(RobotController rc, int m) {
        if (m == 0) {
            return null;
        }
        m--;
        return new MapLocation(m % rc.getMapWidth(), m / rc.getMapWidth());
    }
}