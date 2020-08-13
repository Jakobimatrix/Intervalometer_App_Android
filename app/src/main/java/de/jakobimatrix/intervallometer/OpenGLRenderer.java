package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    public OpenGLRenderer(Context c, int screen_width_px, int screen_height_px){
        context = c;
        // will be set onSurfaceChanged which also will be called once
        this.screen_width_px = screen_width_px;
        this.screen_height_px = screen_height_px;

        double i = 0;
    }

    // fucking no destructors in java, so you call this manually.
    public void close(){
        stopTouchActionThread();
        for (Movable movable : movables.values()) {
            movable.clean();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig egl_config){
        // TODO this is called every time the app loads the activity (wanted)
        // TODO but also when the app was in the background and is now put again into the foreground
        // TODO thus all vectors and lists must be emptied or every entity must be checked if it is already existing/initiated
        // TODO THIS IS CALLED too WHEN ROTATEING THE PHONE so lock rotation!
        gl10.glShadeModel(GL10.GL_SMOOTH); // Enable Smooth Shading

        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
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
        float far = -Globals.zoom+1;
        float bottom = -1.0f;
        float top = 1.0f;
        float left = -ratio;
        float right = ratio;
        gl10.glFrustumf(left , right , bottom, top, near, far);

        for (Movable movable : movables.values()) {
            movable.forceReRender();
        }
    }

    @Override
    public void onDrawFrame(GL10 gl10){

        prepareFrame(gl10);
        // TODO it would be faster to collect all vertices and ids and copy that to the gpu at once.
        // TODO if this gets slow implement the thing one line above.

        for (Movable movable : movables.values()) {
            movable.draw(gl10);
        }
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
        gl10.glTranslatef(0.0f, 0.0f, Globals.zoom);
        /* No rotation vor now
        float roll = 0;
        float pitch = 0;
        float yaw = 0;
        gl10.glRotatef(roll, 1, 0, 0);
        gl10.glRotatef(pitch, 0, 1, 0);
        gl10.glRotatef(yaw, 0, 0, 1);
        */
        gl10.glEnable(GL10.GL_BLEND);
        gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }

    /*!
     * \brief onTouchEvent Is called through the EditTemplateActivity whenever the user dares to touch his screen.
     */
    public boolean onTouchEvent(MotionEvent e) {
        // Log.d("EVENT", e + "");
        // TODO multi finger touch:
        // int num_fingers = e.getPointerCount();
        // for(int i = 0; i < num_fingers; i++){
        //     e...
        // }

        float x = e.getX();
        float y = e.getY();
        last_pos_screen = new Pos3d(x, y,0);
        last_pos_openGL = Utility.screen2openGl(last_pos_screen);

        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                stop_touch_tread = true;
                stopTouchActionThread();
                manageOnFingerReleaseCall();
                return true;
        }
        if(timer_touch_action == null) {
            on_finger_released_called = false;
            stop_touch_tread = false;
            timer_touch_action = new Timer();
            timer_touch_action.scheduleAtFixedRate(createTimerTaskRunTouchEvent(), 0,  Globals.HOLD_DELAY_MS);
        }
        return true;
    }

    private void manageOnFingerReleaseCall(){
        if(!on_finger_released_called){
            on_finger_released_called = true;
            movable_release_callback.onFingerRelease();
            movable_release_callback = new CallBackOnFingerReleaseNOP();
        }
    }

    public void stopTouchActionThread(){
        if(timer_touch_action != null) {
            timer_touch_action.cancel();
            timer_touch_action = null;
        }
    }

    public void stopAnalogActionThread(){
        if(timer_analog_action != null) {
            timer_analog_action.cancel();
            timer_analog_action = null;
        }
    }

    /*!
     * \brief startTouchActionThread direct with a command rather than with the touch position.
     * The tread will be stopped at release (see onTouchEvent)
     */
    public void startTouchActionThread(final CMD cmd, Movable mv){
        stopAnalogActionThread();
        setAnalogCmd(cmd, mv);
        timer_analog_action = new Timer();
        timer_analog_action.scheduleAtFixedRate(createTimerTaskRunAnalogCmd(), 0,  Globals.HOLD_DELAY_MS);
    }

    /*!
     * \brief touchAction Shall be used in an own thread!
     * If started it will handle all movables and the current touch point
     * as long as action_hold_down == true.
     * It will run loop every HOLD_DELAY_MS ms if possible.
     */
    private void setAnalogCmd(CMD cmd, Movable mv){
        analog_command = cmd;
        analog_movable = mv;
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
            movables.get(id).clean();
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

    private TimerTask createTimerTaskRunAnalogCmd() {
        return new TimerTask() {
            @Override
            public void run() {
                if(analog_movable != null){
                    analog_movable.executeCommand(analog_command);
                }
            }
        };
    }

    private TimerTask createTimerTaskRunTouchEvent() {
        return new TimerTask() {
            @Override
            public void run() {
                if(stop_touch_tread){
                    manageOnFingerReleaseCall();
                }else {
                    if (movables.containsKey(movable_guess)) {
                        Movable mv = movables.get(movable_guess);
                        if (mv.isHold(last_pos_openGL)) {
                            mv.setPosition(last_pos_openGL);
                            movable_release_callback = mv.on_finger_release_callback;
                        } else {
                            movable_guess = INVALID_KEY;
                        }
                    }
                    if (movable_guess == INVALID_KEY) {
                        for (Map.Entry<Integer, Movable> entry : movables.entrySet()) {
                            Movable mv = entry.getValue();
                            if (mv.isHold(last_pos_openGL)) {
                                mv.setPosition(last_pos_openGL);
                                // there can only be one movable to be hold at a time;
                                // set a guess for the next time
                                movable_guess = entry.getKey();
                                movable_release_callback = mv.on_finger_release_callback;
                                break;
                            }
                        }
                    }
                }
            }
        };
    }

    public interface CallBackOnFingerRelease{
        void onFingerRelease();
    }

    class CallBackOnFingerReleaseNOP implements CallBackOnFingerRelease {
        public void onFingerRelease() { }
    }

    Pos3d last_pos_screen = new Pos3d(0,0,0);
    Pos3d last_pos_openGL = new Pos3d(0,0,0);

    private Context context;
    private Integer movables_id_counter = 0;
    private Map<Integer, Movable> movables = new HashMap<Integer, Movable>();

    CMD analog_command = CMD.NULL;
    Movable analog_movable = null;
    Timer timer_touch_action = null;
    boolean on_finger_released_called = false;
    boolean stop_touch_tread = false;
    Timer timer_analog_action = new Timer();

    // contains the id of the last moved movable for a first guess.
    final static int INVALID_KEY = -1;
    int movable_guess = INVALID_KEY;
    CallBackOnFingerRelease movable_release_callback = new CallBackOnFingerReleaseNOP();

    float screen_height_px;
    float screen_width_px;
}
