package de.jakobimatrix.intervallometer;

import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

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
        Display screen_size = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screen_size.getSize(size);

        gl_view = (GLSurfaceView)findViewById(R.id.openGlView);
        gl_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new OpenGLRenderer(this, size.x, size.y);
        gl_view.setRenderer(renderer);

        button_save = (Button) findViewById(R.id.button_save);

        // DEBUG
        seeker_y = (TextView) findViewById(R.id.seeker_y);
        seeker_x = (TextView) findViewById(R.id.seeker_x);

        double width_100p_in_px = size.x;
        double height_100p_in_px = size.y;
        double margin_px = 40;

        Pos3d complete_left_down_screen = new Pos3d(margin_px, size.y/2f-2*margin_px, 0);
        Pos3d complete_top_right_screen = new Pos3d(size.x - 2*margin_px, margin_px, 0);
        Pos3d complete_top_right_open_gl = renderer.screen2openGl(complete_top_right_screen);
        Pos3d complete_left_down_open_gl = renderer.screen2openGl(complete_left_down_screen);
        float width = (float) (complete_top_right_open_gl.x - complete_left_down_open_gl.x);
        float height = (float) (complete_top_right_open_gl.y - complete_left_down_open_gl.y);
        coord_overview = new MovableCoordinateSystem(this, complete_left_down_open_gl, width, height );

        ArrayList<Double> poly = new ArrayList<>(2);
        poly.add(2.5);
        poly.add(1.);
        Function f = new Function(poly);
        int index = coord_overview.addFunction(f, 0, 4, 1);
        coord_overview.setFunctionLocked(false, index);
        renderer.addMovable(coord_overview);

        Pos3d complete_left_down_screen2 = new Pos3d(10, size.y-200, 0);
        Pos3d complete_top_right_screen2 = new Pos3d(size.x/2-10, size.y/2+10., 0);
        Pos3d complete_top_right_open_gl2 = renderer.screen2openGl(complete_top_right_screen2);
        Pos3d complete_left_down_open_gl2 = renderer.screen2openGl(complete_left_down_screen2);
        float width2 = (float) (complete_top_right_open_gl2.x - complete_left_down_open_gl2.x);
        float height2 = (float) (complete_top_right_open_gl2.y - complete_left_down_open_gl2.y);
        coord_overview2 = new MovableCoordinateSystem(this, complete_left_down_open_gl2, width2, height2 );

        ArrayList<Double> poly2 = new ArrayList<>(3);
        poly2.add(2.);
        poly2.add(0.);
        poly2.add(1.);
        Function f2 = new Function(poly2);
        ArrayList<Double> poly3 = new ArrayList<>(1);
        poly3.add(3.);
        Function f3 = new Function(poly3);
        index = coord_overview2.addFunction(f2, 0.0, 4);
        coord_overview2.setFunctionLocked(false, index);
        index = coord_overview2.addFunction(f3, -4.0, 0, 1);
        coord_overview2.setFunctionLocked(false, index);
        renderer.addMovable(coord_overview2);

        Pos3d complete_left_down_screen3 = new Pos3d(size.x/2 + 10, size.y-200, 0);
        Pos3d complete_top_right_screen3 = new Pos3d(size.x-10, size.y/2+10., 0);
        Pos3d complete_top_right_open_gl3 = renderer.screen2openGl(complete_top_right_screen3);
        Pos3d complete_left_down_open_gl3 = renderer.screen2openGl(complete_left_down_screen3);
        float width3 = (float) (complete_top_right_open_gl3.x - complete_left_down_open_gl3.x);
        float height3 = (float) (complete_top_right_open_gl3.y - complete_left_down_open_gl3.y);
        coord_overview3 = new MovableCoordinateSystem(this, complete_left_down_open_gl3, width3, height3 );

        ArrayList<Double> poly4 = new ArrayList<>(1);
        poly4.add(2.);
        Function f4 = new Function(poly4);
        index = coord_overview3.addFunction(f4, -4.0, 4);
        coord_overview3.setFunctionLocked(false, index);
        renderer.addMovable(coord_overview3);
    }

    /*!
     * \brief setGUIFunctions
     * set all OnTouch-, Onchange-, and Onwhatever events to private connected Gui elements.
     */
    private void setGUIFunctions(){
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
            }
        });
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

    private void save(){
        // TODO
        if(is_new_template){

        }else{

        }
    }

    private void saveAndExit(){
        save();
        exit();
    }

    private void exit(){
        renderer.close();
        this.finish();
    }

    public void onBackPressed(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        saveAndExit();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        exit();
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.edit_template_save_dialog)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
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
            is_new_template = true;
        }else{
            is_new_template = false;
        }
    }
    // UI stuff
    private GLSurfaceView gl_view;
    private OpenGLRenderer renderer;

    private Button button_save;
    boolean is_new_template;

    MovableCoordinateSystem coord_overview;
    MovableCoordinateSystem coord_overview2;
    MovableCoordinateSystem coord_overview3;

    // DEBUG
    private TextView seeker_y;
    private TextView seeker_x;
    final boolean DEBUG_TOUCH = false;
}