package de.jakobimatrix.intervallometer;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditTemplateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        charToBitmapConverter.init(getBaseContext());
        setContentView(R.layout.activity_edit_template);

        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(screen_size);
        Globals.screen_width = screen_size.x;
        Globals.screen_height = screen_size.y;

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

        buttons[SAVE_BTN] = (Button) findViewById(R.id.button_save);
        buttons[MV_UP_BTN] = (Button) findViewById(R.id.move_up);
        buttons[MV_LEFT_BTN] = (Button) findViewById(R.id.move_left);
        buttons[MV_RIGHT_BTN] = (Button) findViewById(R.id.move_right);
        buttons[MV_DOWN_BTN] = (Button) findViewById(R.id.move_down);
        buttons[INFO_BTN] = (Button) findViewById(R.id.info);

        // dynamically set width to have squared buttons
        int button_width = getButtonWidth();
        for(int i = 0; i < NUM_BUTTONS; i++){
            ViewGroup.LayoutParams layout = buttons[i].getLayoutParams();
            layout.height = button_width;
            layout.width = button_width;
            buttons[i].setLayoutParams(layout);
        }
        // Set negative margin (-button_width) to buttonLayout since it is to the right of the gl_view.
        // That will bring it into the screen.
        LinearLayout ll = (LinearLayout) findViewById(R.id.buttonLayout);
        RelativeLayout.LayoutParams params_ll = (RelativeLayout.LayoutParams) ll.getLayoutParams();
        params_ll.width = button_width;
        params_ll.setMargins(-button_width, 0, 0, 0);
        ll.setLayoutParams(params_ll);

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
        // for the symbols
        double MARGIN_RIGHT = getButtonWidth();

        Pos3d bot_left_screen = new Pos3d(0, screen_size.y, 0);
        Pos3d top_right_screen = new Pos3d(screen_size.x - MARGIN_RIGHT, 0, 0);
        Pos3d bot_left_view_gl  = Utility.screen2openGl(bot_left_screen);
        Pos3d top_right_view_gl  = Utility.screen2openGl(top_right_screen);
        float width = (float) Math.abs(top_right_view_gl.x - bot_left_view_gl.x);
        float height = (float) Math.abs(top_right_view_gl.y - bot_left_view_gl.y);

        coord_overview = new MovableCoordinateSystem(this, bot_left_view_gl, width, height);

        int index;

        Function f1 = new ConstantFunction(2);
        index = coord_overview.addFunction(f1, 0., 4.);

        Function f2 = new LinearFunction(-2, 1);
        index = coord_overview.addFunction(f2, 4., 8.);

        Function f3 = new ConstantFunction(6);
        index = coord_overview.addFunction(f3, 8., 12.);

        Pos3d  left = new Pos3d(12,6,0);
        Pos3d  right = new Pos3d(16,0,0);
        Function f4 = new SigmoidFunction(left, right);
        index = coord_overview.addFunction(f4, left.x, right.x);

        Function f5 = new LinearFunction(-right.x, 1);
        index = coord_overview.addFunction(f5, right.x, right.x+4);

        coord_overview.stickToGrid(new Pos3d(1,1,0));
        coord_view_id = renderer.addMovable(coord_overview);
    }

    /*!
     * \brief getButtonWidth
     * calculates the width of the left buttons such that thy are quadratic.
     */
    int getButtonWidth(){
        return screen_size.y/NUM_BUTTONS;
    }

    /*!
     * \brief setGUIFunctions
     * set all OnTouch-, Onchange-, and Onwhatever events to private connected Gui elements.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setGUIFunctions(){
        buttons[SAVE_BTN].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
            }
        });

        buttons[MV_UP_BTN].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    renderer.startTouchActionThread(CMD.UP, coord_overview);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    renderer.stopAnalogActionThread();
                    coord_overview.manuelEndTouch();
                }
                return false;
            }
        });

        buttons[MV_DOWN_BTN].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    renderer.startTouchActionThread(CMD.DOWN, coord_overview);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    renderer.stopAnalogActionThread();
                    coord_overview.manuelEndTouch();
                }
                return false;
            }
        });

        buttons[MV_LEFT_BTN].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    renderer.startTouchActionThread(CMD.LEFT, coord_overview);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    renderer.stopAnalogActionThread();
                    coord_overview.manuelEndTouch();
                }
                return false;
            }
        });

        buttons[MV_RIGHT_BTN].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    renderer.startTouchActionThread(CMD.RIGHT, coord_overview);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    renderer.stopAnalogActionThread();
                    coord_overview.manuelEndTouch();
                }
                return false;
            }
        });


        buttons[INFO_BTN].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO show how many photos, how long the video (depend on fps) and how long it takes to take all pictures.
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

            debug_seeker.setPosition(Utility.screen2openGl(new Pos3d(x, y, 0)));
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

    final static int NUM_BUTTONS = 6;
    final static int SAVE_BTN = 0;
    final static int MV_UP_BTN = 1;
    final static int MV_LEFT_BTN = 2;
    final static int MV_RIGHT_BTN = 3;
    final static int MV_DOWN_BTN = 4;
    final static int INFO_BTN = 5;
    private Button[] buttons = new Button[NUM_BUTTONS];

    boolean is_new_template;

    MovableCoordinateSystem coord_overview;
    int coord_view_id = -1;

    AlphabetDatabase charToBitmapConverter = AlphabetDatabase.getInstance();

    Point screen_size = new Point();;

    // DEBUG
    private TextView seeker_y;
    private TextView seeker_x;
    final boolean DEBUG_TOUCH = false;
    MovableDot debug_seeker;
}