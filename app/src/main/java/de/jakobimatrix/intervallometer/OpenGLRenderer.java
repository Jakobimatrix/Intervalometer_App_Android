package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    public OpenGLRenderer(Context c, int screen_width_px, int screen_height_px){
        context = c;
        // will be set onSurfaceChanged which also will be called once
        this.screen_width_px = screen_width_px;
        this.screen_height_px = screen_height_px;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig egl_config){
        // TODO this is called every time the app loads the activity (wanted)
        // TODO but also when the app was in the background and is now put again into the foreground
        // TODO thus all vectors and lists must be emptied or every entity must be checked if it is already existing/initiated
        // TODO THIS IS CALLED too WHEN ROTATEING THE PHONE so lock rotation!

        gl10.glClearColor(0.5f, 0.5f, 0.0f, 0.5f);
        gl10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl10.glEnable(GL10.GL_DEPTH_TEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height){
        screen_height_px = height;
        screen_width_px = width;
        // when rotating the phone
        GLES20.glViewport(0,0,width,height);
        float ratio  = (float) width / (float) height;
        gl10.glMatrixMode(GL10.GL_PROJECTION);
        gl10.glLoadIdentity();
        // clipping distance from camera
        float near = 1.0f;
        float far = -zoom+1;
        float bottom = -1.0f;
        float top = 1.0f;
        float left = -ratio;
        float right = ratio;
        gl10.glFrustumf(left , right , bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 gl10){
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        prepareFrame(gl10);
        // TODO it would be faster to collect all vertices and ids and copy that to the gpu at once.
        // TODO if this gets slow implement the thing one line above.

        for (Movable movable : movables.values()) {
            movable.draw(gl10);
        }
        gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //gl10.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    /*!
     * \brief prepareFrame. Does some technical stuff needed to be done before drawing every element.
     */
    void prepareFrame(GL10 gl10){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        gl10.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl10.glMatrixMode(GL10.GL_MODELVIEW);
        gl10.glLoadIdentity();

        // zoom and rotation of the scene
        // zoom should be negative to render things at z=0x
        gl10.glTranslatef(0.0f, 0.0f, zoom);
        /* No rotation vor now
        float roll = 0;
        float pitch = 0;
        float yaw = 0;
        gl10.glRotatef(roll, 1, 0, 0);
        gl10.glRotatef(pitch, 0, 1, 0);
        gl10.glRotatef(yaw, 0, 0, 1);
        */
        gl10.glEnable(GL10.GL_BLEND);
        gl10.glEnable(GL10.GL_TEXTURE_2D);
        gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }

    /*!
     * \brief onTouchEvent Is called through the EditTemplateActivity whenever the user dares to touch his screen.
     */
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        Pos3d pos_screen = new Pos3d(x,y,0);
        Pos3d pos_openGL = screen2openGl(pos_screen);

        final Pos3d dp_screen = Pos3d.sub(last_pos_screen, pos_screen);
        final Pos3d dp_openGL = Pos3d.sub(pos_openGL, last_pos_openGL);
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if(movables.containsKey(movable_guess)){
                    Movable mv = movables.get(movable_guess);
                    if(!mv.isLocked() && mv.isHold(pos_openGL)){
                        mv.setPosition(pos_openGL);
                    }else{
                        movable_guess = INVALID_KEY;
                    }
                }
                if(movable_guess == -1) {
                    for (Map.Entry<Integer, Movable> entry : movables.entrySet()) {
                        Movable mv = entry.getValue();
                        if (!mv.isLocked()) {
                            if (mv.isHold(pos_openGL)) {
                                mv.setPosition(pos_openGL);
                                // there can only be one movable to be hold at a time;
                                // set a guess for the next time
                                movable_guess = entry.getKey();
                                break;
                            }
                        }
                    }
                }
                break;
        }
        last_pos_openGL = pos_openGL;
        last_pos_screen = pos_screen;
        return true;
    }

    /*!
     * \brief addMovable Adds a Movable to the intern vector and returns the unique Id for later deletion.
     * It will be rendered henceforth until the Movable gets deleted by this->removeMovable(int id).
     * \param movable The Movable to add.
     * \return The unique Key for deletion later.
     */
    public int addMovable(Movable movable){
        movables.put(movables_id_counter, movable);
        int new_id = movables_id_counter;
        movables_id_counter++;
        return new_id;
    }

    /*!
     * \brief removeMovable Removes the Movable which id was given.
     * \param id The unique identifier for the to be removed Movable.
     * \return True if the given id existed indicating a successful deletion, false otherwise.
     */
    public boolean removeMovable(int id){
        if(movables.containsKey(id)){
            movables.remove(id);
            return true;
        }
        return false;
    }

    /*!
     * \brief getMovableById Returns the requested Movable.
     * \param id The unique identifier of the Movable.
     * \return A Movable object if the id existed, null otherwise.
     */
    public Movable getMovableById(int id){
        if(movables.containsKey(id)){
            return movables.get(id);
        }
        return null;
    }

    /*!
     * \brief screen2openGl Given coordinates this function returns the corresponding coordinates in openGL view.
     * \param screen_coordinate The screen coordinates
     * \return Pos3d the converted coordinates
     */
    public Pos3d screen2openGl(Pos3d screen_coordinate){
        Pos3d openGL_coordinate = new Pos3d(screen_coordinate);
        final float scaling = Math.min(screen_width_px, screen_height_px);
        final float MAGIC_NUMBER = 1.125f;
        // TODO this magic number works only on screens 1080*1920
        // TODO E.g. the number must be 2 for screens of 1920*1080
        // I have no idea where I fucked up and I also don't care anymore.
        // I mean this: https://www.khronos.org/registry/OpenGL-Refpages/es1.1/xhtml/glFrustum.xml
        final float factor = (zoom*MAGIC_NUMBER) / (scaling);
        openGL_coordinate.x = -(screen_coordinate.x - screen_width_px/2.f) * factor;
        openGL_coordinate.y = (screen_coordinate.y - screen_height_px/2.f) * factor;
        return openGL_coordinate;
    }

    Pos3d last_pos_screen = new Pos3d(0,0,0);
    Pos3d last_pos_openGL = new Pos3d(0,0,0);

    private Context context;
    private Integer movables_id_counter = 0;
    private Map<Integer, Movable> movables = new HashMap<Integer, Movable>();

    // this should scale everything
    final private float zoom = -4;

    // contains the id of the last moved movable for a first guess.
    final static int INVALID_KEY = -1;
    int movable_guess = INVALID_KEY;

    float screen_height_px;
    float screen_width_px;
}
