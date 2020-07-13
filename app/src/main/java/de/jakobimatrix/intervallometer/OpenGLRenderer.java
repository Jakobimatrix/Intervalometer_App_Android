package de.jakobimatrix.intervallometer;

import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    @Override
    public void onSurfeceCreated(GL10 gl10, EGLConfig egl_config){
        // reset background color
        GLES20.glClearColor(0, 0,0,1f);
    }


    @Override
    public void onSurfaceChanged(GL10 gl10, EGLConfig egl_config){

    }

    @Override
    public void onDrawFrame(GL10 gl10, EGLConfig egl_config){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}
