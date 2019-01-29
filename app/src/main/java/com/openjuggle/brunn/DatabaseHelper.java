package com.openjuggle.brunn;

        import android.content.ContentValues;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.DatabaseUtils;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteOpenHelper;
        import android.widget.Toast;

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
    public static final String PATTERNS_TABLE_NAME = "PATTERNS";
    public static final String PATTERNS_COL_ID = "ID";
    public static final String PATTERNS_COL_NAME = "ENTRYNAME";
    public static final String PATTERNS_COL_DESCRIPTION = "DESCRIPTION";
    public static final String PATTERNS_COL_SS = "SS";
    public static final String PATTERNS_COL_TYPE = "OBJECTTYPE";
    public static final String PATTERNS_COL_NUMBER = "NUMBER";
    public static final String PATTERNS_COL_ATTRIBUTES = "ATTRIBUTES";
    public static final String PATTERNS_COL_LINK = "LINK";

    public static final String MODIFIERS_TABLE_NAME = "MODIFIERS";
    public static final String MODIFIERS_COL_ID = "ID";
    public static final String MODIFIERS_COL_NAME = "NAME";
    public static final String MODIFIERS_COL_DESCRIPTION = "DESCRIPTION";

    //both of the history tables are simply patterns along one axis and modifiers along the other,
    //      the modifiers are both solo and in groups EX:(modifier1)(modifier2)(modifier1,modifier2)... *EACH() IS A NEW CELL*
    //      each combination of pattern & modifier holds the entire history of that combination,
    //      in each cell for drills it follows this setup:
    //        -(#ofAttempts-if fatal drops, otherwise just make it a 0)(#ofSets)(setLength)(date/time),
    //       in each cell for endurance it follows this setup:
    //          -(run length)(date/time),
    //    -for both history tables, the first item in the MODIFIERS column is 'Pattern Names' because going along
    //              that row is just the pattern names

    public static final String HISTORYDRILL_TABLE_NAME = "HISTORYDRILL";
    public static final String HISTORYDRILL_COL_MODIFIERS = "MODIFIERS";

    public static final String HISTORYENDURANCE_TABLE_NAME = "HISTORYENDURANCE";
    public static final String HISTORYENDURANCE_COL_MODIFIERS = "MODIFIERS";

    //I don't know yet what all will be in here, but some guesses are:
    //      -max number of objects
    //      -types of objects
    //      -default settings such as # of objects and type of object
    public static final String SETTINGS_TABLE_NAME = "SETTINGS";
    public static final String SETTINGS_COL_ID = "ID";
    //the MISC column is used for lots of different settings that need only one cell each. Here is the Key:
    //      0 = max number of objects
    public static final String SETTINGS_COL_MISC = "MISC";

    public static final String SESSIONS_TABLE_NAME = "SESSIONS";


    //things to do to make a varients table
    //      it is the same as the current patterns table, just with an extra column for description

    //things to do to make the complete patterns table
    //      -get the number of columns we need from the other to do list
    //          they should probaby all be string cells, it may be simpler to just go ahead and make the
    //              # of objects column a string as well and just convert toInt as we need it.

    //we are going to need differnet functions in here to add stuff to varients and patterns

    //we are going to also need different functions to remove rows(entries) from either which will be hooked up
    //      to the delete button in info

    //and we are going to need function that can edit preexisting rows(entries)

    public DatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, 1);//this puts our database in cache so that every time'getWritableDatabase()'
        //is used, it uses that database. super() also will run onCreate() IF the
        //database does not already exist
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //'db.execSQL' executes whatever SQLite command(quarry) we give after it
        //in this case it creates our 4 tables
        db.execSQL("create table " + PATTERNS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,ENTRYNAME TEXT,DESCRIPTION TEXT,SS TEXT,ATTRIBUTES TEXT,LINK TEXT)");

        db.execSQL("create table " + MODIFIERS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,NAME TEXT,DESCRIPTION TEXT)");

        db.execSQL("create table " + HISTORYDRILL_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,MODIFIERS TEXT)");

        db.execSQL("create table " + HISTORYENDURANCE_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,MODIFIERS TEXT)");

        db.execSQL("create table " + SETTINGS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,MISC TEXT)");

        db.execSQL("create table " + SESSIONS_TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,BEGINTIME TEXT,ENDTIME TEXT,NOTES TEXT)");
        //fillDBwithDefaultValues();
    }


    //i don't know what this function is used for or when it is called,
    //      i also don't know if we want to make an onUprgradePattern & onUpgradeVariant, or just this one for everything
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //the drop command removes a table
        db.execSQL("DROP TABLE IF EXISTS " + PATTERNS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MODIFIERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORYDRILL_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORYENDURANCE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME);
        onCreate(db);
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
