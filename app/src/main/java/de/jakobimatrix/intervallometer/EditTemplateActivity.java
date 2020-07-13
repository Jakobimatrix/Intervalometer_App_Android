package de.jakobimatrix.intervallometer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

public class EditTemplateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_template);

        gl_view = (GLSurfaceView)findViewById(R.id.openGlView);
        gl_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new OpenGLRenderer(this);
        gl_view.setRenderer(renderer);

        loadTemplate();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gl_view.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        gl_view.onPause();
    }

    /*!
     * \brief onTouchEvent Passes every touch event to the renderer.
     */
    public boolean onTouchEvent(MotionEvent event) {
        return renderer.onTouchEvent(event);
    }


    private void loadTemplate(){
        Bundle b = getIntent().getExtras();
        int value = -1;
        if(b != null) {
            value = b.getInt(ActivityParameters.selectedTemplate);
        }
        if(value == -1){
            // new template
        }else{
            // from db
        }
    }


    private GLSurfaceView gl_view;
    private OpenGLRenderer renderer;
}