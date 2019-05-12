
package com.openjuggle.brunn;
import android.net.Uri;
import android.provider.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    DatabaseHelper myDb;
    TextFileHelper myTfh;
    FormatHelper myFh;
    String dbName = "brunn.db";
    AutoCompleteTextView choose_pattern_dialog_actv;
    List<String> list_of_list_names = Arrays.asList("proplist", "patternlist", "modifierlist", "specialthrowlist", "specialthrowsequencelist");
    List<String> list_of_props = new LinkedList<>();
    List<Integer> list_of_numbers = new LinkedList<>();
    List<String> list_of_patterns = new LinkedList<>();
    List<String> list_of_modifiers = new LinkedList<>();
    List<String> list_of_special_throws = new LinkedList<>();
    List<String> list_of_special_throw_sequences = new LinkedList<>();
    LineGraphSeries<DataPoint> graphseriescatch;
    LineGraphSeries<DataPoint> graphseriesdrop;
    private GraphView graph;
    public Boolean run_is_selected = true;
    public Boolean graph_is_selected = false;
    public Boolean history_is_selected = false;
    private Timer timer;
    private TextView timertext;
    public Boolean inRun = false;
    public Boolean there_are_completed_runs_not_yet_added_to_db;
    int currentVolume;
    public int run_duration;
    public int start_time_of_last_run = 0;
    private TextView prop_textview;
    String specifics_prop = "";
    private TextView pattern_textview;
    String specifics_pattern = "";
    String specifics_number = "";
    private TextView modifier_textview;
    String specifics_modifiers = "";
    private TextView special_throw_textview;
    String specifics_special_throws = "";
    String specifics_special_throws_sequences = "";
    private TextView personal_best_textview;

    private ListView session_runs_listview;
    private ArrayAdapter<String> session_runs_listview_adapter;
    private ArrayList<String> session_runs_arraylist;

    Handler volume_checker = new Handler();
    static final int delay = 300; //1 second=1000 millisecond, 15*1000=15seconds
    Runnable runnable;
    private float x1,x2;
    static final int MIN_SWIPE_DISTANCE = 150;
    private int prop_text_viewCycleIndex = 0;

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myTfh = new TextFileHelper(this);
        myFh = new FormatHelper();
        if(first_use_of_app()){
            do_first_use_of_app_stuff();
        }
        on_create_db();
        set_current_volume();
        fill_lists_from_text_files();
        prop_textview = findViewById(R.id.proptextview);
        try {
            prop_textview.setText(myDb.getAllFromColumn("PROP").get(myDb.getAllFromColumn("PROP").size()-1));
            specifics_prop = prop_textview.getText().toString();
        } catch (Exception e) {e.printStackTrace(); }

        prop_textview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TAG", "onTouch");
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("TAG", "ACTION_DOWN");
                    x1 = event.getX();
                }else if(event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("TAG", "ACTION_UP");
                    x2 = event.getX();
                    float deltaX = x2 - x1;
                    Log.d("TAG", Float.toString(deltaX));
                    if (deltaX > MIN_SWIPE_DISTANCE){
                        prop_text_viewCycleIndex++;
                        if (prop_text_viewCycleIndex==list_of_props.size()){
                            prop_text_viewCycleIndex = 0;
                        }
                        prop_textview.setText(list_of_props.get(prop_text_viewCycleIndex));
                        specifics_prop = list_of_props.get(prop_text_viewCycleIndex);
                    }
                    if (deltaX < -MIN_SWIPE_DISTANCE){
                        prop_text_viewCycleIndex--;
                        if (prop_text_viewCycleIndex<0){
                            prop_text_viewCycleIndex = list_of_props.size()-1;
                        }
                        prop_textview.setText(list_of_props.get(prop_text_viewCycleIndex));
                        specifics_prop = list_of_props.get(prop_text_viewCycleIndex);
                    }
                    specifics_changed();
                }
                return true;
            }
        });
        pattern_textview = findViewById(R.id.patterntextview);
        modifier_textview = findViewById(R.id.modifiertextview);
        special_throw_textview = findViewById(R.id.specialthrowtextview);
        personal_best_textview = findViewById(R.id.personalbesttextview);
        graph = findViewById(R.id.historygraph);
        timertext = findViewById(R.id.timertext);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                make_toast("fab");
                import_db();
                //do_first_use_of_app_stuff();
                //myTfh.appendTextFile(get_file_output_append_stream("patternlist"),"mytest2");
            }
        });
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb=(RadioButton)findViewById(checkedId);
                if (!inRun) {
                    run_is_selected = graph_is_selected = history_is_selected = false;
                    if (rb.getText().toString().equals("Run")) {
                        set_widgets_visibility("run");
                        run_is_selected = true;
                    } else if (rb.getText().toString().equals("Graph")) {
                        set_widgets_visibility("graph");
                        graph_is_selected = true;
                    } else if (rb.getText().toString().equals("History")) {
                        set_widgets_visibility("history");
                        history_is_selected = true;
                    }
                }else{
                    RadioButton b = (RadioButton) findViewById(R.id.run_radioButton);
                    b.setChecked(true);
                    make_toast("In a run");
                }
            }
        });
        final Button propbutton = findViewById(R.id.propbutton);
        propbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_prop_dialog, null);
                final AutoCompleteTextView choosepropdialogactv = view.findViewById(R.id.propinputactv);
                choosepropdialogactv.setText(prop_textview.getText());
                ArrayAdapter<String> choosepropdialogactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_props);
                choosepropdialogactv.setAdapter(choosepropdialogactvAdapter);
                choosepropdialogactv.setThreshold(0);//this is number of letters that must match for autocomplete
                choosepropdialogactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
                choosepropdialogactv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosepropdialogactv.showDropDown();
                    }
                });
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this,android.R.style.Theme_Material_Light_Dialog_NoActionBar);
                alertBuilder.setView(view);
                alertBuilder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userInput = choosepropdialogactv.getText().toString();
                                prop_textview.setText(userInput);
                                specifics_prop = userInput;
                                specifics_changed();
                                if (!list_of_props.contains(userInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("proplist"),userInput);
                                    list_of_props.add(userInput);
                                }
                            }
                        });
                final Dialog dialog = alertBuilder.create();
                dialog.show();
                dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                        Resources.getSystem().getDisplayMetrics().heightPixels / 3);
            }
        });
        final Button change_pattern_button = findViewById(R.id.patternbutton);
        change_pattern_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                String[] pattern_textview_split = {"","3"};
                make_toast(pattern_textview.getText().toString());
                if (pattern_textview.getText().toString().contains(" / objs:")){
                    pattern_textview_split  =pattern_textview.getText().toString().split(" / objs:");
                }
                final View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_pattern_dialog, null);
                choose_pattern_dialog_actv = view.findViewById(R.id.patterninputactv);
                ArrayAdapter<String> choose_pattern_dialog_actvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_patterns);
                choose_pattern_dialog_actv.setAdapter(choose_pattern_dialog_actvAdapter);
                choose_pattern_dialog_actv.setThreshold(0);//this is number of letters that must match for autocomplete
                choose_pattern_dialog_actv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
                choose_pattern_dialog_actv.setText(pattern_textview_split[0]);
                choose_pattern_dialog_actv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choose_pattern_dialog_actv.showDropDown();
                    }
                });
                final Spinner choose_number_spinner = view.findViewById(R.id.numberspinner);
                ArrayAdapter<Integer> choose_number_spinnerAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_numbers);
                choose_number_spinner.setAdapter(choose_number_spinnerAdapter);
                choose_number_spinner.setSelection(choose_number_spinnerAdapter.getPosition
                        (Integer.parseInt(pattern_textview_split[1])));
                choose_number_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view2, int position, long id) {
                        remove_siteswaps_of_other_numbers(position+1);
                        Collections.sort(list_of_patterns);
                        choose_pattern_dialog_actv = view.findViewById(R.id.patterninputactv);
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_patterns);
                        choose_pattern_dialog_actv.setAdapter(adapter);
                        choose_pattern_dialog_actv.setThreshold(0);//this is number of letters that must match for autocomplete
                        choose_pattern_dialog_actv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);

                        choose_pattern_dialog_actv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                choose_pattern_dialog_actv.showDropDown();
                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                final CheckBox onlyPatternsWithHistoryCB = view.findViewById(R.id.onlyPatternsWithHistoryCB);
                onlyPatternsWithHistoryCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if ( isChecked )
                        {
                            make_toast("is checked");
                        }
                    }
                });
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this,android.R.style.Theme_Material_Light_Dialog_NoActionBar);
                alertBuilder.setView(view);
                alertBuilder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userInput = choose_pattern_dialog_actv.getText().toString();
                                pattern_textview.setText(userInput + " / objs:"+choose_number_spinner.getSelectedItem().toString());
                                specifics_pattern = userInput;
                                specifics_number = choose_number_spinner.getSelectedItem().toString();
                                specifics_changed();
                                if (!list_of_patterns.contains(userInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("patternlist"),userInput);
                                    list_of_patterns.add(userInput);
                                }
                            }
                        });
                final Dialog dialog = alertBuilder.create();
                dialog.show();
                dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                        Resources.getSystem().getDisplayMetrics().heightPixels / 2);
            }
        });
        final Button modifierbutton = findViewById(R.id.modifierbutton);
        modifierbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_modifier_dialog, null);
                final MultiAutoCompleteTextView choosemodifierdialogactv = view.findViewById(R.id.modifierinputmactv);
                choosemodifierdialogactv.setText(modifier_textview.getText());
                ArrayAdapter<String> choosemodifierdialogactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_modifiers);
                choosemodifierdialogactv.setAdapter(choosemodifierdialogactvAdapter);
                choosemodifierdialogactv.setThreshold(0);//this is number of letters that must match for autocomplete
                choosemodifierdialogactv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                choosemodifierdialogactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);

                choosemodifierdialogactv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosemodifierdialogactv.showDropDown();
                    }
                });
                final CheckBox onlyModifiersWithHistoryCB = view.findViewById(R.id.onlyModifiersWithHistoryCB);
                onlyModifiersWithHistoryCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if ( isChecked )
                        {
                            make_toast("is checked");
                            // perform your action here
                        }

                    }
                });
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this,android.R.style.Theme_Material_Light_Dialog_NoActionBar);
                alertBuilder.setView(view);
                alertBuilder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userInput = choosemodifierdialogactv.getText().toString();
                                modifier_textview.setText(userInput);
                                specifics_modifiers = userInput;
                                specifics_changed();
                                if (!list_of_modifiers.contains(userInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("modifierlist"),userInput);
                                    list_of_modifiers.add(userInput);
                                }
                            }
                        });
                final Dialog dialog = alertBuilder.create();
                dialog.show();
                dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                        Resources.getSystem().getDisplayMetrics().heightPixels / 3);
            }
        });
        final Button specialthrowbutton = findViewById(R.id.specialthrowbutton);
        specialthrowbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_special_throw_dialog, null);
                final MultiAutoCompleteTextView choosespecialthrowdialogmactv = view.findViewById(R.id.specialthrowinputmactv);
                String[] special_throw_textview_split = new String[2];
                special_throw_textview_split[0] = "";
                special_throw_textview_split[1] = "";
                Log.d("special", special_throw_textview.getText().toString());
                if (special_throw_textview.getText().toString().contains("/")){
                    if (special_throw_textview.getText().toString().split("/").length>0) {
                        special_throw_textview_split[0] = special_throw_textview.getText().toString().split("/")[0];
                    }
                    if (special_throw_textview.getText().toString().split("/").length>1) {
                        special_throw_textview_split[1] = special_throw_textview.getText().toString().split("/")[1];
                    }

                }

                ArrayAdapter<String> choosespecialthrowdialogactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_special_throws);
                choosespecialthrowdialogmactv.setAdapter(choosespecialthrowdialogactvAdapter);
                choosespecialthrowdialogmactv.setThreshold(0);//this is number of letters that must match for autocomplete
                choosespecialthrowdialogmactv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                choosespecialthrowdialogmactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
                choosespecialthrowdialogmactv.setText(special_throw_textview_split[0]);
                choosespecialthrowdialogmactv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosespecialthrowdialogmactv.showDropDown();
                    }
                });
                final MultiAutoCompleteTextView specialthrowsequenceinputmactv = view.findViewById(R.id.specialthrowsequenceinputmactv);
                ArrayAdapter<String> specialthrowsequenceinputmactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_special_throw_sequences);
                specialthrowsequenceinputmactv.setAdapter(specialthrowsequenceinputmactvAdapter);
                specialthrowsequenceinputmactv.setThreshold(0);//this is number of letters that must match for autocomplete
                specialthrowsequenceinputmactv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                specialthrowsequenceinputmactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
                specialthrowsequenceinputmactv.setText(special_throw_textview_split[1]);
                specialthrowsequenceinputmactv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        specialthrowsequenceinputmactv.showDropDown();
                    }
                });
                final CheckBox onlySpecialThrowsWithHistoryCB = view.findViewById(R.id.onlySpecialThrowsWithHistoryCB);
                onlySpecialThrowsWithHistoryCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if(isChecked) make_toast("is checked");
                    }
                });
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this,android.R.style.Theme_Material_Light_Dialog_NoActionBar);
                alertBuilder.setView(view);
                alertBuilder.setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String userInput = choosespecialthrowdialogmactv.getText().toString();
                                Log.d("userInput1", userInput);
                                if (Character.toString(userInput.charAt(userInput.length()-2)).equals(",")){
                                    userInput = userInput.substring(0, userInput.length()-2);
                                    Log.d("userInput2", userInput);
                                }
                                if (!list_of_special_throws.contains(userInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("specialthrowlist"),userInput);
                                    list_of_special_throws.add(userInput);
                                }
                                String specialThrowSequenceUserInput = specialthrowsequenceinputmactv.getText().toString();
                                if (Character.toString(specialThrowSequenceUserInput.charAt(specialThrowSequenceUserInput.length()-2)).equals(",")){
                                    specialThrowSequenceUserInput = specialThrowSequenceUserInput.substring(0, specialThrowSequenceUserInput.length()-2);
                                }
                                if (!list_of_special_throw_sequences.contains(specialThrowSequenceUserInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("specialthrowsequencelist"),specialThrowSequenceUserInput);
                                    list_of_special_throw_sequences.add(userInput);
                                }
                                special_throw_textview.setText(userInput+"/"+specialThrowSequenceUserInput);
                                specifics_special_throws = userInput;
                                specifics_special_throws_sequences = specialThrowSequenceUserInput;
                                specifics_changed();
                            }
                        });
                final Dialog dialog = alertBuilder.create();
                dialog.show();
                dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                        Resources.getSystem().getDisplayMetrics().heightPixels / 2);
            }
        });
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        final Button cancelbutton = findViewById(R.id.cancelbutton);
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        session_runs_listview = (ListView) findViewById(R.id.runslistview);
        session_runs_arraylist = new ArrayList<>();
        session_runs_listview_adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, session_runs_arraylist);
        session_runs_listview.setAdapter(session_runs_listview_adapter);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inRun) {
                    Boolean can_begin_run = true;
                    if(specifics_prop.equals("")) {
                        can_begin_run = false;
                        make_toast("select prop");
                    }
                    if (can_begin_run) {
                        if (specifics_pattern.equals("")) {
                            can_begin_run = false;
                            make_toast("select pattern");
                        }
                    }
                    if (can_begin_run) {
                        begin_run();
                    }
                }
            }
        });
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inRun) {
                    cancel_run();
                }
            }
        });
        catchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inRun) {
                    end_run("catch");
                }
            }
        });
        dropbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inRun) {
                    end_run("drop");
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //settings button
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            make_toast("settings clicked");
        if (myDb.exportDatabase(getPackageName(), MainActivity.this) == false){
                    show_give_permission_dialog();
                    //todo EXPORT AS CSV instead or with DB
                }
            //if (myDb.importDatabase(getPackageName(), MainActivity.this) == false) show_give_permission_dialog();
            //do_first_use_of_app_stuff();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void set_widgets_visibility(String widgets_to_make_visible){
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        final Button cancelbutton = findViewById(R.id.cancelbutton);
        final TextView timertext = findViewById(R.id.timertext);
        startbutton.setVisibility(View.GONE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        cancelbutton.setVisibility(View.GONE);
        timertext.setVisibility(View.GONE);
        graph.setVisibility(View.GONE);
        if (widgets_to_make_visible == "run"){
            startbutton.setVisibility(View.VISIBLE);
            timertext.setVisibility(View.VISIBLE);
            dropbutton.setVisibility(View.GONE);
            catchbutton.setVisibility(View.GONE);
            cancelbutton.setVisibility(View.GONE);
        }
        if (widgets_to_make_visible == "graph"){
            graph.setVisibility(View.VISIBLE);
        }
        if (widgets_to_make_visible == "history"){

        }
    }
    public void cancel_run(){
        timer.cancel();
        timer.purge();
        inRun = false;
        timertext.setText(myFh.formatSeconds(0));
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        final Button cancelbutton = findViewById(R.id.cancelbutton);
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        cancelbutton.setVisibility(View.GONE);
    }
    public void openbegindialog(){
        inRun = true;
    }
    public void informationMenu() {
        startActivity(new Intent("android.intent.action.INFOSCREEN"));
    }
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }
    public void make_toast(String toastMessage){
        Toast.makeText(getBaseContext(), toastMessage,Toast.LENGTH_SHORT).show();
    }
    public void specifics_changed(){
        make_toast("specifics_changed");
        //if (there_are_completed_runs_not_yet_added_to_db) {
        //    add_completed_runs_to_db();
        //}
        update_personal_best_textview();
        update_graph();
    }
    public void update_graph(){
        double longest_runs_seconds = 0;

        //graph.removeAllSeries();
        //make_toast("spprop :"+specifics_prop);
        //make_toast("txt :"+prop_textview.getText().toString());
        ArrayList<String> list_of_durations = myDb.getDurationsFromSpecifics(specifics_pattern,specifics_number,
                specifics_prop,specifics_modifiers,specifics_special_throws, specifics_special_throws_sequences);

        graphseriescatch = new LineGraphSeries<DataPoint>();
        ArrayList<Integer> list_of_duration_ints = new ArrayList<>();
        for (int i=0;i<list_of_durations.size();i++) {
            list_of_duration_ints.add(Integer.parseInt(list_of_durations.get(i)));
            if (list_of_duration_ints.get(i)>longest_runs_seconds){
                longest_runs_seconds = list_of_duration_ints.get(i);
            }
        }
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(longest_runs_seconds/60);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(list_of_durations.size());
        for (int i=0;i<list_of_durations.size();i++){

            graphseriescatch.appendData(new DataPoint(i,Integer.parseInt(list_of_durations.get(i))/60.0000), false, list_of_durations.size());
        }

        graph.addSeries(graphseriescatch);

    }
    public void update_personal_best_textview(){
        String catch_pb = myDb.getPersonalBestFromSpecifics(specifics_pattern,"catch",specifics_number,
                specifics_prop,specifics_modifiers,specifics_special_throws, specifics_special_throws_sequences);

        String drop_pb = myDb.getPersonalBestFromSpecifics(specifics_pattern,"drop",specifics_number,
                specifics_prop, specifics_modifiers,specifics_special_throws, specifics_special_throws_sequences);

        personal_best_textview.setText("Personal Best - Catch: "+myFh.formatSeconds(Integer.parseInt(catch_pb))+"  Drop: "+myFh.formatSeconds(Integer.parseInt(drop_pb)));
    }
    public boolean first_use_of_app(){
        Boolean toReturn = false;
        try {
            FileInputStream fileIn=openFileInput("patternlist.txt");
        } catch (Exception e) {
            e.printStackTrace();
            toReturn = true;
        }
        return toReturn;
    }
    public void on_create_db(){
        File file = this.getDatabasePath(dbName);
        if (!file.exists()) { //If there are no issues then we can remove the comme ted line and reduce this function
            Log.d("didntexist", "5");
            myDb = new DatabaseHelper(this);
            //addFirstTimeRunDatabaseData();
        } else {
            myDb = new DatabaseHelper(this);
        }
    }
    public void import_db(){
        if (myDb.importDatabase(getPackageName(), MainActivity.this) == false){
            show_give_permission_dialog();
        }
    }
    public void remove_siteswaps_of_other_numbers(int objectNumber){
        Log.d("TAG", "list_of_patterns.size"+list_of_patterns.size());
        list_of_patterns = myTfh.fillListFromTextFile(get_file_input_stream("patternlist"));
        List<String> toRemove = new ArrayList<>();
        for (String pattern : list_of_patterns){
            if (pattern.matches("[0-9]+")){
                int sum = 0;
                for (char c: pattern.toCharArray()) {
                    sum += Character.getNumericValue(c);
                }
                int numberOfDigits = pattern.length();
                int numberOfObjectsInThisSiteswap = sum/numberOfDigits;
                if (objectNumber != numberOfObjectsInThisSiteswap){
                    toRemove.add(pattern);
                }
            }
        }
        for (String itemToRemove : toRemove) {
            //Log.d("TAG", "itemToRemove."+itemToRemove);
            //make_toast("itemToRemove."+itemToRemove);
            if (list_of_patterns.contains(itemToRemove)) {
                list_of_patterns.remove(itemToRemove);
            }
        }
    }
    public InputStream get_input_stream(String listName){
        InputStream ins = null;
        try {
            ins = getResources().openRawResource(getResources().getIdentifier
                    (listName + "template", "raw", getPackageName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ins;
    }
    public FileInputStream get_file_input_stream(String listName){
        FileInputStream ins = null;
        try {
            ins = openFileInput(listName+".txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ins;
    }
    public FileOutputStream get_file_output_stream(String listName){
        FileOutputStream fileout = null;
        try {
            fileout = openFileOutput(listName + ".txt", MODE_PRIVATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileout;
    }
    public FileOutputStream get_file_output_append_stream(String listName){
        FileOutputStream fileout = null;
        try {
            fileout = openFileOutput(listName + ".txt", MODE_PRIVATE | MODE_APPEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileout;
    }
    public void do_first_use_of_app_stuff() {
        make_toast("do_first_use_of_app_stuff()");
        myTfh.resetuserslistfromtemplate(get_input_stream("proplist"), get_file_output_stream("proplist"), "proplist");
        myTfh.resetuserslistfromtemplate(get_input_stream("patternlist"), get_file_output_stream("patternlist"), "patternlist");
        myTfh.resetuserslistfromtemplate(get_input_stream("modifierlist"), get_file_output_stream("modifierlist"), "modifierlist");
        myTfh.resetuserslistfromtemplate(get_input_stream("specialthrowlist"), get_file_output_stream("specialthrowlist"), "specialthrowlist");
        myTfh.resetuserslistfromtemplate(get_input_stream("specialthrowsequencelist"), get_file_output_stream("specialthrowsequencelist"), "specialthrowsequencelist");
        fill_lists_from_text_files();
    }
    public void show_give_permission_dialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Permission")
                .setMessage("Permission denied. Would you like to grant permission?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);

                    }
                })

                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
    public void fill_lists_from_text_files(){
        list_of_numbers.clear();
        for (int i = 1; i <= 13; i++) {
            list_of_numbers.add(i);
        }
            list_of_props = myTfh.fillListFromTextFile(get_file_input_stream("proplist"));
            list_of_patterns = myTfh.fillListFromTextFile(get_file_input_stream("patternlist"));
            list_of_modifiers = myTfh.fillListFromTextFile(get_file_input_stream("modifierlist"));
            list_of_special_throws = myTfh.fillListFromTextFile(get_file_input_stream("specialthrowlist"));
            list_of_special_throw_sequences = myTfh.fillListFromTextFile(get_file_input_stream("specialthrowsequencelist"));
            Log.d("trytry", "1 ");
    }
    public void add_data_to_db(String table, String col, String textToAdd) {
        //this calls up 'insertData' from DatabaseHelper and inserts the user provided add
        //      the EditTexts from above which were taken from our Layout
        boolean isInserted = myDb.insertData(table, col, textToAdd);
    }
    @Override
    protected void onResume() {
        // start handler as activity become visible
        volume_checker.postDelayed( runnable = new Runnable() {
            public void run() {
                Boolean volumeWentDown = false;
                Boolean volumeWentUp = false;
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (currentVolume > audio.getStreamVolume(AudioManager.STREAM_MUSIC)) volumeWentDown = true;
                if (currentVolume < audio.getStreamVolume(AudioManager.STREAM_MUSIC)) volumeWentUp = true;
                if (!inRun){
                    if (volumeWentDown || volumeWentUp) begin_run();
                }else if (inRun){
                    if (volumeWentDown) end_run("drop");
                    if (volumeWentUp) end_run("catch");
                }
                volume_checker.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }
    @Override
    protected void onPause() {
        //add_completed_runs_to_db();
        volume_checker.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
    public void set_current_volume(){ //the reason we are keeping track of the current volume is that
        //  we will be checking later to see if the volume has changed and using that change to start/stop runs.
        //  volume UP = run ended with CATCH, DOWN = DROP. Either button starts runs.
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public void begin_run(){
        //make_toast("begin_run()");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        long tsLong = System.currentTimeMillis()/1000;
        start_time_of_last_run = (int)tsLong;
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        final Button cancelbutton = findViewById(R.id.cancelbutton);
        startbutton.setVisibility(View.GONE);
        dropbutton.setVisibility(View.VISIBLE);
        catchbutton.setVisibility(View.VISIBLE);
        cancelbutton.setVisibility(View.VISIBLE);
        start_timer();
        inRun = true;
        set_current_volume();
    }
    public void end_run(String endtype){
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        final Button cancelbutton = findViewById(R.id.cancelbutton);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        cancelbutton.setVisibility(View.GONE);
        set_current_volume();
        timer.cancel();
        timer.purge();
        inRun = false;
        session_runs_arraylist.add(pattern_textview.getText().toString().replace("objs:","")+" - "+ myFh.formatSeconds(run_duration)+" ("+endtype+")");
        session_runs_listview_adapter.notifyDataSetChanged();
        add_completed_run_to_db(endtype);
        //there_are_completed_runs_not_yet_added_to_db = true;
        timertext.setText(myFh.formatSeconds(0));
        update_personal_best_textview();
    }
    public void add_completed_run_to_db(String endtype){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());
        //String special_throws_to_insert = "";
        //String special_throw_sequences_to_insert = "";
//        if (special_throw_textview.getText().toString().contains("/")){
//            try {
//                special_throws_to_insert = special_throw_textview.getText().toString().split("/")[0];
//                special_throw_sequences_to_insert = special_throw_textview.getText().toString().split("/")[1];
//            }catch (Exception e) {e.printStackTrace(); }
//        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        myDb.insertRun ( formattedDate, specifics_pattern, String.valueOf(run_duration),endtype,
                specifics_number, specifics_prop,
                specifics_modifiers, specifics_special_throws , specifics_special_throws_sequences);
    }
    public void start_timer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (inRun) {
                            long tsLong = System.currentTimeMillis()/1000;
                            int current_time = (int)tsLong;
                            run_duration = current_time - start_time_of_last_run;
                            timertext.setText(myFh.formatSeconds(run_duration));
                        }else{
                            timer.cancel();
                            timer.purge();
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }
}


