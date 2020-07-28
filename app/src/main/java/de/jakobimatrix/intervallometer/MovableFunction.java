package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

public class MovableFunction extends Movable {
    public MovableFunction(Context context, Pos3d position,Function f,double min, double max, Homography2d system_2_open_gl) {
        super(new DrawableFunction(context, position, f, min, max, system_2_open_gl));
        for (int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i] = new MovableDot(context,position, manipulator_radius);
        }
        setManipulatorsBasedOnFunction();
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        boolean is_within = checkLastPosHolds(position_);
        if(!is_within) {
            active_manipulator = INVALID_MANIPULATOR_ID;
            for (int i = 0; i < NUM_MANIPULATORS; i++) {
                if (manipulator[i].isHold(position_)) {
                    active_manipulator = i;
                    active_quadrant = manipulator[i].getQuadrant(position_);
                    is_within = true;
                    break;
                }
            }
        }
        setAndHoldInputCommand(position_);
        return is_within;
    }

    private boolean checkLastPosHolds(final Pos3d p) {
        p.z = 0;
        // Basically if the user did not move his finger from the old position
        // than we use the first input he gave as long as he touches that position.

        if(isValidManipulatorId(active_manipulator)){
            double distance = p.distance(pos_command_system);
            if(distance < manipulator_radius/4.){
                return true;
            }
        }
        return false;
    }

    private void setAndHoldInputCommand(Pos3d input_open_gl){
        // basically calculate the command from the first touch and hold that command until touch is over
        if(last_active_manipulator == active_manipulator){
            return;
        }
        last_active_manipulator = active_manipulator;

        if(active_manipulator == INVALID_MANIPULATOR_ID){
            dpos_command_system = Pos3d.Zero();
            return;
        }

        DrawableFunction df = getDrawableFunction();
        Pos3d p_system_new = df.f2openGL.invTransform(input_open_gl);
        Pos3d p_system_old = df.f2openGL.invTransform(manipulator[active_manipulator].getPosition());
        dpos_command_system = Pos3d.sub(p_system_new, p_system_old);
        pos_command_system = input_open_gl;
        pos_command_system.z = 0;
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
        if(active_manipulator > -1){
            Pos3d p = new Pos3d(dp);
            p.add(manipulator[active_manipulator].getPosition());
            this.setPosition(dp);
        }
    }

    @Override
    public void endTouch() {
        last_active_manipulator = INVALID_MANIPULATOR_ID;
        active_manipulator = INVALID_MANIPULATOR_ID;
        dpos_command_system = Pos3d.Zero();
        pos_command_system = Pos3d.Zero();
        active_quadrant = null;
        for(int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].endTouch();
        }
    }

    @Override
    public void setPosition(Pos3d p){
        if(!isValidManipulatorId(active_manipulator)){
            return;
        }

        boolean top_dow_direction = active_quadrant == MovableDot.QUADRANT.BOT || active_quadrant == MovableDot.QUADRANT.TOP;
        switch (active_manipulator){
            case 0:
                if(top_dow_direction){
                    // set function given 3 points
                }else {
                    scaleFunctionMinX(dpos_command_system.x);
                }
                break;
            case 1:
                if(top_dow_direction){
                    // move offset
                }else {
                    // change steepness
                }
                break;
            case 2:
                if(top_dow_direction){
                    // set function given 3 points
                }else {
                    scaleFunctionMaxX(dpos_command_system.x);
                }
                break;
        }
    }

    private boolean isValidManipulatorId(int id){
        if(id != INVALID_MANIPULATOR_ID && manipulator.length > id){
            return true;
        }
        return false;
    }

    private void scaleFunctionMinX(double dx){
        DrawableFunction df = getDrawableFunction();
        df.setMin(df.min_x + dx);
    }

    private void scaleFunctionMaxX(double dx){
        DrawableFunction df = getDrawableFunction();
        df.setMax(df.max_x + dx);
    }

    private void scaleFunctionLeft(double dy){
        //TODO
    }

    private void scaleFunctionRight(double dy){
        //TODO
    }

    private void scaleFunctionGradient(double dy){
        //TODO
    }

    private void scaleFunctionOffset(double dy){
        //TODO
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

    DrawableFunction getDrawableFunction(){
        return (DrawableFunction) parent;
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

    final static int INVALID_MANIPULATOR_ID = -1;
    int active_manipulator = INVALID_MANIPULATOR_ID;
    int last_active_manipulator = INVALID_MANIPULATOR_ID;

    Pos3d dpos_command_system = Pos3d.Zero();
    Pos3d pos_command_system = Pos3d.Zero();
    MovableDot.QUADRANT active_quadrant;

    float manipulator_radius = DEFAULT_MANIPULATOR_RADIUS;
    final static float DEFAULT_MANIPULATOR_RADIUS = 0.25f;

    public boolean lock_manipulation;
}
