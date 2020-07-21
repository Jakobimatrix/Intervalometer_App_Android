package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

// implements the minimal functionality to draw something.
public abstract class Drawable {

    public Drawable(Context context_, Pos3d position_){
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
            if(child == this){
                throw new IllegalArgumentException( "NEIN FUCK" );
            }
            child.draw(gl);
        }

        gl.glVertexPointer(COORDS_PER_VERTEX, GL10.GL_FLOAT, vertex_stride, vertex_buffer);
        gl.glColor4f(color.r, color.g, color.b, color.a);
       /*
        gl.glDrawRangeElements(
                GLenum mode,
                GLuint start,
                GLuint end,
                GLsizei count,
                GLenum type,
  	            const void * indices);
        */
       gl.glDrawElements(triangle_coloring, index_buffer_size, GL10.GL_UNSIGNED_SHORT, index_buffer);
    }

    /*!
     * \brief setColor Set a bew color for the drawable.
     */
    public void setColor(ColorRGBA c){
        color = c;
        needs_rendering = true;
    }

    /*!
     * \brief getPosition Returns the current Position of the Drawable in openGL-view coordinates
     */
    public final Pos3d getPosition() {
        return position;
    }

    /*!
     * \brief setPosition Sets a new Position for the drawable. It also moves all children accordingly.
     * p The new pose in openGL-view coordinates
     */
    public void setPosition(final Pos3d p){
        position = new Pos3d(p);
        position.add(translation_2_parent);
        needs_rendering = true;
        for (Drawable child : children) {
            child.setPosition(position);
        }
    }

    /*!
     * \brief move Moves the drawable and all his children relative to its current position
     * dx Relative movement in openGL-view x-coordinate
     * dy Relative movement in openGL-view y-coordinate
     * dz Relative movement in openGL-view z-coordinate
     */
    public void move(float dx, float dy, float dz){
        move(new Pos3d(dx,dy,dz));
    }

    /*!
     * \brief move Moves the drawable and all his children relative to its current position
     * dp Relative movement in openGL-view coordinate
     */
    public void move(Pos3d dp){
        position.add(dp);
        needs_rendering = true;
        for (Drawable child : children) {
            child.setPosition(position);
        }
    }

    /*!
     * \brief setRelativePositionToParent set the relative transposition to its parent.
     * Every call of this->setPosition() will be affected.
     * \param p The relative pose translation.
     */
    public void setRelativePositionToParent(Pos3d p){
        translation_2_parent = new Pos3d(p);
        // set the new position which will than add the translation to the parent
        // as well as the new position for all other children.
        setPosition(position);
    }

    /*!
     * \brief isWithin This should return true if a given pose is within the region of the drawable
     */
    abstract public boolean isWithin(Pos3d p);

    /*!
     * \brief hasChildren returns true if this drawable has child-drawables which are defined relative to this drawable
     * \return true if this drawable has at least 1 child.
     */
    public boolean hasChildren(){
        return children.size() > 0;
    }

    /*!
     * \brief adChild adds the child to an internal vector. The relative translation of that child is 0
     * \param child The new Drawable child
     */
    public void adChild(Drawable child){
        adChild(child, new Pos3d(0,0,0));
    }

    /*!
     * \brief adChild adds the child to an internal vector.
     * \param child The new Drawable child
     * \param relative_translation_2_parent the translation between this Drawable (parent) and the new child.
     */
    public void adChild(Drawable child, Pos3d relative_translation_2_parent){
        if(this == child){
            throw new IllegalArgumentException( "Drawable::adChild: Parent cant be his own child!");
        }
        child.setRelativePositionToParent(relative_translation_2_parent);
        children.add(child);
    }

    void setColoringMethodFill(){
        triangle_coloring = GL10.GL_TRIANGLE_STRIP;
    }

    void setColoringMethodLines(){
        triangle_coloring = GL10.GL_LINE_STRIP;
    }

    protected Context context;

    protected Pos3d position;
    static final ColorRGBA DEFAULT_COLOR = new ColorRGBA(0,0,0,1);//Black
    protected ColorRGBA color = DEFAULT_COLOR;
    // number of triangles*3
    protected int vertex_stride = 0;
    protected FloatBuffer vertex_buffer;
    int index_buffer_size;
    protected ShortBuffer index_buffer;

    static final int COORDS_PER_VERTEX = 3; // x,y,z
    static final int SIZE_OF_FLOAT = 4; // 4 byte
    static final int SIZE_OF_SHORT = 2; // 2 byte

    protected boolean needs_rendering = true;

    ArrayList<Drawable> children = new ArrayList<>();

    Pos3d translation_2_parent = new Pos3d(0,0,0);

    int triangle_coloring = GL10.GL_TRIANGLE_STRIP;
}
