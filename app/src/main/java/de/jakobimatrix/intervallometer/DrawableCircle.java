package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Vector;

import pos3d.Pos3d;

public class DrawableCircle extends Drawable {
    public DrawableCircle(Context context_, Pos3d position_, float radius) {
        super(context_, position_);
        R = radius;
        Render();
    }

    @Override
    protected void Render() {
        Vector<Short> vertices_ids_v = new Vector<>();
        Vector<Float> vertices_v = new Vector<Float>();

        final int num_vertices = (NUM_POINTS+1)*COORDS_PER_VERTEX; // + center
        final int num_vertices_ids = (NUM_POINTS)*3;// triangle

        vertices_ids_v.setSize(num_vertices_ids);
        vertices_v.setSize(num_vertices);

        // center
        int v = 0;
        int g = 0;
        vertices_v.set(v++, (float)position.getX());
        vertices_v.set(v++, (float)position.getY());
        vertices_v.set(v++, (float)position.getZ());
        final short center_id = 0;

        short connect_nr = 1;
        final float increment = 360f / (float)NUM_POINTS;
        int points = 0;
        for (float Phi = 0; points < NUM_POINTS; Phi += increment) {
            points++;
            vertices_v.set(v++, (float) (R * Math.cos(Math.toRadians(Phi)) +  position.getX()));//X
            vertices_v.set(v++, (float) (R * Math.sin(Math.toRadians(Phi)) + position.getY()));//Y
            //vertices_v.set(v++, (float) (R * Math.sin(Math.toRadians(Phi)) + position.getZ()));
            vertices_v.set(v++, (float) position.getZ());//Z
            vertices_ids_v.set(g++, connect_nr);
            vertices_ids_v.set(g++, center_id);
            connect_nr++;
            vertices_ids_v.set(g++, connect_nr);
        }
        // the last vertices doesnt exist, this should be the very first instead
        vertices_ids_v.set( (int) connect_nr, (short) 1);

        // convert to arrays
        final short vertices_ids_a[] = Vector2ArrayShort(vertices_ids_v);
        final float vertices_a[] = Vector2ArrayFloat(vertices_v);

        // set the buffer and the size variables
        connected_vertex_count = vertices_ids_v.size();
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
        Pos3d dif = p.sub(position);
        double norm = dif.getNorm();
        return norm < R;
    }

    private float R;
    final static int NUM_POINTS = 30;
}
