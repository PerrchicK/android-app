package com.perrchick.someapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.perrchick.someapplication.R;

/**
 * Created by perrchick on 11/17/15.
 */
public class DictionaryOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DICTIONARY_TABLE_NAME = "dictionary";
    private SQLiteDatabase dataBase;

    public DictionaryOpenHelper(Context context) {
        // The reason of passing null is you want the standard SQLiteCursor behaviour
        super(context, context.getResources().getString(R.string.app_name) + "_db", null, DATABASE_VERSION);
    }

    private String DICTIONARY_TABLE_CREATE(String KEY_WORD, String KEY_DEFINITION) {
        return "CREATE TABLE " + DICTIONARY_TABLE_NAME + " ( " + KEY_WORD + " TEXT PRIMARY KEY, " + KEY_DEFINITION + " TEXT)";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE("Key", "Value"));
        this.dataBase = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long put(ContentValues values) {
        long rowId;

        SQLiteDatabase database = this.getWritableDatabase();

        rowId = database.insertWithOnConflict(DICTIONARY_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        database.close();

        return rowId;
    }

    /**
     * General method for inserting a row into the database.
     *
     * @return the row ID of the newly inserted row
     * OR -1 if any error
     */
    public long put(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("Key", key);
        values.put("Value", value);

        return this.put(values);
    }

    public String get(String key, String defaultValue) {
        String returnValue = defaultValue;
        Cursor cursor = getCursor(key);

        if (cursor.moveToFirst()) {
            returnValue = cursor.getString(0);
        }

        return returnValue;
    }

    public Cursor getCursor(String key) {
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT Value FROM " + DICTIONARY_TABLE_NAME + " where Key = '" + key + "'"; // Instead of "SELECT * FROM"
        Cursor cursor = database.rawQuery(selectQuery, null);

        return cursor;
    }
}