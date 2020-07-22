package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class MovableFunction extends Movable {
    public MovableFunction(Context context, Pos3d position,Function f,double min, double max, Homography2d system_2_open_gl) {
        super(new DrawableFunction(context, position, f, min, max, system_2_open_gl));
        manipulator_left = new MovableDot(context,position,manipulator_diameter);
        manipulator_mid = new MovableDot(context,position,manipulator_diameter);
        manipulator_right = new MovableDot(context,position,manipulator_diameter);
        setManipulatorsBasedOnFunction();
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        if(!lock_manipulation){
            // for each movable Circle
        }
        return false;
    }

    @Override
    public void draw(GL10 gl){
        parent.draw(gl);
        manipulator_left.draw(gl);
        manipulator_mid.draw(gl);
        manipulator_right.draw(gl);
    }

    Pos3d getLeftManipulatorGLpos(){
        return new Pos3d(manipulator_left.getPosition());
    }
    Pos3d getMidManipulatorGLpos(){
        return new Pos3d(manipulator_mid.getPosition());
    }
    Pos3d getRightManipulatorGLpos(){
        return new Pos3d(manipulator_right.getPosition());
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
        double mid = (df.max_x -df.min_x)/2.0;
        Pos3d left_f = new Pos3d(df.min_x, f.f(df.min_x), 0.f);
        Pos3d mid_f = new Pos3d(mid, f.f(mid), 0.f);
        Pos3d right_f = new Pos3d(df.max_x, f.f(df.max_x), 0.f);

        manipulator_left.setPosition(df.f2openGL.transform(left_f));
        manipulator_mid.setPosition(df.f2openGL.transform(mid_f));
        manipulator_right.setPosition(df.f2openGL.transform(right_f));
    }

    Function getFunction(){
        DrawableFunction df = (DrawableFunction) parent;
        return df.getFunction();
    }

    public void setHomography(Homography2d system_2_open_gl){
        DrawableFunction df = (DrawableFunction) parent;
        df.setHomography(system_2_open_gl);
    }

    MovableDot manipulator_left;
    MovableDot manipulator_mid;
    MovableDot manipulator_right;

    float manipulator_diameter = DEFAULT_MANIPULATOR_DIAMETER;
    final static float DEFAULT_MANIPULATOR_DIAMETER = 0.2f;

    public boolean lock_manipulation;
}
