package de.jakobimatrix.intervallometer;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;

public class MovableDot extends Movable{

    public MovableDot(Context context_, Pos3d position_, float diameter_) {
        super(new DrawableCircle(context_, position_, diameter_));
        final float golden_ratio = 1.6180339887f;
        inner_circle = new DrawableCircle(context_, position_, diameter_/golden_ratio);
        unlock();
        parent.adChild(inner_circle);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return parent.isWithin(p);
    }

    @Override
    public void draw(GL10 gl) {
        parent.draw(gl);
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
