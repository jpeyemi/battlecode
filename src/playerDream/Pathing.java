package playerDream;

import battlecode.common.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pathing {
    // Basic bug nav - Bug 0
    static MapLocation lastPos = null;
    static Direction currentDirection = null;
    static MapLocation start = null;
    static MapLocation end = null;
    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        rc.setIndicatorString(target.toString());
        if (rc.getLocation().distanceSquaredTo(target) < 2) {
            return;
        }
        if (!rc.isMovementReady()) {
            return;
        }
        Direction d = rc.getLocation().directionTo(target);
        if (rc.canMove(d)) {
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
                if (rc.canMove(currentDirection) && !rc.canSenseRobotAtLocation(rc.getLocation().add(currentDirection))) {
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
    static void moveTowards(RobotController rc, MapLocation target, int i) throws GameActionException{
        bugZero(rc, target);
    }


}


