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

public class EditTemplateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        charToBitmapConverter.init(getBaseContext());

        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(screen_size);

        // landscape x <--> y
        int temp = screen_size.x;
        screen_size.x = screen_size.y;
        screen_size.x = temp;

        screen_viewport = new ViewPort(new Pos3d(0,screen_size.y,0), new Pos3d(screen_size.x,0,0));

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
        renderer = new OpenGLRenderer(this, screen_size.x, screen_size.y);
        gl_view.setRenderer(renderer);

        button_save = (Button) findViewById(R.id.button_save);

        // DEBUG
        if(DEBUG_TOUCH) {
            seeker_y = (TextView) findViewById(R.id.seeker_y);
            seeker_x = (TextView) findViewById(R.id.seeker_x);

            debug_seeker = new MovableDot(getBaseContext(),
                    new Pos3d(0, 0, 0), 0.1f);
            renderer.addMovable(debug_seeker);
        }
    }

    private void setUpCoordSystem(){
        // TODO To show the x,y ticks
        // TODO THIS SHOULD BE DONE BY MovableCoordinateSystem
        double MARGIN_BOT = 75;
        double MARGIN_LEFT = 75;
        double MARGIN_TOP = 75;

        // for the symbols
        float MARGIN_RIGHT = getResources().getDimension(R.dimen.min_clickable_button_size);

        Pos3d bot_left_screen = new Pos3d(0 + MARGIN_BOT, screen_size.y - MARGIN_LEFT, 0);
        Pos3d top_right_screen = new Pos3d(screen_size.x - MARGIN_RIGHT, 0 + MARGIN_TOP, 0);
        ViewPort coord_screen = new ViewPort(bot_left_screen, top_right_screen);
        ViewPort coord_sys_view_gl = renderer.screen2openGL(coord_screen);
        coord_overview = new MovableCoordinateSystem(this, coord_sys_view_gl.min, (float) coord_sys_view_gl.widthAbs(), (float) coord_sys_view_gl.heightAbs());


        Function f1 = new ConstantFunction(2);
        int index = coord_overview.addFunction(f1, 0., 4.);
        coord_overview.setFunctionLocked(false, index);

        Function f2 = new LinearFunction(-2, 1);
        index = coord_overview.addFunction(f2, 4., 8.);
        coord_overview.setFunctionLocked(false, index);

        Function f3 = new ConstantFunction(6);
        index = coord_overview.addFunction(f3, 8., 12.);
        coord_overview.setFunctionLocked(false, index);

        Pos3d  left = new Pos3d(0,0,0);
        Pos3d  right = new Pos3d(2,2,0);
        Function f4 = new SigmoidFunction(left, right);

        coord_overview.stickToGrid(new Pos3d(1,1,0));
        renderer.addMovable(coord_overview);
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

            debug_seeker.setPosition(renderer.screen2openGl(new Pos3d(x, y, 0)));
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


        setUpCoordSystem();
    }
    // UI stuff
    private GLSurfaceView gl_view;
    private OpenGLRenderer renderer;

    private Button button_save;
    boolean is_new_template;

    MovableCoordinateSystem coord_overview;

    AlphabetDatabase charToBitmapConverter = AlphabetDatabase.getInstance();

    ViewPort screen_viewport = null;
    Point screen_size = new Point();;

    // DEBUG
    private TextView seeker_y;
    private TextView seeker_x;
    final boolean DEBUG_TOUCH = false;
    MovableDot debug_seeker;
}