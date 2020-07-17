package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

import pos3d.Pos3d;

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

    /*!
     * \brief draw Draws the Drawable as well as all its children. If something changed it also renders
     * itself or its children gain.
     */
    public void draw(GL10 gl){
        if(needs_rendering){
            Render();
            needs_rendering = false;
        }
        for (Drawable child : children) {
            child.draw(gl);
        }

        // Enabled the vertices buffer for writing and to be used during
        // rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Specifies the location and data format of an array of vertex
        // coordinates to use when rendering.
        gl.glVertexPointer(COORDS_PER_VERTEX, GL10.GL_FLOAT, vertex_stride, vertex_buffer);
        gl.glColor4f(color.r, color.g, color.b, color.a);

        //gl.glDrawElements(GL10.GL_LINES, vertex_count, GL10.GL_UNSIGNED_SHORT, index_buffer);    //You see longitude and latitude
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, connected_vertex_count, GL10.GL_UNSIGNED_SHORT, index_buffer); //Surface colored
        //gl.glDrawElements(GL10.GL_TRIANGLES, vertex_count, GL10.GL_UNSIGNED_SHORT, index_buffer); //does not what you want! try it...
        //gl.glDrawElements(GL10.GL_TRIANGLE_FAN, vertex_count, GL10.GL_UNSIGNED_SHORT, index_buffer);//Surface colored
    }

    public void setColor(ColorRGBA c){
        color = c;
        needs_rendering = true;
    }

    public Pos3d getPosition() {
        return position;
    }

    public void setPosition(final Pos3d p){
        // Is this necessary? this is just a reference right? I dont want to change it.
        Pos3d new_position = new Pos3d(p);
        position = new_position.add(translation_2_parent);
        needs_rendering = true;
        for (Drawable child : children) {
            child.move(position);
        }
    }

    public void move(float dx, float dy, float dz){
        move(new Pos3d(dx,dy,dz));
    }

    public void move(Pos3d dp){
        position.add(dp);
        needs_rendering = true;
        for (Drawable child : children) {
            child.move(dp);
        }
    }

    public void setRelativePositionToParent(Pos3d p){
        translation_2_parent = p;
        // set the new position which will than add the translation to the parent
        // as well as the new position for all other children.
        setPosition(position);
    }

    abstract public boolean isWithin(Pos3d p);

    public boolean isParent(){
        return parent == null;
    }

    public boolean hasChildren(){
        return children.size() > 0;
    }

    public void adChild(Drawable child){
        Pos3d relative_translation_2_parent = new Pos3d(0,0,0);
        adChild(child, relative_translation_2_parent);
    }

    public void adChild(Drawable child, Pos3d relative_translation_2_parent){
        child.setRelativePositionToParent(relative_translation_2_parent);
        child.parent = this;
        children.add(child);
    }

    final protected short[] Vector2ArrayShort(final Vector<Short> v){
        short a[] = new short[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }

    final protected float[] Vector2ArrayFloat(final Vector<Float> v){
        float a[] = new float[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }

    protected Context context;

    protected pos3d.Pos3d position;
    protected ColorRGBA color;
    // number of triangles*3
    protected int connected_vertex_count;
    protected int vertex_stride = 0;
    protected FloatBuffer vertex_buffer;
    protected ShortBuffer index_buffer;

    static final int COORDS_PER_VERTEX = 3; // x,y,z
    static final int SIZE_OF_FLOAT = 4; // 4 byte
    static final int SIZE_OF_SHORT = 2; // 2 byte

    protected boolean needs_rendering = true;

    Vector<Drawable> children = new Vector<>();
    Drawable parent = null;
    Pos3d translation_2_parent = new Pos3d(0,0,0);
}
