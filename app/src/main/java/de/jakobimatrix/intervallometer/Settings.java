package de.jakobimatrix.intervallometer;

public class Settings {
    public Settings(){

    }
    public void setLastConnectedDeviceName(String name){
        // todo update DB
        last_connected_device = name;
    }

    public String getLastConnectedDeviceName(){
        return last_connected_device;
    }
    private String last_connected_device = "HC-05";

    public int getEditTemplateFpsId() {
        return edit_template_fps_id;
    }

    public void setEditTemplateFpsId(int id) {
        // todo update DB
        edit_template_fps_id = id;
    }

    public int edit_template_fps_id = -1;
}
