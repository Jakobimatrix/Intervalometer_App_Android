package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    public OpenGLRenderer(Context c){
        context = c;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig egl_config){

        gl10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl10.glEnable(GL10.GL_DEPTH_TEST);
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        //test_dot = new MovableDot(context, pos3d.Pos2d(1,1));
         Sp1 = new Sphere(1f,0,0,0,0,(char)1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height){
        // when rotating the phone
        GLES20.glViewport(0,0,width,height);
        float aspect = (float)width / height;
        gl10.glMatrixMode(GL10.GL_PROJECTION);
        gl10.glLoadIdentity();
        gl10.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl10){
        prepareFrame(gl10);


        Sp1.draw(gl10);
        //test_dot.draw();


        // Disable the vertices buffer.
        gl10.glDisableClientState(GL10.GL_VERTEX_ARRAY);
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
        float zoom = 1;
        gl10.glTranslatef(0.0f, 0.0f, zoom);
        float roll = 0;
        float pitch = 0;
        float yaw = 0;
        gl10.glRotatef(roll, 1, 0, 0);
        gl10.glRotatef(pitch, 0, 1, 0);
        gl10.glRotatef(yaw, 0, 0, 1);

        gl10.glEnable(GL10.GL_BLEND);
        gl10.glEnable(GL10.GL_TEXTURE_2D);
        gl10.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }

    /*!
     * \brief onTouchEvent Is called through the EditTemplateActivity whenever the user dares to touch his screen.
     */
    public boolean onTouchEvent(MotionEvent e) {
        Log.i("touch event", "touched");
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Log.i("touch event", "x: "+x+" - y:"+ y);
                //float dx = x - mPreviousX;
                //float dy = y - mPreviousY;
                //mAngleY = (mAngleY + (int)(dx * TOUCH_SCALE_FACTOR) + 360) % 360;
                //mAngleX = (mAngleX + (int)(dy * TOUCH_SCALE_FACTOR) + 360) % 360;
                break;
        }
        //mPreviousX = x;
        //mPreviousY = y;
        return true;
    }

    private Context context;
    private MovableDot test_dot;
    private Sphere Sp1;
}
