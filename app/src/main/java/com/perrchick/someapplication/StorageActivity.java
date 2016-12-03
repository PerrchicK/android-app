package com.perrchick.someapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.backendless.exceptions.BackendlessException;
import com.firebase.client.FirebaseError;
import com.perrchick.onlinesharedpreferences.OnlineSharedPreferences;
import com.perrchick.onlinesharedpreferences.SyncedSharedPreferences;
import com.perrchick.someapplication.data.DictionaryOpenHelper;
import com.perrchick.someapplication.utilities.PerrFuncs;

import java.util.ArrayList;
import java.util.HashMap;

public class StorageActivity extends AppCompatActivity implements SyncedSharedPreferences.SyncedSharedPreferencesListener {

    private static final String EDIT_TEXT_PERSISTENCE_KEY = "EDIT_TEXT_PERSISTENCE_KEY";
    private static final String SELECTED_ENUM_PERSISTENCE_KEY = "SELECTED_ENUM_PERSISTENCE_KEY";
    private static final String TAG = StorageActivity.class.getSimpleName();

    private OnlineSharedPreferences db_backendlessSharedPreferences;
    private SyncedSharedPreferences db_firebaseSharedPreferences;
    private SharedPreferences db_sharedPreferences;
    private SharedPreferences.Editor db_sharedPreferencesEditor;
    private DictionaryOpenHelper db_sqLiteHelper;

    private EditText editTextSharedPrefs;
    private EditText editTextSQLite;
    private EditText editTextBackendless;
    private EditText editTextFirebase;

    private Spinner dropdownList;
    private ListView listOfBackendlessSavedObjects;
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
        // Also 'this' may be passed as context
        this.db_backendlessSharedPreferences = OnlineSharedPreferences.getOnlineSharedPreferences(this);
        this.db_firebaseSharedPreferences = SyncedSharedPreferences.getSyncedSharedPreferences(this, this);
        this.db_sqLiteHelper = new DictionaryOpenHelper(this);

        this.editTextSharedPrefs = (EditText) findViewById(R.id.txt_shared_prefs);
        this.editTextSQLite = (EditText) findViewById(R.id.txt_sqlite);
        this.editTextBackendless = (EditText) findViewById(R.id.txt_backendless);
        this.editTextFirebase = (EditText) findViewById(R.id.txt_firebase);

