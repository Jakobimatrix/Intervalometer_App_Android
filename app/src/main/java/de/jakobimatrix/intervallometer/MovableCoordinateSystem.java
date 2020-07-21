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
        Pos3d view_port_top_right = new Pos3d(position_);
        view_port_top_right.add(new Pos3d(width, height, 0));
        openGL_viewport = new ViewPort(new Pos3d(position_), view_port_top_right);
        system_viewport = new ViewPort(Pos3d.Zero(), new Pos3d(0,0,GRID_ELEVATION_Z));

        x_axis = new DrawableArrow(context_,new Pos3d(position_), width, AXIS_WIDTH);
        x_axis.setRotationRIGHT();
        y_axis = new DrawableArrow(context_,new Pos3d(position_), height, AXIS_WIDTH);
        y_axis.setRotationUP();

        static_axis_offset[0] = new Pos3d(0, AXIS_WIDTH/2f, GRID_ELEVATION_Z);
        static_axis_offset[1] = new Pos3d(AXIS_WIDTH/2f, 0, GRID_ELEVATION_Z);
        static_axis_offset[2] = new Pos3d(AXIS_WIDTH/2f, AXIS_WIDTH/2f, 0);

        parent.adChild(x_axis, static_axis_offset[0]);
        parent.adChild(y_axis, static_axis_offset[1]);

        x_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES); // ||
        y_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES); // ==

        for(int i = 0; i < NUM_GRID_STRIPES; i++){
            y_grid.add(new DrawableRectangle(context_, new Pos3d(position_), width, GRID_WIDTH));
            x_grid.add(new DrawableRectangle(context_, new Pos3d(position_), GRID_WIDTH, height));
            //y_grid.get(i).setLeftCenterORIGEN();
            //x_grid.get(i).setBotCenterORIGEN();
            parent.adChild(y_grid.get(i));
            parent.adChild(x_grid.get(i));
        }
        this.grid_color = grid_color;
        x_axis.setColor(grid_color);
        y_axis.setColor(grid_color);

        for(int i = 0; i < 4 ; i++) {
            debug_circle[i] = new DrawableCircle(context_, new Pos3d(position_), 0.1f);
            parent.adChild(debug_circle[i]);
        }
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        for(int i = 0; i < functions.size(); i++){
            if(functions.get(i).isWithin(position_)){
                active_function = i;
                return true;
            }
        }
        active_function = -1;
        return false;
    }

    @Override
    public void setPosition(Pos3d position_){
        if(active_function > 0){
            functions.get(active_function).setPosition(position_);
        }else{
            parent.setPosition(position_);
        }
        scale();
    }

    @Override
    public void move(Pos3d dp){
        if(active_function > 0){
            functions.get(active_function).move(dp);
        }else{
            parent.move(dp);
        }
        scale();
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
        Pos3d diag = system_viewport.diag();
        for(int i = 0; i < 2; i++){
            double span = diag.get(i);
            double log_span = Math.log10(span);
            grid_power[i] = Math.pow(10,(int) log_span - 1);
            ArrayList <DrawableRectangle> grid = getGrid(i);

            double is_x = (i == 0)?1:0;
            double is_y = (i == 1)?1:0;

            // truncates e.g.
            // min = 123.456
            // power = 0.1
            // start = 123.4 + 0.1
            double start = (double) Math.ceil(system_viewport.min.get(i) * grid_power[i]) / grid_power[i];
            // bulid the grid, starting at start, adding d_pos every iteration, adding the transition since the rectangles natural center is mid.
            Pos3d pos_iterator = new Pos3d(start*is_x,start*is_y, 0);
            Pos3d d_pos_iterator = new Pos3d(grid_power[0]*is_x, grid_power[1]*is_y, 0);

            for(DrawableRectangle grid_line : grid){
                Log.d("pos_iterator", pos_iterator.toString());
                boolean inside = (!(pos_iterator.x > system_viewport.max.x) || is_x != 1) &&
                        (!(pos_iterator.y > system_viewport.max.y) || is_y != 1);
                Pos3d translation_openGL = system_2_open_gl.transform(pos_iterator);
                Log.d("translation_openGL", translation_openGL.toString());
                Log.d("grid_line tx", grid_line.translation_x + "");
                Log.d("grid_line ty", grid_line.translation_y + "");
                Log.d("grid_line pos", grid_line.getPosition() + "");
                //translation_openGL.mul(-1.);
                grid_line.setRelativePositionToParent(translation_openGL);

                Log.d("grid_line tx", grid_line.translation_x + "");
                Log.d("grid_line ty", grid_line.translation_y + "");
                Log.d("grid_line pos", grid_line.getPosition() + "");

                pos_iterator.add(d_pos_iterator);
                if(inside){
                    grid_line.setColor(grid_color);
                }else{
                    // TODO It is not very efficient to calculate in Renderer() the transparent grid parts
                    grid_line.setColor(new ColorRGBA(1,1,1,1));
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
        adjustGrid();
    }

    private void scale(){
        system_viewport.min.x = getAllFunctionsMinX();
        system_viewport.min.y = getAllFunctionsMinY();
        system_viewport.max.x = getAllFunctionsMaxX();
        system_viewport.max.y = getAllFunctionsMaxY();

        Log.d("scale", "system_viewport: " + system_viewport.toString());
        Log.d("scale", "openGL_viewport: " + openGL_viewport.toString());

        open_gl_2_system.calculateHomography2DNoRotation(openGL_viewport.min, system_viewport.min, openGL_viewport.max, system_viewport.max);
        system_2_open_gl.calculateHomography2DNoRotation(system_viewport.min, openGL_viewport.min, system_viewport.max, openGL_viewport.max);

        adjustGrid();
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

    public void addFunction(int index, Function f, double min, double max, boolean replace){
        double mf_width = openGL_viewport.width() - AXIS_WIDTH/2f;
        double mf_height= openGL_viewport.height() - AXIS_WIDTH/2f;
        Pos3d relative_position = new Pos3d(parent.getPosition());
        relative_position.add(static_axis_offset[2]);

        //check if we need to rescale
        boolean need_rescale = false;
        // if we swap a function, we look if that old function was responsible for a maximum
        if(replace){
            MovableFunction old_func = functions.get(index);
            ViewPort old_function_view = old_func.getFunctionViewport();
            if(!system_viewport.isWithin(old_function_view)) {
                need_rescale = true;
            }
        }
        //
        double new_func_max = f.getMax(min, max);
        double new_func_min = f.getMin(min, max);
        ViewPort new_function_view = new ViewPort(new Pos3d(min, new_func_min, 0), new Pos3d(max, new_func_max, 0));
        if(!system_viewport.isWithin(new_function_view)){
            need_rescale = true;
            Log.d("NEED RESCALE", "TRUE");
        }

        MovableFunction mf = new MovableFunction(parent.context, relative_position, f, min, max, system_2_open_gl);
        if(index < functions.size()){
            if(replace){
                functions.set(index, mf);
            }else{
                functions.insertElementAt(mf, index);
            }
        }else{
            functions.add(mf);
        }
        if(need_rescale){
            scale();
            rescaleAllFunctions();
        }

    }

    private void rescaleAllFunctions(){
        for(MovableFunction mf:functions){
            mf.setHomography(system_2_open_gl);
        }
    }

    int active_function = -1;
    Vector<MovableFunction> functions = new Vector<>();

    // visual
    ViewPort openGL_viewport;

    // values on axis
    ViewPort system_viewport;

    double [] grid_power = {1,1};

    DrawableArrow x_axis;
    DrawableArrow y_axis;

    Homography2d open_gl_2_system = new Homography2d();
    Homography2d system_2_open_gl = new Homography2d();

    ArrayList <DrawableRectangle> x_grid;
    ArrayList <DrawableRectangle> y_grid;

    Pos3d [] static_grid_offset_openGL = new Pos3d[2];
    Pos3d [] static_axis_offset = new Pos3d[3];

    final static int NUM_GRID_STRIPES = 30;
    final static float AXIS_WIDTH = 0.2f;
    final static float GRID_WIDTH = 0.02f;
    final static double GRID_ELEVATION_Z = 0.001;

    ColorRGBA grid_color = BG_DEFAULT_GRID_COLOR;
    final static ColorRGBA TRANSPARENT = new ColorRGBA(0,0,0,0.0);
    final static ColorRGBA BG_DEFAULT_COLOR = new ColorRGBA(0.7,0.7,0.7,1);
    final static ColorRGBA BG_DEFAULT_GRID_COLOR = new ColorRGBA(0,0,0,1);

    DrawableCircle [] debug_circle = new DrawableCircle [4];
}
