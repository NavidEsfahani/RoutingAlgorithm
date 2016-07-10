package com.casebank;


public class Main {
    public static void main (String[] args){
        WorldGeometry worldGeometry = new WorldGeometryImpl(WorldGeometryImpl.ElevationGenerator.RANDOM);

        RouteSelector routeSelector=new RouteSelectorImpl(worldGeometry);
        String[] path = routeSelector.getRoute(10,1,80,20);

        String result="";
        for (String str : path){
            result += ">" + str;
        }

        System.out.println(result.substring(1));

    }

}
