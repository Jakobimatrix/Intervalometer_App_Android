package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class OpenCLView extends GLSurfaceView {
    public OpenCLView(Context context){
        super(context);
        init();
    }

    public OpenCLView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    private void init(){
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        setRenderer(new OpenGLRenderer());
    }
}
