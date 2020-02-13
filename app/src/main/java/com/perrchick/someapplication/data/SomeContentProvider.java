package com.perrchick.someapplication.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.perrchick.someapplication.SomeApplication;

/**
 * Created by perrchick on 11/19/15.
 */
public class SomeContentProvider extends ContentProvider {
    // In MANIFEST.XML: <provider android:name=".data.SomeContentProvider" android:authorities={PROVIDER_NAME} />
    static final String PROVIDER_NAME = "com.perrchick.someapplication.provider";
    static final String URL = "content://" + PROVIDER_NAME + "/string/Key";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String KEY = "Key";

    static final UriMatcher uriMatcher;
    private static final int URI_MATCH_KEY_VALUE = 1;

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, KEY, URI_MATCH_KEY_VALUE);
    }

    private DictionaryOpenHelper db_sqLiteHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() { // NOTE: This is called BEFORE the `android.app.Application.onCreate()`, cool ha?
        // Reference: https://www.youtube.com/watch?v=AJqakuas_6g&feature=youtu.be&t=20m38s
        SomeApplication.setContext(getContext()); // The idea from: https://firebase.googleblog.com/2016/12/how-does-firebase-initialize-on-android.html

        // Initialize the simple DictionaryOpenHelper
        this.db_sqLiteHelper = new DictionaryOpenHelper(this.getContext());
        // Create a write able database which will trigger its creation if it doesn't already exist.
        this.db = db_sqLiteHelper.getWritableDatabase();
        return this.db != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // <prefix>://<authority>/<data_type>/<id>
        // content://com.perrchick.someapplication/string/Key
        // content://contacts/people/5
        Cursor cursor = null;

        switch (SomeContentProvider.uriMatcher.match(uri)) {
            case URI_MATCH_KEY_VALUE:
                cursor = this.db_sqLiteHelper.getCursor(selection);
                break;
            default: // Unknown URI
                cursor = this.db_sqLiteHelper.getCursor(selection);
                //throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
/**
 * Add a new student record
 */
        long rowID = db_sqLiteHelper.put(values);

        /**
         * If record is added successfully
         */

        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
