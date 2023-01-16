package player2;

import java.util.HashMap;

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
        priority.put(RobotType.CARRIER, 4);
        priority.put(RobotType.LAUNCHER, 3);
        priority.put(RobotType.HEADQUARTERS, 3);
    }

    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        int lowestHealth = 100;
        int smallestDistance = 100;
        int bestTarget = 5;
        RobotInfo target = null;
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        Communication.clearObsoleteEnemies(rc);
        if (enemies.length > 0) {
            for (RobotInfo enemy: enemies){
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
        Communication.tryWriteMessages(rc);
        if (target != null){
            if (rc.canAttack(target.getLocation()))
                rc.attack(target.getLocation());
        }
        else {
           
            WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0){
                MapLocation wellLoc = wells[0].getMapLocation();
                Direction dir = rc.getLocation().directionTo(wellLoc);
                if (rc.canMove(dir))
                    rc.move(dir);
            }
        }

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        for (RobotInfo enemy : visibleEnemies) {
        	if (enemy.getType() != RobotType.HEADQUARTERS) {
        		MapLocation enemyLocation = enemy.getLocation();
        		MapLocation robotLocation = rc.getLocation();
        		Direction moveDir = robotLocation.directionTo(enemyLocation);
        		if (rc.canMove(moveDir)) {
        			rc.move(moveDir);
        		}
        	}
        }
        
        // Also try to move randomly.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
