package de.jakobimatrix.intervalometer;

import android.util.Log;

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

    /*!
     * \brief isRepresentable Validates if the given floating point value can be represented
     * on the microcontroller.
     * \return True if the number can be represented.
     */
    public static boolean isRepresentableIn4ByteInt(double d){
        return !(d > MAX_FLOAT_VALUE) && !(d < MIN_FLOAT_VALUE) && !(Math.abs(d) < MIN_NONZERO_FLOAT_VAlUE);
    }

    /*!
     * \brief Convert a floating point number to an integer saving FLOAT_PRECISION decimals for
     * arithmetic operations on microcontroller.
     * \return int representation of float.
     */
    public static int float2Int4byte(double d){
        if(d > MAX_FLOAT_VALUE){
            Log.d("Warning:Globals", "float2Int4byte: " + d + " is bigger as the biggest possible representable float (" + MAX_FLOAT_VALUE + ")");
            return 0x0FFFFFFF;
        }else if( d < MIN_FLOAT_VALUE){
            Log.d("Warning:Globals", "float2Int4byte: " + d + " is lower as the lowest possible representable float (" + MIN_FLOAT_VALUE + ")");
            return 0xF0000000;
        }else if( Math.abs(d) < MIN_NONZERO_FLOAT_VAlUE){
            Log.d("Warning:Globals", "float2Int4byte: " + d + " is smaller than the smallest possible representable float (" + MIN_NONZERO_FLOAT_VAlUE + ")");
            return 0;
        }
        return (int) Math.round(d*FLOAT_PRECISION);
    }

    static int screen_width = 0;
    static int screen_height = 0;
    // this should scale everything
    static float zoom = -4;

    final static public double GRID_Z_ELEVATION = 0.001;
    final static public double FUNCTION_Z_ELEVATION = 0.002;
    final static public double MANIPULATOR_Z_ELEVATION = 0.009;

    // layout ids
    final static int EDIT_TEMPLATE_LAYOUT = R.id.button_add_function_layout;
    final static int EDIT_FUNCTION_LAYOUT = R.id.editFunctionLayout;
    final static int EDIT_FUNCTION_APPLY_BTN = R.id.applyFunctionEditBtn;
    final static int EDIT_FUNCTION_CANCEL_BTN = R.id.cancelFunctionEditBtn;
    final static int EDIT_FUNCTION_CHOOSER = R.id.spinnerChooseFunction;
    final static int EDIT_FUNCTION_NUM_PICS_INPUT = R.id.chooseNumPictures;

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
    final static public double FLOAT_PRECISION = 1000;
    final static public double MAX_FLOAT_VALUE = 0x0FFFFFFF / FLOAT_PRECISION;
    final static public double MIN_FLOAT_VALUE = 0xF0000000 / FLOAT_PRECISION;
    final static public double MIN_NONZERO_FLOAT_VAlUE = 1. / FLOAT_PRECISION;

    // Haptic
    final static long HOLD_DELAY_MS = 33;

    final static String ANDROID_CODE_URL = "https://github.com/Jakobimatrix/Intervalometer_App_Android";
    final static String HARDWARE_CODE_URL = "https://github.com/Jakobimatrix/Intervalometer_Hardware";
    final static String AUTHOR = "Jakob Wandel";
    final static int NUM_SUPPORTED_FUNCTION_BYTES = 120; // as of v1.1
}
