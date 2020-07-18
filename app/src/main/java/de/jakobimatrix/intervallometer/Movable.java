package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.time.Instant;
import java.util.Calendar;

import javax.microedition.khronos.opengles.GL10;

// implements the basic features of a movable and drawable object.
public abstract class Movable {

    public Movable(Drawable parent_) {
        parent = parent_;
    }

    abstract public boolean isWithin(Pos3d position_);

    abstract public void draw(GL10 gl);

    public boolean isHold(Pos3d touch_pos){
        if(isWithin(touch_pos)){
            long now;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant current_instant = Instant.now();
                now = current_instant.toEpochMilli();
            }else{
                now = Calendar.getInstance().getTimeInMillis();
            }
            long time_hold = now - last_contact;
            last_contact = now;
            if(time_hold < HOLD_DURATION_MS){
                return true;
            }
        }
        return false;
    }

    public void setPosition(Pos3d p){
        parent.setPosition(p);
    }

    public Pos3d getPosition(){
        return parent.getPosition();
    }

    public void move(Pos3d p){
        parent.move(p);
    }

    Drawable parent;

    private long last_contact = 0;
    private static final long HOLD_DURATION_MS = 100;
}
