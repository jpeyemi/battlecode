package playerfuncs1;

import battlecode.common.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LauncherStrategy {
    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            //MapLocation toAttack = rc.getLocation().add(Direction.EAST);

            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");
                rc.attack(toAttack);
            }
        }

        RobotInfo[] visableEnemies = rc.senseNearbyRobots(-1, opponent); //senses at max visave range
        for (RobotInfo enemy: visableEnemies){
            if (enemy.getType() != RobotType.HEADQUARTERS){
                MapLocation enemyLocation = enemy.getLocation();
                MapLocation robotLocation = rc.getLocation();
                Direction moveDir = robotLocation.directionTo(enemyLocation);
                if (rc.canMove(moveDir)){
                    rc.move(moveDir);
                }
            }
        }

        RobotInfo[] team = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo teammate: team){
            if (teammate.getType() != RobotType.HEADQUARTERS && teammate.getType() != RobotType.LAUNCHER) {
                MapLocation teammateLocation = teammate.getLocation();
                MapLocation robotLocation = rc.getLocation();
                Direction moveDir = robotLocation.directionTo(teammateLocation);
                if (rc.canMove(moveDir)){
                    rc.move(moveDir);
                }
            }
        }
        /*// Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }*/
    }
}
