package com.openjuggle.brunn;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.DatabaseUtils;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteException;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.os.Environment;
        import android.view.ViewStructure;
        import android.widget.Toast;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileOutputStream;
        import java.nio.channels.FileChannel;
        import java.util.ArrayList;
        import java.util.List;

        import static android.R.id.list;

/**
 * Created by ProgrammingKnowledge on 4/3/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    //when the database name changes here it also must change in MainActivity onCreate()
    //      -in order to actually get rid of an unwanted DB, you must remove the data and cache of the app,
    //              uninstalling and recreating the same DB doesn't work
    public static final String DATABASE_NAME = "brunn.db";
    public static final String TABLE_NAME = "HISTORY";
    public static final String COL_DATE = "DATE";
    public static final String COL_NAME = "NAME";
    public static final String COL_NUMBER = "NUMBER";
    public static final String COL_PROP = "PROP";
    public static final String COL_MODIFIERS = "MODIFIERS";
    public static final String COL_SPECIALTHROWS = "SPECIALTHROWS";
    public static final String COL_SPECIALTHROWSEQUENCES = "SPECIALTHROWSEQUENCES";
    public static final String COL_LINK = "LINK";

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, 1);//this puts our database in cache so that every time'getWritableDatabase()'
        //is used, it uses that database. super() also will run onCreate() IF the
        //database does not already exist
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //'db.execSQL' executes whatever SQLite command(quarry) we give after it
        //in this case it creates our 4 tables
        db.execSQL("create table HISTORY (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "DATE TEXT,NAME TEXT,NUMBER INTEGER,PROP TEXT,MODIFIERS TEXT,SPECIALTHROWS TEXT,SPECIALTHROWSEQUENCES INTEGER)");

        //fillDBwithDefaultValues();
    }


    //i don't know what this function is used for or when it is called,
    //      i also don't know if we want to make an onUprgradePattern & onUpgradeVariant, or just this one for everything
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //the drop command removes a table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //THIS WORKS
    //  this checks to see if the .db file exists
    public boolean checkDataBase() {
        //SQLiteDatabase checkDB = null;gg
        boolean toreturn = false;
        try {

            toreturn =true;
            //checkDB = SQLiteDatabase.openDatabase(DATABASE_NAME, null,
            //        SQLiteDatabase.OPEN_READONLY);
            //checkDB.close();

        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return toreturn;
    }

    //THIS WORKS
    public boolean insertContact () {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("PROP", "poop");
        db.insert("HISTORY", null, contentValues);
        return true;
    }

    //THIS WORKS
    public ArrayList<String> getAllFromColumn(String columnName) {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from HISTORY", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(columnName)));
            res.moveToNext();
        }
        return array_list;
    }

    //THIS WORKS
    public boolean exportDatabase(String packageName, Context context){
        boolean toReturn = false;
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/data/" + packageName + "/databases/" + DATABASE_NAME;
                String backupDBPath = DATABASE_NAME;
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                //Toast.makeText(MainActivity.this, "DB exported!", Toast.LENGTH_LONG).show();
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Toast.makeText(context, "DB exported!", Toast.LENGTH_LONG).show();
                }
                toReturn = true;
            }
        } catch (Exception e) {
            Toast.makeText(context, "DB not exported", Toast.LENGTH_LONG).show();
        }
        return toReturn;
    }

    //THIS WORKS
    public boolean importDatabase(String packageName, Context context){
        boolean toReturn = false;
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {

                String currentDBPath = DATABASE_NAME;
                String backupDBPath = "/data/data/" + packageName + "/databases/" + DATABASE_NAME;
                File currentDB = new File(sd, currentDBPath);
                File backupDB = new File(backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();

                    dst.close();
                    //fillAllFromDB();
                    Toast.makeText(context, "DB imported!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "There is no file to import in the devices main directory.", Toast.LENGTH_LONG).show();
                }
                toReturn = true;
            } else {
                toReturn = false;
            }
        } catch (Exception e) {
            Toast.makeText(context, "DB not imported", Toast.LENGTH_LONG).show();
        }
        return toReturn;
    }

    public List<String> getEveryCellFromTable(String tableName) {

        List<String> listToReturn = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();

        //Cursor res = db.rawQuery("select * from "+tableName,null);
        //Cursor res = db.query(tableName, new String[]{"*"},null, null, null, null, null);
        Cursor res = db.rawQuery("select * from "+tableName,null);

        if (res.moveToFirst()) {
            do {
                String name=res.getString(1); // Here you can get data from table and stored in ArrayList
                listToReturn.add(name);
            } while (res.moveToNext());
        }

        res.close();


        return listToReturn;




    }



    //this add columns to already existing tables,
    //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
    //          it be taken literally which allows us to use parentheses in our string
    public void addColumn(String table, String col, String type){
        SQLiteDatabase db = this.getWritableDatabase();
        //db.execSQL("ALTER TABLE "+table+" ADD COLUMN "+col+" "+type);
        db.execSQL("ALTER TABLE "+table+" ADD COLUMN \""+col+"\" "+type);
    }

    //we use this one insertData function for inserting data into any column of any table,
    //      we just pass the table name and column along with what is to be inserted
    public boolean insertData(String table, String col, String textSent) {

        //Toast.makeText(DatabaseHelper.this,"Data not Inserted",Toast.LENGTH_LONG).show();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
        //          it be taken literally which allows us to use parentheses in our string
        contentValues.put("\""+col+"\"",textSent);
        long result = db.insert(table,null,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }



    public Cursor getFromDB(String table) { //Cursor class provides random read-write access

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+table,null);
        return res;
    }

    //the 1st ID is '1', NOT 0
    //for example ("MODIFIERS","DESCRIPTION","NAME","eyes closed")
    public Cursor getCellFromDatabase(String table,String col,String row,String rowIdentifier) { //Cursor class provides random read-write access

        SQLiteDatabase db = this.getWritableDatabase();
        //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
        //          it be taken literally which allows us to use parentheses in our string
        Cursor res = db.rawQuery("select \""+col+"\" from "+table+" where "+row+" = '"+rowIdentifier+"'",null);

        return res;
    }



    public Cursor getColumnFromDatabase(String table,String col) { //Cursor class provides random read-write access

        SQLiteDatabase db = this.getWritableDatabase();
        //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
        //          it be taken literally which allows us to use parentheses in our string
        Cursor res = db.rawQuery("select \""+col+"\" from "+table,null);

        return res;
    }

    public Cursor getAllFromDatabase(String table) { //Cursor class provides random read-write access

        SQLiteDatabase db = this.getWritableDatabase();
        //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
        //          it be taken literally which allows us to use parentheses in our string
        Cursor res = db.rawQuery("select * from "+table,null);

        return res;
    }

    //THIS WORKS
    public void clearTable(String tableName){

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from "+ tableName);
    }



    //this is very similar to the insertData function, the only difference is that we use 'db.update()'
    public boolean updateData(String table,String col,String row,String rowIdentifier,String textSent) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //Toast.makeText(DatabaseHelper.this, textSent + " isInDB", Toast.LENGTH_LONG).show();

        //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
        //          it be taken literally which allows us to use parentheses in our string
        //contentValues.put("\""+col+"\"","'"+textSent+"'");
        contentValues.put("\""+col+"\"",textSent);
        //contentValues.put(col,textSent);
        //contentValues.put(PATTERNS_COL_NAME,name);
        // I'm not sure exactly why, but it does seem like the second two parts of this, the ' "ID = ?",new String[] { id }'
        //  can be any of our table columns, whichever are chosen that is what the input that is used to identify the
        //  column, or entry, that is being updated. The 'ID = ?' is the column name that the database has on record,
        //      and the next, the new String[] {id}, is what is in that column in the database
        db.update(table, contentValues, row+" = ?",new String[] { rowIdentifier });
        return true;
    }

//    public void deleteDB(){
//        context.deleteDatabase("myDatabaseB");
//    }



    public boolean existsColumnInTable(String table, String columnToCheck) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = null;
        try {
            // Query 1 row
            mCursor = db.rawQuery("SELECT * FROM " + table + " LIMIT 0", null);

            // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
            if (mCursor.getColumnIndex(columnToCheck) != -1)
                return true;
            else
                return false;

        } catch (Exception Exp) {
            // Something went wrong. Missing the database? The table?
            return false;
        } finally {
            if (mCursor != null) mCursor.close();
        }
    }



    public Integer deleteData (String table, String col, String columnIdentifier) {
        SQLiteDatabase db = this.getWritableDatabase();//this.getWritableDatabase() is the database currently in cache,
        //i think it stays there until we close it.
        //it seems to me like this would be the same as the 'updateData()' function above in terms of
        //  how we are identifying which entry to delete

        //  the reason we use \""+col+"\" is because it puts quotes around the string that we pass through in the query and makes
        //          it be taken literally which allows us to use parentheses in our string
        return db.delete(table, "\""+col+"\""+" = ?",new String[] {columnIdentifier});
    }
}
