package de.jakobimatrix.intervallometer;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

enum SUPPORTED_FUNCTION{
    LINEAR,
    QUADRATIC_EXTREMA_LEFT,
    QUADRATIC_EXTREMA_RIGHT,
    SIGMOID,
    UNKNOWN
};

public class MovableCoordinateSystem extends Movable {

    /*!
     * \brief MovableCoordinateSystem Constructor
     * \param context_ anyone? context anyone?
     * \param position_ the bot, left position of the coordinate system in open gl coordinates
     * \param width The width in openGL scale of the coordinate system
     * \param height The height in openGL scale of the coordinate system
     */
    public MovableCoordinateSystem(Activity activity, Pos3d position_, float width, float height) {
        super(new DrawableRectangle(activity.getBaseContext(), new Pos3d(position_), width, height));
        //// TODO
        this.activity = activity;
        this.rl = (RelativeLayout) activity.findViewById(Globals.EDIT_TEMPLATE_LAYOUT);
        //// TODO
        DrawableRectangle bg = (DrawableRectangle) parent;
        bg.setBotLeftOrigin();
        bg.setColor(BG_DEFAULT_COLOR);

        // Move everything such that the axis (arrows) are inside the bg (rectangle).
        // Otherwise the arrows would be half off the bg.
        // todo depends on size of Manipulator and Tick
        static_axis_offset[0] = new Pos3d(0, margin_left, 0);
        static_axis_offset[1] = new Pos3d(margin_bot, 0, 0);
        static_axis_offset[2] = Pos3d.add(static_axis_offset[0], static_axis_offset[1]);
        static_axis_offset[3] = new Pos3d(MARGIN_TOP_RIGHT, MARGIN_TOP_RIGHT, 0);

        openGL_viewport = ViewPort.Zero();
        setViewPortOpenGL();

        // this will be changed according to the functions added later.
        system_viewport = new ViewPort(Pos3d.Zero(), new Pos3d(0,0, Globals.FUNCTION_Z_ELEVATION));

        final float arrow_body_width = 0.5f*AXIS_WIDTH/Utility.GOLDEN_RATIO;

        x_axis = new DrawableArrow(activity.getBaseContext(),new Pos3d(position_), width - margin_left, AXIS_WIDTH);
        x_axis.setRotationRIGHT();
        y_axis = new DrawableArrow(activity.getBaseContext(),new Pos3d(position_), height - margin_bot - arrow_body_width, AXIS_WIDTH);
        y_axis.setRotationUP();

        parent.adChild(x_axis, new Pos3d(margin_bot-arrow_body_width/2, margin_left, 0));
        parent.adChild(y_axis, new Pos3d(margin_bot, margin_left, 0));

        x_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES); // ||
        y_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES); // ==

        for(int i = 0; i < NUM_GRID_STRIPES; i++){
            y_grid.add(new DrawableRectangle(activity.getBaseContext(), new Pos3d(position_), (float) (width-margin_left-MARGIN_TOP_RIGHT), GRID_WIDTH));
            x_grid.add(new DrawableRectangle(activity.getBaseContext(), new Pos3d(position_), GRID_WIDTH, (float) (height-margin_bot-MARGIN_TOP_RIGHT)));
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

        is_locked = false;
        placeAddFunctionButtons();
    }

    @Override
    public void clean(){
        super.clean();
        removeAllButtons();
        for(DrawableString ds : x_ticks){
            ds.clean();
        }
        x_ticks.clear();
        for(DrawableString ds : y_ticks){
            ds.clean();
        }
        y_ticks.clear();
    }

    /*!
     * \brief isWithin Return true if the position_ corresponds to one of the manipulator points of one of the displayed functions.
     * active_function will be set for manipulation
     * \param position_ the position in openGL.
     */
    @Override
    public boolean isWithin(Pos3d position_) {
        if(is_locked){
            return false;
        }
        if(parent.isWithin(position_)) {
            for (int i = 0; i < functions.size(); i++) {
                if(toggle_not){
                    if (functions.get(i).isWithin(position_)) {
                        active_function = i;
                        return true;
                    }
                }else {
                    if (functions.get(i).isWithinToggle(position_)) {
                        active_function = i;
                        return true;
                    }
                }
            }
            // Single tap: User clicked within coord but not on a point -> lock all points
            for(MovableFunction mf : functions){
                mf.setLocked(true);
            }
            // long tap: edit function

            if(lifted){
                lifted = false;
                last_tap_edit_function = System.currentTimeMillis();
            }else{
                long now = System.currentTimeMillis();
                if(now - last_tap_edit_function > HOLD_TO_EDIT_FUNC_DURATION_MS){
                    Pos3d func_pos = open_gl_2_system.transform(position_);
                    for(int i = 0; i < functions.size(); i++){
                        MovableFunction mf = functions.get(i);
                        if(mf.getFunctionMinX() < func_pos.x && func_pos.x < mf.getFunctionMaxX()){
                            openAddFunctionDialog(i, true);
                            break;
                        }
                    }
                }
            }
            active_function = INVALID_FUNCTION;
            return true;
        }
        active_function = INVALID_FUNCTION;
        return false;
    }

    /*!
     * \brief setPosition sets the position of the active_functions active manipulator to change that function.
     */
    @Override
    public void setPosition(Pos3d position_){
        if(is_locked){
            return;
        }
        toggle_not = true;
        if(parent.isWithin(position_)) {
            if(isValidFunctionId(active_function)){
                functions.get(active_function).setPosition(position_);
                removeZeroWidthFunctions();
            }
        }
    }

    @Override
    public void move(Pos3d dp){
        //TODO manual moving the manipulator
    }

    @Override
    public void endTouch() {
        active_function = INVALID_FUNCTION;
        lifted = true;
        for(MovableFunction df: functions){
            df.endTouch();
        }
        toggle_not = false;
        scale();
    }

    private void removeZeroWidthFunctions(){
        boolean removed = false;

        for(int i = functions.size()-1; i > -1; i--){
            MovableFunction mf = functions.get(i);
            double manipulator_dif = mf.manipulator[MovableFunction.RIGHT_MANIPULATOR_ID].getPosition().x - mf.manipulator[MovableFunction.LEFT_MANIPULATOR_ID].getPosition().x;
            if(manipulator_dif < stick_to_grid_distance.x/3.){

                if(i == active_function){
                    int right = i+1;
                    int left = i-1;
                    if (isValidFunctionId(left) && isValidFunctionId(right)) {
                        // expect left to adjust since we moved right
                        Pos3d manipulator_pos = getMovableFunction(right).manipulator[MovableFunction.LEFT_MANIPULATOR_ID].getPosition();
                        getMovableFunction(left).manipulator[MovableFunction.RIGHT_MANIPULATOR_ID].setPosition(manipulator_pos);
                        getMovableFunction(left).setFunctionGivenManipulators();
                    }
                    active_function = INVALID_FUNCTION;
                }
                if(i-1 == active_function){
                    int right = i+1;
                    int left = i-1;
                    if (isValidFunctionId(left) && isValidFunctionId(right)) {
                        // expect right to adjust since we moved left
                        Pos3d manipulator_pos = getMovableFunction(left).manipulator[MovableFunction.RIGHT_MANIPULATOR_ID].getPosition();
                        getMovableFunction(right).manipulator[MovableFunction.LEFT_MANIPULATOR_ID].setPosition(manipulator_pos);
                        getMovableFunction(right).setFunctionGivenManipulators();
                    }
                }

                removeFunction(i);
                removed = true;
            }
        }
        if(removed){
            synchronizeFunctions();
        }
    }

    /*!
     * \brief endTouch must be called abd will pass that call to the active function to
     * signal that the user stopped touching the screen.
     */
    public void manuelEndTouch() {
        hold_start = FIRST_CONTACT;
    }

    public void executeCommand(CMD cmd){
        if(sensitivity(cmd)){
            for(MovableFunction mf : functions){
                mf.executeCommand();
            }
        }
        removeZeroWidthFunctions();
        scale();
    }

    /*!
     * \brief sensitivity sets the step width at which the active function can be manipulated, it also
     * returns weather a manipulation shall occur according to the length of the touch.
     * It discretize the manipulations depending on the holding time.
     * \return true if it is time for manipulation.
     */
    private boolean sensitivity(CMD cmd){
        if(hold_start == FIRST_CONTACT){
            hold_start = System.currentTimeMillis();
            active_tick_start = 0;
        }
        long now = System.currentTimeMillis();
        long duration = now - hold_start;

        long wait = MEDIUM_TICK_DURATION_MS;
        double multiply = 1.;

        // depending on how long the user holds down, we wait x ms until we let the next change happen.
        if(duration < SLOW_TICK_DURATION_HOLD_MS){
            wait = SLOW_TICK_DURATION_MS;
        }
        if(duration > MEDIUM_TICK_DURATION_HOLD_MS){
            multiply = 10.;
        }

        // update the command for all functions
        Pos3d command = stick_to_grid_distance;
        if(stick_to_grid_distance.norm() < Utility.EPSILON_D){
            command = current_grid_distance;
        }
        for(MovableFunction mf : functions){
            mf.setCommand(cmd, Pos3d.mul(command, multiply));
        }

        duration = now - active_tick_start;
        if(duration > wait){
            active_tick_start = now;
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

        // we have to split the drawing from function and manipulator since the
        // drawing order needs to be from far to near
        function_access.LOCK(1);
        for (MovableFunction f : functions) {
            f.drawFunction(gl);
        }
        for (MovableFunction f : functions) {
            f.drawManipulators(gl);
        }
        function_access.UNLOCK();

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
            final double ROUNDING_POINT_FINE_GRID = 0.95;
            // e.g a span of 4 -> log(4) = 0.6 ::> roundAT(0.6, 0.75) = 0 -> grid partition is 10^0 = 0.1
            // e.g a span of 8 -> log(8) = 0.9 ::> roundAT(0.9, 0.75) = 1 -> grid partition is 10^1 = 1
            double span = system_viewport.width()*is_x_d + system_viewport.height()*is_y_d;
            double log_span = Math.log10(span);
            double log_span_rounded = Utility.roundAT(log_span,ROUNDING_POINT_FINE_GRID);
            double grid_power = Math.pow(10,(int) log_span_rounded - 1);
            ArrayList <DrawableRectangle> grid = getGrid(i);

            // For fine grids, only display every tenth grid, else display every grid.

            double tick_power = (span/grid_power > NUM_TICK_LABELS)?10*grid_power:grid_power;
            double modulo_integer_multiplicator = 1;
            if(tick_power < 1){
                modulo_integer_multiplicator = 1./tick_power;
            }
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
            for(DrawableRectangle grid_line : grid){
                boolean inside = ((pos_iterator.x < system_viewport.max.x + Utility.EPSILON_D) || is_y) &&
                        ((pos_iterator.y < system_viewport.max.y + Utility.EPSILON_D) || is_x);
                Pos3d translation_openGL = system_2_open_gl.transform(pos_iterator);
                // translation_openGL is now absolute position
                Pos3d rel_translation_openGL = absolutePos2relativePos(translation_openGL);
                rel_translation_openGL.z += Globals.GRID_Z_ELEVATION; // to be sure that grid is above bg.
                grid_line.setRelativePositionToParent(rel_translation_openGL);

                double d_tick = pos_iterator.x * is_x_d + pos_iterator.y * is_y_d;
                d_tick = Utility.roundAtDecimal(d_tick, grid_power);

                // Modulo is not very precise: using doubles the results precision is a bit better than float.
                float remainder = (float) (Math.abs(d_tick*modulo_integer_multiplicator)%(tick_power*modulo_integer_multiplicator));
                boolean draw_tick = (tick_id_counter < NUM_TICK_LABELS && remainder < Utility.EPSILON_F);

                if(inside){
                    grid_line.setColor(grid_color);

                    if(draw_tick){
                        grid_line.setWidth((float) (GRID_WIDTH_TICK*is_x_d + grid_line.getWidth()*is_y_d));
                        grid_line.setHeight((float) (GRID_WIDTH_TICK*is_y_d + grid_line.getHeight()*is_x_d));

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
        stick_to_grid_distance = new Pos3d(grid);
        stick_to_grid_distance.abs();
        for(MovableFunction mf : functions){
            mf.stickToGrid(stick_to_grid_distance);
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
        float ROTATION = (float) -(Math.PI/4);
        Pos3d pos = new Pos3d(coord_pos);
        pos.z = Globals.FUNCTION_Z_ELEVATION;
        DrawableString ds = new DrawableString(parent.context, pos, FONT_SIZE, FONT_SIZE_PIX, tick);
        ds.setRotation(0);
        Pos3d mv = Pos3d.Zero();
        if(direction == 1){
            mv.x = -ds.getWidth() - AXIS_WIDTH/2;
            mv.y = - 0.5* FONT_SIZE;
            ds.move(mv);
            y_ticks.set(id, ds);
        }else{
            ds.setRotation(ROTATION);
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
    private boolean setViewportSystem(){
        double margin_top = 0;
        if(add_function_button.size() > 1){
            int button_height = (int) add_function_button.get(0).height;
            margin_top = Utility.screen2OpenGly(button_height) + static_axis_offset[3].y + MovableFunction.DEFAULT_MANIPULATOR_RADIUS;
        }

        ViewPort old = new ViewPort(system_viewport);
        // always have 1 grid distance to the border
        system_viewport.min.x = getAllFunctionsMinX();
        system_viewport.min.y = getAllFunctionsMinY();
        system_viewport.max.x = getAllFunctionsMaxX();
        system_viewport.max.y = getAllFunctionsMaxY();
        double dif_x = system_viewport.max.x - system_viewport.min.x;
        double dif_y = system_viewport.max.y - system_viewport.min.y;

        if(margin_top > 0){
            double y_open_gl_dif = openGL_viewport.max.y - openGL_viewport.min.y;
            // cross-multiplication
            double additional_height = ((y_open_gl_dif + margin_top)/y_open_gl_dif)*dif_y - dif_y;
            system_viewport.max.y += additional_height;
            dif_y += additional_height;
        }
        // avoid function at border
        system_viewport.min.x -= dif_x/20;
        system_viewport.max.x += dif_x/20;
        system_viewport.min.y -= dif_y/20;
        system_viewport.max.y += dif_y/20;

        dif_x = system_viewport.max.x - system_viewport.min.x;
        dif_y = system_viewport.max.y - system_viewport.min.y;

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

        return ViewPort.equals(old, system_viewport);
    }

    /*!
     * \brief setViewPortOpenGL Set the viewport of the Coordinate System in open GL coordinates according to position width and height.
     */
    private boolean setViewPortOpenGL(){
        ViewPort old = new ViewPort(openGL_viewport);
        Pos3d position_ = getPosition();
        float width = getWidth();
        float height = getHeight();

        Pos3d view_port_top_right = new Pos3d(position_);
        view_port_top_right.add(new Pos3d(width, height, 0));
        view_port_top_right.sub(static_axis_offset[3]);
        Pos3d view_port_left_bot = new Pos3d(position_);
        view_port_left_bot.add(static_axis_offset[2]);
        openGL_viewport = new ViewPort(view_port_left_bot, view_port_top_right);

        return ViewPort.equals(old, openGL_viewport);
    }

    /*!
     * \brief scale rescales the coordinate system, calculating the homography between openGL and
     * coordinates, calculates the grid and redraws all functions.
     */
    private void scale(){
        boolean new_system_view = setViewportSystem();
        boolean new_gl_view = setViewPortOpenGL();
        if(!new_system_view && !new_gl_view){
            // no scaling needed
            return;
        }

        //Log.d("scale", "system_viewport: " + system_viewport.toString());
        //Log.d("scale", "openGL_viewport: " + openGL_viewport.toString());

        open_gl_2_system.calculateHomography2DNoRotation(openGL_viewport.min, system_viewport.min, openGL_viewport.max, system_viewport.max);
        system_2_open_gl.calculateHomography2DNoRotation(system_viewport.min, openGL_viewport.min, system_viewport.max, openGL_viewport.max);

        adjustGrid();
        rescaleAllFunctions();
        placeAddFunctionButtons();
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

    private void removeFunction(int id){
        if(isValidFunctionId(id)){
            function_access.LOCK(0);
            functions.remove(id);
            function_access.UNLOCK();
            if(isValidFunctionId(id) && isValidFunctionId(id-1)){
                Pos3d new_pos = getMovableFunction(id-1).manipulator[MovableFunction.RIGHT_MANIPULATOR_ID].getPosition();
                Pos3d old_pos = getMovableFunction(id).manipulator[MovableFunction.LEFT_MANIPULATOR_ID].getPosition();
                Pos3d dp = Pos3d.sub(new_pos,old_pos);
                getMovableFunction(id).moveTail(dp, true);
            }
            synchronizeFunctions();
            scale();
        }
    }

    /*!
     * \brief addFunction Adds a function to be displayed
     * \param f The function
     * \param min The min x value to be displayed
     * \param max The max x value to be displayed
     * \return an index over which the function can be deleted/locked etc...
     */
    public int addFunction(Function f, double min, double max){
        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);
        MovableFunction mf = new MovableFunction(parent.context, relative_position, f, min, max, system_2_open_gl);
        mf.setLocked(true);
        mf.stickToGrid(stick_to_grid_distance);
        function_access.LOCK(2);
        functions.add(mf);
        function_access.UNLOCK();

        synchronizeFunctions();
        scale();

        return functions.size()-1;
    }

    public int insertFunction(int id, Function f, double length){
        if(functions.size() <= id){
            // add to the end
            double min = 0;
            if(functions.size() > 0) {
                min = getMovableFunction(functions.size() - 1).getFunctionMaxX();
            }
            return addFunction(f, min, min+length);
        }

        double min = getMovableFunction(id).getFunctionMinX();
        // first shift all functions about length
        Pos3d dp = new Pos3d(length,0,0);
        getMovableFunction(id).moveTail(dp, false);

        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);
        MovableFunction mf = new MovableFunction(parent.context, relative_position, f, min, min+length, system_2_open_gl);
        mf.setLocked(true);
        mf.stickToGrid(stick_to_grid_distance);
        function_access.LOCK(2);
        functions.insertElementAt(mf, id);
        function_access.UNLOCK();

        synchronizeFunctions();
        scale();
        return functions.size()-1;
    }

    private void replaceFunction(int index, Function f, double length){
        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);
        MovableFunction mf = getMovableFunction(index);
        double min = mf.getFunctionMinX();
        double max = min + length;
        if(isValidFunctionId(index + 1)) {
            // we have to move all functions to the right to fit length
            double prev_max = mf.getFunctionMaxX();
            double dx_function = max - prev_max;
            Pos3d dp = new Pos3d(dx_function,0,0);
            getMovableFunction(index+1).moveTail(dp, false);
        }

        MovableFunction new_mf = new MovableFunction(parent.context, relative_position, f, min, max, system_2_open_gl);
        new_mf.stickToGrid(stick_to_grid_distance);
        new_mf.setLocked(true);
        function_access.LOCK(3);
        functions.set(index, new_mf);
        function_access.UNLOCK();

        synchronizeFunctions();
        scale();
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
        if(functions.size() > index && index > -1){
            return functions.get(index);
        }
        throw new IllegalArgumentException( "MovableCoordinateSystem::getMovableFunction: given index is not a function set prior");
    }

    /*!
     * \brief setFunctionLocked (un)lock a function to make it manipulable via manipulator.
     * \param l look true/false
     * \param function_index The function to lock/unlock
     */
    private void setFunctionLocked(boolean l, int function_index){
        getMovableFunction(function_index).setLocked(l);
    }

    private boolean isValidFunctionId(int id){
        if(id < functions.size() && id > -1){
            return true;
        }
        return false;
    }

    public void synchronizeFunctions(){
        if(functions.size() == 0){
            return;
        }
        // decouple begin
        functions.get(0).unregisterLeftCoupledFunction();
        for(int i = 0; i < functions.size() - 1; i++){
            MovableFunction.registerCoupledFunctionPair(functions.get(i), functions.get(i+1));
        }
        // decouple end
        functions.get(functions.size()-1).unregisterRightCoupledFunction();
    }

    private ArrayList<String> getSupportedFunctions(boolean add_delete){
        ArrayList<String> supported = new ArrayList<String>(){{
            add(activity.getString(R.string.linear_function));
            add(activity.getString(R.string.quadratic_function_left));
            add(activity.getString(R.string.quadratic_function_right));
            add(activity.getString(R.string.sigmoid_function));
        }};
        if(add_delete){
            supported.add(activity.getString(R.string.delete_function));
        }
        return supported;
    }

    private SUPPORTED_FUNCTION FunctionString2Enum(String function_class){
        if(activity.getString(R.string.linear_function).equals(function_class)){
            return SUPPORTED_FUNCTION.LINEAR;
        }
        if(activity.getString(R.string.quadratic_function_left).equals(function_class)){
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_LEFT;
        }
        if(activity.getString(R.string.quadratic_function_right).equals(function_class)){
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_RIGHT;
        }
        if(activity.getString(R.string.sigmoid_function).equals(function_class)){
            return SUPPORTED_FUNCTION.SIGMOID;
        }
        return SUPPORTED_FUNCTION.UNKNOWN;
    }

    private SUPPORTED_FUNCTION FunctionClass2Enum(Object unknown_function_class){
        // sigmoid must be checked for LinearFunction since all sigmoids are linear functions (see inheritance)
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.SigmoidFunction) {
            return SUPPORTED_FUNCTION.SIGMOID;
        }
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.LinearFunction ||
                unknown_function_class instanceof de.jakobimatrix.intervallometer.ConstantFunction) {
            return SUPPORTED_FUNCTION.LINEAR;
        }
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.QuadraticFunctionExtremaRight) {
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_RIGHT;
        }
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.QuadraticFunctionExtremaLeft) {
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_LEFT;
        }
        return SUPPORTED_FUNCTION.UNKNOWN;
    }

    private void placeAddFunctionButtonsActivity(){
        int num_functions = functions.size();
        int num_buttons = add_function_button.size();
        // we always have 1 button more than we have functions
        int diff = num_buttons - num_functions - 1;
        if(diff > 0){
            // more buttons than needed
            int id = add_function_button.size() - 1;
            for(int i = 0; i < diff; i++){
                // remove the last diff buttons
                if(id > -1){
                    add_function_button.get(id).delete();
                    add_function_button.remove(id);
                }
                id--;
            }
        }else if(diff < 0){
            // we have diff to less buttons
            createAddFunctionButton(-diff);
        }

        //now set the correct positions
        // we will always have the right amount of buttons
        // The ButtonOrigin is center-top
        if(num_functions == 0){
            // no function, set the plus in the center of the coord system
            Pos3d center = parent.getPosition();
            center.x += getWidth()/2.;
            center.y += getHeight()/2.;
            Pos3d screen_pos = Utility.openGl2Screen(center);
            screen_pos.y -= add_function_button.get(0).height/2; // origin is top/center
            add_function_button.get(0).place((int) Math.round(screen_pos.x), Math.round((int) screen_pos.y));
        }else{
            double y_pos = system_viewport.max.y;
            for(int i = 0; i < num_functions; i++){
                MovableFunction mf = functions.get(i);
                double max_x = mf.getFunctionMaxX();
                Pos3d max = new Pos3d(max_x, y_pos, 0);
                Pos3d open_gl_pos = system_2_open_gl.transform(max);
                Pos3d screen_pos = Utility.openGl2Screen(open_gl_pos);
                add_function_button.get(i+1).place((int) Math.round(screen_pos.x), Math.round((int) screen_pos.y));
            }
            // the very first plus is at the first function min
            MovableFunction mf = functions.get(0);
            double min_x = mf.getFunctionMinX();
            Pos3d min = new Pos3d(min_x, y_pos, 0);
            Pos3d open_gl_pos = system_2_open_gl.transform(min);
            Pos3d screen_pos = Utility.openGl2Screen(open_gl_pos);
            add_function_button.get(0).place((int) Math.round(screen_pos.x), Math.round((int) screen_pos.y));
        }
    }

    private void placeAddFunctionButtons(){
        // all ui stuff must be done by activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                placeAddFunctionButtonsActivity();
            }
        });
    }

    private void createAddFunctionButtonActivity(int num_buttons_2_add){
        while(num_buttons_2_add > 0){
            DynamicButton dynBut = new DynamicButton(parent.context, rl, 0, 0, 0, 0);
            dynBut.setColor(0xFFFFFFFF);
            dynBut.setBackground(R.drawable.ic_plus_button);
            dynBut.setTopCenterOrigin();
            final int id = add_function_button.size();

            dynBut.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openAddFunctionDialog(id, false);
                    hideAllButtons();
                }
            });
            add_function_button.add(dynBut);
            num_buttons_2_add--;
        }
    }

    private void hideAllButtons(){
        // all ui stuff must be done by activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideAllButtonsActivity();
            }
        });
    }

    private void hideAllButtonsActivity(){
        for(DynamicButton db :add_function_button){
            db.place(-1000, -1000); // somewhere far away
        }
    }

    private void createAddFunctionButton(final int num_buttons_2_add){
        // all ui stuff must be done by activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAddFunctionButtonActivity(num_buttons_2_add);
            }
        });
    }

    private void removeAllButtons(){
        // all ui stuff must be done by activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(DynamicButton db : add_function_button){
                    db.delete();
                }
                add_function_button.clear();
            }
        });
    }

    private void prepareAdFunctionDialogActivity(final int id, final boolean edit_function){

        Button apply = (Button) activity.findViewById(Globals.EDIT_FUNCTION_APPLY_BTN);
        Button cancel = (Button) activity.findViewById(Globals.EDIT_FUNCTION_CANCEL_BTN);

        final Spinner function_chooser = (Spinner) activity.findViewById(Globals.EDIT_FUNCTION_CHOOSER);
        final EditText num_pic_chooser = (EditText) activity.findViewById(Globals.EDIT_FUNCTION_NUM_PICS_INPUT);

        ArrayList<String> spinner_array = getSupportedFunctions(edit_function);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (activity.getBaseContext(), android.R.layout.simple_spinner_item, spinner_array);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        function_chooser.setAdapter(adapter);

        // Write either default values or current values in the function_chooser and the num_pic_chooser.
        if(edit_function){
            MovableFunction mf = getMovableFunction(id);
            Integer num_pics = (int) Math.round(mf.getFunctionMaxX() - mf.getFunctionMinX());
            num_pic_chooser.setText(num_pics.toString(), TextView.BufferType.EDITABLE);
            SUPPORTED_FUNCTION current_function_type = FunctionClass2Enum(mf.getFunction());
            int position = 0;
            for(int i = 0; i < spinner_array.size(); i++){
                SUPPORTED_FUNCTION sf = FunctionString2Enum(spinner_array.get(i));
                if(sf == current_function_type){
                    position = i;
                    break;
                }
            }
            function_chooser.setSelection(position,false);
        }else{
            // default number
            num_pic_chooser.setText("500");
            function_chooser.setSelection(0,false);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeAddFunctionActivity();
                placeAddFunctionButtons();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String function_string = (String) function_chooser.getSelectedItem();
                SUPPORTED_FUNCTION selected_function = FunctionString2Enum(function_string);
                int num_pictures = Integer.parseInt(num_pic_chooser.getText().toString());
                functionCreate(id, edit_function, selected_function, num_pictures);
                closeAddFunctionActivity();
            }
        });

        openAddFunctionDialogActivity();
    }

    private void functionCreate(int id, boolean edit, SUPPORTED_FUNCTION type, int length){
        Pos3d left = Pos3d.Zero();
        Pos3d right = Pos3d.Zero();

        if(edit){
            if(!isValidFunctionId(id)){
                Toast toast=Toast. makeText(activity.getApplicationContext(),activity.getString(R.string.something_went_wrong),Toast. LENGTH_SHORT);
                toast.show();
            }
            MovableFunction mf = getMovableFunction(id);
            left = mf.manipulator[MovableFunction.LEFT_MANIPULATOR_ID].getPosition();
            right = mf.manipulator[MovableFunction.RIGHT_MANIPULATOR_ID].getPosition();
            Homography2d h = mf.getDrawableFunction().f2openGL;
            left = h.invTransform(left);
            right = h.invTransform(right);
            right.x = left.x + length;
        }else{
            if(id == 0){
                // new function at begin
                if(functions.size() > 0){
                    left.y = getMovableFunction(0).getFunction().f(0);
                }else{
                    left.y = 1000; // TODO DEFAULT 1 sec delay
                }
                right.y = left.y;
                right.x = length;
            }else if(id == functions.size()){
                // new function at end
                MovableFunction mf = getMovableFunction(functions.size()-1);
                left = mf.manipulator[MovableFunction.RIGHT_MANIPULATOR_ID].getPosition();
                Homography2d h = mf.getDrawableFunction().f2openGL;
                left = h.invTransform(left);
                right.y = left.y;
                right.x = left.x + length;
            }else {
                // insert function between two other functions
                MovableFunction mf = getMovableFunction(id);
                left = mf.manipulator[MovableFunction.LEFT_MANIPULATOR_ID].getPosition();
                Homography2d h = mf.getDrawableFunction().f2openGL;
                left = h.invTransform(left);
                right = new Pos3d(left);
                right.x = left.x + length;
            }
        }

        Function new_function;
        switch (type){
            case LINEAR:
                new_function = new LinearFunction(left,right);
                break;
            case QUADRATIC_EXTREMA_LEFT:
                new_function = new QuadraticFunctionExtremaLeft(left,right);
                break;
            case QUADRATIC_EXTREMA_RIGHT:
                new_function = new QuadraticFunctionExtremaRight(left,right);
                break;
            case UNKNOWN:
                // delete function
                removeFunction(id);
                return;
            default:
                // Why Java not able to see, that ENUM has no default?!
                throw new IllegalStateException("Unexpected value: " + type);
        }
        if(edit){
            replaceFunction(id, new_function, length);
        }else{
            insertFunction(id, new_function, length);
        }
    }

    private void closeAddFunctionActivity(){
        setLocked(false);

        LinearLayout dialog_layout = (LinearLayout) activity.findViewById(Globals.EDIT_FUNCTION_LAYOUT);
        RelativeLayout.LayoutParams params_dialog_layout = (RelativeLayout.LayoutParams) dialog_layout.getLayoutParams();
        params_dialog_layout.width = 0;
        params_dialog_layout.height = 0;
        dialog_layout.setLayoutParams(params_dialog_layout);
    }

    private void openAddFunctionDialogActivity(){
        LinearLayout dialog_layout = (LinearLayout) activity.findViewById(Globals.EDIT_FUNCTION_LAYOUT);
        RelativeLayout.LayoutParams params_dialog_layout = (RelativeLayout.LayoutParams) dialog_layout.getLayoutParams();
        params_dialog_layout.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params_dialog_layout.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        dialog_layout.setLayoutParams(params_dialog_layout);
    }

    private void openAddFunctionDialog(final int id, final boolean edit_function){
        // all ui stuff must be done by activity
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLocked(true);
                prepareAdFunctionDialogActivity(id, edit_function);
            }
        });
    }


    final static int INVALID_FUNCTION = -10;
    int active_function = INVALID_FUNCTION;
    Vector<MovableFunction> functions = new Vector<>();
    Vector<DynamicButton> add_function_button = new Vector<>();
    Lock function_access = new Lock("MovableCoordSys");
    boolean toggle_not = false;

    // visual
    ViewPort openGL_viewport;
    // values on axis
    ViewPort system_viewport;

    Activity activity;
    RelativeLayout rl;

    Pos3d current_grid_distance = Pos3d.Zero();
    long hold_start = FIRST_CONTACT;
    long active_tick_start = 0;
    final static long FIRST_CONTACT = -1;
    final static long SLOW_TICK_DURATION_MS = 750;
    final static long SLOW_TICK_DURATION_HOLD_MS = SLOW_TICK_DURATION_MS;
    final static long MEDIUM_TICK_DURATION_MS = 200;
    final static long MEDIUM_TICK_DURATION_HOLD_MS = SLOW_TICK_DURATION_HOLD_MS + 10*MEDIUM_TICK_DURATION_MS;

    boolean lifted = true;
    long last_tap_edit_function = 0;
    final static long HOLD_TO_EDIT_FUNC_DURATION_MS = 750;

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
    final static int FONT_SIZE_PIX = 100;

    Pos3d [] static_grid_offset_openGL = new Pos3d[2];
    Pos3d [] static_axis_offset = new Pos3d[4];

    final static int NUM_GRID_STRIPES = 100;
    final static int NUM_TICK_LABELS = 15;
    final static float AXIS_WIDTH = 0.2f;
    final static float GRID_WIDTH = 0.01f;
    final static float GRID_WIDTH_TICK = 0.04f;

    final static float MIN_WIDTH = 1.f;
    final static float MIN_HEIGHT= 10.f;

    final static float MARGIN_TOP_RIGHT = 0.25f;
    float margin_bot = 0.55f;
    float margin_left = 0.65f;

    Pos3d stick_to_grid_distance = Pos3d.Zero();

    final static ColorRGBA BG_DEFAULT_COLOR = new ColorRGBA(0.7,0.7,0.7,1);
    final static ColorRGBA BG_DEFAULT_GRID_COLOR = new ColorRGBA(0.2,0.2,0.2,1);
    ColorRGBA grid_color = BG_DEFAULT_GRID_COLOR;
}
