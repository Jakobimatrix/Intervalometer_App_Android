package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

public class MovableCoordinateSystem extends Movable {

    /*!
     * \brief MovableCoordinateSystem Constructor
     * \param context_ anyone? context anyone?
     * \param position_ the bot, left position of the coordinate system in open gl coordinates
     * \param width The width in openGL scale of the coordinate system
     * \param height The height in openGL scale of the coordinate system
     */
    public MovableCoordinateSystem(Context context_, Pos3d position_, float width, float height) {
        super(new DrawableRectangle(context_, new Pos3d(position_), width, height));
        DrawableRectangle bg = (DrawableRectangle) parent;
        bg.setBotLeftOrigin();
        bg.setColor(BG_DEFAULT_COLOR);

        // Move everything such that the axis (arrows) are inside the bg (rectangle).
        // Otherwise the arrows would be half off the bg.
        // todo depends on size of Manipulator and Tick
        double margin = 0.35;
        static_axis_offset[0] = new Pos3d(0, margin, 0);
        static_axis_offset[1] = new Pos3d(margin, 0, 0);
        static_axis_offset[2] = new Pos3d(margin, margin, 0);

        setViewPortOpenGL();

        // this will be changed according to the functions added later.
        system_viewport = new ViewPort(Pos3d.Zero(), new Pos3d(0,0, Globals.FUNCTION_Z_ELEVATION));

        x_axis = new DrawableArrow(context_,new Pos3d(position_), width, AXIS_WIDTH);
        x_axis.setRotationRIGHT();
        y_axis = new DrawableArrow(context_,new Pos3d(position_), height, AXIS_WIDTH);
        y_axis.setRotationUP();

        parent.adChild(x_axis, static_axis_offset[0]);
        parent.adChild(y_axis, static_axis_offset[1]);

        x_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES); // ||
        y_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES); // ==

        for(int i = 0; i < NUM_GRID_STRIPES; i++){
            y_grid.add(new DrawableRectangle(context_, new Pos3d(position_), (float) (width-2*margin), GRID_WIDTH));
            x_grid.add(new DrawableRectangle(context_, new Pos3d(position_), GRID_WIDTH, (float) (height-2*margin)));
            y_grid.get(i).setLeftCenterOrigin();
            x_grid.get(i).setBotCenterOrigin();
            x_grid.get(i).setColor(ColorRGBA.TRANSPARENT);
            y_grid.get(i).setColor(ColorRGBA.TRANSPARENT);
            parent.adChild(y_grid.get(i));
            parent.adChild(x_grid.get(i));
        }

        for(int i = 0; i < NUM_TICK_LABELS; i++) {
            final float ROTATION = (float) -(Math.PI/4); // to save place
            DrawableString dsx = new DrawableString(parent.context, getPosition(), FONT_SIZE, FONT_SIZE_PIX, "");
            DrawableString dsy = new DrawableString(parent.context, getPosition(), FONT_SIZE, FONT_SIZE_PIX, "");
            dsx.setRotation(ROTATION);
            dsy.setColor(ColorRGBA.TRANSPARENT);
            dsy.setRotation(ROTATION);
            x_ticks.add(dsx);
            y_ticks.add(dsy);
        }

        x_axis.setColor(grid_color);
        y_axis.setColor(grid_color);

        // we lock the functions individually
        is_locked = false;
    }

    /*!
     * \brief isWithin Return true if the position_ corresponds to one of the manipulator points of one of the displayed functions.
     * active_function will be set for manipulation
     * \param position_ the position in openGL.
     */
    @Override
    public boolean isWithin(Pos3d position_) {
        // Todo we have to deal with Manipulators going over the edge
        if(true || parent.isWithin(position_)) {
            for (int i = 0; i < functions.size(); i++) {
                if (functions.get(i).isWithin(position_)) {
                    // toggle
                    active_function = i;
                    return true;
                }
            }
        }
        active_function = INVALID_FUNCTION;
        return false;
    }

    /*!
     * \brief setPosition sets the position of the active_functions active manipulator to change that function.
     */
    @Override
    public void setPosition(Pos3d position_){
        //throw new IllegalArgumentException( "MovableCoordinateSystem::setPosition: please use executeCommand" );
    }

    @Override
    public void move(Pos3d dp){
        //throw new IllegalArgumentException( "MovableCoordinateSystem::move: please use executeCommand" );
    }

    @Override
    public void endTouch() {
        // obsolete concept
    }

    /*!
     * \brief endTouch must be called abd will pass that call to the active function to
     * signal that the user stopped touching the screen.
     */
    public void manuelEndTouch() {
        for(MovableFunction df: functions){
            df.endTouch();
        }
        hold_start = FIRST_CONTACT;
    }

    public void executeCommand(CMD cmd){
        if(isValidFunctionId(active_function) && sensitivity()){
            functions.get(active_function).setCommand(cmd);
            functions.get(active_function).executeCurrentCommand();
            scale(); // TODO we could check if scaling in necessary
        }
    }

    /*!
     * \brief sensitivity sets the step width at which the active function can be manipulated, it also
     * returns weather a manipulation shall occur according to the length of the touch.
     * It discretize the manipulations depending on the holding time.
     * \return true if it is time for manipulation.
     */
    private boolean sensitivity(){
        if(hold_start == FIRST_CONTACT){
            hold_start = System.currentTimeMillis();
            active_tick_start = 0;
        }
        long now = System.currentTimeMillis();
        long duration = now - hold_start;

        long wait;
        double multiply = 1.;

        // depending on how long the user holds down, we wait x ms until we let the next change happen.
        if(duration < SLOW_TICK_DURATION_HOLD_MS){
            wait = SLOW_TICK_DURATION_MS;
        }else if(duration < MEDIUM_TICK_DURATION_HOLD_MS){
            wait = MEDIUM_TICK_DURATION_MS;
        }else{
            wait = MEDIUM_TICK_DURATION_MS;
            multiply = 10.;
        }

        duration = now - active_tick_start;
        if(duration > wait){
            active_tick_start = now;
            functions.get(active_function).setStepWidth(Pos3d.mul(current_grid_distance,multiply));
            return true;
        }
        return false;
    }

    public void setCoordinateSystemPosition(Pos3d position_){
        parent.setPosition(position_);
        scale();
    }

    @Override
    public void draw(GL10 gl){
        parent.draw(gl);
        for(MovableFunction f:functions){
            f.draw(gl);
        }
        for(DrawableString ds: x_ticks){
            ds.draw(gl);
        }
        for(DrawableString ds: y_ticks){
            ds.draw(gl);
        }
    }

    public void setBackgroundColor(ColorRGBA color){
        parent.setColor(color);
    }

    /*!
     * \brief adjustGrid Calculates the grid and sets the ticks.
     */
    private void adjustGrid(){
        current_grid_distance = Pos3d.Zero();
        for(int i = 0; i < 2; i++){
            int tick_id_counter = 0;
            // to avoid if else later
            double is_x_d = (i == 0)?1:0.0;
            boolean is_x = i == 0;
            double is_y_d = (i == 1)?1:0.0;
            boolean is_y = !is_x;

            // choose the grid partition depending on the span = max - min
            final double ROUNDING_POINT = 0.75;
            // e.g a span of 4 -> log(4) = 0.6 ::> roundAT(0.6, 0.75) = 0 -> grid partition is 10^0 = 0.1
            // e.g a span of 8 -> log(8) = 0.9 ::> roundAT(0.9, 0.75) = 1 -> grid partition is 10^1 = 1
            double span = system_viewport.width()*is_x_d + system_viewport.height()*is_y_d;
            double log_span = Math.log10(span);
            double log_span_rounded = Utility.roundAT(log_span,ROUNDING_POINT);
            double grid_power = Math.pow(10,(int) log_span_rounded - 1);
            ArrayList <DrawableRectangle> grid = getGrid(i);

            // For fine grids, only display every tenth grid, else display every grid.
            int tick_count_mod = (log_span>log_span_rounded)?10:1;

            /* truncates e.g.
            // min = 123.456
            // power = 0.1
            // start = 123.4 + 0.1
            */
            double start = system_viewport.min.x*is_x_d + system_viewport.min.y*is_y_d;
            start = Utility.cutAtDecimal(start, grid_power);
            // build the grid, starting at start, adding d_pos every iteration

            Pos3d pos_iterator = new Pos3d(start*is_x_d,start*is_y_d, 0);
            // add the offset of the direction we don't iterate through.
            pos_iterator.add(new Pos3d(system_viewport.min.x*is_y_d,system_viewport.min.y*is_x_d, 0));

            Pos3d d_pos_iterator = new Pos3d(grid_power*is_x_d, grid_power*is_y_d, 0);
            current_grid_distance.add(d_pos_iterator);
            int grid_count = 0;
            for(DrawableRectangle grid_line : grid){
                boolean inside = ((pos_iterator.x < system_viewport.max.x + Utility.EPSILON_D) || is_y) &&
                        ((pos_iterator.y < system_viewport.max.y + Utility.EPSILON_D) || is_x);
                Pos3d translation_openGL = system_2_open_gl.transform(pos_iterator);
                // translation_openGL is now absolute position
                Pos3d rel_translation_openGL = absolutePos2relativePos(translation_openGL);
                rel_translation_openGL.z += Globals.GRID_Z_ELEVATION; // to be sure that grid is above bg.
                grid_line.setRelativePositionToParent(rel_translation_openGL);

                boolean draw_tick = (tick_id_counter < NUM_TICK_LABELS && grid_count++%tick_count_mod == 0);

                if(inside){
                    grid_line.setColor(grid_color);

                    if(draw_tick){
                        grid_line.setWidth((float) (GRID_WIDTH_TICK*is_x_d + grid_line.getWidth()*is_y_d));
                        grid_line.setHeight((float) (GRID_WIDTH_TICK*is_y_d + grid_line.getHeight()*is_x_d));

                        double d_tick = pos_iterator.x * is_x_d + pos_iterator.y * is_y_d;
                        d_tick = Utility.roundAtDecimal(d_tick, grid_power*tick_count_mod);
                        setAxisTick(translation_openGL, prepareTick(d_tick), i, tick_id_counter++ );
                    }else{
                        grid_line.setWidth((float) (GRID_WIDTH*is_x_d + grid_line.getWidth()*is_y_d));
                        grid_line.setHeight((float) (GRID_WIDTH*is_y_d + grid_line.getHeight()*is_x_d));
                    }
                }else{
                    grid_line.setColor(ColorRGBA.TRANSPARENT);
                }
                pos_iterator.add(d_pos_iterator);
            }
            // make the rest invisible by displaying nothing
            for(;tick_id_counter < NUM_TICK_LABELS; tick_id_counter++){
                setAxisTick(Pos3d.Zero(),"", i, tick_id_counter );
            }
        }
    }

    /*!
     * \brief stickToGrid set the minimal grid size for all functions
     * The function will always stick to the grid.
     * \param grid A 3D point where x and y denote the grid width.
     */
    public void stickToGrid(Pos3d grid){
        for(MovableFunction mf : functions){
            mf.stickToGrid(grid);
        }
    }

    /*!
     * \brief prepareTick transform double to string to be displayed as tick value.
     * \param d_tick the grid value
     * \return tick value as string
     */
    private String prepareTick(Double d_tick){
        // don't display comma if integer
        boolean is_integer = (d_tick == Math.round(d_tick));
        String tick = is_integer? ((Integer) d_tick.intValue()).toString(): d_tick.toString();
        tick = tick.substring(0, Math.min(tick.length(), MAX_NUM_TICK_CHARS));

        if(!tick.contains(".")){
            return tick;
        }
        // delete trailing zeros and trailing comma
        while (tick.length() > 1){
            char c = tick.charAt(tick.length() - 1);
            if(c == '0' || c == '.'){
                tick = tick.substring(0, tick.length()-1);
                continue;
            }
            break;
        }
        return tick;
    }

    /*!
     * \brief setAxisTick Called by adjustGrid to set the tick
     * \param coord_pos Position of the tick (origin of the grid bar)
     * \param tick The string to be displayed
     * \param direction 0 = x tick, 1 = y tick
     * \param id the number of the tick to be displayed. = position in x_ticks/y_ticks vector.
     */
    private void setAxisTick(Pos3d coord_pos, String tick, int direction, int id){
        final float ROTATION = (float) -(Math.PI/4); // save place
        Pos3d pos = new Pos3d(coord_pos);
        pos.z = Globals.FUNCTION_Z_ELEVATION;
        DrawableString ds = new DrawableString(parent.context, pos, FONT_SIZE, FONT_SIZE_PIX, tick);
        ds.setRotation(ROTATION);
        Pos3d mv = Pos3d.Zero();
        if(direction == 1){
            mv.x = -ds.getWidth()*Math.cos(ROTATION) + FONT_SIZE*Math.sin(ROTATION) - AXIS_WIDTH/2;
            mv.y = -ds.getWidth()*Math.sin(ROTATION);
            ds.move(mv);
            y_ticks.set(id, ds);
        }else{
            mv.y = FONT_SIZE*Math.sin(ROTATION) - AXIS_WIDTH/2f;
            ds.move(mv);
            x_ticks.set(id, ds);
        }
    }

    private ArrayList <DrawableRectangle> getGrid(int i){
        if(i == 0){
            return x_grid;
        }
        if(i == 1){
            return y_grid;
        }
        throw new IllegalArgumentException( "MovableCoordinateSystem::getGrid: onli input 0 = x or 1 = y allowed.");
    }

    public void setGridColor(ColorRGBA grid_color) {
        this.grid_color = grid_color;
        x_axis.setColor(grid_color);
        y_axis.setColor(grid_color);
        for(int i = 0; i < NUM_GRID_STRIPES; i++) {
            x_grid.get(i).setColor(grid_color);
            y_grid.get(i).setColor(grid_color);
        }
    }

    /*!
     * \brief setViewportSystem Set the viewport of the Coordinate System according to the max and min Values needed to be displayed.
     */
    private void setViewportSystem(){
        system_viewport.min.x = getAllFunctionsMinX();
        system_viewport.min.y = getAllFunctionsMinY();
        system_viewport.max.x = getAllFunctionsMaxX();
        system_viewport.max.y = getAllFunctionsMaxY();
        double dif_x = system_viewport.max.x - system_viewport.min.x;
        double dif_y = system_viewport.max.y - system_viewport.min.y;
        // avoid no height if function is a constant
        if(dif_y < MIN_HEIGHT){
            double center = system_viewport.min.y + dif_y/2.0;
            system_viewport.min.y = center - MIN_HEIGHT/2.0;
            system_viewport.max.y = center + MIN_HEIGHT/2.0;
        }

        // avoid no width if function is very small
        final double SMOOL_NUMBER = 0.0000001;
        if(dif_x < SMOOL_NUMBER){
            double center = system_viewport.min.x + dif_x/2.0;
            system_viewport.min.x = center - SMOOL_NUMBER/2.0;
            system_viewport.max.x = center + SMOOL_NUMBER/2.0;
        }
    }

    /*!
     * \brief setViewPortOpenGL Set the viewport of the Coordinate System in open GL coordinates according to position width and height.
     */
    private void setViewPortOpenGL(){
        Pos3d position_ = getPosition();
        float width = getWidth();
        float height = getHeight();

        Pos3d view_port_top_right = new Pos3d(position_);
        view_port_top_right.add(new Pos3d(width, height, 0));
        view_port_top_right.sub(static_axis_offset[2]);
        Pos3d view_port_left_bot = new Pos3d(position_);
        view_port_left_bot.add(static_axis_offset[2]);
        openGL_viewport = new ViewPort(view_port_left_bot, view_port_top_right);
    }

    /*!
     * \brief scale rescales the coordinate system, calculating the homography between openGL and
     * coordinates, calculates the grid and redraws all functions.
     */
    private void scale(){
        setViewportSystem();
        setViewPortOpenGL();

        //Log.d("scale", "system_viewport: " + system_viewport.toString());
        //Log.d("scale", "openGL_viewport: " + openGL_viewport.toString());

        open_gl_2_system.calculateHomography2DNoRotation(openGL_viewport.min, system_viewport.min, openGL_viewport.max, system_viewport.max);
        system_2_open_gl.calculateHomography2DNoRotation(system_viewport.min, openGL_viewport.min, system_viewport.max, openGL_viewport.max);

        adjustGrid();
        rescaleAllFunctions();
    }

    private double getAllFunctionsMaxX(){
        double max = Double.NEGATIVE_INFINITY;
        for(MovableFunction f: functions){
            max = Math.max(max, f.getFunctionMaxX());
        }
        return max;
    }

    private double getAllFunctionsMinX(){
        double min = Double.POSITIVE_INFINITY;
        for(MovableFunction f: functions){
            min = Math.min(min, f.getFunctionMinX());
        }
        return min;
    }

    private double getAllFunctionsMaxY(){
        double max = Double.NEGATIVE_INFINITY;
        for(MovableFunction f: functions){
            max = Math.max(max, f.getFunctionMaxY());
        }
        return max;
    }

    private double getAllFunctionsMinY(){
        double min = Double.POSITIVE_INFINITY;
        for(MovableFunction f: functions){
            min = Math.min(min, f.getFunctionMinY());
        }
        return min;
    }

    /*!
     * \brief addFunction Adds a function to be displayed
     * \param f The function
     * \param min The min x value to be displayed
     * \param max The max x value to be displayed
     * \return an index over which the function can be deleted/locked etc...
     */
    public int addFunction(Function f, double min, double max){
        Pos3d grid = functions.size()>0? functions.get(0).grid:Pos3d.Zero();

        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);
        MovableFunction mf = new MovableFunction(parent.context, relative_position, f, min, max, system_2_open_gl);
        mf.setLocked(true);
        mf.stickToGrid(grid);
        functions.add(mf);

        synchronizeFunctions();

        scale();
        rescaleAllFunctions();
        return functions.size()-1;
    }

    public void replaceFunction(int index, Function f, double min, double max){
        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);
        getMovableFunction(index); // throw if index is out of bounds
        MovableFunction mf = new MovableFunction(parent.context, relative_position, f, min, max, system_2_open_gl);
        mf.setLocked(true);
        functions.set(index, mf);

        synchronizeFunctions();

        scale();
        rescaleAllFunctions();
    }

    /*!
     * \brief rescaleAllFunctions
     * set the current homography to each unction which will trigger a redraw.
     */
    private void rescaleAllFunctions(){
        for(MovableFunction mf:functions){
            mf.setHomography(system_2_open_gl);
        }
    }

    public float getWidth(){
        DrawableRectangle rect = (DrawableRectangle)parent;
        return rect.getWidth();
    }

    public float getHeight(){
        DrawableRectangle rect = (DrawableRectangle)parent;
        return rect.getHeight();
    }

    public void setHeight(float height){
        DrawableRectangle rect = (DrawableRectangle)parent;
        rect.setHeight(height);
        scale();
    }

    public void setWidth(float width){
        DrawableRectangle rect = (DrawableRectangle)parent;
        rect.setWidth(width);
        scale();
    }

    private MovableFunction getMovableFunction(int index){
        if(functions.size() > index){
            return functions.get(index);
        }
        throw new IllegalArgumentException( "MovableCoordinateSystem::getMovableFunction: given index is not a function set prior");
    }

    /*!
     * \brief setFunctionLocked (un)lock a function to make it manipulable via manipulator.
     * \param l look true/false
     * \param function_index The function to lock/unlock
     */
    public void setFunctionLocked(boolean l, int function_index){
        getMovableFunction(function_index).setLocked(l);
    }

    @Override
    public void setLocked(boolean l){
        throw new IllegalArgumentException( "MovableCoordinateSystem::setLocked: Don't use this function. Lock individual functions with setFunctionLocked().");
    }

    private boolean isValidFunctionId(int id){
        if(id < functions.size() && id > INVALID_FUNCTION){
            return true;
        }
        return false;
    }

    public void synchronizeFunctions(){
        for(int i = 0; i < functions.size() - 1; i++){
            MovableFunction.registerCoupledFunctionPair(functions.get(i), functions.get(i+1));
        }
    }

    final static int INVALID_FUNCTION = -1;
    int active_function = INVALID_FUNCTION;
    Vector<MovableFunction> functions = new Vector<>();

    // visual
    ViewPort openGL_viewport;
    // values on axis
    ViewPort system_viewport;

    Pos3d current_grid_distance = Pos3d.Zero();
    long hold_start = FIRST_CONTACT;
    long active_tick_start = 0;
    final static long FIRST_CONTACT = -1;
    final static long SLOW_TICK_DURATION_MS = 750;
    final static long SLOW_TICK_DURATION_HOLD_MS = SLOW_TICK_DURATION_MS;
    final static long MEDIUM_TICK_DURATION_MS = 200;
    final static long MEDIUM_TICK_DURATION_HOLD_MS = SLOW_TICK_DURATION_HOLD_MS + 10*MEDIUM_TICK_DURATION_MS;

    DrawableArrow x_axis;
    DrawableArrow y_axis;

    Homography2d open_gl_2_system = new Homography2d();
    Homography2d system_2_open_gl = new Homography2d();

    ArrayList <DrawableRectangle> x_grid;
    ArrayList <DrawableRectangle> y_grid;
    ArrayList <DrawableString> x_ticks = new ArrayList<>(NUM_TICK_LABELS);
    ArrayList <DrawableString> y_ticks = new ArrayList<>(NUM_TICK_LABELS);
    final static int MAX_NUM_TICK_CHARS = 6;

    final static float FONT_SIZE = 0.4f;
    final static int FONT_SIZE_PIX = 75;

    Pos3d [] static_grid_offset_openGL = new Pos3d[2];
    Pos3d [] static_axis_offset = new Pos3d[3];

    final static int NUM_GRID_STRIPES = 65;
    final static int NUM_TICK_LABELS = 15;
    final static float AXIS_WIDTH = 0.2f;
    final static float GRID_WIDTH = 0.01f;
    final static float GRID_WIDTH_TICK = 0.03f;

    final static float MIN_WIDTH = 1.f;
    final static float MIN_HEIGHT= 10.f;

    final static ColorRGBA BG_DEFAULT_COLOR = new ColorRGBA(0.7,0.7,0.7,1);
    final static ColorRGBA BG_DEFAULT_GRID_COLOR = new ColorRGBA(0,0,0,1);
    ColorRGBA grid_color = BG_DEFAULT_GRID_COLOR;
}
