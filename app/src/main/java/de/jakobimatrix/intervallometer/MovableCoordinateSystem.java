package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class MovableCoordinateSystem extends Movable {
    public MovableCoordinateSystem(Context context_, Pos3d position_, float width_, float height_) {
        super(new DrawableRectangle(context_, position_, width_, height_));
        DrawableRectangle bg = (DrawableRectangle) parent;
        bg.setBotLeftORIGEN();

        x_axis = new DrawableArrow(context_,position_, width_, AXIS_WIDTH);
        x_axis.setRotationRIGHT();
        y_axis = new DrawableArrow(context_,position_, height_, AXIS_WIDTH);
        y_axis.setRotationUP();

        parent.adChild(x_axis, new Pos3d(0, x_axis.getBodyWidth()/2f, 0));
        parent.adChild(y_axis, new Pos3d(y_axis.getBodyWidth()/2f, 0, 0));


        for(int i = 0; i < MAX_NUM_GRID_STRIPES; i++){
            y_grid.set(i, new DrawableRectangle(context_, Pos3d.Zero(), width_, GRID_WIDTH));
            x_grid.set(i, new DrawableRectangle(context_, Pos3d.Zero(), GRID_WIDTH, height_));
            parent.adChild(y_grid.get(i));
            parent.adChild(x_grid.get(i));
        }
    }

    @Override
    public boolean isWithin(Pos3d position_) {
        return false;
    }

    public void setBackgroundColor(ColorRGBA color){
        parent.setColor(color);
    }

    public void setAxisColor(ColorRGBA color){
        x_axis.setColor(color);
        y_axis.setColor(color);
    }

    private void adjustGrid(){
        // min,min start pos
        Pos3d [] standard_transition = {new Pos3d(0, height/2f, GRID_ELEVATION_Z) ,
                                        new Pos3d(width/2f, 0, GRID_ELEVATION_Z)};
        for(int i = 0; i < 2; i++){
            double span = max[1] - min[1];
            double log_span = Math.log10(span);
            grid_power[i] = (int) log_span - 1;
            ArrayList <DrawableRectangle> grid = getGrid(i);

            Pos3d pos_iterator = new Pos3d(min[0], min[1], 0);
            Pos3d d_pos_iterator = new Pos3d(min[0], min[1], 0);
            for(DrawableRectangle grid_line : grid){
                Pos3d translation = new Pos3d(pos_iterator);
                boolean inside = (translation.x > max[0] || translation.y > max[1])? false:true;
                translation.add(standard_transition[i]);
                grid_line.setRelativePositionToParent(translation);
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
        adjustGrid();
    }

    // visual
    float width;
    float height;

    // values on axis
    double [] max = {1,1};
    double [] min = {1,1};

    double [] grid_power = {1,1};

    DrawableArrow x_axis;
    DrawableArrow y_axis;

    ArrayList <DrawableRectangle> x_grid = new ArrayList <DrawableRectangle>(MAX_NUM_GRID_STRIPES);
    ArrayList <DrawableRectangle> y_grid = new ArrayList <DrawableRectangle>(MAX_NUM_GRID_STRIPES);

    final static int MAX_NUM_GRID_STRIPES = 30;
    final static float AXIS_WIDTH = 0.2f;

    final static float GRID_WIDTH = 0.07f;

    final static double GRID_ELEVATION_Z = 0.01;

    ColorRGBA grid_color = new ColorRGBA(0,0,0,1);
    final static ColorRGBA TRANSPARENT = new ColorRGBA(0,0,0,0.0);

}
