package playerDream;

import battlecode.common.*;

public class AmplifierStrategy {


    static void runAmplifier(RobotController rc) throws GameActionException{
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        scanIslands(rc);
        RobotPlayer.scan(rc);
        RobotPlayer.squad(rc);
        RobotPlayer.moveSquad(rc);

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (RobotInfo enemy : visibleEnemies) {
            if(enemy.getType() == RobotType.HEADQUARTERS){
                Communication.addEHq(enemy, rc);
                Direction moveDir = rc.getLocation().directionTo(enemy.getLocation()).opposite();
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
                break;
            }
        }


        if(RobotPlayer.following != null && rc.canSenseRobot(RobotPlayer.following.getID())){
            Pathing.moveTowards(rc, rc.senseRobot(RobotPlayer.following.getID()).getLocation());
            Direction moveDir = rc.getLocation().directionTo(RobotPlayer.following.getLocation());
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
        }else{
            RobotPlayer.follower = false;
        }
        MapLocation enemyLocation= Communication.getClosestEnemy(rc);
        if(enemyLocation != null){
            MapLocation robotLocation = rc.getLocation();
            Pathing.moveTowards(rc, enemyLocation);
        }
    }
    // static void runAmplifier(RobotController rc, int temp) throws GameActionException {
    //     if (RobotPlayer.turnCount == 1){
    //         Clock.yield();
    //     }
    //     if (RobotPlayer.turnCount == 2) {
    //         Communication.updateHeadquarterInfo(rc);
    //         Clock.yield();
    //     }
    //     scanIslands(rc);
    //     RobotPlayer.scan(rc);
    //     Direction d = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
    //     RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
    //     for (RobotInfo enemy : visibleEnemies) {
    //         if(enemy.getType() == RobotType.LAUNCHER || enemy.getType() == RobotType.DESTABILIZER ){
    //             d = rc.getLocation().directionTo(enemy.getLocation()).opposite();
    //             break;
    //         }
    //     }

        
    //     if (rc.canMove(d)) {
    //         rc.move(d);
    //     } else {
    //         for (int i = 0; i < 8; i++) {
    //             if(d == rc.getLocation().directionTo(Communication.headquarterLocs[0])) continue;
    //             if (rc.canMove(d)) {
    //                 rc.move(d);
    //                 break;
    //             } else {
    //                 d = d.rotateLeft();
    //             }
    //         }
    //     }

    //     // Direction moveDir = rc.getLocation().directionTo(Communication.headquarterLocs[0]).opposite();
    //     // if (rc.canMove(moveDir)) {
    //     //     rc.move(moveDir);
    //     // }
    //     // Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
    //     // if (rc.canMove(dir)) {
    //     //     rc.move(dir);
    //     // }
    //     // // move to furtherst corner and run away from enemies then move randomly
    // }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            Communication.updateIslandInfo(rc, id);
        }
    }
}
