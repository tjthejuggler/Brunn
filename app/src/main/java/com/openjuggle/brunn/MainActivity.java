
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
    LineGraphSeries<DataPoint> graphseriescatch;
    LineGraphSeries<DataPoint> graphseriesdrop;
    private GraphView graph;
    public Boolean graph_is_visible = false;
    private Timer timer;
    private TextView timertext;
    public Boolean inRun = false;
    public Boolean there_are_completed_runs_not_yet_added_to_db;
    int currentVolume;
    public int run_duration;
    public int start_time_of_last_run = 0;
    private TextView prop_textview;
    private TextView pattern_textview;
    private TextView modifier_textview;
    private TextView special_throw_textview;
    private TextView personal_best_textview;
    List<String> list_of_props = new LinkedList<>();
    List<Integer> list_of_numbers = new LinkedList<>();
    List<String> list_of_patterns = new LinkedList<>();
    List<String> list_of_modifiers = new LinkedList<>();
    List<String> list_of_special_throws = new LinkedList<>();
    List<String> list_of_special_throw_sequences = new LinkedList<>();
    private ListView runs_listview;
    private ArrayAdapter<String> runs_listviewadapter;
    private ArrayList<String> runs_arraylist;
    Handler volume_checker = new Handler();
    int delay = 300; //1 second=1000 millisecond, 15*1000=15seconds
    Runnable runnable;
    private float x1,x2;
    static final int MIN_DISTANCE = 150;
    private int prop_text_viewCycleIndex = 0;

    @SuppressLint("ClickableViewAccessibility")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTfh = new TextFileHelper(this);//creates an object from our database class over in DatabaseHelper
        myFh = new FormatHelper();
        //fillMaps();
        prop_textview = findViewById(R.id.proptextview);
        if(first_use_of_app()){
            do_first_use_of_app_stuff();
        }
        on_create_db();
        try {
            prop_textview.setText(myDb.getAllFromColumn("PROP").get(myDb.getAllFromColumn("PROP").size()-1));
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
                    if (deltaX > MIN_DISTANCE){
                        prop_text_viewCycleIndex++;
                        if (prop_text_viewCycleIndex==list_of_props.size()){
                            prop_text_viewCycleIndex = 0;
                        }
                        prop_textview.setText(list_of_props.get(prop_text_viewCycleIndex));
                    }
                    if (deltaX < -MIN_DISTANCE){
                        prop_text_viewCycleIndex--;
                        if (prop_text_viewCycleIndex<0){
                            prop_text_viewCycleIndex = list_of_props.size()-1;
                        }
                        prop_textview.setText(list_of_props.get(prop_text_viewCycleIndex));
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

        set_current_volume();
        graph = findViewById(R.id.historygraph);
        timertext = findViewById(R.id.timertext);
        fill_lists_from_text_files();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                make_toast("fab");
                import_db();

                //do_first_use_of_app_stuff();

                //do_first_use_of_app_stuff();
                //myTfh.appendTextFile(get_file_output_append_stream("patternlist"),"mytest2");
            }
        });
        final Button propbutton = findViewById(R.id.propbutton);
        propbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_prop_dialog, null);
                final AutoCompleteTextView choosepropdialogactv = view.findViewById(R.id.propinputactv);
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
                final View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_pattern_dialog, null);
                choose_pattern_dialog_actv = view.findViewById(R.id.patterninputactv);
                ArrayAdapter<String> choose_pattern_dialog_actvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_patterns);
                choose_pattern_dialog_actv.setAdapter(choose_pattern_dialog_actvAdapter);
                choose_pattern_dialog_actv.setThreshold(0);//this is number of letters that must match for autocomplete
                choose_pattern_dialog_actv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
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
                ArrayAdapter<String> choosespecialthrowdialogactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, list_of_special_throws);
                choosespecialthrowdialogmactv.setAdapter(choosespecialthrowdialogactvAdapter);
                choosespecialthrowdialogmactv.setThreshold(0);//this is number of letters that must match for autocomplete
                choosespecialthrowdialogmactv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                choosespecialthrowdialogmactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
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
                                if (!list_of_special_throws.contains(userInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("specialthrowlist"),userInput);
                                    list_of_special_throws.add(userInput);
                                }
                                String specialThrowSequenceUserInput = specialthrowsequenceinputmactv.getText().toString();
                                if (!list_of_special_throw_sequences.contains(specialThrowSequenceUserInput)){
                                    myTfh.appendTextFile(get_file_output_append_stream("specialthrowsequencelist"),specialThrowSequenceUserInput);
                                    list_of_special_throw_sequences.add(userInput);
                                }
                                special_throw_textview.setText(userInput+"/"+specialThrowSequenceUserInput);
                                specifics_changed();
                            }
                        });
                final Dialog dialog = alertBuilder.create();
                dialog.show();
                dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                        Resources.getSystem().getDisplayMetrics().heightPixels / 2);
            }
        });
        final Button showgraphbutton = findViewById(R.id.show_graph_button);
        showgraphbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                if (graph_is_visible) {
                    graph.setVisibility(View.GONE);
                    graph_is_visible = false;
                }else{
                    graph.setVisibility(View.VISIBLE);
                    graph_is_visible = true;
                }
            }
        });
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        runs_listview = (ListView) findViewById(R.id.runslistview);
        runs_arraylist = new ArrayList<>();
        runs_listviewadapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, runs_arraylist);
        runs_listview.setAdapter(runs_listviewadapter);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inRun) {
                    Boolean can_begin_run = true;
                    if(prop_textview.getText().toString().equals("")) {
                        can_begin_run = false;
                        make_toast("select prop");
                    }
                    if (can_begin_run) {
                        if (!pattern_textview.getText().toString().contains(" / objs:")) {
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
                }
            //if (myDb.importDatabase(getPackageName(), MainActivity.this) == false) show_give_permission_dialog();
            //do_first_use_of_app_stuff();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    //todo
    //every time a specifics textview changes, make a global variable with that kind of specific get updated so we can get rid of the clutter
    // that is in update_graph and personalrecord and anywhere we insert to db.
    //-instead of graph button, make a radiobutton toggle that either shows a history list, graph, or the start button. Only do the logic of the
    //      thing the toggle is set on so we arent loading stuff in the background needlessly
    //-get rid of the bug that erquires us to go into special throws and hit ok even if it is blank (in order to fill in graph/querrry db)
    public void update_graph(){
        String pattern = "";
        String number = "";
        if (pattern_textview.getText().toString().contains(" / objs:")){
            pattern = pattern_textview.getText().toString().split(" / objs:")[0];
            number = pattern_textview.getText().toString().split(" / objs:")[1];
        }
        String modifiers = modifier_textview.getText().toString();
        if (modifiers.isEmpty()){modifiers="";}
        String special_throws = "";
        String special_throw_sequences = "";
        if (special_throw_textview.getText().toString().contains("/")){
            try {
                special_throws = special_throw_textview.getText().toString().split("/")[0];
                special_throw_sequences = special_throw_textview.getText().toString().split("/")[1];
            }catch (Exception e) {e.printStackTrace(); }
        }

        int longest_runs_seconds = 0;

        ArrayList<String> list_of_durations = myDb.getDurationsFromSpecifics(pattern,number,
                prop_textview.getText().toString(),modifiers,special_throws, special_throw_sequences);

        graphseriescatch = new LineGraphSeries<DataPoint>();
        ArrayList<Integer> list_of_duration_ints = new ArrayList<>();
        for (int i=0;i<list_of_durations.size();i++) {
            list_of_duration_ints.add(Integer.parseInt(list_of_durations.get(i)));
            if (list_of_duration_ints.get(i)>longest_runs_seconds){
                longest_runs_seconds = list_of_duration_ints.get(i);
            }
        }
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMaxY(longest_runs_seconds);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(list_of_durations.size());
        for (int i=0;i<list_of_durations.size();i++){

            graphseriescatch.appendData(new DataPoint(i,Integer.parseInt(list_of_durations.get(i))), false, list_of_durations.size());
        }

        graph.addSeries(graphseriescatch);

    }


    public void update_personal_best_textview(){
        String pattern = "";
        String number = "";
        if (pattern_textview.getText().toString().contains(" / objs:")){
            pattern = pattern_textview.getText().toString().split(" / objs:")[0];
            number = pattern_textview.getText().toString().split(" / objs:")[1];
        }
        String modifiers = modifier_textview.getText().toString();
        if (modifiers.isEmpty()){modifiers="";}
        String special_throws = "";
        String special_throw_sequences = "";
        if (special_throw_textview.getText().toString().contains("/")){
            try {
                special_throws = special_throw_textview.getText().toString().split("/")[0];
                special_throw_sequences = special_throw_textview.getText().toString().split("/")[1];
            }catch (Exception e) {e.printStackTrace(); }
        }
        String catch_pb = myDb.getPersonalBestFromSpecifics(pattern,"catch",number,
                prop_textview.getText().toString(),modifiers,special_throws, special_throw_sequences);

        String drop_pb = myDb.getPersonalBestFromSpecifics(pattern,"drop",number,
                prop_textview.getText().toString(), modifiers,special_throws, special_throw_sequences);

//        String mycatch_pb = myDb.getPersonalBestFromSpecifics("441","catch",
//                "3","Balls",
//                "","", "");
//
//        String mydrop_pb = myDb.getPersonalBestFromSpecifics(pattern_textview.getText().toString().split(" / objs:")[0],"drop",
//                pattern_textview.getText().toString().split(" / objs:")[1],prop_textview.getText().toString(),
//                modifiers,special_throws, special_throw_sequences);

        personal_best_textview.setText("Personal Best - Catch: "+catch_pb+"  Drop: "+drop_pb);
        //personal_best_textview.setText(myDb.getAllFromColumn("NAME").toString());
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
        if (!file.exists()) {
            Log.d("didntexist", "5");
            //if the db doesn't currently exist, then this is the first time the app has been run and we need to add
            //      the stuff to the db to make the default settings
            myDb = new DatabaseHelper(this);//creates an object from our db class over in DatabaseHelper
            //addFirstTimeRunDatabaseData();
        } else {
            myDb = new DatabaseHelper(this);//creates an object from our database class over in DatabaseHelper
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
    public void set_current_volume(){
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
        startbutton.setVisibility(View.GONE);
        dropbutton.setVisibility(View.VISIBLE);
        catchbutton.setVisibility(View.VISIBLE);
        start_timer();
        inRun = true;
        set_current_volume();
    }
    public void end_run(String endtype){
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        set_current_volume();
        timer.cancel();
        timer.purge();
        inRun = false;
        runs_arraylist.add(myFh.formatSeconds(run_duration)+" ("+endtype+")");

        runs_listviewadapter.notifyDataSetChanged();
        add_completed_run_to_db(endtype);
        //there_are_completed_runs_not_yet_added_to_db = true;
        timertext.setText(myFh.formatSeconds(0));
        update_personal_best_textview();
    }
    public void add_completed_run_to_db(String endtype){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());
        String special_throws_to_insert = "";
        String special_throw_sequences_to_insert = "";
        if (special_throw_textview.getText().toString().contains("/")){
            try {
                special_throws_to_insert = special_throw_textview.getText().toString().split("/")[0];
                special_throw_sequences_to_insert = special_throw_textview.getText().toString().split("/")[1];
            }catch (Exception e) {e.printStackTrace(); }
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        myDb.insertRun ( formattedDate, pattern_textview.getText().toString().split(" / objs:")[0], String.valueOf(run_duration),endtype,
                pattern_textview.getText().toString().split(" / objs:")[1], prop_textview.getText().toString(),
                modifier_textview.getText().toString(), special_throws_to_insert , special_throw_sequences_to_insert);
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
/*
//Toast.makeText(getBaseContext(), "A Toast to be used!",Toast.LENGTH_SHORT).show();
-NEXT:
    do the stuff in specifics_changed()
    make make a formatHelper class
    make the settings activity
-TO MAKE DB:
    -every time the specifics change:
        -if there were completed runs with the previous specifics, we upload them to the db, to do this:
            -check if there are completed runs that have not been uploaded to the db yet, if there are..
                -make a new row for each run and fill in the info of the runs
        -once completed runs of the previous specifics have been uploaded:
            -check the db for all runs which match the newly set specifics, get the personal bests for it and put them in a textview
        -then we just start doing runs until we switch specifics and start the process over
-We should upload completed runs to DB if:
    -the app is being closed
    -specifics change
    -enough time has elapsed without a run
    -maybe make an 'upload runs to db' button
-beyond basics DB stuff we want:
    -make easy way to input records in the past
-possible mail button uses
    -update db
    -ai coach(recommends patterns)
    -set by user in settings
-a sound played when a run starts would get rid of the problem of runs accidentally being started and stopped in the background
-once db is installed:

-settings:
    -make another screen show when settings is clicked
    -buffer time
    -make sounds? set sounds?
    -make import/export db stuff
-eventually
    -maybe we will want to distiguish between intentional endings with drop, intentional with catch, unintentional with drop, unintentional with catch
    -make letters work for siteswaps
    -there can be an 'only show patterns/modifiers without history checkbox
    -swipping on number instead of it being a spinner(in the pattern dialog)
    -swipping on pattern/mod/st could automatically go through whatever list is currently
            set in the dialog. THIS MEANS THAT WE NEED TO KEEP TRACK OF EVERYTHING THAT IS CURRENTLY SET
            IN DIALOGS. We should autoload that stuff into dialogs when they open.
    -make graphs of progress
    -use calendar view to go back in time(maybe this could also be used to add past runs
    -an automatic throw count estimator for patterns that have had both time and throw data given
    -maybe make an 'upload runs to db' button
-Things I don't understand
    -static


------MOST IMPORTANT REQUIREMENTS-------------
        As few app pages as possible
        Main Screen
        POPUP: ability to select pattern from filterable list
        POPUP: ability to select modifier(s) - this could also be a filterable list
        Settings
        Voice activated (start, drop, catch) if it is reliable, as well as more commands if reliable
        The ability to use the app quickly, no long loading times
        This may mean that we don't interact with the DB so often, maybe just to update it when the session is ending.
        All attempts recorded.
        I can quickly restart a run.


        ------POSSIBLE REQUIREMENTS-----------
        I can always see the screen, no need to pick up phone. It is on a big screen or projector in front of me.
        - a way to tag patterns as being currently interested in them, maybe favorited, disliked, other tags
        -a stat that would be interesting is the % of patterns that have intentional records greater than unintentional
        -occasionally having sessions where I always go to failure would be good for seeing just how far I can go on a pattern
        -maybe nfc tags could be used in 1 or 2 shoes, or on balls if barefooted to indicate that a timer should start or stop, or a run should be repeated
        -it would be cool to have some sort of drawing option for patterns so I can show exactly where balls are going, for instance there is a whole family of patterns for the box, that back and forth ball can go above, around in figure eights, loop one of the column balls.
        -pick the prop when app opens just like I was picking a username and it wont have to load stuff from other props I'm not using
        -it would be nice if a voice told me the next siteswap to do
        -practice routines could be created by using just a couple siteswap numbers with 0s and 2s sprinkled in, for example 6, 1: 612, 61120, 6211,66661.they could make different shapes based on difficulty. By this I mean things like a hill, starts with low difficulty, goes to high difficulty, and then returns to low. Or an inverse hill, or an s shape, or a (hill, mountain, hill)
        -newly input siteswaps should be added as their equivalent with the highest digit listed first, so 03057 should be 70305. This idea should continue onto the second digit in the case of times, 52053 should be 53520 (btw, that's not a valid siteswap)
        -there should be no inputting of new siteswaps, just start a run with one by putting it in and it 'adds' it


        --------OBSERVATIONS-----------
        Just finding out an average or normal endurance records for a given number of consecutive attempts would be nice.
        It may be fairly standard that first attempt is low, next is higher, next even higher, and from there attempts

        -----THOUGHTS------
        Ideal setup:
        User inputs as much or as little info on what they want to practice, app tells them what to do and shows them a juggling animation of it being done, they do it, the app notes how long they do it for and how it ends, gives stats on pattern form, identifies mistakes, records video of attempt.

        ------AI COACH THOUGHTS----------
        -initially, coach doesnt even need to be real ai, it can just be recommending stuff based on a few simple rules that I create
        -Juggler could have a 'warmness' rating based on how much they have juggled how recently, the warmer they are, they more difficult of patterns are suggested. Pattern difficulty can be based on the current personal best with that pattern.
        -a 'tiredness' rating may also be useful so that as patterns get more difficult, the coach can recomend easier patterns again to give the juggler a bit of a break, and they are not pushed into failure. The rate at which this rating changes can be dynamically based on when the juggler starts getting lower runs
        -Some concept of 'themes' would be nice so that it chooses patterns that build on each other, or uses some less difficult patterns to warm the juggler up to more difficult patterns. It would be great if the patterns always start simple enough and undemanding enough and build gradually enough that long times can go without any drops occurring.
        -Less time spent juggling is acceptable if the result is more records being broken and less drops happening
        -hooking up to spotify playlist would be really cool, it could start/stop songs when runs start/stop, it could select songs with lengths based on the current record on the pattern. It could also play around withusing songs consistently with the same patterns. Or it could do similar patterns with songs from the same album or artist, real crazy could be songs with multiple artists after patterns with certain qualities have been used with either of the artists, then patterns that combine aspects with songs that have both artists




        -------BELOW HERE NEEDS SORTED----------

        If it could play songs then it could start them at the right time to end when a record or goal is reached

        Personalized audio could be set as notifications for all kinds of stuff:
        You beat your unintentional record
        You beat you intentional record
        You doubled your intentional record
        You are X% higher than your intentional record
        You dont usually make it this much % higher than your intentional record(this needs to be more precise)
        You have a X% chance of dropping based on your history(needs to be more precise)
        This would be the X number intentional records beaten this session
        This session has been an hour long

        Need a way to compare progress recently to progress in the past, independant of personal best lengths. Meaning I want to know if now I am breaking records more often than I use to, and doing so more impressively, like by bigger %s, with less drops, or by any other measure.

        It would be nice to see charts/graphs or pattern progress over time, as well as some of the stats information. There may be an easy way to do this like there was in python

        If it knew a normal average throws per second of a pattern, and also knew beats per minute of songs, it could play songs that match patterns

        There could be a timer for 'im working on/figuring out this pattern' that is separate from endurance time

        'Recomend a pattern to me' would be nice, either of patterns I have done, but not for awhile, patterns I have never done, patterns other people who do the patterns I do can do, maybe more types of recomends.

        So far as special throws go and all their combinations like yny, nny, yynny, yyyyn, and so on, we dont have to input them all, they can be automatically generated and if it offers one that we dont want then we can have some kind of skip button. Maybe to varying degrees, like 'skip and never ask again', 'skip and dont ask again for awhile'

        It should come preloaded with a ton of siteswaps. User should be able to browse all siteswaps, input siteswaps, browse siteswaps that have records.

        User should be able to input catch record or time record, there should also be a simple feature with camera that starts timer when it sees movement so that the camera can be aimed up and juggling can trigger it.

        Patterns that are different if they start on left or right side should have different records

        There should be different training modes, juggler can either decide what to do or be told what to do. If juggler deciding, they can be as specific or vague as desired, for example, choose number of objects, choose siteswap length, choose lowest and highest possible siteswap digits, and probable more stuff

        AI coach could tell juggler to stop, this could be right after the best unintentional record was set, a certain % of time after best intentional record was set(for instance if % is 10, and record is 1 minute, then ai coach tells juggler to stop at 1:06). By using this ai coach it would be cool if we could eliminate drops and keep setting new intentional records. An alternative to this could be juggler is their own coach and the app just tells them when they have gotten to certain markers such as unintentional, intentional, and %s beyond those records. The audio for these could be set by the juggler so they could even record themselves saying the %s

        A nice feature would be the ability to focus on siteswap sequences, like 11. It could suggest 5511, then 711, and then any more siteswaps that include 11

        Default prop type would be nice so accidental records are not made for the wrong prop type

        A strictness rating would be good for things like collisions allowed or accidental body bumps, or wild throws that get recovered from, or foot movements

        A mode like the anki app that just gives little missions with a built in timer would be cool. It tells you a pattern, and an amount of time/catches, you hit start, try to do it, tell it when you stop and if it was a drop/catch

        Some kind of enforced break length between runs may be useful

        The goal of the app is to help juggler break as many records as possible with as little effort possible. The whole ferriss 80% / 20% thing

        New Juggling Database UI:
        Go over current drawing
        Make a drawing for each screen. Filter, settings, create set, in set, set history, general stats, pattern stats
        Before start programming, should know what I want


        Eventually:
        The + sign could give option to make a new set or to start a new AI session that asks for desires and then suggests sets



*/

