package de.jakobimatrix.intervallometer;

import android.util.Log;

public class Lock {

    public Lock(String s){
        name = s;
    }

    public void LOCK(int id){
        long start = System.currentTimeMillis();
        while(locked){
            // DEBUG msg if locked for longer than 1 sek
            if(System.currentTimeMillis() - start > 1000){
                Log.d("Locked", name + " and held hostage by " + last_lock);
            }
        }
        last_lock = id;
        locked = true;
    }

    public void UNLOCK(){
        last_lock = -1;
        locked = false;
    }

    private boolean locked = false;
    int last_lock = -1;

    // TODO debug
    private String name;
}
