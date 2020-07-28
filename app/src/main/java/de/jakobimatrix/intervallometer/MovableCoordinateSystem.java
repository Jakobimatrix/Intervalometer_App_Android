package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

public class MovableCoordinateSystem extends Movable {
    public MovableCoordinateSystem(Context context_, Pos3d position_, float width, float height) {
        super(new DrawableRectangle(context_, new Pos3d(position_), width, height));
        DrawableRectangle bg = (DrawableRectangle) parent;
        bg.setBotLeftORIGEN();
        bg.setColor(BG_DEFAULT_COLOR);

        // Move everything such that the axis (arrows) are inside the bg (rectangle).
        // Otherwise the arrows would be half off the bg.
        static_axis_offset[0] = new Pos3d(0, AXIS_WIDTH/2f, 0);
        static_axis_offset[1] = new Pos3d(AXIS_WIDTH/2f, 0, 0);
        static_axis_offset[2] = new Pos3d(AXIS_WIDTH/2f, AXIS_WIDTH/2f, 0);

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
            y_grid.add(new DrawableRectangle(context_, new Pos3d(position_), width-AXIS_WIDTH, GRID_WIDTH));
            x_grid.add(new DrawableRectangle(context_, new Pos3d(position_), GRID_WIDTH, height-AXIS_WIDTH));
            y_grid.get(i).setLeftCenterORIGEN();
            x_grid.get(i).setBotCenterORIGEN();
            x_grid.get(i).setColor(ColorRGBA.TRANSPARENT);
            y_grid.get(i).setColor(ColorRGBA.TRANSPARENT);
            parent.adChild(y_grid.get(i));
            parent.adChild(x_grid.get(i));
        }

        x_axis.setColor(grid_color);
        y_axis.setColor(grid_color);

