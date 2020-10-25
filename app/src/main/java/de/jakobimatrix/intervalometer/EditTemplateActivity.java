package de.jakobimatrix.intervalometer;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class EditTemplateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        charToBitmapConverter.init(getBaseContext());
        setContentView(R.layout.activity_edit_template);

        settings = Settings.getInstance(this);
        db = new DB(this);
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(screen_size);
        Globals.screen_width = screen_size.x;
        Globals.screen_height = screen_size.y;

        Iterator it = MovableCoordinateSystem.Y_UNIT_LOOKUP.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Y_UNIT y_unit = (Y_UNIT) pair.getKey();
            y_units_s.add(Utility.Y_UNIT2String(this, y_unit));
        }

        connectWithGUI();
        loadTemplate(); // before setGUIFunctions!
        setGUIFunctions();
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

        //Info
        total_number_frames_output = (TextView) findViewById(R.id.total_number_frames_output);
        total_clip_duration_output = (TextView) findViewById(R.id.total_clip_duration_output);
        total_recording_time_output = (TextView) findViewById(R.id.total_recording_time_output);
        number_frames_output = (TextView) findViewById(R.id.number_frames_output);
        clip_duration_output = (TextView) findViewById(R.id.clip_duration_output);
        recording_time_output = (TextView) findViewById(R.id.recording_time_output);
        functions_output = (TextView) findViewById(R.id.functions_output);

        function_name = (EditText) findViewById(R.id.function_name);
        choose_frame_rate = (Spinner) findViewById(R.id.choose_frame_rate);
        shutter_delay_spinner = (Spinner) findViewById(R.id.spinner_shutter_delay_unit);

        // DEBUG
        if(DEBUG_TOUCH) {
            seeker_y = (TextView) findViewById(R.id.seeker_y);
            seeker_x = (TextView) findViewById(R.id.seeker_x);

            debug_seeker = new MovableDot(getBaseContext(),
                    new Pos3d(0, 0, 0), 0.1f);
            renderer.addMovable(debug_seeker);
        }
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
                if(info_open){
                    closeFunctionInfo();
                    info_open = false;
                }else{
                    openFunctionInfo();
                    info_open = true;
                }
            }
        });

        ArrayList<String> spinner_array = new ArrayList<String>(Settings.FRAME_RATES_LOOKUP.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.spinner_layout, spinner_array);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        choose_frame_rate.setAdapter(adapter);
        choose_frame_rate.setSelection(settings.getEditTemplateFpsId() > -1?settings.getEditTemplateFpsId():Settings.DEFAULT_FPS_ID);

        choose_frame_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                settings.setEditTemplateFpsId(position);
                updateFunctionInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        adapter = new ArrayAdapter<String>
                (this, R.layout.spinner_layout, y_units_s);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        shutter_delay_spinner.setAdapter(adapter);
        Y_UNIT selected = coord_overview.getY_UNIT();
        String selected_s = Utility.Y_UNIT2String(this, selected);
        int selected_id = 0;
        for(int i = 0; i < y_units_s.size(); i++){
            if(selected_s.equals(y_units_s.get(i))){
                selected_id = i;
                break;
            }
        }
        shutter_delay_spinner.setSelection(selected_id);
        shutter_delay_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String y_unit_s = (String) shutter_delay_spinner.getSelectedItem();
                setY_Unit(y_unit_s);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setY_Unit(String y_unit_s){
        coord_overview.setYUnit(Utility.String2Y_UNIT(this, y_unit_s));
    }

    private void openFunctionInfo(){
        updateFunctionInfo();
        LinearLayout dialog_layout = (LinearLayout) findViewById(R.id.infoLayout);
        RelativeLayout.LayoutParams params_dialog_layout = (RelativeLayout.LayoutParams) dialog_layout.getLayoutParams();
        params_dialog_layout.width = screen_size.x - getButtonWidth();
        params_dialog_layout.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        dialog_layout.setLayoutParams(params_dialog_layout);
    }

    private void closeFunctionInfo(){
        LinearLayout dialog_layout = (LinearLayout) findViewById(R.id.infoLayout);
        RelativeLayout.LayoutParams params_dialog_layout = (RelativeLayout.LayoutParams) dialog_layout.getLayoutParams();
        params_dialog_layout.width = 0;
        params_dialog_layout.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        dialog_layout.setLayoutParams(params_dialog_layout);
    }

    private void updateFunctionInfo() {
        Vector<MovableFunction> functions = coord_overview.getFunctions();
        String total_number_frames_s = "-";
        String total_clip_duration_s = "-";
        String total_recording_time_s = "-";
        String number_frames_s = "";
        String clip_duration_s = "";
        String recording_time_s = "";
        String function_s = "";
        DecimalFormat format_seconds = new DecimalFormat("#.#");
        if(functions.size() > 0){
            double min_x = functions.get(0).getFunctionMinX();
            double max_x = functions.get(functions.size()-1).getFunctionMaxX();
            double Frames = Math.round(max_x - min_x) + 1;
            total_number_frames_s = (int) Frames + "";
            String fps_s = (String) choose_frame_rate.getSelectedItem();
            double clip_duration = Frames / Settings.FRAME_RATES_LOOKUP.get(fps_s);
            total_clip_duration_s = format_seconds.format(clip_duration) + getString(R.string._s);

            long duration = 0;

            for(int i = 0; i < functions.size(); i++){
                MovableFunction mf = functions.get(i);
                double x_max = mf.getFunctionMaxX();
                double x_min = mf.getFunctionMinX();
                int f_frames = (int) Math.round(x_max - x_min);
                if(i == 0){
                    f_frames++;
                }
                double x = mf.getFunctionMinX() + 1;
                Function f = mf.getFunction();
                double f_duration = 0;
                while(x < x_max){
                    f_duration += f.f(x);
                    x++;
                }
                f_duration += f.f(x_max);

                double f_clip_duration = f_frames / Settings.FRAME_RATES_LOOKUP.get(fps_s);
                String f_clip_duration_s = format_seconds.format(f_clip_duration) + getString(R.string._s);
                duration += f_duration;
                function_s += "f"+(i+1)+":";
                recording_time_s += Utility.millis2hms((long) f_duration);
                clip_duration_s += f_clip_duration_s;
                number_frames_s += f_frames;

                if(i < functions.size()-1){
                    function_s += "\n";
                    number_frames_s += "\n";
                    recording_time_s += "\n";
                    clip_duration_s += "\n";
                }
            }
            total_recording_time_s = Utility.millis2hms(duration);
        }
        total_number_frames_output.setText(total_number_frames_s);
        total_clip_duration_output.setText(total_clip_duration_s);
        total_recording_time_output.setText(total_recording_time_s);

        number_frames_output.setText(number_frames_s);
        clip_duration_output.setText(clip_duration_s);
        recording_time_output.setText(recording_time_s);
        functions_output.setText(function_s);
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

    private String getFunctionName(){
        String name = function_name.getText().toString();
        if(name.equals("")){
            Vector<MovableFunction> vmf = coord_overview.getFunctions();
            if(vmf.size()> 0) {
                for (MovableFunction mf : vmf) {
                    SUPPORTED_FUNCTION sf = Function.FunctionClass2Enum(mf.getFunction());
                    name += Function.FunctionEnum2String(this, sf) + "-";
                }
                name = name.substring(0, name.length() - 1);
            }
        }
        return name;
    }

    private void save(){
        if(coord_overview.getFunctions().size() > 0) {
            String name = getFunctionName();
            if (db_function_id > -1) {
                db.updateFunctionDescription(db_function_id, coord_overview.getFunctions(), name, coord_overview.getY_UNIT());
            } else {
                db.addFunctionDescription(coord_overview.getFunctions(), name, coord_overview.getY_UNIT());
            }
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
        db_function_id = -1;
        if(b != null) {
            db_function_id = b.getInt(ActivityParameters.selectedTemplate);
        }

        // for the symbols
        double MARGIN_RIGHT = getButtonWidth();

        Pos3d bot_left_screen = new Pos3d(0, screen_size.y, 0);
        Pos3d top_right_screen = new Pos3d(screen_size.x - MARGIN_RIGHT, 0, 0);
        Pos3d bot_left_view_gl  = Utility.screen2openGl(bot_left_screen);
        Pos3d top_right_view_gl  = Utility.screen2openGl(top_right_screen);
        float width = (float) Math.abs(top_right_view_gl.x - bot_left_view_gl.x);
        float height = (float) Math.abs(top_right_view_gl.y - bot_left_view_gl.y);
        coord_overview = new MovableCoordinateSystem(this, bot_left_view_gl, width, height);

        coord_overview.stickToGrid(new Pos3d(Settings.DEFAULT_SMALLEST_FRAME_TICK, Settings.DEFAULT_SMALLEST_DELAY_TICK_MS,0));
        coord_view_id = renderer.addMovable(coord_overview);

        if(db_function_id > -1){
            JSONArray functions_description = db.getFunctionDescription(db_function_id);
            int size = functions_description.length();
            ArrayList<Function> fs = new ArrayList<>(size);
            ArrayList<Double> start = new ArrayList<>(size);
            ArrayList<Double> stop = new ArrayList<>(size);
            try {
                DB.Json2Function(this, fs,  start, stop, functions_description);
                for(int i = 0; i < size; i++){
                    Function f = fs.get(i);
                    double x_min = start.get(i);
                    double x_max = stop.get(i);
                    coord_overview.addFunction(f, x_min, x_max);
                }
                String name = db.getFunctionName(db_function_id);
                Y_UNIT y_unit = db.getY_UNIT(db_function_id);
                coord_overview.setYUnit(y_unit);
                function_name.setText(name);
            } catch (JSONException e) {
                // todo inform user
                e.printStackTrace();
            }
        }
    }

    // UI stuff
    private GLSurfaceView gl_view;
    private OpenGLRenderer renderer;
    private TextView number_frames_output;
    private TextView clip_duration_output;
    private TextView recording_time_output;
    private TextView functions_output;
    private TextView total_number_frames_output;
    private TextView total_clip_duration_output;
    private TextView total_recording_time_output;
    private Spinner shutter_delay_spinner;

    private EditText function_name;
    private Spinner choose_frame_rate;

    final static int NUM_BUTTONS = 6;
    final static int SAVE_BTN = 0;
    final static int MV_UP_BTN = 1;
    final static int MV_LEFT_BTN = 2;
    final static int MV_RIGHT_BTN = 3;
    final static int MV_DOWN_BTN = 4;
    final static int INFO_BTN = 5;
    private Button[] buttons = new Button[NUM_BUTTONS];

    int db_function_id = -1;

    boolean info_open = false;

    MovableCoordinateSystem coord_overview;
    int coord_view_id = -1;

    AlphabetDatabase charToBitmapConverter = AlphabetDatabase.getInstance();
    DB db;
    Settings settings;

    Point screen_size = new Point();
    ArrayList<String> y_units_s = new ArrayList<>();

    // DEBUG
    private TextView seeker_y;
    private TextView seeker_x;
    final boolean DEBUG_TOUCH = false;
    MovableDot debug_seeker;
}