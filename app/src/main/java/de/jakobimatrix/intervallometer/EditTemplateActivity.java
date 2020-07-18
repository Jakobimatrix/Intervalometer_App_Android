package de.jakobimatrix.intervallometer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.Toast;

public class EditTemplateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_template);

        connectWithGUI();
        setGUIFunctions();

        loadTemplate();
    }

    private void connectWithGUI(){
        gl_view = (GLSurfaceView)findViewById(R.id.openGlView);
        gl_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new OpenGLRenderer(this);
        gl_view.setRenderer(renderer);


        slider_x = new Slider((SeekBar)findViewById(R.id.x_mover), -1, 1);
        slider_x.setValue(0);
        slider_y = new Slider((SeekBar)findViewById(R.id.y_mover), -1, 1);
        slider_y.setValue(0);
        slider_z = new Slider((SeekBar)findViewById(R.id.z_mover), -10, -4);
        slider_z.setValue(0);
    }

    private void setGUIFunctions(){
        slider_x.seekbar.setOnSeekBarChangeListener(
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
                        renderer.moveX(slider_x.getValue());
                    }
                }
        );
        slider_y.seekbar.setOnSeekBarChangeListener(
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
                        renderer.moveY(slider_y.getValue());
                    }
                }
        );
        slider_z.seekbar.setOnSeekBarChangeListener(
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
                        renderer.moveZ(slider_z.getValue());
                    }
                }
        );
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

    private Slider slider_x;
    private Slider slider_y;
    private Slider slider_z;
}