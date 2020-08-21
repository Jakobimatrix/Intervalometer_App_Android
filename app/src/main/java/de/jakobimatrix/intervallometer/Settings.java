package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static Settings mInstance= null;

    private Settings(){}

    public static synchronized Settings getInstance(Context c) {
        if(null == mInstance){
            mInstance = new Settings();
            mInstance.sharedpreferences = c.getSharedPreferences(SETTINGS_NAME,
                    c.MODE_PRIVATE);
            mInstance.editor = mInstance.sharedpreferences.edit();
            mInstance.setup();
        }
        return mInstance;
    }

    private void setup(){
        if (!sharedpreferences.contains(EDIT_TEMPLATE_FPS_ID_KEY)) {
            editor.putInt(EDIT_TEMPLATE_FPS_ID_KEY, EDIT_TEMPLATE_FPS_ID);
        }
        if (!sharedpreferences.contains(LAST_CONNECTED_DEVICE_KEY)) {
            editor.putString(LAST_CONNECTED_DEVICE_KEY, LAST_CONNECTED_DEVICE);
        }
        if (!sharedpreferences.contains(MIN_PERIOD_MS_KEY)) {
            editor.putFloat(MIN_PERIOD_MS_KEY, MIN_PERIOD_MS);
        }
    }

    public void setLastConnectedDeviceName(String name){
        editor.putString(LAST_CONNECTED_DEVICE_KEY, name);
    }

    public String getLastConnectedDeviceName(){
        return sharedpreferences.getString(LAST_CONNECTED_DEVICE_KEY, LAST_CONNECTED_DEVICE);
    }

    public int getEditTemplateFpsId() {
        return sharedpreferences.getInt(EDIT_TEMPLATE_FPS_ID_KEY, EDIT_TEMPLATE_FPS_ID);
    }

    public void setEditTemplateFpsId(int id) {
        editor.putInt(EDIT_TEMPLATE_FPS_ID_KEY, id);
    }

    public float getMinPeriodMs(){
        return sharedpreferences.getFloat(MIN_PERIOD_MS_KEY, MIN_PERIOD_MS);
    }

    public void setMinPeriodMs(float min_ms){
        editor.putFloat(MIN_PERIOD_MS_KEY, min_ms);
    }

    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    private static String SETTINGS_NAME = "intervalometer_settings";
    // no need to give rel names, this does speed up string comparison
    private static String EDIT_TEMPLATE_FPS_ID_KEY = "a";
    private static String LAST_CONNECTED_DEVICE_KEY = "b";
    private static String MIN_PERIOD_MS_KEY = "c";

    private static int EDIT_TEMPLATE_FPS_ID = -1;
    private static String LAST_CONNECTED_DEVICE = "HC-05";
    private static float MIN_PERIOD_MS = 300f;
}
