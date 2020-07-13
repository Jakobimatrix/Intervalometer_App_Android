package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.graphics.Color;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

// implements the minimal functionality to draw something.
public abstract class Drawable {

    public Drawable(Context context_, pos3d.Pos3d position_){
        context = context_;
        position = position_;
    }

    /*!
     * \brief Render Gets called if the object needs new rendering.
     * It writes into vertex_buffer, vertex_count, index_buffer
     */
    protected abstract void Render();

    public void draw(GL10 gl){
        Render();
        // Enabled the vertices buffer for writing and to be used during
        // rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Specifies the location and data format of an array of vertex
        // coordinates to use when rendering.
        gl.glVertexPointer(COORDS_PER_VERTEX, GL10.GL_FLOAT, vertex_stride, vertex_buffer);
        gl.glColor4f(color.r, color.g, color.b, color.a);

        //gl.glDrawElements(GL10.GL_LINES, vertex_count, GL10.GL_UNSIGNED_SHORT, indexBuffer);    //You see longitude and latitude
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, vertex_count, GL10.GL_UNSIGNED_SHORT, index_buffer); //Surface colored
        //gl.glDrawElements(GL10.GL_TRIANGLES, vertex_count, GL10.GL_UNSIGNED_SHORT, indexBuffer); //does not what you want! try it...
        //gl.glDrawElements(GL10.GL_TRIANGLE_FAN, vertex_count, GL10.GL_UNSIGNED_SHORT, indexBuffer);//Surface colored
    }


    protected Context context;

    public pos3d.Pos3d position;
    public ColorRGBA color;
    protected int vertex_buffer_id;
    protected int vertex_count;
    protected int vertex_stride = 0;
    private FloatBuffer vertex_buffer;
    private ShortBuffer index_buffer;

    static final int COORDS_PER_VERTEX = 3; // x,y,z
    static final int SIZE_OF_FLOAT = 4; // 4 byte
}
