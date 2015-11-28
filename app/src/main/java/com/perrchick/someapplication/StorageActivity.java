package com.perrchick.someapplication;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.perrchick.someapplication.data.DictionaryOpenHelper;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class StorageActivity extends AppCompatActivity {

    private static final String EDIT_TEXT_PERSISTENCE_KEY = "EDIT_TEXT_PERSISTENCE_KEY";
    private static final String SELECTED_ENUM_PERSISTENCE_KEY = "SELECTED_ENUM_PERSISTENCE_KEY";

    private SharedPreferences db_sharedPreferences;
    private SharedPreferences.Editor db_sharedPreferencesEditor;
    private DictionaryOpenHelper db_sqLiteHelper;

    private EditText editTextSharedPrefs;
    private EditText editTextSQLite;
    private Spinner dropdownList;

    private enum KeepCalmAnd {
        Relax(-1),
        DoHomeWork(100),
        GoCoding(999),
        Run(42);

        private final int enumId;

        KeepCalmAnd(int enumId) {
            this.enumId = enumId;
        }

        public int getEnumId() {
            return this.enumId;
        }

        public static KeepCalmAnd valueOf(int enumId) {
            KeepCalmAnd[] values = KeepCalmAnd.values();
            for(KeepCalmAnd keepCalmAnd : values) {
                if(keepCalmAnd.getEnumId() == enumId)
                    return keepCalmAnd;
            }

            return KeepCalmAnd.Relax;
        }

        @Override
        public String toString() {
            switch (this) {
                case Relax:
                    return super.toString();
                case DoHomeWork:
                    return "Do Home Work";
                case GoCoding:
                    return "Go Coding :P";
                case Run:
                    return super.toString();
            }

            // else...
            return super.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        this.db_sharedPreferences = getSharedPreferences(StorageActivity.class.getSimpleName(), MODE_PRIVATE);
        this.db_sharedPreferencesEditor = db_sharedPreferences.edit();
        this.db_sqLiteHelper = new DictionaryOpenHelper(this);

        this.editTextSharedPrefs = (EditText) findViewById(R.id.txt_shared_prefs);
        this.editTextSQLite = (EditText) findViewById(R.id.txt_sqlite);

        this.dropdownList = (Spinner)findViewById(R.id.enums_spinner);
        KeepCalmAnd[] items = KeepCalmAnd.values();
        ArrayAdapter<KeepCalmAnd> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdownList.setAdapter(adapter);
        dropdownList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeepCalmAnd selectedItem = KeepCalmAnd.values()[position];
                db_sharedPreferencesEditor.putInt(SELECTED_ENUM_PERSISTENCE_KEY, selectedItem.getEnumId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing...
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_storage, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Restore texts
        this.editTextSharedPrefs.setText(this.db_sharedPreferences.getString(EDIT_TEXT_PERSISTENCE_KEY, ""));
        this.editTextSQLite.setText(this.db_sqLiteHelper.get(EDIT_TEXT_PERSISTENCE_KEY, ""));

        // Restore Spinner selected option
        int lastSelectedEnumId = db_sharedPreferences.getInt(SELECTED_ENUM_PERSISTENCE_KEY, KeepCalmAnd.Relax.getEnumId());
        dropdownList.setSelection(PerrFuncs.getIndexOfItemInArray(KeepCalmAnd.valueOf(lastSelectedEnumId),KeepCalmAnd.values()));
    }

    @Override
    public void onPause() {
        super.onPause();

        String editTextSharedPrefsString = this.editTextSharedPrefs.getText().toString();
        String editTextSQLiteString = this.editTextSQLite.getText().toString();
        if (!this.db_sharedPreferencesEditor.putString(EDIT_TEXT_PERSISTENCE_KEY, editTextSharedPrefsString).commit() ||
                this.db_sqLiteHelper.put(EDIT_TEXT_PERSISTENCE_KEY, editTextSQLiteString) == -1) {
            PerrFuncs.toast("Failed to update");
        }
    }
}