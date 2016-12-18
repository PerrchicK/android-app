package com.perrchick.someapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.perrchick.someapplication.ui.SensorsFragmentBlue;
import com.perrchick.someapplication.ui.SensorsFragmentRed;
import com.perrchick.someapplication.uiexercises.AnimationsActivity;
import com.perrchick.someapplication.uiexercises.ImageDownloadActivity;
import com.perrchick.someapplication.uiexercises.SensorsFragment;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorsFragment.SensorsFragmentListener {

    static final int NOTIFICATION_REQUEST_CODE = 1000;
    private final String TAG = MainActivity.class.getSimpleName();

    private static final boolean SHOULD_USE_MOCK = false;
    private static final int COLS_NUM = 3;
    private static final int ROWS_NUM = 3;

    private TicTacToeButton[] buttons = new TicTacToeButton[9];
    private GridLayout mGridLayout;
    private boolean mXTurn = true;

    private Fragment sensorsFragment;
    private FragmentManager fragmentManager;

    private boolean isServiceBound = false;
    public SensorService.SensorServiceBinder binder;
    private LinearLayout boardLayout;
    private GridLayout grid;
    private int threadCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("data")) {
            String data = getIntent().getExtras().getString("data");
            if (data != null && data.length() > 0) {
                PerrFuncs.toast("Got data from notification: " + getIntent().getExtras().getString("data"));
            }
        }

        setContentView(R.layout.activity_main);

