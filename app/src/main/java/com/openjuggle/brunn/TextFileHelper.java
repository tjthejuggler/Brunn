package com.openjuggle.brunn;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


public class TextFileHelper extends AppCompatActivity {

    private Context context;

    public TextFileHelper(Context context) {

        this.context = context;

    }

    public void appendTextFile(FileOutputStream fileout, String stringtoadd){
        Log.d("myTfh2", "appendTextFile1");
        try {
            //Log.d("myTfh2", );

            //FileOutputStream fileout=openFileOutput(listfile+".txt",MODE_PRIVATE | MODE_APPEND);
            Log.d("myTfh2", "appendTextFile2");
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            Log.d("myTfh2", "appendTextFile3");
            outputWriter.write("\n"+stringtoadd);
            Log.d("myTfh2", "appendTextFile4");
            outputWriter.close();
            Log.d("myTfh2", "appendTextFile5");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetuserslistfromtemplate(InputStream ins, FileOutputStream fileout,String userlist){
        Log.d("myTfh", "resetuserslistfromtemplate");
        final int READ_BLOCK_SIZE = 100;
        try {
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
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(s);
            outputWriter.close();
            Log.d("myTfh", "resetuserslistfromtemplate2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public LinkedList<String> fillListFromTextFile(FileInputStream fileIn){
        LinkedList<String> listToReturn = null;
        final int READ_BLOCK_SIZE = 100;
        Log.d("mine2", "1 ");
        try {
            Log.d("mine2", "2 ");
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
            listToReturn = removeDuplicates(s.split("\\r?\\n"));
            Log.d("mine2", s);
            //Toast.makeText(getBaseContext(), s,Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listToReturn;

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
}
