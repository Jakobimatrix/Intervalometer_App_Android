package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

public class MovableDot extends Movable{

    public MovableDot(Context context_, Pos3d position_, float radius) {
        super(new DrawableCircle(context_, position_, radius));

        inner_circle = new DrawableCircle(context_, position_, radius/Utility.GOLDEN_RATIO);
        parent.adChild(inner_circle);
        unlock();
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return parent.isWithin(p);
    }

    @Override
    public void endTouch() {

    }

    public QUADRANT getQuadrant(Pos3d p){
        Pos3d center = parent.getPosition();
        Pos3d dif = Pos3d.sub(p, center);
        if(Math.abs(dif.x) > Math.abs(dif.y)){
            if(dif.x > 0){
                return QUADRANT.RIGHT;
            }
            return  QUADRANT.LEFT;
        }
        if(dif.y > 0){
            return QUADRANT.TOP;
        }
        return QUADRANT.BOT;
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

    @Override
    public void setLocked(boolean l){
        if (l){
            lock();
        }else{
            unlock();
        }
    }

    DrawableCircle inner_circle;

    final static ColorRGBA LOCKED_OUTER_CIRCLE = new ColorRGBA(0.8,0.8,0.8,1.0);
    final static ColorRGBA LOCKED_INNER_CIRCLE = new ColorRGBA(0.5,0.5,0.0,1.0);
    final static ColorRGBA MOVABLE_OUTER_CIRCLE = new ColorRGBA(0.7,0.7,0.7,1.0);
    final static ColorRGBA MOVABLE_INNER_CIRCLE = new ColorRGBA(0,0.8,0,1.0);

    public enum QUADRANT{RIGHT,TOP,LEFT,BOT};

}
