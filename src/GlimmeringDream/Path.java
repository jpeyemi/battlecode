package GlimmeringDream;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;


public class Path {
    
    private ArrayList<MapLocation> path;
    private MapLocation start;
    private MapLocation end;
    private int index;

    public Path(MapLocation start, MapLocation end, RobotController rc) {
        this.start = new MapLocation(start.x,start.y);
        this.end =  new MapLocation(end.x,end.y);
        this.path = new ArrayList<MapLocation>();
        this.path.add(start);
        this.path.add(end);
        this.setIndex(0);


    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    public Path(MapLocation start, MapLocation end, RobotController rc, ArrayList<MapLocation> path) {
        this.start = new MapLocation(start.x,start.y);
        this.end =  new MapLocation(end.x,end.y);
        this.path = new ArrayList<MapLocation>();
        this.path.add(start);
        this.path.add(end);
        this.setIndex(0);

    }

    public void amend(RobotController rc){
        if(this.path.get(this.path.size() - 2).distanceSquaredTo(rc.getLocation()) <= 2){
            this.path.set(this.path.size() - 2, rc.getLocation());
        }else{
            this.path.add(this.path.size() - 1, rc.getLocation());
            this.setIndex(this.getIndex() + 1);
        }
    }

    public Path copy(RobotController rc){
        ArrayList<MapLocation> rpath = new ArrayList<MapLocation>();
        for(int i = 0; i < this.path.size(); i ++){
            rpath.add(this.path.get(i));
        }
        //Collections.reverse(rpath);
        return new Path(end,start,rc,rpath);
    }

    public Path reverse(RobotController rc){
        ArrayList<MapLocation> rpath = new ArrayList<MapLocation>();
        for(int i = this.path.size() -1; i >=0; i --){
            rpath.add(this.path.get(i));
        }
        //Collections.reverse(rpath);
        return new Path(end,start,rc,rpath);
    }

    public MapLocation getStart(){
        return this.start;
    }

    public MapLocation getEnd(){
        return this.end;
    }

    public MapLocation getPath(){
        return this.path.get(this.index);
    }

    public ArrayList<MapLocation> getFullPath(){
        return this.path;
    }

    public int getIndex(){
        return this.index;
    }

    public void reset(){
        this.setIndex(0);
    }

    public void inc(){
        this.setIndex(this.getIndex() + 1);
    }


}