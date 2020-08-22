package de.jakobimatrix.intervallometer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

enum MSG{FIX,DISMISS,ERROR};
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // to trigger the rotation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        db = new DB(this);

        loadSettings();
        connectWithGUI();
        setButtonFunctions();
        loadTemplates();
        //setUpBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates();
    }

    /*!
     * \brief loadSettings Load the settings from a DB.
     */
    private void loadSettings(){
        // Todo save and load settings
        settings = Settings.getInstance(this);
    }

    private void loadTemplates(){
        LinkedHashMap<Integer, String> available_templates = db.getAllFunctionNames();
        LinearLayout ll = (LinearLayout) findViewById(R.id.scroll_view_templates_layout);
        for (Map.Entry<Integer, String> entry : available_templates.entrySet()) {
            if(!selected.containsKey(entry.getKey())){
                // new template
                selected.put(entry.getKey(), false);
                TextView tv = getTextViewTemplate(entry.getValue(), entry.getKey());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(10, 5, 10, 5);
                ll.addView(tv, params);
            }
        }
        enableGuiElementsIf();
    }

    private TextView getTextViewTemplate(String s, final int id){
        TextView tv = new TextView(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv.setBackgroundColor(getColor(R.color.Secondary));
        }else{
            tv.setBackgroundColor(0x454745);
        }
        tv.setText(s);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv.setTextColor(getColor(R.color.Letters));
        }else{
            tv.setTextColor(0xDED2BA);
        }
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected.put(id, !selected.get(id));
                if(selected.get(id)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        v.setBackgroundColor(getColor(R.color.Selected));
                    }else{
                        v.setBackgroundColor(0x4547FF);
                    }
                    selected_view.put(id, v);
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        v.setBackgroundColor(getColor(R.color.Secondary));
                    }else{
                        v.setBackgroundColor(0x454745);
                    }
                    selected_view.remove(id);
                }
                enableGuiElementsIf();
            }
        });

        return tv;
    }

    private int getNumSelected(){
        return selected_view.size();
    }

    void enableGuiElementsIf(){
        int num_selected = getNumSelected();
        boolean enable_copy_and_delete = num_selected > 0;
        boolean enable_execute = num_selected == 1;

        copy_template_button.setEnabled(enable_copy_and_delete);
        delete_template_button.setEnabled(enable_copy_and_delete);
        run_selected_template_button.setEnabled(enable_execute);

        // Todo visualize
    }

    /*!
     * \brief connectWithUI Connects all buttons and input fields with private variables.
     */
    private void connectWithGUI(){
        bluetooth_connection_button = (Button) findViewById(R.id.button_bt);
        delete_template_button = (Button) findViewById(R.id.button_delete_selection);
        add_new_template_button = (Button) findViewById(R.id.button_add_new_template);
        copy_template_button = (Button) findViewById(R.id.button_copy_selected_template);
        run_selected_template_button = (Button) findViewById(R.id.button_start_intervalometer);
        open_settings_button = (Button) findViewById(R.id.button_settings);
        bluetooth_status = (TextView) findViewById(R.id.bt_status);
        spinner_bluetooth_device = (Spinner) findViewById(R.id.spinner_bluetooth_device);
    }

    private void setButtonFunctions(){
        open_settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingsActivity();
            }
        });
        delete_template_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedTemplates();
            }
        });

        add_new_template_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditTemplateActivity(getSelectedTemplate());
            }
        });

        copy_template_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copySelectedTemplates();
            }
        });

        run_selected_template_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getNumSelected() > 0) {
                    sendFunction2Device();
                }
            }
        });
    }

    private void sendFunction2Device(){
        int id = getSelectedTemplate();
        if(id > 0){
            JSONArray ja = db.getFunctionDescription(id);
            ArrayList<Function> fs = new ArrayList<>();
            ArrayList<Double> start = new ArrayList<>();
            ArrayList<Double> stop = new ArrayList<>();
            try {
                db.Json2Function(this, fs, start, stop, ja);
            } catch (JSONException e) {
                e.printStackTrace();
                //todo tell user
            }

            byte[] buffer = {};

            for(int i = 0; i < fs.size(); i++){
                Function f = fs.get(i);
                double begin = start.get(i);
                double end = start.get(i);
                buffer = Utility.concatAll(buffer,f.toByteStream((int) Math.round(begin), (int) Math.round(end)));
            }
            buffer = Utility.concatAll(buffer, new byte[]{Globals.SYMBOL_STOP});

            try {
                if(bluetooth_manager.send(buffer)){
                    Toast.makeText(this, "Successful Transmission", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "Transmission failed", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                PrintInfoMsg("Transmittion failed with exeption throw", MSG.ERROR, send_sample);
            }
        }
    }

    private int getSelectedTemplate(){
        Iterator it = selected_view.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            return (int) pair.getKey();
        }
        return -1;
    }

    private void startSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startEditTemplateActivity(int selected){
        Intent intent = new Intent(this, EditTemplateActivity.class);
        Bundle b = new Bundle();
        b.putInt(ActivityParameters.selectedTemplate, selected);
        intent.putExtras(b);
        startActivity(intent);
    }

    private void deleteSelectedTemplates(){
        Iterator it = selected.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if((boolean) pair.getValue()){
                db.deleteFunctionDescription((Integer) pair.getKey());
                it.remove();
            }
        }

        LinearLayout ll = (LinearLayout) findViewById(R.id.scroll_view_templates_layout);
        for (View v : selected_view.values()) {
            ll.removeView(v);
        }
        selected_view.clear();
    }

    private void copySelectedTemplates(){
        for (int id : selected_view.keySet()) {
            db.copyFunctionDescription(id);
        }
        loadTemplates();
    }

    private void setUpBluetooth(){
        if(bluetooth_manager == null){
            bluetooth_manager = new BluetoothManager(settings.getLastConnectedDeviceName());
        }else{
            // update visible devices
            bluetooth_manager.refresh();
        }

        if(!bluetooth_manager.isBluetoothSupported()){
            bluetooth_connection_button.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_disabled_24));
            }
            PrintInfoMsg(getString(R.string.bt_status_bt_unsupported), MSG.ERROR, shut_down);
            return;
        }
        if(!bluetooth_manager.isBluetoothEnabled()){
            bluetooth_connection_button.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_disabled_24));
            }
            PrintInfoMsg(getString(R.string.bt_status_bt_off), MSG.FIX, restart_bluetooth);
            return;
        }
        bluetooth_connection_button.setEnabled(true);
        if(bluetooth_manager.isConnected()){
            // connection with last device was successful
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_audio_24));
            }
            bluetooth_status.setText(getString(R.string.bt_status_connected) + " "  + settings.getLastConnectedDeviceName());
            bluetooth_connection_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        bluetooth_manager.disconnect();
                        setUpBluetooth();
                    } catch (IOException e) {
                        e.printStackTrace();
                        PrintInfoMsg(getString(R.string.msg_bt_disconnect_failed), MSG.ERROR, do_nothing);
                    }
                }
            });
        }else{
            // no device connected
            bluetooth_status.setText(getString(R.string.bt_status_disconnected));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_24));
            }
            bluetooth_connection_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    enableSpinnerBluetooth(true);
                    bluetooth_status.setText(getString(R.string.bt_status_select_device));
                    ArrayList<String> spinnerArray = new ArrayList<String>();
                    bluetooth_manager.getPairedDeviceNames(spinnerArray);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (getBaseContext(), android.R.layout.simple_spinner_item, spinnerArray);
                    adapter.setDropDownViewResource(android.R.layout
                            .simple_spinner_dropdown_item);

                    user_is_interacting = false;
                    spinner_bluetooth_device.setAdapter(adapter);
                    spinner_bluetooth_device.setSelection(0,false);
                    spinner_bluetooth_device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view,
                                                   int position, long id) {
                            if(!user_is_interacting){
                                return;
                            }
                            final String chosen_device = (String) parent.getItemAtPosition(position);
                            try {
                                bluetooth_manager.connect(chosen_device);
                                enableSpinnerBluetooth(false);
                                settings.setLastConnectedDeviceName(chosen_device);
                                setUpBluetooth();
                            } catch (IOException e) {
                                e.printStackTrace();
                                PrintInfoMsg(getString(R.string.msg_bt_connecting_failed)
                                        + " " + chosen_device + " "
                                        + getString(R.string.msg_failed), MSG.FIX, restart_bluetooth);
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // TODO Auto-generated method stub
                        }
                    });
                }
            });
        }
    }

    private void enableSpinnerBluetooth(boolean enable){
        ViewGroup.LayoutParams layout = spinner_bluetooth_device.getLayoutParams();
        if(enable){
            layout.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }else{
            layout.height = 0;
        }
        spinner_bluetooth_device.setLayoutParams(layout);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        user_is_interacting = true;
    }

    /*!
     * \brief PrintInfoMsg Display a message for the user which can be dismissed by clicking on it.
     * \param msg The to be displayed message
     * \param type What kind of message that is (for appearance)
     * \param func A function which should be called, if the user dismisses the message.
     */
    private void PrintInfoMsg(String msg, MSG type, final CallBackVoid func){
        TextView tv = new TextView(this);
        final int new_id = msg_id;
        msg_id++;
        msgs.put(new_id, tv);
        // Needed since I dont know when the text is really written and displayed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            tv.addOnLayoutChangeListener( new View.OnLayoutChangeListener()
            {
                public void onLayoutChange( View v,
                                            int left,    int top,    int right,    int bottom,
                                            int leftWas, int topWas, int rightWas, int bottomWas )
                {
                    moveTableVieweToMakePlaceForMessagesWhichIKnowVeryUglyDontJugeMe();
                }
            });
        }
        switch (type){
            case FIX:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tv.setTextColor(getColor(R.color.WARNING));
                }else{
                    tv.setTextColor(0xFFFFA500);
                }
                break;
            case DISMISS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tv.setTextColor(getColor(R.color.Letters));
                }else{
                    tv.setTextColor(0xFFDED2BA);
                }
                break;
            case ERROR:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tv.setTextColor(getColor(R.color.ERROR));
                }else{
                    tv.setTextColor(0xFFFF0000);
                }
                break;
            default: // gibts für sowas keine statischen compiler?!
                throw new IllegalStateException("Unexpected value: " + type);
        }

        CharSequence text = "\n" + msg + "\n\n" + func.dismiss_info() + "\n";
        tv.setText(text);

        // let the info box vanish on clicking on it by removing the margin of the table layout.
        tv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // change the layout to trigger on layout change listener
                func.callback();
                final Integer currentID = new_id;
                deleteMsg(currentID);
            }
        });
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0f));
        tv.setClickable(true);
        tv.setFocusable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tv.setTextAlignment(tv.TEXT_ALIGNMENT_CENTER);
        }
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setId(new_id);
        //msg_layout.addView(tv);
    }

    /*!
     * \brief CallBackVoid A function interface to give that as a parameter to another function since Java has no std::function.
     */
    public interface CallBackVoid{
        CharSequence dismiss_info();
        void callback();
    }

    final private CallBackVoid do_nothing = new CallBackVoid(){
        public CharSequence dismiss_info(){return getString(R.string.msg_click_to_dismiss);}
        public void callback(){}
    };

    final private CallBackVoid restart_bluetooth = new CallBackVoid(){
        public CharSequence dismiss_info(){return getString(R.string.msg_click_after_fix);}
        public void callback(){setUpBluetooth();}
    };

    final private CallBackVoid shut_down = new CallBackVoid(){
        public CharSequence dismiss_info(){return getString(R.string.msg_click_to_shutdown);}
        public void callback(){finish();}
    };

    final private CallBackVoid read = new CallBackVoid() {
        @Override
        public CharSequence dismiss_info(){return "DEBUG click to red";}

        @Override
        public void callback() {
            Log.i("callbackRead", "called back");
            StringBuilder msg = new StringBuilder("");
            try {
                if(bluetooth_manager.read(4, msg)){
                    PrintInfoMsg("Next picture in "+msg+"ms", MSG.DISMISS, read);
                }else{
                    PrintInfoMsg("nothing to read", MSG.DISMISS, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
                PrintInfoMsg("reading throw exeption", MSG.DISMISS, read);
            }
        }
    };

    final private CallBackVoid send_sample = new CallBackVoid(){
        public CharSequence dismiss_info(){return "DEBUG click to transmit working example";}
        public void callback(){
// const , 15 pictures , c = 500 (alle 500 ms)

            byte buffer_temp_a[] = new byte[4];
            byte buffer_temp_b[] = new byte[4];
            byte buffer_temp_c[] = new byte[4];
            Utility.int2Bytes(1000, buffer_temp_c);
            byte[] buffer_f1 = {0x01, 0x00,0x00,0x00,0x0F, buffer_temp_c[0],buffer_temp_c[1],buffer_temp_c[2],buffer_temp_c[3]};
            Utility.int2Bytes(100, buffer_temp_b);
            byte[] buffer_f2 = {0x02, 0x00,0x00,0x00,0x0A,
                    buffer_temp_c[0],buffer_temp_c[1],buffer_temp_c[2],buffer_temp_c[3],
                    buffer_temp_b[0],buffer_temp_b[1],buffer_temp_b[2],buffer_temp_b[3]};
            Utility.int2Bytes(-100, buffer_temp_b);
            Utility.int2Bytes(2000, buffer_temp_c);
            byte[] buffer_f3 = {0x02, 0x00,0x00,0x00,0x0A,
                    buffer_temp_c[0],buffer_temp_c[1],buffer_temp_c[2],buffer_temp_c[3],
                    buffer_temp_b[0],buffer_temp_b[1],buffer_temp_b[2],buffer_temp_b[3]};
            byte[] buffer_fEnd = {0x00};
            byte[] combined = Utility.concatAll(buffer_f1, buffer_f2, buffer_f3, buffer_fEnd);
            try {
                if(bluetooth_manager.send(combined)){
                    PrintInfoMsg("Transmittion successful", MSG.DISMISS, send_sample);
                }else{
                    PrintInfoMsg("Transmittion failed", MSG.ERROR, send_sample);
                }
            } catch (IOException e) {
                e.printStackTrace();
                PrintInfoMsg("Transmittion failed with exeption throw", MSG.ERROR, send_sample);
            }
        }
    };

    /*!
     * \brief getMessageHeight Calculates the height of all messages and returns the sum.
     * \return sum of all heights.
     */
    private int getMessageHeight(){
        final Collection<TextView> tvs = msgs.values();
        int h = 0;
        for (TextView tv : tvs) {
            h = h + tv.getHeight();
        }
        return h;
    }

    /*!
     * \brief deleteMsg Delete the message which ids was given
     * \param id The message to delete.
     */
    private void deleteMsg(final Integer id){
        ((ViewManager)msgs.get(id).getParent()).removeView(msgs.get(id));
        msgs.remove(id);
        moveTableVieweToMakePlaceForMessagesWhichIKnowVeryUglyDontJugeMe();
    }

    /*!
     * \brief moveTableVieweToMakePlaceForMessagesWhichIKnowVeryUglyDontJugeMe moves the table view to show messages underneath.
     */
    private void moveTableVieweToMakePlaceForMessagesWhichIKnowVeryUglyDontJugeMe(){
       // DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) table_layout.getLayoutParams();
        //params.setMargins(0,getMessageHeight(),0,0);
        //table_layout.setLayoutParams(params);
    }

    private Settings settings;
    private Vector<String> paired_devices;
    private BluetoothManager bluetooth_manager = null;

    // UI
    private Button bluetooth_connection_button;
    private TextView bluetooth_status;
    private Button delete_template_button;
    private Button add_new_template_button;
    private Button copy_template_button;
    private Button run_selected_template_button;
    private Button open_settings_button;
    private Map<Integer,TextView> msgs = new HashMap<>();
    private Integer msg_id = 0;
    private Spinner spinner_bluetooth_device;
    private boolean user_is_interacting;

    private DB db;
    private LinkedHashMap<Integer, Boolean> selected = new LinkedHashMap<>();
    private LinkedHashMap<Integer, View> selected_view = new LinkedHashMap<>();
}