        // Use Backendless abilities for A/B Testing:
        db_backendlessSharedPreferences.getString("hide action bar", new OnlineSharedPreferences.GetStringCallback() {
            @Override
            public void done(String value, BackendlessException exception) {
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
                    if (exception != null) {
                        exception.printStackTrace();
                    }
                }
            }
        });

        this.listOfBackendlessSavedObjects = (ListView)findViewById(R.id.listOfBackendlessSavedObjects);
        this.listOfBackendlessSavedObjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object pressedKey = StorageActivity.this.listOfBackendlessSavedObjects.getAdapter().getItem(position);
                Object value = objects.get(pressedKey.toString());
                PerrFuncs.toast(value.toString());
            }
        });
        this.listOfBackendlessSavedObjects.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final Object pressedKey = StorageActivity.this.listOfBackendlessSavedObjects.getAdapter().getItem(position);
                Object oldValue = objects.get(pressedKey.toString());
                getTextFromUser("New string", oldValue.toString(), new PerrFuncs.CallbacksHandler() {
                    @Override
                    public void callbackWithObject(Object callbackObject) {
                        if (callbackObject == null) { // Delete
                            PerrFuncs.askUser(StorageActivity.this, "Delete?", new PerrFuncs.CallbacksHandler() {
                                @Override
                                public void callbackWithObject(Object callbackObject) {
                                    if (callbackObject instanceof Boolean) {
                                        if ((Boolean) callbackObject) {
                                            db_backendlessSharedPreferences.remove(pressedKey.toString(), new OnlineSharedPreferences.RemoveCallback() {
                                                @Override
                                                public void done(BackendlessException e) {
                                                    if (e == null) {
                                                        PerrFuncs.toast("Deleted");
                                                        refreshBackendlessList();
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
                            final String newValue = (String) callbackObject;
                            saveInBackendlessCloud(key, newValue, new OnlineSharedPreferences.CommitCallback() {
                                @Override
                                public void done(BackendlessException e) {
                                    String completionMessage;
                                    if (e == null) {
                                        objects.put(key, newValue);
                                        completionMessage = "Saved successfully in Backendless cloud";
                                    } else {
                                        completionMessage = "Failed to save in Backendless cloud, Exception: " + e.getMessage();
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

        findViewById(R.id.btnAddBackendlessSavedObject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final StorageActivity storageActivity = StorageActivity.this;
                EditText keyTextInput = new EditText(storageActivity);
                keyTextInput.setHint("key");
                EditText valueTextInput = new EditText(storageActivity);
                valueTextInput.setHint("value");
                EditText[] textInputs = new EditText[]{keyTextInput, valueTextInput};

                PerrFuncs.getTextsFromUser(storageActivity, "Add new <key,value>", textInputs , new PerrFuncs.CallbacksHandler() {
                    @Override
                    public void callbackWithObject(Object callbackObject) {
                        if (callbackObject instanceof ArrayList) {
                            ArrayList<String> texts = (ArrayList<String>) callbackObject;
                            String key = texts.get(0);
                            String value = texts.get(1);

                            // Validate
                            if (key.length() == 0 && value.length() == 0) {
                                return;
                            }

                            // <key,value> are valid, proceed...
                            db_backendlessSharedPreferences.putString(key, value).commitInBackground(new OnlineSharedPreferences.CommitCallback() {
                                @Override
                                public void done(BackendlessException e) {
                                    if (e == null) {
                                        PerrFuncs.toast("Added!");
                                        refreshBackendlessList();
                                    } else {
                                        PerrFuncs.toast("Error in adding  <"+","+">" + e.toString());
                                    }

                                }
                            });
                        }
                    }
                });
                //saveInBackendlessCloud(key, value);
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
        db_backendlessSharedPreferences.getString(EDIT_TEXT_PERSISTENCE_KEY, new OnlineSharedPreferences.GetStringCallback() {
            @Override
            public void done(String value, BackendlessException e) {
                if (value != null && e == null) {
                    editTextBackendless.setText(value);
                } else {
                    Log.e(TAG, "Error fetching edit text value from Backendless");
                }
            }
        });

        db_firebaseSharedPreferences.getString(EDIT_TEXT_PERSISTENCE_KEY, new SyncedSharedPreferences.GetStringCallback() {
            @Override
            public void done(String value, Exception e) {
                editTextFirebase.setText(value);
            }
        });

        // Restore Spinner selected option
        int lastSelectedEnumId = db_sharedPreferences.getInt(SELECTED_ENUM_PERSISTENCE_KEY, KeepCalmAnd.Relax.getEnumId());
        dropdownList.setSelection(PerrFuncs.getIndexOfItemInArray(KeepCalmAnd.valueOf(lastSelectedEnumId), KeepCalmAnd.values()));

        refreshBackendlessList();
    }

    private void refreshBackendlessList() {
        // Restore Backendless List View
        db_backendlessSharedPreferences.getAllObjects(new OnlineSharedPreferences.GetAllObjectsCallback() {
            @Override
            public void done(HashMap<String, String> objects, BackendlessException e) {
                if (e == null) {
                    StorageActivity.this.objects = objects;
                    ArrayAdapter<Object> adapter = new ArrayAdapter<>(StorageActivity.this, android.R.layout.simple_spinner_dropdown_item, objects.keySet().toArray());
                    listOfBackendlessSavedObjects.setAdapter(adapter);
                } else {
                    PerrFuncs.toast("Error! Exception:\n" + e, false);
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
        String editTextBackendlessString = this.editTextBackendless.getText().toString();
        String editTextFirebase = this.editTextFirebase.getText().toString();

        // Shared Preferences
        if (!this.db_sharedPreferencesEditor.putString(EDIT_TEXT_PERSISTENCE_KEY, editTextSharedPrefsString).commit()) {
            PerrFuncs.toast("Failed to update Shared Preferences!");
        }
        // SQLite
        if (this.db_sqLiteHelper.put(EDIT_TEXT_PERSISTENCE_KEY, editTextSQLiteString) == -1) {
            PerrFuncs.toast("Failed to update SQLite!");
        }
        // Backendless cloud
        saveInBackendlessCloud(EDIT_TEXT_PERSISTENCE_KEY, editTextBackendlessString, new OnlineSharedPreferences.CommitCallback() {
            @Override
            public void done(BackendlessException e) {
                if (e != null) {
                    PerrFuncs.toast("Failed to update Backendless! Exception:\n" + e);
                }
            }
        });
        // firebase cloud
        db_firebaseSharedPreferences.putString(EDIT_TEXT_PERSISTENCE_KEY, editTextFirebase);
        db_firebaseSharedPreferences.remove("temp");
    }

    public void onSyncedSharedPreferencesChanged(SyncedSharedPreferencesChangeType changeType, String key, String value) {
        if (EDIT_TEXT_PERSISTENCE_KEY == key) {
            if (changeType.compareTo(SyncedSharedPreferencesChangeType.Removed) == 0) {
                editTextFirebase.setText("");
            } else {
                editTextFirebase.setText(value);
            }
        }

        PerrFuncs.toast("Firebase key value " + changeType + ": <" + key + "," + value + ">");
    }

    @Override
    public void onSyncedSharedPreferencesError(FirebaseError error) {
        // Reconnect?
    }

    protected void saveInBackendlessCloud(String key, String value, OnlineSharedPreferences.CommitCallback saveCallback) {
        if (saveCallback == null) {
            db_backendlessSharedPreferences.putString(key, value).commitInBackground();
        } else {
            db_backendlessSharedPreferences.putString(key, value).commitInBackground(saveCallback);
        }
    }

    protected void saveInBackendlessCloud(final String key, final String value) {
        saveInBackendlessCloud(key, value, null);
    }

    public void getTextFromUser(String title, String defaultText, final PerrFuncs.CallbacksHandler callbacksHandler) {
        if (callbacksHandler == null) {
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
                callbacksHandler.callbackWithObject(result);
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callbacksHandler.callbackWithObject(null);
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