        // we lock the functions individually
        is_locked = false;
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        // Todo we have to deal with Manipulators going over the edge
        if(true || parent.isWithin(position_)) {
            for (int i = 0; i < functions.size(); i++) {
                if (functions.get(i).isWithin(position_)) {
                    active_function = i;
                    return true;
                }
            }
        }
        active_function = INVALID_FUNCTION;
        return false;
    }

    @Override
    public void setPosition(Pos3d position_){
        if(isValidFUnctionId(active_function)){
            functions.get(active_function).setPosition(position_);
            scale(); // TODO we could check if scaling in necessary
        }
    }

    @Override
    public void move(Pos3d dp){
        if(isValidFUnctionId(active_function)){
            functions.get(active_function).move(dp);
            scale(); // TODO we could check if scaling in necessary
        }
    }

    @Override
    public void endTouch() {
        for(MovableFunction df: functions){
            df.endTouch();
        }
    }

    public void setGridPosition(Pos3d position_){
        parent.setPosition(position_);
        scale();
    }

    @Override
    public void draw(GL10 gl){
        parent.draw(gl);
        for(MovableFunction f:functions){
            f.draw(gl);
        }
    }

    public void setBackgroundColor(ColorRGBA color){
        parent.setColor(color);
    }

    private void adjustGrid(){
        for(int i = 0; i < 2; i++){
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
            double log_span = Utility.roundAT(Math.log10(span),ROUNDING_POINT);
            double grid_power = Math.pow(10,(int) log_span - 1);
            ArrayList <DrawableRectangle> grid = getGrid(i);
            /* truncates e.g.
            // min = 123.456
            // power = 0.1
            // start = 123.4 + 0.1
            */
            double start = system_viewport.min.x*is_x_d + system_viewport.min.y*is_y_d;
            start = (double) Math.ceil(start / grid_power) * grid_power;
            // build the grid, starting at start, adding d_pos every iteration

            Pos3d pos_iterator = new Pos3d(start*is_x_d,start*is_y_d, 0);
            // add the offset of the direction we don't iterate through.
            pos_iterator.add(new Pos3d(system_viewport.min.x*is_y_d,system_viewport.min.y*is_x_d, 0));
            Pos3d d_pos_iterator = new Pos3d(grid_power*is_x_d, grid_power*is_y_d, 0);

            for(DrawableRectangle grid_line : grid){
                boolean inside = (!(pos_iterator.x > system_viewport.max.x) || is_y) &&
                        (!(pos_iterator.y > system_viewport.max.y) || is_x);
                Pos3d translation_openGL = system_2_open_gl.transform(pos_iterator);
                // translation_openGL is now absolute position
                Pos3d rel_translation_openGL = absolutePos2relativePos(translation_openGL);
                rel_translation_openGL.z += Globals.GRID_Z_ELEVATION; // to be sure that grid is above bg.
                grid_line.setRelativePositionToParent(rel_translation_openGL);

                pos_iterator.add(d_pos_iterator);
                if(inside){
                    grid_line.setColor(grid_color);
                }else{
                    grid_line.setColor(ColorRGBA.TRANSPARENT);
                }
            }
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

    private void setViewportSystem(){
        system_viewport.min.x = getAllFunctionsMinX();
        system_viewport.min.y = getAllFunctionsMinY();
        system_viewport.max.x = getAllFunctionsMaxX();
        system_viewport.max.y = getAllFunctionsMaxY();
        // double dif_x = system_viewport.max.x - system_viewport.min.x;
        double dif_y = system_viewport.max.y - system_viewport.min.y;
        // avoid no height if function is a constant
        if(dif_y < MIN_HEIGHT){
            double center = system_viewport.min.y + dif_y/2.0;
            system_viewport.min.y = center - MIN_HEIGHT/2.0;
            system_viewport.max.y = center + MIN_HEIGHT/2.0;
        }
    }

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
        double max = Double.MIN_VALUE;
        for(MovableFunction f: functions){
            max = Math.max(max, f.getFunctionMaxX());
        }
        return max;
    }

    private double getAllFunctionsMinX(){
        double min = Double.MAX_VALUE;
        for(MovableFunction f: functions){
            min = Math.min(min, f.getFunctionMinX());
        }
        return min;
    }

    private double getAllFunctionsMaxY(){
        double max = Double.MIN_VALUE;
        for(MovableFunction f: functions){
            max = Math.max(max, f.getFunctionMaxY());
        }
        return max;
    }

    private double getAllFunctionsMinY(){
        double min = Double.MAX_VALUE;
        for(MovableFunction f: functions){
            min = Math.min(min, f.getFunctionMinY());
        }
        return min;
    }

    public int addFunction(Function f, double min, double max){
        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);
        MovableFunction mf = new MovableFunction(parent.context, relative_position, f, min, max, system_2_open_gl);
        mf.setLocked(true);
        functions.add(mf);

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

        scale();
        rescaleAllFunctions();
    }

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

    public void setFunctionLocked(boolean l, int function_index){
        getMovableFunction(function_index).setLocked(l);
    }

    @Override
    public void setLocked(boolean l){
        throw new IllegalArgumentException( "MovableCoordinateSystem::setLocked: Don't use this function. Lock individual functions with setFunctionLocked().");
    }

    private boolean isValidFUnctionId(int id){
        if(id < functions.size() && id > INVALID_FUNCTION){
            return true;
        }
        return false;
    }

    final static int INVALID_FUNCTION = -1;
    int active_function = INVALID_FUNCTION;
    Vector<MovableFunction> functions = new Vector<>();

    // visual
    ViewPort openGL_viewport;

    // values on axis
    ViewPort system_viewport;

    DrawableArrow x_axis;
    DrawableArrow y_axis;

    Homography2d open_gl_2_system = new Homography2d();
    Homography2d system_2_open_gl = new Homography2d();

    ArrayList <DrawableRectangle> x_grid;
    ArrayList <DrawableRectangle> y_grid;

    Pos3d [] static_grid_offset_openGL = new Pos3d[2];
    Pos3d [] static_axis_offset = new Pos3d[3];

    final static int NUM_GRID_STRIPES = 65;
    final static float AXIS_WIDTH = 0.2f;
    final static float GRID_WIDTH = 0.02f;

    final static float MIN_WIDTH = 1.f;
    final static float MIN_HEIGHT= 10.f;

    final static ColorRGBA BG_DEFAULT_COLOR = new ColorRGBA(0.7,0.7,0.7,1);
    final static ColorRGBA BG_DEFAULT_GRID_COLOR = new ColorRGBA(0,0,0,1);
    ColorRGBA grid_color = BG_DEFAULT_GRID_COLOR;
}
