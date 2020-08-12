package de.jakobimatrix.intervallometer;

// evil globals disguised as an instance class.
public class Globals {
    private static Globals mInstance= null;

    protected Globals(){}

    public static synchronized Globals getInstance() {
        if(null == mInstance){
            mInstance = new Globals();
        }
        return mInstance;
    }

    final static public double GRID_Z_ELEVATION = 0.001;
    final static public double FUNCTION_Z_ELEVATION = 0.002;
    final static public double MANIPULATOR_Z_ELEVATION = 0.009;

    // protocol for hardware program
    final static public byte SYMBOL_STOP = 0x00;
    final static public byte SYMBOL_STOP_SHUTDOWN = 0x0F;
    final static public byte SYMBOL_F_CONST = 0x01;
    final static public byte SYMBOL_F_LIN = 0x02;
    final static public byte SYMBOL_F_QUAD = 0x03;
    final static public byte NUM_BYTES_VALUE = 4;
    final static public byte NUM_VALUES_F_CONST = 2;
    final static public byte NUM_VALUES_F_LIN = 3;
    final static public byte NUM_VALUES_F_QUAD = 4;

    final static long HOLD_DELAY_MS = 33;
}
