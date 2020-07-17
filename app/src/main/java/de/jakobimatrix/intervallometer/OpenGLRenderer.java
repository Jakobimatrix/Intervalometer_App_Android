package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Vector;

import pos3d.Pos3d;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

class OpenGLRenderer implements GLSurfaceView.Renderer {

    public OpenGLRenderer(Context c){
        context = c;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig egl_config){

        gl10.glClearColor(0.5f, 0.5f, 0.0f, 0.5f);
        gl10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl10.glEnable(GL10.GL_DEPTH_TEST);
        gl10.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        for(int i = 0; i < 10; i++){
            dots.add(new MovableDot(context, new Pos3d(i,i,0), 1));
        }
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
        Pos3d p = new Pos3d();
        for(int i = 0; i < dots.size(); i++){
            p.setRand();
            //p.setZ(0);
            dots.get(i).setPosition(p);
            dots.get(i).draw(gl10);
        }

        sp.draw(gl10);

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
        float x = e.getX();
        float y = e.getY();
        Pos3d pos = new Pos3d(x,y,0);
        final Pos3d dp = lastPos.sub(pos);
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                for(int i = 0; i < dots.size(); i++){
                    if(!dots.get(i).is_locked()){
                        if(dots.get(i).isHold(pos)){
                            dots.get(i).move(dp);
                            Log.i("touch event", "moved by " + dp.toString());
                            break;
                        }
                    }
                }
                break;
        }
        lastPos = pos;
        return true;
    }

    Pos3d lastPos = new Pos3d(0,0,0);
    Sphere sp = new Sphere(1f,0f,0f,0f,3f, (char) 1);
    private Context context;
    private Vector<MovableDot> dots = new Vector<>();
}
