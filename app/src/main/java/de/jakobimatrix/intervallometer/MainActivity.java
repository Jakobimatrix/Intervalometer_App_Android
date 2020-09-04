package de.jakobimatrix.intervallometer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

enum MSG{FIX,DISMISS,ERROR};
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // to trigger the rotation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        db = new DB(this);
        bluetooth_manager = new BluetoothManager();

        loadSettings();
        connectWithGUI();
        setGuiFunctions();
        loadTemplates(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates(true);
    }

    /*!
     * \brief loadSettings Load the settings from a DB.
     */
    private void loadSettings(){
        // Todo save and load settings
        settings = Settings.getInstance(this);
    }

    private void loadTemplates(boolean reload){
        LinkedHashMap<Integer, String> available_templates = db.getAllFunctionNames();
        LinearLayout ll = (LinearLayout) findViewById(R.id.scroll_view_templates_layout);
        if(reload){
            selected.clear();
            selected_view.clear();
            ll.removeAllViews();
        }
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
        boolean enable_execute = (num_selected == 1) && (bluetooth_manager.isConnected());
        boolean edit_template = (num_selected == 1);

        copy_template_button.setEnabled(enable_copy_and_delete);
        delete_template_button.setEnabled(enable_copy_and_delete);
        run_selected_template_button.setEnabled(enable_execute);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if(enable_copy_and_delete){
                copy_template_button.setBackground(getDrawable(R.drawable.ic_copy_template));
                delete_template_button.setBackground(getDrawable(R.drawable.ic_bin_icon));
            }else{
                copy_template_button.setBackground(getDrawable(R.drawable.ic_copy_template_disabled));
                delete_template_button.setBackground(getDrawable(R.drawable.ic_bin_icon_disabled));
            }
            if(enable_execute){
                run_selected_template_button.setBackground(getDrawable(R.drawable.ic_start_intervalometer));
            }else{
                run_selected_template_button.setBackground(getDrawable(R.drawable.ic_start_intervalometer_disabled));
            }
            if(num_selected == 0){
                add_new_template_button.setBackground(getDrawable(R.drawable.ic_new_template));

            }else if (num_selected == 1){
                add_new_template_button.setBackground(getDrawable(R.drawable.ic_edit_template));
            }else{
                add_new_template_button.setBackground(getDrawable(R.drawable.ic_unselect_templates));
            }
        }
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
        status_msg = (TextView) findViewById(R.id.status_msg);
        spinner_bluetooth_device = (Spinner) findViewById(R.id.spinner_bluetooth_device);
    }

    private void setGuiFunctions(){
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
                enableGuiElementsIf();
            }
        });

        add_new_template_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getNumSelected() <2) {
                    startEditTemplateActivity(getSelectedTemplate());
                }else{
                    uncheckAll();
                }
                enableGuiElementsIf();
            }
        });

        copy_template_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copySelectedTemplates();
                enableGuiElementsIf();
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
        setGuiFunctionsBt();
    }

    private void uncheckAll() {
        while(selected_view.size() > 0){
            Iterator it = selected_view.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                View v = (View) pair.getValue();
                v.performClick();
            }
        }
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
                status_msg.setText(getString(R.string.db_read_error));
                return;
            }

            byte[] buffer = {};

            for(int i = 0; i < fs.size(); i++){
                Function f = fs.get(i);
                double begin = start.get(i);
                double end = stop.get(i);
                byte[] buffer_f = f.toByteStream((int) Math.round(begin), (int) Math.round(end));
                buffer = Utility.concatAll(buffer, buffer_f);
            }

            if(buffer.length > Globals.NUM_SUPPORTED_FUNCTION_BYTES){
                status_msg.setText(String.format(getString(R.string.transmission_failed_too_long), Globals.NUM_SUPPORTED_FUNCTION_BYTES, buffer.length));
                return;
            }
            buffer = Utility.concatAll(buffer, new byte[]{Globals.SYMBOL_STOP});

            Utility.bytes2string(buffer, buffer.length);

            try {
                if(bluetooth_manager.send(buffer)){
                    status_msg.setText(getString(R.string.transmission) + ": " + Utility.bytes2string(buffer, buffer.length));
                }else{
                    status_msg.setText(getString(R.string.transmission_failed));
                }
            } catch (IOException e) {
                e.printStackTrace();
                status_msg.setText(getString(R.string.transmission_failed) + " " + String.valueOf(e.getMessage()));
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
        loadTemplates(false);
    }

    private void setGuiFunctionsBt(){
        bluetooth_connection_button.setEnabled(false);
        if(!bluetooth_manager.isBluetoothSupported()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_disabled_24));
            }
            return;
        }
        if(!bluetooth_manager.isBluetoothEnabled()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_disabled_24));
            }
            return;
        }
        bluetooth_connection_button.setEnabled(true);
        updateBtDeviceSpinner();
        setBtButtonFunction();
    }

    private void setBtButtonFunction(){
        if(bluetooth_manager.isConnected()){
            enableSpinnerBluetooth(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_audio_24));
            }
            status_msg.setText(getString(R.string.bt_status_connected) + " "  + settings.getLastConnectedDeviceName());
            bluetooth_connection_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        bluetooth_manager.disconnect();
                        setBtButtonFunction();
                    } catch (IOException e) {
                        e.printStackTrace();
                        status_msg.setText(R.string.msg_bt_disconnect_failed);
                    }
                    enableGuiElementsIf();
                }
            });
        }else{
            // no device connected
            enableSpinnerBluetooth(true);
            updateBtDeviceSpinner();
            status_msg.setText(getString(R.string.bt_status_disconnected));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetooth_connection_button.setBackground(getDrawable(R.drawable.ic_baseline_bluetooth_24));
            }
            bluetooth_connection_button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String chosen_device = (String) spinner_bluetooth_device.getSelectedItem();
                    try {
                        if(!bluetooth_manager.connect(chosen_device)){
                            status_msg.setText(R.string.msg_bt_connecting_failed + " "+ chosen_device + " " + R.string.msg_failed);
                            return;
                        }
                        settings.setLastConnectedDeviceName(chosen_device);
                        setBtButtonFunction();
                    } catch (IOException e) {
                        e.printStackTrace();
                        status_msg.setText(R.string.msg_bt_connecting_failed + " "+ chosen_device + " " + R.string.msg_failed + " an exception was thrown.");
                    }
                    enableGuiElementsIf();
                }
            });
        }
    }

    private void updateBtDeviceSpinner(){
        bluetooth_manager.refresh();
        status_msg.setText(getString(R.string.bt_status_select_device));
        ArrayList<String> spinnerArray = new ArrayList<>();
        bluetooth_manager.getPairedDeviceNames(spinnerArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (getBaseContext(), android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);

        int position = 0;
        String default_device = settings.getLastConnectedDeviceName();
        for(int i = 0; i < spinnerArray.size(); i++){
            String s = spinnerArray.get(i);
            if(s.equals(default_device)){
                position = i;
                break;
            }
        }

        spinner_bluetooth_device.setAdapter(adapter);
        spinner_bluetooth_device.setSelection(position,false);
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
    protected void onDestroy() {
        super.onDestroy();
        try {
            bluetooth_manager.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Settings settings;
    private BluetoothManager bluetooth_manager = null;

    // UI
    private Button bluetooth_connection_button;
    private TextView status_msg;
    private Button delete_template_button;
    private Button add_new_template_button;
    private Button copy_template_button;
    private Button run_selected_template_button;
    private Button open_settings_button;
    private Spinner spinner_bluetooth_device;

    private DB db;
    private LinkedHashMap<Integer, Boolean> selected = new LinkedHashMap<>();
    private LinkedHashMap<Integer, View> selected_view = new LinkedHashMap<>();
}