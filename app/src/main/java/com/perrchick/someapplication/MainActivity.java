package com.perrchick.someapplication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.perrchick.someapplication.ui.SensorsFragment;
import com.perrchick.someapplication.ui.SensorsFragmentBlue;
import com.perrchick.someapplication.ui.SensorsFragmentRed;
import com.perrchick.someapplication.uiexercises.AnimationsActivity;
import com.perrchick.someapplication.uiexercises.ImageDownload;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorsFragment.SensorsFragmentListener {

    private final String TAG = MainActivity.class.getSimpleName();

    private TicTacToeButton[] buttons = new TicTacToeButton[9];
    private GridLayout mGridLayout;
    private boolean mXTurn = true;

    SensorsFragment sensorsFragment;

    private boolean isServiceBound = false;
    private SensorService sensorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PerrFuncs.setApplicationContext(getApplicationContext());

        setContentView(R.layout.activity_main);

        // The main layout (vertical)
        LinearLayout boardLayout = (LinearLayout) findViewById(R.id.verticalLinearLayout);
        boardLayout.addView(createNewGrid(3, 3), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Q: Should I bind it to the main activity or to the app?
        // A: It doesn't matter as long as you remenber to shut the service down / destroy the Application
        // (for more info: http://stackoverflow.com/questions/3154899/binding-a-service-to-an-android-app-activity-vs-binding-it-to-an-android-app-app)
        bindService(new Intent(this, SensorService.class), mConnection, Context.BIND_AUTO_CREATE);

        boardLayout.setOnClickListener(this);
    }

    private GridLayout createNewGrid(int colsNum, int rowsNum) {
        ViewGroup.LayoutParams gridLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mGridLayout = new GridLayout(MainActivity.this);
        mGridLayout.setLayoutParams(gridLayoutParams);
        mGridLayout.setOrientation(GridLayout.HORIZONTAL);
        mGridLayout.setColumnCount(colsNum);
        mGridLayout.setRowCount(rowsNum);
        mGridLayout.setId(0);

        // Programmatically create the buttons layout
        for (int column = 0; column < colsNum; column++) {
            for (int row = 0; row < rowsNum; row++) {
                TicTacToeButton btnTicTacToe = new TicTacToeButton(this,column, row);
                btnTicTacToe.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                btnTicTacToe.setOnClickListener(this);
                buttons[row + column * colsNum] = btnTicTacToe;
                mGridLayout.addView(btnTicTacToe);
            }
        }

        return mGridLayout;
    }

    @Override
    public void onResume() {
        super.onResume();

        PerrFuncs.setTopActivity(this);
        PerrFuncs.toast("resumed activity");
    }

    // The method that was defined from the hard coded XML file
    public void clicked(View v) {
        PerrFuncs.toast("clicked button, the method defined by layout's XML");
    }

    @Override
    public void onClick(View v) {
        //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
        if (v instanceof TicTacToeButton) {
            TicTacToeButton clickedButton = (TicTacToeButton)v;
            if (clickedButton.getButtonPlayer() == TicTacToeButtonPlayer.None) {
                if (mXTurn) {
                    clickedButton.setText("X");
                    clickedButton.setButtonPlayer(TicTacToeButtonPlayer.xPlayer);
                } else {
                    clickedButton.setText("O");
                    clickedButton.setButtonPlayer(TicTacToeButtonPlayer.oPlayer);
                }
                mXTurn = !mXTurn;

                checkWinner();
            }
        } else {
            Log.v(TAG, "clicked on a View which is not a '" + TicTacToeButton.class.getSimpleName());

            switch (-1) {//temporarily disabled [v.getId()]
                case R.id.verticalLinearLayout: {
                    FragmentManager fragmentManager = getFragmentManager();

                    sensorsFragment = (SensorsFragment) getFragmentManager().findFragmentById(R.id.sensorsFragment);

                    if(sensorsFragment instanceof SensorsFragmentBlue) {
                        sensorsFragment = (SensorsFragment) fragmentManager.getFragment(new Bundle(), SensorsFragmentRed.class.getSimpleName());
                    }else {
                        sensorsFragment = (SensorsFragment) fragmentManager.getFragment(new Bundle(), SensorsFragmentBlue.class.getSimpleName());
                    }

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.sensorsFragment, sensorsFragment, sensorsFragment.getClass().getSimpleName());
                    fragmentTransaction.commit();
                }
                break;
            }
        }
    }

    private void checkWinner() {
        TicTacToeButtonPlayer winningPlayer = TicTacToeButtonPlayer.None;
        // Upper row
        if (buttons[0].getButtonPlayer() == buttons[1].getButtonPlayer() &&
                buttons[0].getButtonPlayer() == buttons[2].getButtonPlayer() &&
                buttons[0].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[0].getButtonPlayer();
        }
        // Middle row
        else if (buttons[3].getButtonPlayer() == buttons[4].getButtonPlayer() &&
                buttons[3].getButtonPlayer() == buttons[5].getButtonPlayer() &&
                buttons[3].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[3].getButtonPlayer();
        }
        // Bottom row
        else if (buttons[6].getButtonPlayer() == buttons[7].getButtonPlayer() &&
                buttons[6].getButtonPlayer() == buttons[8].getButtonPlayer() &&
                buttons[6].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[6].getButtonPlayer();
        }
        // Left column
        else if (buttons[0].getButtonPlayer() == buttons[3].getButtonPlayer() &&
                buttons[0].getButtonPlayer() == buttons[6].getButtonPlayer() &&
                buttons[0].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[0].getButtonPlayer();
        }
        // Middle column
        else if (buttons[1].getButtonPlayer() == buttons[4].getButtonPlayer() &&
                buttons[1].getButtonPlayer() == buttons[7].getButtonPlayer() &&
                buttons[1].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[1].getButtonPlayer();
        }
        // Right column
        else if (buttons[2].getButtonPlayer() == buttons[5].getButtonPlayer() &&
                buttons[2].getButtonPlayer() == buttons[8].getButtonPlayer() &&
                buttons[2].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[2].getButtonPlayer();
        }
        // Diagonal column
        else if (buttons[0].getButtonPlayer() == buttons[4].getButtonPlayer() &&
                buttons[0].getButtonPlayer() == buttons[8].getButtonPlayer() &&
                buttons[0].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[0].getButtonPlayer();
        }

        // Reversed diagonal column
        else if (buttons[2].getButtonPlayer() == buttons[4].getButtonPlayer() &&
                buttons[2].getButtonPlayer() == buttons[6].getButtonPlayer() &&
                buttons[2].getButtonPlayer()!= TicTacToeButtonPlayer.None) {
            winningPlayer = buttons[2].getButtonPlayer();
        }

        if (winningPlayer != TicTacToeButtonPlayer.None) {
            String winningPlayerStr = winningPlayer == TicTacToeButtonPlayer.xPlayer ? "'X' Player" : "'O' Player";

            new AlertDialog.Builder(this)
                    .setTitle("We Have a Winner")
                    .setMessage("The " + winningPlayerStr + " is the winner")
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            for (TicTacToeButton button: buttons) {
                button.setEnabled(false);
            }
        }
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
        switch (id) {
            case R.id.action_go_to_animations:
                startActivity(new Intent(this, AnimationsActivity.class));
                return true;
            case R.id.action_rotate: {
                // Do animations

                //Animation animHyperspaceJump = AnimationUtils.loadAnimation(this, R.anim.hyperspace_jump);
                //Animation slideInFromLeftAnimation = AnimationUtils.makeInAnimation(this, true);
                //this.mGridLayout.setAnimation(animHyperspaceJump);

                RotateAnimation rotateAnimation = new RotateAnimation(0f, -360f, mGridLayout.getWidth() / 2.0f, mGridLayout.getHeight() / 2.0f);
                rotateAnimation.setInterpolator(new AccelerateInterpolator());
                rotateAnimation.setDuration(2000);
                rotateAnimation.setFillAfter(true);
                this.mGridLayout.startAnimation(rotateAnimation);
            }
                return true;
            case R.id.action_go_notification:
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            case R.id.action_go_storage:
                startActivity(new Intent(this, StorageActivity.class));
                return true;
            case R.id.action_download_image:
                PerrFuncs.getTextFromUser(this, "Put a string for intent's extra data", new PerrFuncs.Callback() {
                    @Override
                    public void callbackCall(Object callbackObject) {
                        Intent otherActivityIntent = new Intent();
                        otherActivityIntent.setComponent(new ComponentName(MainActivity.this, ImageDownload.class));
                        otherActivityIntent.putExtra("data", (String) callbackObject);
                        startActivity(otherActivityIntent);
                    }
                });
                return true;
            case R.id.action_make_phone_call:
                PerrFuncs.getTextFromUser(this, "What number should we call?", new PerrFuncs.Callback() {
                    @Override
                    public void callbackCall(Object callbackObject) {
                        Intent phoneIntent = new Intent(Intent.ACTION_CALL);

                        phoneIntent.setData(Uri.parse("tel:" + callbackObject));
                        startActivity(phoneIntent);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    enum TicTacToeButtonPlayer {
        None,
        xPlayer,
        oPlayer,
    }

    private class TicTacToeButton extends Button {
        private final int x;
        private final int y;
        private TicTacToeButtonPlayer buttonPlayer = TicTacToeButtonPlayer.None;

        public TicTacToeButton(Context context, int x, int y) {
            super(context);
            this.x = x;
            this.y = y;
        }

        public TicTacToeButtonPlayer getButtonPlayer() {
            return buttonPlayer;
        }

        public void setButtonPlayer(TicTacToeButtonPlayer buttonPlayer) {
            this.buttonPlayer = buttonPlayer;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SensorService.SensorServiceBinder binder = (SensorService.SensorServiceBinder) service;
            sensorService = binder.getService();
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    @Override
    public void valuesUpdated(float someData) {
        // In case the fragment wants to update its parent view
    }
}