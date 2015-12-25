package com.perrchick.someapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.parse.ParseException;
import com.parse.SaveCallback;
import com.perrchick.someapplication.data.SomeGlobalParseService;
import com.perrchick.someapplication.data.DictionaryOpenHelper;
import com.perrchick.someapplication.uiexercises.ImageDownload;
import com.perrchick.someapplication.utilities.PerrFuncs;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageActivity extends AppCompatActivity {

    private static final String EDIT_TEXT_PERSISTENCE_KEY = "EDIT_TEXT_PERSISTENCE_KEY";
    private static final String SELECTED_ENUM_PERSISTENCE_KEY = "SELECTED_ENUM_PERSISTENCE_KEY";
    private static final String TAG = StorageActivity.class.getSimpleName();

    private SomeGlobalParseService.ParseSharedPreferences db_parseSharedPreferences;
    private SharedPreferences db_sharedPreferences;
    private SharedPreferences.Editor db_sharedPreferencesEditor;
    private DictionaryOpenHelper db_sqLiteHelper;

    private EditText editTextSharedPrefs;
    private EditText editTextSQLite;
    private EditText editTextParse;

    private Spinner dropdownList;
    private ListView listOfParseSavedObjects;
    private HashMap<String, String> objects;

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
        this.db_parseSharedPreferences = SomeGlobalParseService.getParseSharedPreferences(this);
        this.db_sqLiteHelper = new DictionaryOpenHelper(this);

        this.editTextSharedPrefs = (EditText) findViewById(R.id.txt_shared_prefs);
        this.editTextSQLite = (EditText) findViewById(R.id.txt_sqlite);
        this.editTextParse = (EditText) findViewById(R.id.txt_parse);

        // Use Parse abilities for A/B Testing:
        db_parseSharedPreferences.getObject("hide action bar", new SomeGlobalParseService.GetObjectCallback() {
            @Override
            public void done(String value, ParseException parseException) {
                if (value != null) {
                    if (Boolean.parseBoolean(value.toString()) == true) {
                        PerrFuncs.hideActionBarOfActivity(StorageActivity.this);
                    } else {
                        try {
                            if (Integer.parseInt(value.toString()) == 1) {
                                PerrFuncs.hideActionBarOfActivity(StorageActivity.this);
                            }
                        } catch (NumberFormatException numberFormatException) {
                            Log.e(TAG, "Unknown boolean value (" + value + ") for 'show action bar' test.\nException:" + numberFormatException.toString());
                        }
                    }
                } else {
                    if (parseException != null) {
                        parseException.printStackTrace();
                    }
                }
            }
        });

        this.listOfParseSavedObjects = (ListView)findViewById(R.id.listOfParseSavedObjects);
        this.listOfParseSavedObjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object pressedKey = StorageActivity.this.listOfParseSavedObjects.getAdapter().getItem(position);
                String value = objects.get(pressedKey.toString());
                PerrFuncs.toast(value);
            }
        });
        this.listOfParseSavedObjects.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final Object pressedKey = StorageActivity.this.listOfParseSavedObjects.getAdapter().getItem(position);
                String oldValue = objects.get(pressedKey.toString());
                getTextFromUser("New string", oldValue, new PerrFuncs.Callback() {
                    @Override
                    public void callbackCall(Object callbackObject) {
                        if (callbackObject == null) { // Delete
                            PerrFuncs.askUser(StorageActivity.this, "Delete?", new PerrFuncs.Callback() {
                                @Override
                                public void callbackCall(Object callbackObject) {
                                    if (callbackObject instanceof Boolean) {
                                        if ((Boolean)callbackObject) {
                                            db_parseSharedPreferences.remove(pressedKey.toString(), new SomeGlobalParseService.RemoveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        PerrFuncs.toast("Deleted");
                                                        refreshParseList();
                                                    } else {
                                                        PerrFuncs.toast("Failed to delete, Exception: " + e.toString());
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        } else if (callbackObject instanceof String) {
                            final String key = (String) pressedKey;
                            final String newValue = (String)callbackObject;
                            saveInParseCloud(key, newValue, new SomeGlobalParseService.CommitCallback() {
                                @Override
                                public void done(ParseException e) {
                                    String completionMessage;
                                    if (e == null) {
                                        objects.put(key, newValue);
                                        completionMessage = "Saved successfully in Parse cloud";
                                    } else {
                                        completionMessage = "Failed to save in Parse cloud, Exception: " + e.toString();
                                    }

                                    PerrFuncs.toast(completionMessage);
                                }
                            });
                        }
                    }
                });
                return true;
            }
        });

        findViewById(R.id.btnAddParseSavedObject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final StorageActivity storageActivity = StorageActivity.this;
                EditText keyTextInput = new EditText(storageActivity);
                keyTextInput.setHint("key");
                EditText valueTextInput = new EditText(storageActivity);
                valueTextInput.setHint("value");
                EditText[] textInputs = new EditText[]{keyTextInput, valueTextInput};

                PerrFuncs.getTextsFromUser(storageActivity, "Add new <key,value>", textInputs , new PerrFuncs.Callback() {
                    @Override
                    public void callbackCall(Object callbackObject) {
                        if (callbackObject instanceof ArrayList) {
                            ArrayList<String> texts = (ArrayList<String>) callbackObject;
                            String key = texts.get(0);
                            String value = texts.get(1);

                            // Validate
                            if (key.length() == 0 && value.length() == 0) {
                                return;
                            }

                            // <key,value> are valid, proceed...
                            db_parseSharedPreferences.putObject(key, value).commitInBackground(new SomeGlobalParseService.CommitCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        PerrFuncs.toast("Added!");
                                        refreshParseList();
                                    } else {
                                        PerrFuncs.toast("Error in adding  <"+","+">" + e.toString());
                                    }

                                }
                            });
                        }
                    }
                });
                //saveInParseCloud(key, value);
            }
        });

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
        //getMenuInflater().inflate(R.menu.menu_storage, menu);
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
        editTextSharedPrefs.setText(db_sharedPreferences.getString(EDIT_TEXT_PERSISTENCE_KEY, ""));
        editTextSQLite.setText(db_sqLiteHelper.get(EDIT_TEXT_PERSISTENCE_KEY, ""));
        db_parseSharedPreferences.getObject(EDIT_TEXT_PERSISTENCE_KEY, new SomeGlobalParseService.GetObjectCallback() {
            @Override
            public void done(String value, ParseException e) {
                if (value != null) {
                    editTextParse.setText(value);
                }
            }
        });

        // Restore Spinner selected option
        int lastSelectedEnumId = db_sharedPreferences.getInt(SELECTED_ENUM_PERSISTENCE_KEY, KeepCalmAnd.Relax.getEnumId());
        dropdownList.setSelection(PerrFuncs.getIndexOfItemInArray(KeepCalmAnd.valueOf(lastSelectedEnumId), KeepCalmAnd.values()));

        refreshParseList();
    }

    private void refreshParseList() {
        // Restore Parse List View
        db_parseSharedPreferences.getAllObjects(new SomeGlobalParseService.GetAllObjectsCallback() {
            @Override
            public void done(HashMap<String, String> objects, ParseException e) {
                if (e == null) {
                    StorageActivity.this.objects = objects;
                    ArrayAdapter<Object> adapter = new ArrayAdapter<>(StorageActivity.this, android.R.layout.simple_spinner_dropdown_item, objects.keySet().toArray());
                    listOfParseSavedObjects.setAdapter(adapter);
                } else {
                    PerrFuncs.toast("Error! Exception:\n" + e);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // Using different kinds of persistence:
        String editTextSharedPrefsString = this.editTextSharedPrefs.getText().toString();
        String editTextSQLiteString = this.editTextSQLite.getText().toString();
        String editTextParseString = this.editTextParse.getText().toString();

        // Shared Preferences
        if (!this.db_sharedPreferencesEditor.putString(EDIT_TEXT_PERSISTENCE_KEY, editTextSharedPrefsString).commit()) {
            PerrFuncs.toast("Failed to update Shared Preferences!");
        }
        // SQLite
        if (this.db_sqLiteHelper.put(EDIT_TEXT_PERSISTENCE_KEY, editTextSQLiteString) == -1) {
            PerrFuncs.toast("Failed to update SQLite!");
        }
        // Parse Cloud
        this.saveInParseCloud(EDIT_TEXT_PERSISTENCE_KEY, editTextParseString, new SomeGlobalParseService.CommitCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    PerrFuncs.toast("Failed to update Parse! Exception:\n" + e);
                }
            }
        });
    }

    protected void saveInParseCloud(String key, String value, SomeGlobalParseService.CommitCallback saveCallback) {
        // Also 'this' may be passed
        db_parseSharedPreferences.putObject(key, value).commitInBackground(saveCallback);
    }

    protected void saveInParseCloud(final String key, final String value) {
        db_parseSharedPreferences.putObject(key, value).commitInBackground();
    }

    public void getTextFromUser(String title, String defaultText, final PerrFuncs.Callback callback) {
        if (callback == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Set up the input control
        final EditText inputText = new EditText(this);

        // Specify the type of input expected; this, for example, add "| InputType.TYPE_TEXT_VARIATION_PASSWORD" and will mask the text
        inputText.setInputType(InputType.TYPE_CLASS_TEXT);
        inputText.setText(defaultText);
        builder.setView(inputText);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String result = inputText.getText().toString();
                callback.callbackCall(result);
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.callbackCall(null);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}