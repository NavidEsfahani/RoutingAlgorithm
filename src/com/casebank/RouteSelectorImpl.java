package com.casebank;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteSelectorImpl implements RouteSelector{
    private WorldGeometry worldGeometry;
    private Map<String,String> pathDirections=new HashMap<>(); //X:Y,0000
    private List<String> path=new ArrayList<>();
    private List<String> backSteps = new ArrayList<>();
    private String[] optimumPath;
    int lowestCost=0;
    int lowestSteps =0;

    private enum Directions{
        NORTH, EAST, SOUTH, WEST, NONE;
    }

    private enum DirectionsUpdateAction {
        LEAD_TO, AVOID_TO;
    }
    private enum SeedRoutType {
        X_THEN_Y, Y_THEN_X;
    }


    public RouteSelectorImpl(WorldGeometry worldGeometry) {
        this.worldGeometry = worldGeometry;
    }

    @Override
    public String[] getRoute(int startX, int startY, int endX, int endY) {
        path.clear();
        pathDirections.clear();backSteps.clear();

        Long startTime = System.nanoTime();
        getSeedCost(startX, startY, endX, endY, SeedRoutType.Y_THEN_X);
        getSeedCost(startX, startY, endX, endY, SeedRoutType.X_THEN_Y);

        findRoute(startX, startY, endX, endY);

        System.out.println("Cost is " + lowestCost);

        Long duration = (System.nanoTime()-startTime)/1000000;
        System.out.println("Total time in mSec is " + duration);

        return optimumPath;
    }

    private void findRoute(int startX, int startY, int endX, int endY) {

        if (!path.contains(startX + ":" + startY)) {
            path.add(startX + ":" + startY);
        }

        if (startX == endX && startY == endY) {
            backSteps.clear();
            int cost = getRouteCost(path);
            if (lowestCost == 0 || (cost < lowestCost && path.size() < lowestSteps)) {
                lowestCost = cost;
                lowestSteps = path.size();
                optimumPath = new String[path.size()];
                path.toArray(optimumPath);
            }

            path.remove(path.size() - 1);
            pathDirections.remove(path.get(path.size() - 1));

            String tempCoordination = path.get(path.size() - 1);
            updateTerrainDirection(tempCoordination, endX + ":" + endY, DirectionsUpdateAction.LEAD_TO);

            path.remove(path.size() - 1);
            updateTerrainDirection(path.get(path.size() - 1), tempCoordination, DirectionsUpdateAction.AVOID_TO);

            int x = Integer.valueOf(path.get(path.size() - 1).split(":")[0]);
            int y = Integer.valueOf(path.get(path.size() - 1).split(":")[1]);
            findRoute(x, y, endX, endY);

            return;
        }

        if (lowestCost > 0) {
            int currentCost = getRouteCost(path);
            if (currentCost >= lowestCost) {
                moveBack(startX, startY, endX, endY);
                return;
            }
        }
        String terrainDirections = pathDirections.get(startX + ":" + startY);
        if (terrainDirections == null) {
            terrainDirections = "0000";
        }

        //North
        if (getNextDirection(terrainDirections).equals(Directions.NORTH)) {
            terrainDirections = "1" + terrainDirections.substring(1, 4);
            pathDirections.put(startX + ":" + startY, terrainDirections);
            if (isValidTerrain(startX, startY - 1)) {
                findRoute(startX, startY - 1, endX, endY);
                return;
            }
        }

        //EAST
        if (getNextDirection(terrainDirections).equals(Directions.EAST)) {
            terrainDirections = terrainDirections.substring(0, 1) + "1" + terrainDirections.substring(2, 4);
            pathDirections.put(startX + ":" + startY, terrainDirections);
            if (isValidTerrain(startX + 1, startY)) {
                findRoute(startX + 1, startY, endX, endY);
                return;
            }
        }

        //SOUTH
        if (getNextDirection(terrainDirections).equals(Directions.SOUTH)) {
            terrainDirections = terrainDirections.substring(0, 2) + "1" + terrainDirections.substring(3, 4);
            pathDirections.put(startX + ":" + startY, terrainDirections);
            if (isValidTerrain(startX, startY + 1)) {
                findRoute(startX, startY + 1, endX, endY);
                return;
            }
        }

        //WEST
        if (getNextDirection(terrainDirections).equals(Directions.WEST)) {
            terrainDirections = terrainDirections.substring(0, 3) + "1";
            pathDirections.put(startX + ":" + startY, terrainDirections);
            if (isValidTerrain(startX - 1, startY)) {
                findRoute(startX - 1, startY, endX, endY);
                return;
            }
        }


        moveBack(startX, startY, endX, endY);
    }

    private void moveBack(int startX, int startY, int endX, int endY){
        backSteps.add(startX + ":" +startY);
        int index = path.indexOf(startX + ":" + startY);
        if(index==0){
            return;
        }
        String nextCoordination = path.get(index-1);
        pathDirections.remove(path.get(index));
        path.remove(index);
        int x = Integer.valueOf(nextCoordination.split(":")[0]);
        int y = Integer.valueOf(nextCoordination.split(":")[1]);
        findRoute(x, y,endX, endY);
    }

    private Directions getNextDirection(String directions){

        if(directions.substring(0,1).equals("0")) {
            return Directions.NORTH;
        } else if(directions.substring(1,2).equals("0")) {
            return Directions.EAST;
        } else if(directions.substring(2,3).equals("0")) {
            return Directions.SOUTH;
        } else if(directions.substring(3,4).equals("0")) {
            return Directions.WEST;
        } else {
            return Directions.NONE;
        }
    }

    private boolean isValidTerrain(int x, int y ){
        if(x==0 || y==0 || x>worldGeometry.getXMAX() || y>worldGeometry.getYMAX()){
            return false;
        }
        if(path.contains(x+":"+y)) {
            return false;
        }
        if(backSteps.contains(x+":"+y)){
            return false;
        }
        return true;
    }


    private void updateTerrainDirection(String coordination, String coordinationOfNext , DirectionsUpdateAction action){
        int x = Integer.valueOf(coordination.split(":")[0]);
        int y = Integer.valueOf(coordination.split(":")[1]);
        int xNext = Integer.valueOf(coordinationOfNext.split(":")[0]);
        int yNext = Integer.valueOf(coordinationOfNext.split(":")[1]);

        String directionStr="0000";
        if(x==xNext && y > yNext){
            if(action.equals(DirectionsUpdateAction.LEAD_TO)) {
                directionStr = "0111";
            } else if(action.equals(DirectionsUpdateAction.AVOID_TO)){
                directionStr = "1" + pathDirections.get(coordination).substring(1,4);
            }
        } else if(x==xNext && y < yNext){
            if(action.equals(DirectionsUpdateAction.LEAD_TO)) {
                directionStr = "1101";
            } else if(action.equals(DirectionsUpdateAction.AVOID_TO)){
                directionStr = pathDirections.get(coordination).substring(0,2) + "1" + pathDirections.get(coordination).substring(3,4);
            }
        }else if(x>xNext && y==yNext){
            if(action.equals(DirectionsUpdateAction.LEAD_TO)) {
                directionStr = "1110";
            } else if(action.equals(DirectionsUpdateAction.AVOID_TO)){
                directionStr = pathDirections.get(coordination).substring(0,3)+"1";
            }
        }else if(x<xNext && y==yNext){
            if(action.equals(DirectionsUpdateAction.LEAD_TO)) {
                directionStr = "1011";
            } else if(action.equals(DirectionsUpdateAction.AVOID_TO)){
                directionStr = pathDirections.get(coordination).substring(0,1) + "1" + pathDirections.get(coordination).substring(2,4);
            }
        }

        pathDirections.put(x + ":" + y, directionStr);
    }


    private int getRouteCost(List<String> path){

        int x,y, lastElevation=0, cost=0;
        for(int index=0; index<path.size();index++){
            String coordination = path.get(index);
            x = Integer.valueOf(coordination.split(":")[0]);
            y = Integer.valueOf(coordination.split(":")[1]);

            if(index==0){
                lastElevation =  worldGeometry.getElevation(x,y);
            } else {
                cost += calculateCost(lastElevation, worldGeometry.getElevation(x,y));
            }
        }
        return cost;
    }

    private int calculateCost(int elevationFrom, int elevationTo){

        int delta = elevationTo-elevationFrom;

        if(delta < -5){
            return -delta;
        } else if (delta <= 5 ){
            return 5+delta;
        } else {
            return 5+2*delta;
        }

    }

    private void getSeedCost(int startX, int startY , int endX, int endY, SeedRoutType defaultPaths){
        int xDiff = endX-startX;
        int yDiff = endY-startY;
        ArrayList<String> samplePath=new ArrayList<>();
        int xSign = (xDiff>0) ? +1 : -1;
        int ySign = (yDiff>0) ? +1 : -1;
        int newVale;
        samplePath.add(startX+":"+startY);

        int counter=0;
        while (counter<2){
            if(defaultPaths.equals(SeedRoutType.X_THEN_Y)) {
                newVale = startX;
                while (xDiff != 0) {
                    newVale += xSign;
                    samplePath.add((newVale) + ":" + startY);
                    xDiff = xDiff - xSign;
                }
                defaultPaths = SeedRoutType.Y_THEN_X;
                startX=endX;
            } else if(defaultPaths.equals(SeedRoutType.Y_THEN_X)) {
                newVale = startY;
                while (yDiff != 0) {
                    newVale += ySign;
                    samplePath.add(startX + ":" + (newVale));
                    yDiff = yDiff - ySign;
                }
                defaultPaths = SeedRoutType.X_THEN_Y;
                startY = endY;
            }
            counter++;
        }

        int cost = getRouteCost(samplePath);
        if(lowestCost==0 || cost<lowestCost){
            lowestCost = cost;
            optimumPath = new String[samplePath.size()];
            samplePath.toArray(optimumPath);
        }
    }
}
