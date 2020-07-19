package de.jakobimatrix.intervallometer;

import android.content.Context;

public class MovableDot extends Movable{

    public MovableDot(Context context_, Pos3d position_, float diameter_) {
        super(new DrawableCircle(context_, position_, diameter_));

        inner_circle = new DrawableCircle(context_, position_, diameter_/Utility.GOLDEN_RATIO);
        parent.adChild(inner_circle);
        unlock();
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return parent.isWithin(p);
    }

    public void lock(){
        is_locked = true;
        parent.setColor(LOCKED_OUTER_CIRCLE);
        inner_circle.setColor(LOCKED_INNER_CIRCLE);
    }

    public void unlock(){
        is_locked = false;
        parent.setColor(MOVABLE_OUTER_CIRCLE);
        inner_circle.setColor(MOVABLE_INNER_CIRCLE);
    }

    DrawableCircle inner_circle;

    final static ColorRGBA LOCKED_OUTER_CIRCLE = new ColorRGBA(0.8,0.8,0.8,1.0);
    final static ColorRGBA LOCKED_INNER_CIRCLE = new ColorRGBA(0.5,0.5,0.0,1.0);
    final static ColorRGBA MOVABLE_OUTER_CIRCLE = new ColorRGBA(0.7,0.7,0.7,1.0);
    final static ColorRGBA MOVABLE_INNER_CIRCLE = new ColorRGBA(0,0.8,0,1.0);
}
