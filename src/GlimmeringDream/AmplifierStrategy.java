package GlimmeringDream;

import battlecode.common.*;

public class AmplifierStrategy {


    static void runAmplifier(RobotController rc) throws GameActionException{
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        scanIslands(rc);
        RobotPlayer.scan(rc);
        // RobotPlayer.squad(rc);
        // RobotPlayer.moveSquad(rc);

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
            Pathing.moveTowards(rc, enemyLocation);
        }

        if(rc.getLocation().distanceSquaredTo(RobotPlayer.center) < 6){
            RobotPlayer.toCenter = false;
        }else if(RobotPlayer.myhq != null && rc.getLocation().distanceSquaredTo(RobotPlayer.myhq) < 3){
            if(RobotPlayer.toCenter == false){
                RobotPlayer.toCenter =true;
                if(RobotPlayer.explore.size()>0){
                    RobotPlayer.center = RobotPlayer.explore.get(0);
                    RobotPlayer.explore.remove(0);
                }
            }
        }

        if(RobotPlayer.toCenter){
            Pathing.moveTowards(rc, RobotPlayer.center);
        }
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            Communication.updateIslandInfo(rc, id);
        }
    }
}
