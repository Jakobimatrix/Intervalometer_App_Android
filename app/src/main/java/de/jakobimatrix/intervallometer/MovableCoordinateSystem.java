package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

public class MovableCoordinateSystem extends Movable {
    public MovableCoordinateSystem(Context context_, Pos3d position_, float width_, float height_) {
        super(new DrawableRectangle(context_, new Pos3d(position_), width_, height_));
        DrawableRectangle bg = (DrawableRectangle) parent;
        bg.setBotLeftORIGEN();
        bg.setColor(BG_DEFAULT_COLOR);
        width = width_;
        height = height_;

        x_axis = new DrawableArrow(context_,new Pos3d(position_), width_, AXIS_WIDTH);
        x_axis.setRotationRIGHT();
        y_axis = new DrawableArrow(context_,new Pos3d(position_), height_, AXIS_WIDTH);
        y_axis.setRotationUP();

        parent.adChild(x_axis, new Pos3d(0, AXIS_WIDTH/2f, 0));
        parent.adChild(y_axis, new Pos3d(AXIS_WIDTH/2f, 0, 0));

        x_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES);
        y_grid = new ArrayList <DrawableRectangle>(NUM_GRID_STRIPES);


        for(int i = 0; i < NUM_GRID_STRIPES; i++){
            y_grid.add(new DrawableRectangle(context_, Pos3d.Zero(), width_, GRID_WIDTH));
            x_grid.add(new DrawableRectangle(context_, Pos3d.Zero(), GRID_WIDTH, height_));
            parent.adChild(y_grid.get(i));
            parent.adChild(x_grid.get(i));
        }

        setGridColor(grid_color);
        adjustGrid();
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        return false;
    }

    public void setBackgroundColor(ColorRGBA color){
        parent.setColor(color);
    }

    private Pos3d getOpenGLPosMax(){
        Pos3d p = new Pos3d(parent.getPosition());
        p.add(new Pos3d(width, height, 0));
        return p;
    }

    private Pos3d getOpenGLPosMin(){
        return new Pos3d(parent.getPosition());
    }

    public Pos3d coordinate2OpenGL(Pos3d coordinate){
        /*homography:
        *|openGL_x|   |f 0 tx|   |system_x|
        *|openGL_y| = |0 f ty| * |system_y|
        *|1       |   |0 0  1|   |1       |
        * f = scale
        * tx, ty = translation
        * */
        Pos3d scale_open_gl = Pos3d.sub(getOpenGLPosMax(), getOpenGLPosMin());
        Pos3d scale_system = new Pos3d(axis_max[0] - axis_min[0], axis_max[1] - axis_min[1], 0);

        double scale_x = scale_open_gl.x/scale_system.x;
        double scale_y = scale_open_gl.y/scale_system.y;

        Pos3d result = new Pos3d(coordinate.x*scale_x, coordinate.y*scale_y, 0);
        // result now scaled, calculate translation
        // for this we scale a known point (bot-left) from system and see how this point needs to be translated
        Pos3d bot_left_system_open_gl = new Pos3d(axis_min[0]*scale_x, axis_min[1]*scale_y,0);
        Pos3d translation = Pos3d.sub(bot_left_system_open_gl, getOpenGLPosMin());
        result.add(translation);
        return result;
    }

    public Pos3d openGL2Coordinate(Pos3d openGL){
        return new Pos3d(1,1,1);
    }

    private void adjustGrid(){
        // min,min start pos
        Pos3d [] standard_transition_openGL = {new Pos3d(0, height/2f, GRID_ELEVATION_Z) ,
                                        new Pos3d(width/2f, 0, GRID_ELEVATION_Z)};
        for(int i = 0; i < 2; i++){
            double span = axis_max[i] - axis_min[i];
            Log.d("span", span + "");
            double log_span = Math.log10(span);
            grid_power[i] = Math.pow(10,(int) log_span - 1);
            Log.d("grid_power[i]", grid_power[i] + "");
            ArrayList <DrawableRectangle> grid = getGrid(i);

            double is_x = (i == 0)?1:0;
            double is_y = (i == 1)?1:0;

            // truncates e.g.
            // min = 123.456
            // power = 0.1
            // start = 123.4 + 0.1
            double start = (double) Math.ceil(axis_min[i] * grid_power[i]) / grid_power[i];
            Log.d("start", start + "");
            // bulid the grid, starting at start, adding d_pos every iteration, adding the transition since the rectangles natural center is mid.
            Pos3d pos_iterator = new Pos3d(start*is_x,start*is_y, 0);
            Pos3d d_pos_iterator = new Pos3d(grid_power[0]*is_x, grid_power[1]*is_y, 0);
            Log.d("d_pos_iterator", d_pos_iterator.toString());
            for(DrawableRectangle grid_line : grid){
                Pos3d translation = new Pos3d(pos_iterator);
                Log.d("translation", translation.toString());
                boolean inside = (translation.x > axis_max[0] || translation.y > axis_max[1])? false:true;
                Pos3d translation_openGL = coordinate2OpenGL(translation);
                Log.d("translation_openGL", translation_openGL.toString());
                translation_openGL.add(standard_transition_openGL[i]);
                Log.d("translation_openGL+std", translation_openGL.toString());
                grid_line.setRelativePositionToParent(translation_openGL);
                pos_iterator.add(d_pos_iterator);
                if(inside){
                    grid_line.setColor(grid_color);
                }else{
                    // TODO It is not very efficient to calculate in Renderer() the transparent grid parts
                    grid_line.setColor(TRANSPARENT);
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

    // visual
    float width;
    float height;

    // values on axis
    double [] axis_max = {1,1};
    double [] axis_min = {0,0};

    double [] grid_power = {1,1};

    DrawableArrow x_axis;
    DrawableArrow y_axis;

    ArrayList <DrawableRectangle> x_grid;
    ArrayList <DrawableRectangle> y_grid;

    final static int NUM_GRID_STRIPES = 30;
    final static float AXIS_WIDTH = 0.2f;

    final static float GRID_WIDTH = 0.07f;

    final static double GRID_ELEVATION_Z = 0.01;

    ColorRGBA grid_color = BG_DEFAULT_GRID_COLOR;
    final static ColorRGBA TRANSPARENT = new ColorRGBA(0,0,0,0.0);
    final static ColorRGBA BG_DEFAULT_COLOR = new ColorRGBA(0.7,0.7,0.7,1);
    final static ColorRGBA BG_DEFAULT_GRID_COLOR = new ColorRGBA(0,0,0,1);
}
