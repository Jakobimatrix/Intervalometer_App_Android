package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DrawableRectangle extends Drawable {
    public DrawableRectangle(Context context_, Pos3d position_, float width_, float height_) {
        super(context_, position_);
        width = width_;
        height = height_;
        if(width_ < 0 || height_ < 0){
            throw new IllegalArgumentException( "DrawableRectangle: given height,width must be positive! Use the SetXXXOrigen methods for changing perspective.");
        }
    }

    @Override
    protected void Render() {
        final int num_vertices = (NUM_CORNERS)*COORDS_PER_VERTEX;
        final int num_vertices_ids = (2)*3;// triangle

        short [] vertices_ids = new short[num_vertices_ids];
        float [] vertices = new float[num_vertices];

        // top-left
        corner[0] = new Pos3d(position);
        corner[0].add(new Pos3d(width/2.0 , height /2.0, 0));
        // bot-left
        corner[1] = new Pos3d(position);
        corner[1].add(new Pos3d(-width/2.0 , height /2.0, 0));
        // bot-right
        corner[2] = new Pos3d(position);
        corner[2].add(new Pos3d(-width/2.0 , -height /2.0, 0));
        // top-right
        corner[3] = new Pos3d(position);
        corner[3].add(new Pos3d(width/2.0 , -height /2.0, 0));

        int counter = 0;
        for(int i = 0; i < NUM_CORNERS; i++){
            corner[i].add(new Pos3d(translation_x, translation_y, 0));
            for(int v = 0; v < COORDS_PER_VERTEX; v++){
                vertices[counter++] = (float) corner[i].get(v);
            }
        }

        vertices_ids[0] = (short) 0;
        vertices_ids[1] = (short) 1;
        vertices_ids[2] = (short) 2;

        vertices_ids[3] = (short) 0;
        vertices_ids[4] = (short) 2;
        vertices_ids[5] = (short) 3;

        // set the buffer and the size variables
        index_buffer_size = num_vertices_ids;
        vertex_stride = 0;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * SIZE_OF_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertex_buffer = vbb.asFloatBuffer();
        vertex_buffer.put(vertices);
        vertex_buffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(vertices_ids.length * SIZE_OF_SHORT);
        ibb.order(ByteOrder.nativeOrder());
        index_buffer = ibb.asShortBuffer();
        index_buffer.put(vertices_ids);
        index_buffer.position(0);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        if(Utility.isWithinTriangle(corner[0],corner[1],corner[2], p) ||
                Utility.isWithinTriangle(corner[0],corner[2],corner[3], p)){
            return true;
        }
        return false;
    }

    public void setCenterORIGEN(){
        translation_x = 0;
        translation_y = 0;
        needs_rendering = true;
    }

    public void setTopLeftORIGEN(){
        translation_x = width/2f;
        translation_y = -height/2f;
        needs_rendering = true;
    }

    public void setTopCenterORIGEN(){
        translation_x = 0;
        translation_y = -height/2f;
        needs_rendering = true;
    }

    public void setTopRightORIGEN(){
        translation_x = -width/2f;
        translation_y = -height/2f;
        needs_rendering = true;
    }

    public void setLeftCenterORIGEN(){
        translation_x = width/2f;
        translation_y = 0;
        needs_rendering = true;
    }

    public void setBotLeftORIGEN(){
        translation_x = width/2f;
        translation_y = height/2f;
        needs_rendering = true;
    }

    public void setBotCenterORIGEN(){
        translation_x = 0;
        translation_y = height/2f;
        needs_rendering = true;
    }

    public void setBotRightORIGEN(){
        translation_x = -width/2f;
        translation_y = height/2f;
    }

    public void setRightCenterORIGEN(){
        translation_x = -width/2f;
        translation_y = 0;
        needs_rendering = true;
    }

    public float getWidth(){
        return width;
    }

    public  float getHeight(){
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        needs_rendering = true;
    }

    public void setWidth(float width){
        this.width = width;
        needs_rendering = true;
    }

    float translation_x = 0;
    float translation_y = 0;

    float width;
    float height;
    final static int NUM_CORNERS = 4;
    Pos3d [] corner = new Pos3d [NUM_CORNERS];
}
