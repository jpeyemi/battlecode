package player2;

import java.util.List;

import java.util.ArrayList;

import battlecode.common.*;

class Squad{

    //public ArrayList <RobotController> 
    public Squad(RobotController rc){

    }

    public Boolean inSquad(RobotController rc){
        if (RobotPlayer.getSquad(rc) != null)
            return true;
        else return false;
    }

    public void joinSquad(RobotController rc){
        
    }

}