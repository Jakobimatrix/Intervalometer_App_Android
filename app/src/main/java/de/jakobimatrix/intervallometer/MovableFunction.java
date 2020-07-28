package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class MovableFunction extends Movable {
    public MovableFunction(Context context, Pos3d position,Function f,double min, double max, Homography2d system_2_open_gl) {
        super(new DrawableFunction(context, position, f, min, max, system_2_open_gl));
        for (int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i] = new MovableDot(context,position,manipulator_diameter);
        }
        setManipulatorsBasedOnFunction();
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        for (int i = 0; i < NUM_MANIPULATORS; i++){
            Log.d("Manipulator within? ", i + " touch: "+ position_.toString() + " center: " + manipulator[i].getPosition().toString());
            if(manipulator[i].isHold(position_)){
                active_manipulator = i;
                Log.d("FC isWithin", "active_manipulator " + active_manipulator);
                return true;
            }
        }
        active_manipulator = -1;
        return false;
    }

    @Override
    public void draw(GL10 gl){
        parent.draw(gl);
        for (int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].draw(gl);
        }
    }

    @Override
    public void move(Pos3d dp){
        Log.d("FC move", "active_manipulator " + active_manipulator);
        if(active_manipulator > -1){
            manipulator[active_manipulator].move(dp);
        }
    }

    @Override
    public void setPosition(Pos3d p){
        Log.d("FC setPosition", "active_manipulator " + active_manipulator);
        if(active_manipulator > -1){
            p.z = Globals.MANIPULATOR_Z_ELEVATION;
            manipulator[active_manipulator].setPosition(p);
        }
    }

    Pos3d getLeftManipulatorGLpos(){
        return manipulator[0].getPosition();
    }

    Pos3d getMidManipulatorGLpos(){
        return manipulator[1].getPosition();
    }

    Pos3d getRightManipulatorGLpos(){
        return manipulator[2].getPosition();
    }

    public double getFunctionMaxX(){
        DrawableFunction df = (DrawableFunction) parent;
        return df.max_x;
    }

    public double getFunctionMinX(){
        DrawableFunction df = (DrawableFunction) parent;
        return df.min_x;
    }

    public double getFunctionMaxY(){
        DrawableFunction df = (DrawableFunction) parent;
        return df.f.getMax(df.min_x, df.max_x);
    }

    public double getFunctionMinY(){
        DrawableFunction df = (DrawableFunction) parent;
        return df.f.getMin(df.min_x, df.max_x);
    }

    public ViewPort getFunctionViewport(){
        return new ViewPort(
                new Pos3d(getFunctionMinX(), getFunctionMinY(), 0),
                new Pos3d(getFunctionMaxX(), getFunctionMaxY(), 0));
    }

    void setManipulatorsBasedOnFunction(){
        DrawableFunction df = (DrawableFunction) parent;
        Function f = getFunction();
        double mid = (df.max_x - df.min_x)/2.0 + df.min_x;
        Pos3d [] ff = new Pos3d[3];
        ff[0] = new Pos3d(df.min_x, f.f(df.min_x), Globals.MANIPULATOR_Z_ELEVATION);
        ff[1] = new Pos3d(mid, f.f(mid), Globals.MANIPULATOR_Z_ELEVATION);
        ff[2] = new Pos3d(df.max_x, f.f(df.max_x), Globals.MANIPULATOR_Z_ELEVATION);

        for(int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].setPosition(df.f2openGL.transform(ff[i]));
            // TODO?? make relative for continuity?
            //manipulator[i].parent.setRelativePositionToParent(absolutePos2relativePos(df.f2openGL.transform(ff[i])));
        }
    }

    Function getFunction(){
        DrawableFunction df = (DrawableFunction) parent;
        return df.getFunction();
    }

    public void setHomography(Homography2d system_2_open_gl){
        DrawableFunction df = (DrawableFunction) parent;
        df.setHomography(system_2_open_gl);
        setManipulatorsBasedOnFunction();
    }

    @Override
    public void setLocked(boolean l){
        for(int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].setLocked(l);
        }
        super.setLocked(l);
    }

    final static int NUM_MANIPULATORS = 3;
    MovableDot [] manipulator = new MovableDot[NUM_MANIPULATORS];

    int active_manipulator = -1;

    float manipulator_diameter = DEFAULT_MANIPULATOR_DIAMETER;
    final static float DEFAULT_MANIPULATOR_DIAMETER = 0.15f;

    public boolean lock_manipulation;
}