//        tickForever(true);

        // The main layout (vertical)
        boardLayout = (LinearLayout) findViewById(R.id.verticalLinearLayout);

        boardLayout.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();
        sensorsFragment = fragmentManager.findFragmentByTag(SensorsFragment.class.getSimpleName());

        // Q: Should I bind it to the main activity or to the app?
        // A: It doesn't matter as long as you remenber to shut the service down / destroy the Application
        // (for more info about this discussion go to: http://stackoverflow.com/questions/3154899/binding-a-service-to-an-android-app-activity-vs-binding-it-to-an-android-app-app)
        if (SHOULD_USE_MOCK) { // if 'true' only the first clause wll be compiled otherwise only the 'else' clause - thanks to the 'final' keyword
            bindService(new Intent(this, SensorServiceMock.class), sensorsBoundServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            bindService(new Intent(this, SensorService.class), sensorsBoundServiceConnection, Context.BIND_AUTO_CREATE);
        }
        // Now, this activity has its own bound service, which broadcasts its own info.
        // In this specific case, a fragment listens to the service's broadcast
    }

    private void tickForever(boolean shouldTickOnMainThread) {
        if (shouldTickOnMainThread) {
            tickOnMainThreadForever();
        } else {
            tickOnAnonymousThreadForever();
        }
    }

    private void tickOnAnonymousThreadForever() {
        // Will count forever (unless... ideas?)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        tick();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    void tickOnMainThreadForever() {
        // Will count forever (unless... ideas?)
        final Handler handler= new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tick();
                // this => that's the runnable that keeps running all the time...
                handler.postDelayed(this, 1000);
            }
        };

        runnable.run();
    }

    private void tick() {
        threadCounter += 1;
        Log.v(TAG, "ticked (threadCounter = " + threadCounter + ") on thread '" + Thread.currentThread().getName() + "'");
    }

    private void putNewBoard() {
        if (grid != null) {
            boardLayout.removeView(grid);
        }
        grid = createNewGrid(COLS_NUM, ROWS_NUM);
        boardLayout.addView(grid, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private GridLayout createNewGrid(int colsNum, int rowsNum) {
        ViewGroup.LayoutParams gridLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mGridLayout = new GridLayout(this);
        mGridLayout.setLayoutParams(gridLayoutParams);
        mGridLayout.setOrientation(GridLayout.HORIZONTAL);
        mGridLayout.setColumnCount(colsNum);
        mGridLayout.setRowCount(rowsNum);
        mGridLayout.setId(0);

        // Programmatically create the buttons layout
        for (int column = 0; column < colsNum; column++) {
            for (int row = 0; row < rowsNum; row++) {
                int fraction = 5;
                int screenWidth = PerrFuncs.screenWidthPixels();
                int screenHeight = PerrFuncs.screenHeightPixels();
                int theSmallerAxis = screenHeight < screenWidth ? screenHeight : screenWidth; // Equals: Math.min(screenHeight, screenWidth);

                int buttonWidth = theSmallerAxis / fraction;

                TicTacToeButton btnTicTacToe = new TicTacToeButton(this,column, row);
                btnTicTacToe.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonWidth));
                btnTicTacToe.setOnClickListener(this);
                buttons[row + column * colsNum] = btnTicTacToe;
                mGridLayout.addView(btnTicTacToe);
            }
        }

        return mGridLayout;
    }

    @Override
    public void onResume() {
        // Starts interaction with the user
        super.onResume();

        PerrFuncs.setTopActivity(this);
        putNewBoard();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // What if... we will put an animation inside the onPause event? Suggestions?
        explodeGrid();
//        OnlineSharedPreferences.getOnlineSharedPreferences(this).putObject("tempFloat",new Float(4)).commitInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: activity destroyed");
        unbindService(sensorsBoundServiceConnection);
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

            switch (-1) {//temporarily disabled, was: switch (v.getId())
                case R.id.verticalLinearLayout: {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    sensorsFragment = fragmentManager.findFragmentById(R.id.sensorsFragment);
                    String sensorsFragmentTag = sensorsFragment.getTag();

                    if(sensorsFragment instanceof SensorsFragmentBlue) {
                        if (fragmentManager.findFragmentByTag(SensorsFragmentRed.class.getSimpleName()) == null) {
                            fragmentTransaction.hide(sensorsFragment);
                            sensorsFragment = new SensorsFragmentRed();
                            fragmentTransaction.add(R.id.sensorsFragment, sensorsFragment);
                        }
                    }else {
                        if (fragmentManager.findFragmentByTag(SensorsFragmentBlue.class.getSimpleName()) == null) {
                            fragmentTransaction.hide(sensorsFragment);
                            sensorsFragment = new SensorsFragmentBlue();
                            fragmentTransaction.add(R.id.sensorsFragment, sensorsFragment);
                        }
                    }

                    fragmentTransaction.show(sensorsFragment);
                    fragmentTransaction.commit();
                }
                break;
            }
        }
    }

    private void checkWinner() {
        TicTacToeButtonPlayer winningPlayer = findWinningPlayer();

        if (winningPlayer != TicTacToeButtonPlayer.None) {
            String winningPlayerStr = winningPlayer == TicTacToeButtonPlayer.xPlayer ? "'X' Player" : "'O' Player";

            PerrFuncs.showDialog("We Have a Winner", "The " + winningPlayerStr + " is the winner");

            for (TicTacToeButton button: buttons) {
                button.setEnabled(false);
                mXTurn = true;
            }
        }
    }

    private TicTacToeButtonPlayer findWinningPlayer() {
        int sum = 0;

        // Diagonal check
        TicTacToeButtonPlayer checkedPlayer = buttons[0].getButtonPlayer();
        for (int index = 0; index < buttons.length; index += 4) {
            if (buttons[index].getButtonPlayer() != TicTacToeButtonPlayer.None && checkedPlayer == buttons[index].getButtonPlayer()) {
                sum++;
            }
        }

        if (sum == 3) {
            return checkedPlayer;
        } else {
            sum = 0;
        }

        // Reversed diagonal check
        checkedPlayer = buttons[2].getButtonPlayer();
        for (int index = 2; index < buttons.length; index += 2) {
            if (buttons[index].getButtonPlayer() != TicTacToeButtonPlayer.None && checkedPlayer == buttons[index].getButtonPlayer()) {
                sum++;
            }
        }

        if (sum == 3) {
            return checkedPlayer;
        } else {
            sum = 0;
        }

        // All columns
        for (int col = 0; col <= 2; col++) {
            checkedPlayer = buttons[col].getButtonPlayer();
            for (int index = col; index < buttons.length; index += 3) {
                if (buttons[index].getButtonPlayer() != TicTacToeButtonPlayer.None && checkedPlayer == buttons[index].getButtonPlayer()) {
                    sum++;
                }
            }
            if (sum == 3) {
                return checkedPlayer;
            } else {
                sum = 0;
            }
        }

        // All rows
        for (int col = 0; col < buttons.length; col += 3) {
            checkedPlayer = buttons[col].getButtonPlayer();
            for (int index = col; index < col + 3; index++) {
                if (buttons[index].getButtonPlayer() != TicTacToeButtonPlayer.None && checkedPlayer == buttons[index].getButtonPlayer()) {
                    sum++;
                }
            }
            if (sum == 3) {
                return checkedPlayer;
            } else {
                sum = 0;
            }
        }

        return TicTacToeButtonPlayer.None;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void explodeGrid() {
        for (int column = 0; column < COLS_NUM; column++) {
            for (int row = 0; row < ROWS_NUM; row++) {
                PerrFuncs.animateRandomlyFlyingOut(buttons[row + column * COLS_NUM], 3000);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Activity.RESULT_OK != resultCode) {
            return;
        }

        switch (requestCode) {
            case NOTIFICATION_REQUEST_CODE:
                String notificationTitle = data.getCharSequenceExtra(NotificationsActivity.EXTRA_NOTIFICATION_TITLE_KEY).toString();
                PerrFuncs.toast("Scheduled notification: '" + notificationTitle + "'");
        }
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
                // Example for explicit intent (the developer chooses the handler, not Android OS)
                startActivity(new Intent(this, AnimationsActivity.class));
                return true;
            /* An old implementation which is no longer needed, the rotation is demonstrated in the AnimationsActivity class
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
            */
            case R.id.action_go_notification:
                startActivityForResult(new Intent(this, NotificationsActivity.class), NOTIFICATION_REQUEST_CODE);
                return true;
            case R.id.action_go_map:
                startActivity(new Intent(this, SomeActivityWithMap.class));
                return true;
            case R.id.action_go_storage:
                startActivity(new Intent(this, StorageActivity.class));
                return true;
            case R.id.action_download_image:
                PerrFuncs.getTextFromUser(this, "Put a string for intent's extra data", new PerrFuncs.CallbacksHandler() {
                    @Override
                    public void callbackWithObject(Object callbackObject) {
                        Intent otherActivityIntent = new Intent();
                        otherActivityIntent.setComponent(new ComponentName(MainActivity.this, ImageDownloadActivity.class));
                        otherActivityIntent.putExtra("data", (String) callbackObject);
                        startActivity(otherActivityIntent);
                    }
                });
                return true;
            case R.id.action_make_phone_call:
                PerrFuncs.getTextFromUser(this, "What number should we call?", new PerrFuncs.CallbacksHandler() {
                    @Override
                    public void callbackWithObject(Object callbackObject) {
                        PerrFuncs.callNumber(callbackObject + "", MainActivity.this);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private enum TicTacToeButtonPlayer {
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
    private ServiceConnection sensorsBoundServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (SensorService.SensorServiceBinder) service;
            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    @Override
    public void valuesUpdated(float[] someData) {
        //Log.v(TAG, "Got an update from fragment: "+Arrays.toString(someData));
    }

    void notifyBoundService(String massageFromActivity) {
        if (isServiceBound) {
            binder.notifyService(massageFromActivity);
        }
    }
}