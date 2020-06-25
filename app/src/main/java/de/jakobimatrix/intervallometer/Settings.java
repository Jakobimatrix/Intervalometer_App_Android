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
}
