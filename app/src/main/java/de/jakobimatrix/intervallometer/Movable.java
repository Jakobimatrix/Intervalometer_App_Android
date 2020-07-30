package de.jakobimatrix.intervallometer;

import javax.microedition.khronos.opengles.GL10;

// implements the basic features of a movable and drawable object.
public abstract class Movable {

    /*!
     * \brief A movable consists at least out of 1 parent.
     */
    public Movable(Drawable parent_) {
        parent = parent_;
    }

    /*!
     * \brief isWithin This should return true if a given position_ is within the region of the drawable
     */
    abstract public boolean isWithin(Pos3d position_);

    /*!
     * \brief draw shall call the parent and his children to draw them.
     */
    public void draw(GL10 gl){
        parent.draw(gl);
    }

    /*!
     * \brief isHold checks of the touch position is withing its limits if it is movable.
     * return true if movable And touched.
     */
    public boolean isHold(Pos3d touch_pos){
        return !is_locked && isWithin(touch_pos);
    }

    /*!
     * \brief setPosition sets the position of this Drawable.
     * \param p The position this Drawable shall have in OpenGL coordinates.
     */
    public void setPosition(Pos3d p){
        parent.setPosition(p);
    }

    /*!
     * \brief getPosition Return the current position of this drawable.
     * \return current position.
     */
    public final Pos3d getPosition(){
        return parent.getPosition();
    }

    /*!
     * \brief move Moves the Movable and all his children relative to its current position
     * dp Relative movement in openGL-view coordinate
     */
    public void move(Pos3d dp){
        parent.move(dp);
    }

    /*
    * \brief Callback class - function java shit.
    * //https://stackoverflow.com/questions/18279302/how-do-i-perform-a-java-callback-between-classes
    * This function shall be called back after touch (finger stopped touching device)
    * */
    class CallBackOnFingerRelease implements OpenGLRenderer.CallBackOnFingerRelease {
        public void onFingerRelease() {
            endTouch();
        }
    }

    /*!
     * \brief endTouch Abstract method to be implemented py child. It will be called if the user stops touching the device if this.movable was touched last.
     */
    public abstract void endTouch();

    /*!
     * \brief isLocked returns if the movable shall be able to be moved or not.
     * There is no real prevention for moving if Movable is locked but this->move() or this->setPosition() is called.
     * \return True if tis Movable shall not be moved.
     */
    public boolean isLocked(){
        return is_locked;
    }

    public void setLocked(boolean lock){
        is_locked = lock;
    }

    protected Pos3d absolutePos2relativePos(final Pos3d abs){
        Pos3d parent_pos = parent.getPosition();
        return Pos3d.sub(abs, parent_pos);
    }

    /*!
     * \brief clean calls Drawable (parent) close method for clean up.
     */
    public void clean(){
        parent.clean();
    }

    Drawable parent;
    boolean is_locked = true;

    public CallBackOnFingerRelease on_finger_release_callback = new CallBackOnFingerRelease();
}
