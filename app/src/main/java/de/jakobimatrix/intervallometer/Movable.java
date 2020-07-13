package de.jakobimatrix.intervallometer;

import android.content.Context;

import pos3d.Pos3d;

// implements the basic features of a movable and drawable object.
public abstract class Movable extends Drawable {

    public Movable(Context context_, Pos3d position_) {
        super(context_, position_);
    }

    public void move(float dx, float dy, float dz){
        position.setX(position.getX() + dx);
        position.setY(position.getY() + dy);
        position.setZ(position.getZ() + dz);
    }

    public void move(Pos3d dp){
        position.add(dp);
    }


}
