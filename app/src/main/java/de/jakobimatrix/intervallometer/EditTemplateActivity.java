package de.jakobimatrix.intervallometer;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class EditTemplateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_edit_template);

        connectWithGUI();
        setGUIFunctions();

        loadTemplate();
    }

    /*!
     * \brief connectWithGUI
     * Sett find all Gui elements by id and connect them to the corresponding variable.
     */
    private void connectWithGUI(){
        gl_view = (GLSurfaceView)findViewById(R.id.openGlView);
        gl_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new OpenGLRenderer(this);
        gl_view.setRenderer(renderer);

        // DEBUG
        seeker_y = (TextView) findViewById(R.id.seeker_y);
        seeker_x = (TextView) findViewById(R.id.seeker_x);
    }

    /*!
     * \brief setGUIFunctions
     * set all OnTouch-, Onchange-, and Onwhatever events to private connected Gui elements.
     */
    private void setGUIFunctions(){

        /*
        // example for on slider listeners
        slider.seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {

                    }
                }
        );
        */
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
        if(DEBUG_TOUCH) {
            float x = event.getX();
            float y = event.getY();
            RelativeLayout.LayoutParams params_y = (RelativeLayout.LayoutParams) seeker_y.getLayoutParams();
            params_y.setMargins(0, (int) y, 0, 0);
            RelativeLayout.LayoutParams params_x = (RelativeLayout.LayoutParams) seeker_x.getLayoutParams();
            params_x.setMargins((int) x, 0, 0, 0);

            seeker_x.setLayoutParams(params_x);
            seeker_y.setLayoutParams(params_y);
        }

        return renderer.onTouchEvent(event);
    }

    /*!
     * \brief loadTemplate
     * \TODO
     */
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
    // UI stuff
    private GLSurfaceView gl_view;
    private OpenGLRenderer renderer;

    //private Slider slider;

    // DEBUG
    private TextView seeker_y;
    private TextView seeker_x;
    final boolean DEBUG_TOUCH = false;
}