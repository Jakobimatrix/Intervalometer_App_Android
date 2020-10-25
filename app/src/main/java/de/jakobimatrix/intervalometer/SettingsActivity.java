package de.jakobimatrix.intervalometer;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.ClipboardManager;
import android.widget.Toast;

import java.util.ArrayList;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        settings = Settings.getInstance(this);
        connectWithGUI();
        setGUIFunctions();

        app_info.setText(Html.fromHtml(getAppInfo()));
    }

    private String getAppInfo() {
        String text = "<b>"+getString(R.string.author)+":</b> " + Globals.AUTHOR+ "<br>" +
                "<b>"+getString(R.string.version)+":</b>: V" +  BuildConfig.VERSION_CODE + " - "+ BuildConfig.VERSION_NAME + " &#10096;" + BuildConfig.BUILD_TYPE + "&#10097;<br>" +
                "<br>" +
                "Android Source Code: <a href=\"" + Globals.ANDROID_CODE_URL + "\">"+Globals.ANDROID_CODE_URL+"</a><br>" +
                "Hardware Source Code: <a href=\"" + Globals.HARDWARE_CODE_URL + "\">"+Globals.HARDWARE_CODE_URL+"</a> ";

        return text;
    }

    private void connectWithGUI(){
        spinner_fps = (Spinner)findViewById(R.id.spinner_fps);
        min_delay_ms = (EditText)findViewById(R.id.editShutterDelay);
        min_delay_ms.setText(Integer.toString(settings.getMinPeriodMs()));
        app_info = (TextView)findViewById(R.id.app_info);
        home = (Button)findViewById(R.id.button_home);
    }

    private void setGUIFunctions(){
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        ArrayList<String> spinner_array = new ArrayList<String>(Settings.FRAME_RATES_LOOKUP.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, R.layout.spinner_layout, spinner_array);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinner_fps.setAdapter(adapter);
        spinner_fps.setSelection(settings.getEditTemplateFpsId() > -1?settings.getEditTemplateFpsId():Settings.DEFAULT_FPS_ID);

        spinner_fps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String fps_s = (String) spinner_fps.getSelectedItem();
                settings.setEditTemplateFpsId(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        min_delay_ms.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String min_delay_ms_s = min_delay_ms.getText().toString();
                int min_delay_ms_i = Integer.decode(min_delay_ms_s);
                if(min_delay_ms_i > 0){
                    settings.setMinPeriodMs(min_delay_ms_i);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        app_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(app_info.getText().toString());
                Toast.makeText(getApplicationContext(), getString(R.string.version_info_copied), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    private void exit(){
        this.finish();
    }

    //GUI
    Spinner spinner_fps;
    EditText min_delay_ms;
    TextView app_info;
    Button home;

    Settings settings;
}