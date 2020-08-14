package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.drm.DrmStore;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

enum CMD{UP,DOWN,LEFT, RIGHT, NULL};

public class MovableFunction extends Movable {
    public MovableFunction(Context context, Pos3d position,Function f,double min, double max, Homography2d system_2_open_gl) {
        super(new DrawableFunction(context, position, f, min, max, system_2_open_gl));
        for (int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i] = new Manipulator(context,position, manipulator_radius);
            active_manipulator[i] = false;
        }
        setManipulatorsBasedOnFunction();
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        for (int i = 0; i < NUM_MANIPULATORS; i++) {
            if (manipulator[i].isWithin(position_)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWithinToggle(Pos3d position_) {
        for (int i = 0; i < NUM_MANIPULATORS; i++) {
            if (manipulator[i].isWithin(position_)) {
                active_manipulator[i] = !active_manipulator[i];
                manipulator[i].setLocked(!active_manipulator[i]);
                return true;
            }
        }
        return false;
    }

    public void setCommand(CMD cmd, Pos3d step_width){
        double dx = 0;
        double dy = 0;
        switch (cmd){
            case RIGHT:
                dx = step_width.x;
                break;
            case LEFT:
                dx = -step_width.x;
                break;
            case UP:
                dy = step_width.y;
                break;
            case DOWN:
                dy = -step_width.y;
                break;
            case NULL:
                break;
        }
        dpos_command_system = new Pos3d(dx, dy, 0);

        Pos3d p1_sys = Pos3d.Zero();
        Pos3d p2_sys = new Pos3d(dpos_command_system);

        DrawableFunction df = (DrawableFunction) parent;
        Pos3d p1_gl = df.f2openGL.transform(p1_sys);
        Pos3d p2_gl = df.f2openGL.transform(p2_sys);
        dpos_command_openGL = Pos3d.sub(p2_gl,p1_gl);
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
        //TODO manual moving the manipulator
    }

    @Override
    public void executeCommand(CMD cmd) {
        throw new IllegalArgumentException( "MovableFunction::executeCommand: not implemented" );
    }

    @Override
    public void endTouch() {
        // snap the function to the grid
        setFunctionGivenManipulators();
    }

    @Override
    public void setPosition(Pos3d p){
        for (int i = 0; i < NUM_MANIPULATORS; i++) {
            if (manipulator[i].isWithin(p)) {
                p.z = Globals.MANIPULATOR_Z_ELEVATION;
                moveManipulatorManuallyAndSetFunction(i, p);
                return;
            }
        }
    }

    public void executeCommand(CMD cmd, Pos3d step_width){
        setCommand(cmd, step_width);
        executeCommand();
    }

    public void executeCommand(){
        for(int i = 0; i < NUM_MANIPULATORS; i++) {
            if(active_manipulator[i]) {
                moveManipulatorAndSetFunction(i, dpos_command_openGL);
            }
        }
    }

    private void moveManipulatorManuallyAndSetFunction(int manipulator_id, Pos3d p ){
        if(!lock_chain) {
            lock_chain = true;

            Pos3d true_grid = grid;
            grid = Pos3d.Zero();

            manipulator[manipulator_id].setPosition(p);
            setFunctionGivenManipulators();
            grid = true_grid;

            if((manipulator_id == LEFT_MANIPULATOR_ID) && isCoupledLeft()){
                // only move if it is not selected otherwise it will move two times
                coupled_function_left.synchronizeThis();
            }else if((manipulator_id == RIGHT_MANIPULATOR_ID) && isCoupledRight()){
                coupled_function_right.synchronizeThis();
            }
            lock_chain = false;
        }
    }

    private void moveManipulatorAndSetFunction(int manipulator_id, Pos3d dp){
        if(!lock_chain) {
            lock_chain = true;

            Pos3d pos = manipulator[manipulator_id].getPosition();
            pos.add(dp);
            manipulator[manipulator_id].setPosition(pos);
            setFunctionGivenManipulators();

            if((manipulator_id == LEFT_MANIPULATOR_ID) && isCoupledLeft()){
                // only move if it is not selected otherwise it will move two times
                boolean selected = coupled_function_left.active_manipulator[RIGHT_MANIPULATOR_ID];
                if(!selected) {
                    coupled_function_left.moveManipulatorAndSetFunction(RIGHT_MANIPULATOR_ID, dp);
                }
            }else if((manipulator_id == RIGHT_MANIPULATOR_ID) && isCoupledRight()){
                // only move if it is not selected otherwise it will move two times
                boolean selected = coupled_function_right.active_manipulator[LEFT_MANIPULATOR_ID];
                if(!selected) {
                    coupled_function_right.moveManipulatorAndSetFunction(LEFT_MANIPULATOR_ID, dp);
                }
            }
            lock_chain = false;
        }
    }

    private boolean scaleFunctionMinX(double dx){
        if(!lock_chain) {
            DrawableFunction df = getDrawableFunction();
            // make sure the function does not get a length of zero
            double new_min = df.min_x + dx;
            if (dx > 0) {
                if (Math.abs(new_min - df.max_x) < Utility.EPSILON_D) {
                    return false;
                }
            }
            df.setMin(new_min);

            lock_chain = true;
            if (isCoupledLeft()) {
                // The min of the right function about dx
                coupled_function_left.scaleFunctionMaxX(dx);
            }
            lock_chain = false;
        }
        return true;
    }

    private boolean scaleFunctionMaxX(double dx){
        if(!lock_chain) {
            DrawableFunction df = getDrawableFunction();
            // make sure the function does not get a length of zero
            double new_max = df.max_x + dx;
            if (dx < 0) {
                if (Math.abs(new_max - df.min_x) < Utility.EPSILON_D) {
                    return false;
                }
            }
            df.setMax(new_max);

            lock_chain = true;
            if (isCoupledRight()) {
                // The min of the right function about dx
                coupled_function_right.scaleFunctionMinX(dx);
            }
            lock_chain = false;
        }
        return true;
    }

    private void setFunctionGivenManipulators(){
        java.util.Arrays.sort(manipulator);
        DrawableFunction df = (DrawableFunction) parent;
        Pos3d left = getNextGridPoint(df.f2openGL.invTransform(manipulator[LEFT_MANIPULATOR_ID].getPosition()));
        Pos3d right = getNextGridPoint(df.f2openGL.invTransform(manipulator[RIGHT_MANIPULATOR_ID].getPosition()));

        Function f = getFunction();
        ArrayList<Pos3d> poses = new ArrayList<>();
        poses.add(left);
        poses.add(right);

        if(Math.abs(left.x - right.x) > Utility.EPSILON_D){
            f.setFunctionGivenPoints(poses);
        }

        df.setFunction(f);
        df.setMax(right.x);
        df.setMin(left.x);

        // to sync things up
        setManipulatorsBasedOnFunction();
    }

    /*!
     * \brief registerCoupledFunctionPair takes two Movable functions and registers them as coupled.
     * \param left The function on the left
     * \param right The function on the right
     */
    public static void registerCoupledFunctionPair(MovableFunction left, MovableFunction right){
        left.coupled_function_right = right;
        right.coupled_function_left = left;
    }

    public void unregisterLeftCoupledFunction(){
        coupled_function_left = null;
    }

    public void unregisterRightCoupledFunction(){
        coupled_function_right = null;
    }

    // tell the left coupled function to let loose too
    private void registerOpenFunctionLeft(){
        if(isCoupledLeft()){
            coupled_function_left.registerOpenFunctionRight();
            coupled_function_left = null;
        }
    }

    // tell the right coupled function to let loose too
    private void registerOpenFunctionRight(){
        if(isCoupledRight()){
            coupled_function_right.registerOpenFunctionLeft();
            coupled_function_right = null;
        }
    }

    public boolean isCoupledRight(){
        return coupled_function_right != null;
    }

    public boolean isCoupledLeft(){
        return coupled_function_left != null;
    }

    /*!
    * \brief synchronizeThis Will set the manipulators according to the coupled functions to match them.
    * */
    public void synchronizeThis(){
        if(isCoupledRight()){
            Pos3d right = coupled_function_right.manipulator[LEFT_MANIPULATOR_ID].getPosition();
            manipulator[RIGHT_MANIPULATOR_ID].setPosition(right);
        }
        if(isCoupledLeft()){
            Pos3d left = coupled_function_left.manipulator[RIGHT_MANIPULATOR_ID].getPosition();
            manipulator[LEFT_MANIPULATOR_ID].setPosition(left);
        }

        // cheat and set order of function to two
        Function f = getFunction();
        ArrayList<Pos3d> poses = new ArrayList<Pos3d>(2){{
            add(manipulator[LEFT_MANIPULATOR_ID].getPosition());
            add(manipulator[RIGHT_MANIPULATOR_ID].getPosition());
        }};

        Pos3d true_grid = grid;
        grid = Pos3d.Zero();
        f.setFunctionGivenPoints(poses);
        setFunctionGivenManipulators();
        grid = true_grid;
    }

    public double getFunctionMaxX(){
        DrawableFunction df = (DrawableFunction) parent;
        return getNextGridPointX(df.max_x);
    }

    public double getFunctionMinX(){
        DrawableFunction df = (DrawableFunction) parent;
        return getNextGridPointX(df.min_x);
    }

    public double getFunctionMaxY(){
        DrawableFunction df = (DrawableFunction) parent;
        return  getNextGridPointY(df.f.getMax(df.min_x, df.max_x));
    }

    public double getFunctionMinY(){
        DrawableFunction df = (DrawableFunction) parent;
        return getNextGridPointY(df.f.getMin(df.min_x, df.max_x));
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
        Pos3d [] ff = new Pos3d[NUM_MANIPULATORS];
        ff[LEFT_MANIPULATOR_ID] = new Pos3d(df.min_x, f.f(df.min_x), Globals.MANIPULATOR_Z_ELEVATION);
        ff[RIGHT_MANIPULATOR_ID] = new Pos3d(df.max_x, f.f(df.max_x), Globals.MANIPULATOR_Z_ELEVATION);

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
            active_manipulator[i] = false;
        }
        super.setLocked(l);
    }

    /*!
     * \brief setStepWidth set the minimal grid size for the function
     * The function will always stick to the grid with its ends.
     */
    public void stickToGrid(Pos3d grid){
        this.grid = grid;
        this.grid.abs();
    }

    public Pos3d getNextGridPoint(Pos3d pos){
        pos.x = getNextGridPointX(pos.x);
        pos.y = getNextGridPointY(pos.y);
        return pos;
    }

    public double getNextGridPointX(double x){
        if(grid.x > Utility.EPSILON_D) {
            return Math.round(x / grid.x) * grid.x;
        }
        return x;
    }

    public double getNextGridPointY(double y){
        if(grid.y > Utility.EPSILON_D) {
            return Math.round(y / grid.y) * grid.y;
        }
        return y;
    }

    final static int NUM_MANIPULATORS = 2;
    Manipulator [] manipulator = new Manipulator[NUM_MANIPULATORS];
    boolean [] active_manipulator = new boolean[NUM_MANIPULATORS];
    static final int LEFT_MANIPULATOR_ID = 0;
    static final int RIGHT_MANIPULATOR_ID = 1;

    Pos3d dpos_command_system = Pos3d.Zero();
    Pos3d dpos_command_openGL = Pos3d.Zero();

    float manipulator_radius = DEFAULT_MANIPULATOR_RADIUS;
    final static float DEFAULT_MANIPULATOR_RADIUS = 0.4f;

    // lock a movable function if it is coupled
    // to avoid loops. (No destructors == no scoped look guards, me sad)
    private boolean lock_chain = false;

    Pos3d grid = Pos3d.Zero();

    MovableFunction coupled_function_left = null;
    MovableFunction coupled_function_right = null;
}
