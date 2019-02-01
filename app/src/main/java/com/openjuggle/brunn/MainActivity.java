
package com.openjuggle.brunn;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.speech.RecognizerIntent;
import android.support.v4.app.NavUtils;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    DatabaseHelper myDb;
    String dbName = "brunn.db";
    AutoCompleteTextView choosepatterndialogactv;
    public ListView mList;
    private Timer timer;
    private TextView timertext;
    public Boolean inRun = false;
    int currentVolume;
    public int runduration;
    public int starttimeoflastrun = 0;
    private TextView proptextview;
    private TextView patterntextview;
    private TextView modifiertextview;
    private TextView specialthrowtextview;
    List<String> listofprops = new LinkedList<>();
    List<Integer> listofnumbers = new LinkedList<>();
    List<String> listofpatterns = new LinkedList<>();
    List<String> listofmodifiers = new LinkedList<>();
    List<String> listofspecialthrows = new LinkedList<>();
    List<String> listofspecialthrowsequences = new LinkedList<>();
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    private ListView runslistview;
    private ArrayAdapter<String> runslistviewadapter;
    private ArrayList<String> runsarraylist;
    Handler volumechecker = new Handler();
    int delay = 300; //1 second=1000 millisecond, 15*1000=15seconds
    Runnable runnable;
    String userslongsiteswaplist = "userslongsiteswaplist.txt";
    private float x1,x2;
    static final int MIN_DISTANCE = 150;
    private int propTextViewCycleIndex = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        proptextview = findViewById(R.id.proptextview);
        proptextview.setOnTouchListener(new View.OnTouchListener() {
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
                    if (deltaX > MIN_DISTANCE)
                    {
                        propTextViewCycleIndex++;
                        if (propTextViewCycleIndex==listofprops.size()){
                            propTextViewCycleIndex = 0;
                        }
                        //Toast.makeText(MainActivity.this, "left2right swipe", Toast.LENGTH_SHORT).show ();
                        proptextview.setText(listofprops.get(propTextViewCycleIndex));
                    }
                    if (deltaX < -MIN_DISTANCE)
                    {
                        propTextViewCycleIndex--;
                        if (propTextViewCycleIndex<0){
                            propTextViewCycleIndex = listofprops.size()-1;
                        }
                        //Toast.makeText(MainActivity.this, "left2right swipe", Toast.LENGTH_SHORT).show ();
                        proptextview.setText(listofprops.get(propTextViewCycleIndex));
                    }
                    else
                    {
                        // consider as something else - a screen tap for example
                    }

                }
                return true;
            }
        });
        patterntextview = findViewById(R.id.patterntextview);
        modifiertextview = findViewById(R.id.modifiertextview);
        specialthrowtextview = findViewById(R.id.specialthrowtextview);
        if(firstuse()){
            Toast.makeText(getBaseContext(), "firstuse", Toast.LENGTH_SHORT).show();
            doFirstUseStuff();
        }
        fillListsFromTextFiles();
        onCreateDatabase();
        setCurrentVolume();
        timertext = findViewById(R.id.timertext);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "fab", Toast.LENGTH_SHORT).show();
                //doFirstRunStuff();
                writeToTextFile("patternlist","mytest");
                fillListsFromTextFiles();
                //Read();
            }
        });
        final Button propbutton = findViewById(R.id.propbutton);
        propbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_prop_dialog, null);
                final AutoCompleteTextView choosepropdialogactv = view.findViewById(R.id.propinputactv);
                ArrayAdapter<String> choosepropdialogactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofprops);
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
                            proptextview.setText(userInput);
                            if (!listofprops.contains(userInput)){
                                writeToTextFile("proplist",userInput);
                                fillListFromTextFile("proplist");
                            }
                        }
                    });
                final Dialog dialog = alertBuilder.create();
            dialog.show();
            dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                    Resources.getSystem().getDisplayMetrics().heightPixels / 3);
            }
        });
        final Button patternbutton = findViewById(R.id.patternbutton);
        patternbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                //Toast.makeText(getBaseContext(), "patternButtonClicked",Toast.LENGTH_SHORT).show();
                final View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.choose_pattern_dialog, null);
                choosepatterndialogactv = view.findViewById(R.id.patterninputactv);
                ArrayAdapter<String> choosepatterndialogactvAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofpatterns);
                choosepatterndialogactv.setAdapter(choosepatterndialogactvAdapter);
                choosepatterndialogactv.setThreshold(0);//this is number of letters that must match for autocomplete
                choosepatterndialogactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
                choosepatterndialogactv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        choosepatterndialogactv.showDropDown();
                    }
                });
                final Spinner choosenumberdialogspinner = view.findViewById(R.id.numberspinner);
                ArrayAdapter<Integer> choosenumberdialogspinnerAdapter =
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofnumbers);
                choosenumberdialogspinner.setAdapter(choosenumberdialogspinnerAdapter);
                choosenumberdialogspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view2, int position, long id) {
                        //Toast.makeText(getBaseContext(), "spinnerItemSelected",Toast.LENGTH_SHORT).show();
                        removeSiteswapsOfOtherNumbers(position+1);
                        //Toast.makeText(getBaseContext(), "listofpatterns.size"+listofpatterns.size(),Toast.LENGTH_SHORT).show();
                        Collections.sort(listofpatterns);
                        choosepatterndialogactv = view.findViewById(R.id.patterninputactv);
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofpatterns);
                        choosepatterndialogactv.setAdapter(adapter);
                        choosepatterndialogactv.setThreshold(0);//this is number of letters that must match for autocomplete
                        choosepatterndialogactv.setDropDownHeight(Resources.getSystem().getDisplayMetrics().heightPixels / 4);
                        choosepatterndialogactv.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                choosepatterndialogactv.showDropDown();
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
                            Toast.makeText(MainActivity.this, "is checked", Toast.LENGTH_SHORT).show();
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
                                String userInput = choosepatterndialogactv.getText().toString();
                                patterntextview.setText(userInput);
                                if (!listofpatterns.contains(userInput)){
                                    writeToTextFile("patternlist",userInput);
                                    fillListFromTextFile("patternlist");
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
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofmodifiers);
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
                            Toast.makeText(MainActivity.this, "is checked", Toast.LENGTH_SHORT).show();
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
                                modifiertextview.setText(userInput);
                                if (!listofmodifiers.contains(userInput)){
                                    writeToTextFile("modifierlist",userInput);
                                    fillListFromTextFile("modifierlist");
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
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofspecialthrows);
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
                        new ArrayAdapter<>(MainActivity.this, android.R.layout.select_dialog_item, listofspecialthrowsequences);
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
                        if ( isChecked )
                        {
                            Toast.makeText(MainActivity.this, "is checked", Toast.LENGTH_SHORT).show();
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
                                String specialThrowUserInput = choosespecialthrowdialogmactv.getText().toString();

                                if (!listofspecialthrows.contains(specialThrowUserInput)){
                                    writeToTextFile("specialthrowlist",specialThrowUserInput);
                                    fillListFromTextFile("specialthrowlist");
                                }
                                String specialThrowSequenceUserInput = specialthrowsequenceinputmactv.getText().toString();
                                if (!listofspecialthrowsequences.contains(specialThrowSequenceUserInput)){
                                    writeToTextFile("specialthrowsequencelist",specialThrowSequenceUserInput);
                                    fillListFromTextFile("specialthrowsequencelist");
                                }
                                specialthrowtextview.setText(specialThrowUserInput+"/"+specialThrowSequenceUserInput);
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
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        runslistview = (ListView) findViewById(R.id.runslistview);
        runsarraylist = new ArrayList<String>();
        runslistviewadapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, runsarraylist);
        runslistview.setAdapter(runslistviewadapter);
        startbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inRun) {
                    beginrun();
                }
            }
        });
        catchbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inRun) {
                    endrun("catch");
                }
            }
        });
        dropbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inRun) {
                    endrun("drop");
                }
            }
        });
    }
    public boolean firstuse(){
        Boolean toReturn = false;
        try {
            FileInputStream fileIn=openFileInput("patternlist.txt");
        } catch (Exception e) {
            e.printStackTrace();
            toReturn = true;
        }
        return toReturn;
    }
    public void removeSiteswapsOfOtherNumbers(int objectNumber){
        //listofpatterns.clear();
        Log.d("TAG", "listofpatterns.size"+listofpatterns.size());
        fillListFromTextFile("patternlist");
        List<String> toRemove = new ArrayList<>();
        for (String pattern : listofpatterns){
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
            Log.d("TAG", "itemToRemove."+itemToRemove);
            //Toast.makeText(getBaseContext(), "itemToRemove."+itemToRemove,Toast.LENGTH_SHORT).show();
            if (listofpatterns.contains(itemToRemove)) {
                listofpatterns.remove(itemToRemove);
            }
        }
    }


    public void doFirstUseStuff(){
        resetuserslistfromtemplate("proplist");
        resetuserslistfromtemplate("patternlist");
        resetuserslistfromtemplate("modifierlist");
        resetuserslistfromtemplate("specialthrowlist");
        resetuserslistfromtemplate("specialthrowsequencelist");
        fillListsFromTextFiles();
    }
    public void fillListsFromTextFiles(){
        fillListFromTextFile("proplist");
        listofnumbers.clear();
        for (int i = 1; i <= 13; i++) {
            listofnumbers.add(i);
        }
        fillListFromTextFile("patternlist");
        fillListFromTextFile("modifierlist");
        fillListFromTextFile("specialthrowlist");
        fillListFromTextFile("specialthrowsequencelist");
    }
    public void fillListFromTextFile(String textFileName){
            final int READ_BLOCK_SIZE = 100;
        Log.d("mine", "1 ");
            try {
                Log.d("mine", "2 ");
                FileInputStream fileIn=openFileInput(textFileName+".txt");
                InputStreamReader InputRead= new InputStreamReader(fileIn);
                char[] inputBuffer= new char[READ_BLOCK_SIZE];
                String s="";
                int charRead;
                while ((charRead=InputRead.read(inputBuffer))>0) {
                    // char to string conversion
                    String readstring=String.copyValueOf(inputBuffer,0,charRead);
                    s +=readstring;
                }
                InputRead.close();
                if (textFileName == "proplist"){
                    String[] stringsofprops = s.split("\\r?\\n");
                    listofprops = removeDuplicates(stringsofprops);
                }
                if (textFileName == "patternlist"){
                    Log.d("mine", "3 ");
                    String[] stringsofpatterns = s.split("\\r?\\n");
                    listofpatterns = removeDuplicates(stringsofpatterns);
                    Log.d("mine", "listofpatterns.sizzze "+listofpatterns.size());
                }
                if (textFileName == "modifierlist"){
                    String[] stringsofmodifiers = s.split("\\r?\\n");
                    listofmodifiers = removeDuplicates(stringsofmodifiers);
                }
                if (textFileName == "specialthrowlist"){
                    String[] stringsofspecialthrows = s.split("\\r?\\n");
                    listofspecialthrows = removeDuplicates(stringsofspecialthrows);
                }
                if (textFileName == "specialthrowsequencelist"){
                    String[] stringsofspecialthrowsequences = s.split("\\r?\\n");
                    listofspecialthrowsequences = removeDuplicates(stringsofspecialthrowsequences);
                }
                //Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }
    public LinkedList<String> removeDuplicates(String[] arrayToRemoveDuplicatesFrom){
        for(int s=0;s<arrayToRemoveDuplicatesFrom.length-1;s++){
            for(int m=s + 1;m<arrayToRemoveDuplicatesFrom.length;m++){
                if(arrayToRemoveDuplicatesFrom[s] != null && arrayToRemoveDuplicatesFrom[s].equals(arrayToRemoveDuplicatesFrom[m])){
                    arrayToRemoveDuplicatesFrom[m] = null; // Mark for deletion later on
                }
            }
        }
        List<String> listWithNulls;
        listWithNulls = new LinkedList<>(Arrays.asList(arrayToRemoveDuplicatesFrom));
        LinkedList<String> listToReturn = new LinkedList<>();
        for(String data: listWithNulls) {
            if(data != null) {
                if (Pattern.compile( "[0-9]" ).matcher( data ).find() || Pattern.compile( "[a-zA-Z]" ).matcher( data ).find())
                listToReturn.add(data);
            }
        }
        return listToReturn;
    }
    public void addDataToDB(String table, String col, String textToAdd) {
        //this calls up 'insertData' from DatabaseHelper and inserts the user provided add
        //      the EditTexts from above which were taken from our Layout
        boolean isInserted = myDb.insertData(table, col, textToAdd);
    }
    public void onCreateDatabase(){
        File file = this.getDatabasePath(dbName);
        if (!file.exists()) {
            Log.d("didntexist", "5");
            //if the database doesn't currently exist, then this is the first time the app has been run and we need to add
            //      the stuff to the database to make the default settings
            myDb = new DatabaseHelper(this);//creates an object from our database class over in DatabaseHelper
            //addFirstTimeRunDatabaseData();
        } else {
            myDb = new DatabaseHelper(this);//creates an object from our database class over in DatabaseHelper
        }
    }
    @Override
    protected void onResume() {
        // start handler as activity become visible
        volumechecker.postDelayed( runnable = new Runnable() {
            public void run() {
                    Boolean volumeWentDown = false;
                    Boolean volumeWentUp = false;
                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    if (currentVolume > audio.getStreamVolume(AudioManager.STREAM_MUSIC)){
                        volumeWentDown = true;
                    }
                    if (currentVolume < audio.getStreamVolume(AudioManager.STREAM_MUSIC)){
                        volumeWentUp = true;
                    }
                    if (!inRun){
                        if (volumeWentDown || volumeWentUp){
                            beginrun();
                        }
                    }else if (inRun){
                        if (volumeWentDown){
                            endrun("drop");
                        }
                        if (volumeWentUp){
                            endrun("catch");
                        }
                }
                volumechecker.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }
    @Override
    protected void onPause() {
        volumechecker.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
    public void setCurrentVolume(){
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    public void beginrun(){
        //Toast.makeText(MainActivity.this, "beginrun()", Toast.LENGTH_SHORT).show();
        long tsLong = System.currentTimeMillis()/1000;
        starttimeoflastrun = (int)tsLong;
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        startbutton.setVisibility(View.GONE);
        dropbutton.setVisibility(View.VISIBLE);
        catchbutton.setVisibility(View.VISIBLE);
        startTimer();
        inRun = true;
        setCurrentVolume();
    }
    public void endrun(String endtype){
        //Toast.makeText(MainActivity.this,
        //        "endrun("+endtype+")", Toast.LENGTH_LONG).show();
        final Button startbutton = findViewById(R.id.startbutton);
        final Button catchbutton = findViewById(R.id.catchbutton);
        final Button dropbutton = findViewById(R.id.dropbutton);
        startbutton.setVisibility(View.VISIBLE);
        dropbutton.setVisibility(View.GONE);
        catchbutton.setVisibility(View.GONE);
        setCurrentVolume();
        timer.cancel();
        timer.purge();
        inRun = false;
        runsarraylist.add(formatSeconds(runduration)+" ("+endtype+")");
        runslistviewadapter.notifyDataSetChanged();
    }
    public void startTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (inRun) {
                            long tsLong = System.currentTimeMillis()/1000;
                            int currenttime = (int)tsLong;
                            runduration = currenttime - starttimeoflastrun;
                            timertext.setText(formatSeconds(runduration));
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
    public static String formatSeconds(int timeInSeconds)
    {
        int hours = timeInSeconds / 3600;
        int secondsLeft = timeInSeconds - hours * 3600;
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        String formattedTime = "";
        if (hours < 10)
            formattedTime += "0";
        formattedTime += hours + ":";
        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";
        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds ;
        return formattedTime;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getBaseContext(), "doFirstUseStuff()",Toast.LENGTH_SHORT).show();
            doFirstUseStuff();
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
    public void Read(){
        final int READ_BLOCK_SIZE = 100;
        try {
            FileInputStream fileIn=openFileInput("patternlist.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);
            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;
            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            //Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void resetuserslistfromtemplate(String userlist){
        final int READ_BLOCK_SIZE = 100;
        try {
            InputStream ins = getResources().openRawResource(
                    getResources().getIdentifier(userlist+"template",
                            "raw", getPackageName()));
            InputStreamReader InputRead= new InputStreamReader(ins);
            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;
            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            FileOutputStream fileout=openFileOutput(userlist+".txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(s);
            outputWriter.close();
            //display file saved message
            //Toast.makeText(getBaseContext(), "File saved successfully!",Toast.LENGTH_SHORT).show();
            //Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void writeToTextFile(String listfile, String stringtoadd){
        try {
            FileOutputStream fileout=openFileOutput(listfile+".txt",MODE_PRIVATE | MODE_APPEND);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write("\n"+stringtoadd);
            outputWriter.close();
            //display file saved message
            //Toast.makeText(getBaseContext(), "File saved successfully!",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
/*
//Toast.makeText(getBaseContext(), "A Toast to be used!",Toast.LENGTH_SHORT).show();
TODO
-NEXT:
    -move some stuff into a new class, whatever it takes, we gotta be organized!!
-TO MAKE DB:
    -once all dialogs are complete, then do this
    -when a run begins, check in db that columns exist for each
-design the inputs for pattern and modifiers. Probably both some kind of alertdialog popup.
    -SPECIAL THROWS. needs an edittext for sequence input if applicable
-move phone brunn phone notes over here
-organize these notes
-next up stuff
    -see if db stuff is working
    -make import/export db stuff in settings
    -see if anything we have now can be turned into its own class by modelling off of formics database helper(once its in here)
-when to update the db:
    -if a few minutes pass of no runs
    -when prop, pattern, or mod changes
    -when mail button is clicked?
-possible mail button uses
    -update db
    -ai coach(recommends patterns)
    -set by user in settings
-a sound played when a run starts would get rid of the problem of runs accidentally being started and stopped in the background
-once db is installed:
    -show records at beginning of list, and also show all the history(but make records obvious), use dates by each duration
    -make easy way to input records in the past
-settings:
    -make another screen show when settings is clicked
    -buffer time
    -make sounds? set sounds?
    -make import/export db stuff
-eventually
    -make letters work for siteswaps
    -there can be an 'only show patterns/modifiers without history checkbox
    -swipping on number instead of it being a spinner(in the pattern dialog)
    -swipping on pattern/mod/st could automatically go through whatever list is currently
            set in the dialog. THIS MEANS THAT WE NEED TO KEEP TRACK OF EVERYTHING THAT IS CURRENTLY SET
            IN DIALOGS. We should autoload that stuff into dialogs when they open.
    -make graphs of progress
    -use calendar view to go back in time(maybe this could also be used to add past runs
    -an automatic throw count estimator for patterns that have had both time and throw data given
-Things I don't understand
    -private/public variables
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

