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

    static int screen_width = 0;
    static int screen_height = 0;
    // this should scale everything
    static float zoom = -4;

    final static public double GRID_Z_ELEVATION = 0.001;
    final static public double FUNCTION_Z_ELEVATION = 0.002;
    final static public double MANIPULATOR_Z_ELEVATION = 0.009;

    // layout ids
    final static int EDIT_TEMPLATE_LAYOUT = R.id.relEditTemplate;
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

    final static long HOLD_DELAY_MS = 33;
}
