package GlimmeringDream;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pathingold {
    // Basic bug nav - Bug 0
    static MapLocation lastPos = null;
    static Direction currentDirection = null;
    static boolean followingObstacle = false;
    static MapLocation start = null;
    static MapLocation end = null;
    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        rc.setIndicatorString(target.toString());
        if (rc.getLocation() == target) {
            return;
        }
        if (!rc.isMovementReady()) {
            return;
        }
        Direction d = rc.getLocation().directionTo(target);
        MapInfo movetile;
        MapLocation sensing;
        if (rc.canMove(d)) {
            movetile = rc.senseMapInfo(rc.getLocation().add(d));
            while(rc.canMove(d) && (!(movetile.getCurrentDirection() == d.opposite())|| movetile.getCurrentDirection() == Direction.CENTER)){
                rc.move(d);
            }
            currentDirection = null; // there is no obstacle we're going around
        } else {
            // Going around some obstacle: can't move towards d because there's an obstacle there
            // Idea: keep the obstacle on our right hand

            if (currentDirection == null) {
                currentDirection = d;
            }
            // Try to move in a way that keeps the obstacle on our right
            for (int i = 0; i < 8; i++) {
                sensing = rc.getLocation().add(currentDirection);
                if(!rc.canSenseLocation(sensing)) continue;
                movetile = rc.senseMapInfo(sensing);
                rc.setIndicatorString(sensing.toString());
                if (rc.canSenseLocation(sensing) && rc.canMove(currentDirection) && !rc.canSenseRobotAtLocation(sensing) && (!(movetile.getCurrentDirection() == d.opposite()) || movetile.getCurrentDirection() == Direction.CENTER)) {
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else {
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
    }
    static void bugZero(RobotController rc, MapLocation target) throws GameActionException {
       if (rc.getLocation().distanceSquaredTo(target) < 2) {
            return;
        }
        if (!rc.isMovementReady()) {
            return;
        }
        Direction d = rc.getLocation().directionTo(target);
        if (rc.canMove(d) && rc.getLocation().add(d) != lastPos) {
            lastPos = rc.getLocation();
            rc.move(d);
            currentDirection = null; // there is no obstacle we're going around
        } else {
            // Going around some obstacle: can't move towards d because there's an obstacle there
            // Idea: keep the obstacle on our right hand

            if (currentDirection == null) {
                currentDirection = d;
            }
            // Try to move in a way that keeps the obstacle on our right
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection) && rc.getLocation().add(currentDirection) != lastPos && rc.sensePassability(rc.getLocation().add(currentDirection))) {
                    //lastPos = rc.getLocation();
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else {
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
    }
    static void bugDream(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(target) < 2) {
             return;
        }
        if (!rc.isMovementReady()) {
             return;
        }
        if(!target.equals(end)){
            start = rc.getLocation();
            end = target;
            
        }
        currentDirection = start.directionTo(end);
        if (rc.canMove(currentDirection) && !rc.getLocation().add(currentDirection).equals(lastPos)) {
            lastPos = rc.getLocation();
            rc.move(currentDirection);


            //currentDirection = null; // there is no obstacle we're going around
        } else {

            // Going around some obstacle: can't move towards d because there's an obstacle there
            // Idea: keep the obstacle on our right hand

            // if (currentDirection == null) {
            //     currentDirection = d;
            // }
            // currentDirection = d.rotateRight();
            // Try to move in a way that keeps the obstacle on our right
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection) && !rc.getLocation().add(currentDirection).equals(lastPos)) {
                    lastPos = rc.getLocation();
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else {
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
    }
    
    static void actualFollowObstacle(RobotController rc) throws GameActionException {
        if (rc.canMove(currentDirection)) {
            rc.move(currentDirection);
        } else {
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection) ) {
                    //lastPos = rc.getLocation();
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else {
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
    }
    
    static void followObstacle(RobotController rc, MapLocation target) throws GameActionException {
        if (currentDirection == null) {
            Direction bestDir = RobotPlayer.directions[0];
            int minDist = Integer.MAX_VALUE;
            MapLocation current = rc.getLocation();
            for (Direction dir: RobotPlayer.directions) {
                MapLocation possibleMove = current.add(dir);
                if (possibleMove.distanceSquaredTo(target) < minDist && rc.canSenseLocation(possibleMove) && rc.sensePassability(possibleMove)) {
                    bestDir = dir;
                    minDist = possibleMove.distanceSquaredTo(target);
                }
            }
            if (bestDir != null && rc.canMove(bestDir)) {
                currentDirection = bestDir;
                rc.move(bestDir);
            } else {
                RobotPlayer.moveRandom(rc);
            }
        } else {
            actualFollowObstacle(rc);
    }
    }

    static void bugTwo(RobotController rc, MapLocation start, MapLocation target, int threshold) throws GameActionException{
        if (rc.getLocation() == target) {
            return;
        }
        if (!rc.isMovementReady()) {
            return;
        }
        MapLocation current = rc.getLocation();
        // Move straight towards path
        Direction dir = current.directionTo(target);
        rc.setIndicatorString(dir.toString());
        if (rc.canMove(dir)) {
            rc.move(dir);
            currentDirection = null;
            return;
        } else { // Obstacle Handling 
            Vector vector1 = new Vector(current.x - start.x, current.y - start.y);
            Vector vector2 = new Vector(target.x - start.x, target.y - start.y);
            double angle = vector1.getAngle(vector2);
            double error = vector1.magnitude() * Math.sin(angle);
            if (error < threshold) currentDirection = null;
            followObstacle(rc, target);
        }
        



    }

    static void moveTowards(RobotController rc, MapLocation target, int i) throws GameActionException{
        if (start == null) start = rc.getLocation();
        if (end == null || target != end) end = target;
        bugTwo(rc, start, end, 2);
    }


}


