package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DrawableArrow extends Drawable {
    public DrawableArrow(Context context_, Pos3d position_, float height_, float width) {
        super(context_, position_);

        height = height_;
        tip_length = width*Utility.GOLDEN_RATIO;
        tip_width = width;
        body_width = 0.5f*width/Utility.GOLDEN_RATIO;
    }

    @Override
    protected void Render() {
        final int num_vertices = (NUM_VERTICES)*COORDS_PER_VERTEX;
        final int num_vertices_ids = (NUM_TRIANGLES)*3;// triangle

        short [] vertices_ids = new short[num_vertices_ids];
        float [] vertices = new float[num_vertices];

        Pos3d [] vertices_3d = new Pos3d[NUM_VERTICES];
        // bot-left
        vertices_3d[0] = new Pos3d(position);
        vertices_3d[0].add(new Pos3d(-body_width/2.0 , 0, 0));
        // bot-right
        vertices_3d[1] = new Pos3d(position);
        vertices_3d[1].add(new Pos3d(+body_width/2.0 , 0, 0));
        // top-right
        vertices_3d[2] = new Pos3d(position);
        vertices_3d[2].add(new Pos3d(+body_width/2.0 , height-tip_length, 0));
        // top-right
        vertices_3d[3] = new Pos3d(position);
        vertices_3d[3].add(new Pos3d(-body_width/2.0 , height-tip_length, 0));

        // tip top
        vertices_3d[4] = new Pos3d(position);
        vertices_3d[4].add(new Pos3d(0 , height, 0));
        // tip left
        vertices_3d[5] = new Pos3d(position);
        vertices_3d[5].add(new Pos3d(-tip_width/2.0 , height-tip_length, 0));
        // tip right
        vertices_3d[6] = new Pos3d(position);
        vertices_3d[6].add(new Pos3d(tip_width/2.0 , height-tip_length, 0));

        int counter = 0;
        for(int i = 0; i < NUM_VERTICES; i++){
            vertices_3d[i].rotateZ(rotation_rad);
            for(int v = 0; v < COORDS_PER_VERTEX; v++){
                vertices[counter++] = (float) vertices_3d[i].get(v);
            }
        }

        vertices_ids[0] = (short) 0;
        vertices_ids[1] = (short) 1;
        vertices_ids[2] = (short) 2;

        vertices_ids[3] = (short) 0;
        vertices_ids[4] = (short) 2;
        vertices_ids[5] = (short) 3;

        vertices_ids[6] = (short) 4;
        vertices_ids[7] = (short) 5;
        vertices_ids[8] = (short) 6;

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

        for(int i = 0; i < NUM_VERTICES; i++) {
            Log.d("vertices_3d [" + i + "]", vertices_3d[i].toString());
        }

        Log.d("index_buffer", " " + index_buffer);
        Log.d("vertex_buffer", " " + vertex_buffer);
    }

    public void setRotationUP(){
        rotation_rad = 0;
        needs_rendering = true;
    }

    public void setRotationDOWN(){
        rotation_rad = Math.PI;
        needs_rendering = true;
    }

    public void setRotationLEFT(){
        rotation_rad = -Math.PI/2;
        needs_rendering = true;
    }

    public void setRotationRIGHT(){
        rotation_rad = Math.PI/2;
        needs_rendering = true;
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    public float getBodyWidth(){
        return body_width;
    }

    float height;
    float tip_length;
    float tip_width;
    float body_width;
    double rotation_rad = 0f;

    final static int NUM_VERTICES = 7;
    final static int NUM_TRIANGLES = 3;
}
