package playerDream;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static boolean leader = false;
    static int squad = -1; 
    static int leaderLast = -1;
    static RobotInfo squadLeader = null;
    static MapLocation squadLocation = null;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    static ArrayList<MapLocation> explore = new ArrayList<MapLocation>();
    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    public static boolean follower = false;
    public static RobotInfo following;
    public static MapLocation center;
    public static MapLocation myhq;
    static boolean toCenter = true;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        // System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");


        //explore.add(new MapLocation(rc.getMapWidth()/2,rc.getMapHeight()/2));
        center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        myhq = rc.getLocation();
        int x = myhq.x;
        int y = myhq.y;
        explore.add(new MapLocation(rc.getMapWidth()-x,rc.getMapHeight()));
        explore.add(new MapLocation(rc.getMapWidth()-x,rc.getMapHeight()-y));
        explore.add(new MapLocation(rc.getMapWidth(),rc.getMapHeight()-y));
        explore.add(new MapLocation(rc.getMapWidth()/2,rc.getMapHeight()/2));
        // for (RobotInfo robot: rc.senseNearbyRobots(2)){
        //     if(robot.getType() == RobotType.HEADQUARTERS){
        //         myhq = robot.getLocation();
        //         int x = myhq.x;
        //         int y = myhq.y;
        //         explore.add(new MapLocation(rc.getMapWidth()-x,rc.getMapHeight()));
        //         explore.add(new MapLocation(rc.getMapWidth()-x,rc.getMapHeight()-y));
        //         explore.add(new MapLocation(rc.getMapWidth(),rc.getMapHeight()-y));
        //         explore.add(new MapLocation(rc.getMapWidth()/2,rc.getMapHeight()/2));
        //         break;
        //     }
        // }
        

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            if(rc.getType() == RobotType.AMPLIFIER && !follower){
                for(RobotInfo robot: rc.senseNearbyRobots(-1,rc.getTeam())){
                    if(robot.getType() == RobotType.LAUNCHER){
                        follower = true;
                        following = robot;
                    }
                }

            }
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case HEADQUARTERS: HeadquartersStrategy.runHeadquarters(rc);  break;
                    case CARRIER: CarrierStrategy.runCarrier(rc);   break;
                    case LAUNCHER: LauncherStrategy.runLauncher(rc); break;
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER: AmplifierStrategy.runAmplifier(rc);      break;
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
   

    static void moveRandom(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if(rc.canMove(dir)) rc.move(dir);
    }

    static void moveTowards(RobotController rc, MapLocation loc) throws GameActionException{
        Direction dir = rc.getLocation().directionTo(loc);
        if(rc.canMove(dir)) rc.move(dir);
        else moveRandom(rc);
    }


    static void scan(RobotController rc) throws GameActionException { // scan should only be on recon bots ex: amp and launcher
        Communication.clearObsoleteEnemies(rc);
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getTeam() == rc.getTeam()){
                if(robot.getID()%10 == leaderLast && robot.getType() == RobotType.LAUNCHER)
                    squadLeader = robot;
                else{
                    squadLeader = null;
                }
            }
            if(robot.getTeam() == rc.getTeam().opponent())
                Communication.reportEnemy(rc, robot.getLocation());
            if(robot.getType() == RobotType.HEADQUARTERS) {
                if(robot.getTeam() != rc.getTeam()){
                    Communication.addEHq(robot, rc);
                }
            }
        }
    }

    static void squad(RobotController rc) throws GameActionException{
        if(leader){
            Communication.updateSquad(rc, squad);
            return;
        } else if(squad == -1 || !(Communication.squadExists(rc, squad))){
            Communication.joinSquad(rc);
            if(squad != -1){
                leaderLast = Communication.readSquadLeader(rc, squad);
            }
            
        }
        if (squadLeader != null && rc.canSenseRobot(squadLeader.getID())){
            squadLocation = squadLeader.getLocation();
        }else if (squad != -1){
            squadLocation = Communication.readSquadLocation(rc, squad);
        }

    }

    static void moveSquad(RobotController rc) throws GameActionException{
        if(leader){
            return;
        }else if(squadLocation != null){
            Pathing.moveTowards(rc, squadLocation);
        }
    }


}
