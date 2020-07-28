package de.jakobimatrix.intervallometer;

public class Globals {
    private static Globals mInstance= null;

    public int someValueIWantToKeep;

    protected Globals(){}

    public static synchronized Globals getInstance() {
        if(null == mInstance){
            mInstance = new Globals();
        }
        return mInstance;
    }

    final static public double GRID_Z_ELEVATION = 0.001;
    final static public double FUNCTION_Z_ELEVATION = 0.003;
    final static public double MANIPULATOR_Z_ELEVATION = 0.005;
}
