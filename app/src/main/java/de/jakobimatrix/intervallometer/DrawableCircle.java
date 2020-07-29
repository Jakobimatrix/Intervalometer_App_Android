package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Vector;

public class DrawableCircle extends Drawable {
    public DrawableCircle(Context context_, Pos3d position_, float radius) {
        super(context_, position_);
        R = radius;

        if(radius < 0){
            throw new IllegalArgumentException( "DrawableCircle: given Radius is smaller than 0" );
        }
    }

    @Override
    protected void Render() {
        final int num_vertices = (NUM_POINTS+1)*COORDS_PER_VERTEX; // + center
        final int num_vertices_ids = (NUM_POINTS)*3;// triangle

        Vector<Short> vertices_ids_v = new Vector<>();
        Vector<Float> vertices_v = new Vector<Float>();
        vertices_ids_v.setSize(num_vertices_ids);
        vertices_v.setSize(num_vertices);

        // center
        int v = 0;
        int g = 0;
        vertices_v.set(v++, (float)position.x);
        vertices_v.set(v++, (float)position.y);
        vertices_v.set(v++, (float)position.z);
        final short center_id = 0;

        short connect_nr = 1;
        final float increment = 360f / (float)NUM_POINTS;
        int points = 0;
        for (float Phi = 0; points < NUM_POINTS; Phi += increment) {
            points++;
            vertices_v.set(v++, (float) (R * Math.cos(Math.toRadians(Phi)) +  position.x));//X
            vertices_v.set(v++, (float) (R * Math.sin(Math.toRadians(Phi)) + position.y));//Y
            vertices_v.set(v++, (float) position.z);//Z
            vertices_ids_v.set(g++, connect_nr);
            vertices_ids_v.set(g++, center_id);
            connect_nr++;
            vertices_ids_v.set(g++, connect_nr);
        }
        // the last vertices doesnt exist, this should be the very first instead
        vertices_ids_v.set( --g , (short) 1);

        // convert to arrays
        final short vertices_ids_a[] = Utility.Vector2ArrayShort(vertices_ids_v);
        final float vertices_a[] = Utility.Vector2ArrayFloat(vertices_v);

        // set the buffer and the size variables
        index_buffer_size = vertices_ids_v.size();
        vertex_stride = 0;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices_a.length * SIZE_OF_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertex_buffer = vbb.asFloatBuffer();
        vertex_buffer.put(vertices_a);
        vertex_buffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(vertices_ids_a.length * SIZE_OF_SHORT);
        ibb.order(ByteOrder.nativeOrder());
        index_buffer = ibb.asShortBuffer();
        index_buffer.put(vertices_ids_a);
        index_buffer.position(0);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        double norm = position.distance(p);
        return norm < R;
    }

    public void setRadius(float R){
        this.R = R;
        needs_rendering = true;
        if(R < 0){
            throw new IllegalArgumentException( "DrawableCircle::setRadius: given Radius is smaller than 0" );
        }
    }

    private float R;
    final static int NUM_POINTS = 30;
}
