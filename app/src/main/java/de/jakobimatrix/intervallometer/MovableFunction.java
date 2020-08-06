package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.util.ArrayList;

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
            double distance = p.distance(pos_command_openGL);
            if(distance < manipulator_radius/4.){
                return true;
            }
        }
        // In case we did not get the signal for some raising conditions.
        // ... I'd rather cheat here a little than finding the real problem.
        endTouch();
        return false;
    }

    private void setAndHoldInputCommand(Pos3d input_open_gl){
        // basically calculate the command from the first touch and hold that command until touch is over
        if(last_active_manipulator == active_manipulator){
            setInputCommand(input_open_gl);
            return;
        }
        last_active_manipulator = active_manipulator;

        for(int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].setLocked(i!=last_active_manipulator);
        }

        if(active_manipulator == INVALID_MANIPULATOR_ID){
            endTouch();
            return;
        }
        setInputCommand(input_open_gl);
    }

    public void setInputCommand(Pos3d input_open_gl){
        if(active_manipulator == INVALID_MANIPULATOR_ID){
            return;
        }
        active_quadrant = manipulator[active_manipulator].getQuadrant(input_open_gl);

        double dx = step_width.x;
        double dy = step_width.y;
        switch (active_quadrant){
            case BOT:
                dpos_command_system = new Pos3d(0, -dy, 0);
                break;
            case TOP:
                dpos_command_system = new Pos3d(0, dy, 0);
                break;
            case LEFT:
                dpos_command_system = new Pos3d(-dx, 0, 0);
                break;
            case RIGHT:
                dpos_command_system = new Pos3d(dx, 0, 0);
                break;
        }

        pos_command_openGL = input_open_gl;
        pos_command_openGL.z = 0;

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
        pos_command_openGL = Pos3d.Zero();
        active_quadrant = null;
        for(int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].endTouch();
            manipulator[i].setLocked(false);
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
                    moveManipulatorYAndSetFunction(0, dpos_command_openGL.y);
                }else {
                    scaleFunctionMinX(dpos_command_system.x);
                }
                break;
            case 1:
                if(top_dow_direction){
                    moveFunctionYOffset(dpos_command_openGL.y);
                }else{
                    moveFunctionXOffset(dpos_command_system.x);
                }
                break;
            case 2:
                if(top_dow_direction){
                    moveManipulatorYAndSetFunction(2, dpos_command_openGL.y);
                }else {
                    scaleFunctionMaxX(dpos_command_system.x);
                }
                break;
        }
    }

    private void moveManipulatorYAndSetFunction(int manipulator_id, double dy){
        moveManipulatorAndSetFunction(manipulator_id, new Pos3d(0,dy,0));
    }

    private void moveManipulatorXAndSetFunction(int manipulator_id, double dx){
        moveManipulatorAndSetFunction(manipulator_id, new Pos3d(dx,0,0));
    }

    private void moveManipulatorAndSetFunction(int manipulator_id, Pos3d dp){
        if(!lock_chain) {
            lock_chain = true;
            if (isValidManipulatorId(manipulator_id)) {
                Pos3d pos = manipulator[manipulator_id].getPosition();
                pos.add(dp);
                manipulator[manipulator_id].setPosition(pos);
                setFunctionGivenManipulators();
            }
            if(manipulator_id == 0 && isCoupledLeft()){
                coupled_function_left.moveManipulatorAndSetFunction(2, dp);
            }else if(manipulator_id == 2 && isCoupledRight()){
                coupled_function_right.moveManipulatorAndSetFunction(0, dp);
            }
            lock_chain = false;
        }
    }

    /*!
     * \brief moveFunctionOffset Moves the function in x direction
     * This is called via mid manipulator left/right direction
     * \param dx The amount to move the function in x direction
     */
    private void moveFunctionXOffset(double dx) {
        if(!lock_chain) {
            lock_chain = true;
            getDrawableFunction().moveX(dx);
            setManipulatorsBasedOnFunction();

            if(isCoupledLeft()){
                if(coupled_function_right.getFunction().getOrder() == 1){
                    coupled_function_left.scaleFunctionMaxX(dx);
                }else{
                    coupled_function_left.moveManipulatorXAndSetFunction(2, dx);
                }
            }
            if(isCoupledRight()){
                coupled_function_right.moveFunctionXOffset(dx);
            }
            lock_chain = false;
        }
    }

    // the right coupled function moved to the left, we squish
    private void squishFromRight(double dx){
        Pos3d new_right_manip_pos = manipulator[2].getPosition();
        new_right_manip_pos.x += dx;
        manipulator[2].setPosition(new_right_manip_pos);
        setFunctionGivenManipulators();
    }

    private boolean isValidManipulatorId(int id){
        if(id != INVALID_MANIPULATOR_ID && manipulator.length > id){
            return true;
        }
        return false;
    }

    private void scaleFunctionMinX(double dx){
        if(!lock_chain) {
            DrawableFunction df = getDrawableFunction();
            // make sure the function does not get a length of zero
            double new_min = df.min_x + dx;
            if (dx > 0) {
                if (Math.abs(new_min - df.max_x) < Utility.EPSILON_D) {
                    return;
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
    }

    private void scaleFunctionMaxX(double dx){
        if(!lock_chain) {
            DrawableFunction df = getDrawableFunction();
            // make sure the function does not get a length of zero
            double new_max = df.max_x + dx;
            if (dx < 0) {
                if (Math.abs(new_max - df.min_x) < Utility.EPSILON_D) {
                    return;
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
    }

    private void setFunctionGivenManipulators(){
        Function f = getFunction();
        DrawableFunction df = (DrawableFunction) parent;
        ArrayList<Pos3d> poses = new ArrayList<>();
        switch(f.getOrder()){
            case 1:
                poses.add(getNextGridPoint(df.f2openGL.invTransform(manipulator[1].getPosition())));
                break;
            case 2:
                poses.add(getNextGridPoint(df.f2openGL.invTransform(manipulator[0].getPosition())));
                poses.add(getNextGridPoint(df.f2openGL.invTransform(manipulator[2].getPosition())));
                break;
            case 3:
                poses.add(getNextGridPoint(df.f2openGL.invTransform(manipulator[0].getPosition())));
                poses.add(df.f2openGL.invTransform(manipulator[1].getPosition())); // don't stick the middle one to grid
                poses.add(getNextGridPoint(df.f2openGL.invTransform(manipulator[2].getPosition())));
                break;
            default:
                throw new IllegalArgumentException( "MovableFunction::setFunctionGivenManipulators: I only support Functions of order 1, 2 or 3." );
        }

        f.setFunctionGivenPoints(poses);
        df.setFunction(f);

        setManipulatorsBasedOnFunction();
    }

    /*!
     * \brief moveFunctionYOffset
     * This is called via mid manipulator top/down direction
     * \param dy The amount to move the function in y direction
     */
    private void moveFunctionYOffset(double dy){
        if(!lock_chain) {
            lock_chain = true;
            getDrawableFunction().moveY(dy);
            setManipulatorsBasedOnFunction();
            if (isCoupledLeft()) {
                coupled_function_left.moveFunctionYOffset(dy);
            }
            if (isCoupledRight()) {
                coupled_function_right.moveFunctionYOffset(dy);
            }
            lock_chain = false;
        }
    }

    /*!
     * \brief registerCoupledFunctionPair takes two Movable functions and registers them as coupled.
     * \param left The function on the left
     * \param right The function on the right
     */
    static void registerCoupledFunctionPair(MovableFunction left, MovableFunction right){
        left.registerCoupledFunctionRight(right);
        right.registerCoupledFunctionRight(left);
    }

    // let loose left and register new
    private void registerCoupledFunctionLeft(MovableFunction mf){
        registerOpenFunctionLeft();
        coupled_function_left = mf;
    }

    // let loose right and register new
    private void registerCoupledFunctionRight(MovableFunction mf){
        registerOpenFunctionRight();
        coupled_function_right = mf;
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

    public void synchronizeCoupledLeft(){
        if(isCoupledLeft()){
            Pos3d actual = manipulator[0].getPosition();
            Pos3d target = coupled_function_left.manipulator[2].getPosition();
            Pos3d dp = Pos3d.sub(target, actual);
            moveFunctionAndTail(dp);
        }
    }

    public void synchronizeCoupledRight(){
        if(isCoupledRight()){
            coupled_function_right.synchronizeCoupledLeft();
        }
    }

    public void synchronizeThis(int order){
        if(isCoupledRight()){
            Pos3d right = coupled_function_right.manipulator[0].getPosition();
            manipulator[2].setPosition(right);
        }
        if(isCoupledLeft()){
            Pos3d left = coupled_function_right.manipulator[2].getPosition();
            manipulator[0].setPosition(left);
        }

        // cheat and set order of function to two
        Function f = getFunction();
        ArrayList<Pos3d> poses = new ArrayList<>(2);
        poses.add(Pos3d.Zero());
        poses.add(Pos3d.Zero());
        f.setFunctionGivenPoints(poses);
        setFunctionGivenManipulators();
    }

    private void moveFunctionAndTail(Pos3d dp){
        for(int i = 0; i < NUM_MANIPULATORS; i++){
            manipulator[i].move(dp);
        }
        setFunctionGivenManipulators();
        if(isCoupledRight()){
            coupled_function_right.moveFunctionAndTail(dp);
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

    /*!
     * \brief setStepWidth
     * Sets the step size at which an input (move) command for the function gets created.
     * The step width cant be smaller than the set grid.
     * If a smaller step size is given return is false and the step size will be adapted.
     * \param step A 3d point where the x and y coordinates are the step command.
     * return true if the given step size was greater than the grid.
     */
    public boolean setStepWidth(Pos3d step){
        // step width cant be smaller  than grid width
        step_width.x = Math.max(step_width.x, grid.x);
        step_width.y = Math.max(step_width.y, grid.y);
        step_width.z = step.z;
        return Pos3d.equals(step, step_width);
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

    final static int NUM_MANIPULATORS = 3;
    MovableDot [] manipulator = new MovableDot[NUM_MANIPULATORS];

    final static int INVALID_MANIPULATOR_ID = -1;
    int active_manipulator = INVALID_MANIPULATOR_ID;
    int last_active_manipulator = INVALID_MANIPULATOR_ID;

    Pos3d dpos_command_system = Pos3d.Zero();
    Pos3d dpos_command_openGL = Pos3d.Zero();
    Pos3d pos_command_openGL = Pos3d.Zero();
    MovableDot.QUADRANT active_quadrant;

    float manipulator_radius = DEFAULT_MANIPULATOR_RADIUS;
    final static float DEFAULT_MANIPULATOR_RADIUS = 0.25f;

    public boolean lock_manipulation;
    // lock a movable function if it is coupled
    // to avoid loops. (No destructors == no scoped look guards, me sad)
    private boolean lock_chain = false;

    Pos3d step_width = new Pos3d(1,1,0);
    Pos3d grid = Pos3d.Zero();

    MovableFunction coupled_function_left = null;
    MovableFunction coupled_function_right = null;
}
