package com.openjuggle.brunn;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class OpenDialogHelper extends MainActivity {

    List<String> addPatObjTypeForMATV = new ArrayList<>();
    List<String> addPatNumberOfObjectsForSpinner = new ArrayList<>();

    public OpenDialogHelper(Context context) {


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






    }

    public void opendialog(String dialogtype){
        if (dialogtype == "prop") {
            View view = (LayoutInflater.from(OpenDialogHelper.this)).inflate(R.layout.choose_prop_dialog, null);





        }/*
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(OpenDialogHelper.this);

            alertBuilder.setView(view);


            alertBuilder.setCancelable(true)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            final Dialog dialog = alertBuilder.create();

            dialog.show();
            dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                    Resources.getSystem().getDisplayMetrics().heightPixels / 2);
}
            //this sets the width of the dialog to the width of the screen, and the height to half the height of the screen




            TextView newAddPatInputDialogBoxNameTV = (TextView) view.findViewById(R.id.newAddPatInputDialogBoxNameTV);
            newAddPatInputDialogBoxNameTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Pattern name", "This is where you input the name of the pattern you are adding.");
                    return true;
                }
            });


            final EditText newAddPatInputDialogBoxName = (EditText) view.findViewById(R.id.newAddPatInputDialogBoxName);


            TextView newAddPatInputDialogNumTV = (TextView) view.findViewById(R.id.newAddPatInputDialogBoxNumTV);
            newAddPatInputDialogNumTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Object number", "This is where you input the number of objects used in the pattern you are adding.");
                    return true;
                }
            });

            final Spinner newAddPatInputDialogNum = (Spinner) view.findViewById(R.id.newAddPatInputDialogBoxNum);
            ArrayAdapter<String> numOfObjsSpinnerAdapter = new ArrayAdapter<>(OpenDialogHelper.this, android.R.layout.select_dialog_item, addPatNumberOfObjectsForSpinner);
            numOfObjsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            newAddPatInputDialogNum.setAdapter(numOfObjsSpinnerAdapter);
            //newAddPatInputDialogNum.setSelection(numOfObjsSpinner.getSelectedItemPosition());
            newAddPatInputDialogNum.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Object number", "This is where you input the number of objects used in the pattern you are adding.");
                    return false;
                }
            });
            newAddPatInputDialogNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " selected", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            TextView newAddPatInputDialogBoxTypeTV = (TextView) view.findViewById(R.id.newAddPatInputDialogBoxTypeTV);
            newAddPatInputDialogBoxTypeTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Prop type", "This is where you input the type of prop used in the pattern you are adding.");
                    return true;
                }
            });

            final MultiAutoCompleteTextView newAddPatInputDialogBoxType =
                    (MultiAutoCompleteTextView) view.findViewById(R.id.newAddPatInputDialogBoxType);
            ArrayAdapter<String> objsTypeMATVAdapter =
                    new ArrayAdapter<>(OpenDialogHelper.this, android.R.layout.select_dialog_item, addPatObjTypeForMATV);
            //objsTypeMATVAdapter.notifyDataSetChanged();
            newAddPatInputDialogBoxType.setAdapter(objsTypeMATVAdapter);

            newAddPatInputDialogBoxType.setThreshold(0);//this is number of letters that must match for autocomplete to suggest a word
            newAddPatInputDialogBoxType.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            newAddPatInputDialogBoxType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Toast.makeText(getBaseContext(), parent.getItemAtPosition(position) + " selected", Toast.LENGTH_LONG).show();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            newAddPatInputDialogBoxType.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        newAddPatInputDialogBoxType.showDropDown();
                        //hideKeyboard();
                    } else {
                        //updateMATV(newAddPatInputDialogBoxType, getCellFromDB("SETTINGS", "MISC", "ID", "2"));
                    }
                }
            });
            newAddPatInputDialogBoxType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newAddPatInputDialogBoxType.showDropDown();

                }
            });
            newAddPatInputDialogBoxType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (newAddPatInputDialogBoxType.getText().toString().contains("Select all")) {
                        newAddPatInputDialogBoxType.setText("");
                        //NEW PHONE: the -1 was added because without it the new phone was crashing there
                        //      because the index was the same as the length, I dont know why this didnt crash the old phone too
                        for (int i = 0; i < newAddPatInputDialogBoxType.getAdapter().getCount()-1; i++) {
                            //showInfoDialog("ya");
                            //newAddPatInputDialogBoxType.clearListSelection();

                            ///newAddPatInputDialogBoxType.append(getCellFromDB("SETTINGS", "MISC", "ID", "2").split(",")[i] + ", ");
                        }

                    }
                    //updateMATV(newAddPatInputDialogBoxType, getCellFromDB("SETTINGS", "MISC", "ID", "2"));
                }
            });


            TextView newAddPatInputDialogBoxDescriptionTV = (TextView) view.findViewById(R.id.newAddPatInputDialogBoxDescriptionTV);
            newAddPatInputDialogBoxDescriptionTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Pattern description", "This is where you input the description of the pattern you are adding.");
                    return true;
                }
            });


            final EditText newAddPatInputDialogBoxDescription = (EditText) view.findViewById(R.id.newAddPatInputDialogBoxDescription);

            TextView newAddPatInputDialogBoxSiteswapTV = (TextView) view.findViewById(R.id.newAddPatInputDialogBoxSiteswapTV);
            newAddPatInputDialogBoxSiteswapTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Siteswap", "This is where you input the siteswap of the pattern you are adding.");
                    return true;
                }
            });


            final EditText newAddPatInputDialogBoxSiteswap = (EditText) view.findViewById(R.id.newAddPatInputDialogBoxSiteswap);


            TextView newAddPatInputDialogBoxLinkTV = (TextView) view.findViewById(R.id.newAddPatInputDialogBoxLinkTV);
            newAddPatInputDialogBoxLinkTV.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //showInfoDialog("Pattern link", "This is where you can input a link relevant to the pattern you are adding.");
                    return true;
                }
            });


            final EditText newAddPatInputDialogBoxLink = (EditText) view.findViewById(R.id.newAddPatInputDialogBoxLink);


            alertBuilder.setCancelable(true)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            final Dialog dialog = alertBuilder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(final DialogInterface dialog) {

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {


                            //updateMATV(newAddPatInputDialogBoxType, getCellFromDB("SETTINGS", "MISC", "ID", "2"));


                            if (!newAddPatInputDialogBoxName.getText().toString().equals("") &&
                                    !newAddPatInputDialogBoxType.getText().toString().equals("")) {
                                //since the ok button was just clicked, we want to add whatever the user has input


                                String siteswapObjType = newAddPatInputDialogBoxType.getEditableText().toString();

                                //we split the modifierMATV string into an array
                                List<String> listOfSplitObjTypeString = new ArrayList<>();
                                Collections.addAll(listOfSplitObjTypeString, siteswapObjType.split(", "));

                                //now we alphabetize the list
                                Collections.sort(listOfSplitObjTypeString, String.CASE_INSENSITIVE_ORDER);

                                for (int j = 0; j < listOfSplitObjTypeString.size(); j++) {


                                    //we make the entry name by following this format @@##&&name@@##&&number@@##&&type@@##&&
                                    //String entryName = buffer + newAddPatInputDialogBoxName.getText().toString() +
                                    //        buffer + newAddPatInputDialogNum.getSelectedItem().toString() +
                                    //        buffer + listOfSplitObjTypeString.get(j) + buffer;

                                    //if (isValidInput(newAddPatInputDialogBoxName.getText().toString())) {

                                        //if (containsCaseInsensitive(entryName, getColumnFromDB("PATTERNS", "ENTRYNAME"))) {
                                            //Toast.makeText(MainActivity.this, "'" + newAddPatInputDialogBoxName.getText().toString() +
                                                    //"' already exists for " + listOfSplitObjTypeString.get(j) +
                                                    //".", Toast.LENGTH_LONG).show();

                                        //} else {


//                                        //but if it isn't empty or already in our database, we add it to the patterns list in the DB

//                                        //now we add our new pattern to the DB in the 'PATTERNS' table
                                            //addDataToDB("PATTERNS", "ENTRYNAME", entryName);

                                            //and add description, siteswap, and link here.
                                            //     this is done the same way as when we change stuff in the add tab inputs
                                            //now we go through and update the DB from all the different inputs

                                            //myDb.updateData("PATTERNS", "DESCRIPTION", "ENTRYNAME", entryName,
                                            //        newAddPatInputDialogBoxDescription.getText().toString());

                                            //myDb.updateData("PATTERNS", "SS", "ENTRYNAME", entryName,
                                            //        newAddPatInputDialogBoxSiteswap.getText().toString());

                                            //myDb.updateData("PATTERNS", "LINK", "ENTRYNAME", entryName,
                                            //        newAddPatInputDialogBoxLink.getText().toString());


//                                                  //and add it to the main Patterns Input
                                            //patternsATV.setText(newAddPatInputDialogBoxName.getText().toString());
                                            //setSpinnerSelection(numOfObjsSpinner, newAddPatInputDialogNum.getSelectedItem().toString());
                                            //setSpinnerSelection(objTypeSpinner, listOfSplitObjTypeString.get(j));

                                            //since we just added a new pattern to the DB, we want to update the mainPatternInputs with it
                                            //Log.d("z", "7");
                                            //fillPatternMainInputsFromDB();

//                                        //debugging
                                            //Toast.makeText(MainActivity.this, "Pattern '"+entryName+"' added", Toast.LENGTH_LONG).show();
//
                                            //clearAddPatternInputs();
                                        }
                                    }
                                }
                                //THIS IS JUST FOR DEBUGGING
                                //Toast.makeText(MainActivity.this, newAddModifierInputDialog.getText().toString(), Toast.LENGTH_LONG).show();


                                //dialog.dismiss();
                            //} else {

                               // Toast.makeText(MainActivity.this, "Name and/or prop type is empty.", Toast.LENGTH_LONG).show();

                            //}
                        //}
                    });
                }
            });
            dialog.show();

            //this sets the width of the dialog to the width of the screen, and the height to half the height of the screen
            dialog.getWindow().setLayout(Resources.getSystem().getDisplayMetrics().widthPixels,
                    Resources.getSystem().getDisplayMetrics().heightPixels / 2);


        }*/







    }
}




