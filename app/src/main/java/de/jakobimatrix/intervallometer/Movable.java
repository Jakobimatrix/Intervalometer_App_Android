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
    public Pos3d getPosition(){
        return parent.getPosition();
    }

    /*!
     * \brief move Moves the Movable and all his children relative to its current position
     * dp Relative movement in openGL-view coordinate
     */
    public void move(Pos3d dp){
        parent.move(dp);
    }

    /*!
     * \brief isLocked returns if the movable shall be able to be moved or not.
     * There is no real prevention for moving if Movable is locked but this->move() or this->setPosition() is called.
     * \return True if tis Movable shall not be moved.
     */
    public boolean isLocked(){
        return is_locked;
    }

    Drawable parent;
    boolean is_locked = true;
}
