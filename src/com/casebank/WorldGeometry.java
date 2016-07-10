package com.casebank;

public interface WorldGeometry {

    /** Width of the game map */

    public int getXMAX();

    /** Height of the game map */

    public int getYMAX();

    /** Terrain elevation at a point */

    public int getElevation(int x, int y);

    public String getGeometry();

}
