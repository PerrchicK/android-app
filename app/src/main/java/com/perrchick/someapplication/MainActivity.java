package com.perrchick.someapplication;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.perrchick.someapplication.service.SensorService;
import com.perrchick.someapplication.service.SensorServiceMock;
import com.perrchick.someapplication.ui.TicTacToeButton;
import com.perrchick.someapplication.uiexercises.AnimationsActivity;
import com.perrchick.someapplication.uiexercises.ImageDownloadActivity;
import com.perrchick.someapplication.ui.fragments.SensorsFragment;
import com.perrchick.someapplication.uiexercises.list.EasyListActivity;
import com.perrchick.someapplication.uiexercises.list.ListActivity;
import com.perrchick.someapplication.utilities.AppLogger;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.perrchick.someapplication.utilities.Synchronizer;
import com.perrchick.someapplication.utilities.SynchronizerV0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorService.SensorServiceListener, SensorsFragment.SensorsFragmentListener {

    static final int NOTIFICATION_REQUEST_CODE = 1000;

    private final String TAG = MainActivity.class.getSimpleName();

    private static final boolean SHOULD_USE_MOCK = false;
    private static final int COLS_NUM = 3;
    private static final int ROWS_NUM = 3;

    private TicTacToeButton[] buttons = new TicTacToeButton[ROWS_NUM * COLS_NUM];
    private boolean mXTurn = true;

    private Fragment sensorsFragment;
    private FragmentManager fragmentManager;

    private boolean isServiceBound = false;
    public SensorService.SensorServiceBinder binder;
    private LinearLayout boardLayout;
    private GridLayout mGridLayout;
    private int threadCounter = 0;
    private Intent intentToHandle;
    private SomeApplication.LocalBroadcastReceiver localBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        localBroadcastReceiver = SomeApplication.LocalBroadcastReceiver.createNewReceiver(new SomeApplication.LocalBroadcastReceiver.PrivateBroadcastListener() {
            @Override
            public void onBroadcastReceived(@NonNull Intent intent, SomeApplication.LocalBroadcastReceiver receiver) {
                if (TextUtils.isEmpty(intent.getAction())) return;

                switch (intent.getAction()) {
                    case SomeApplication.LocalBroadcastReceiver.APPLICATION_GOING_BACKGROUND:
                        AppLogger.log(TAG, "going background...");
                        break;
                    case SomeApplication.LocalBroadcastReceiver.APPLICATION_GOING_FOREGROUND:
                        AppLogger.log(TAG, "coming back to foreground!");
                        break;
                }
            }
        }, SomeApplication.LocalBroadcastReceiver.APPLICATION_GOING_BACKGROUND, SomeApplication.LocalBroadcastReceiver.APPLICATION_GOING_FOREGROUND);
//        tickForever(true);

        //synchronizeAsynchronousOperationsV0();
        synchronizeAsynchronousOperations();

        // The main layout (vertical)
        boardLayout = (LinearLayout) findViewById(R.id.verticalLinearLayout);

        boardLayout.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();
        sensorsFragment = fragmentManager.findFragmentByTag(SensorsFragment.class.getSimpleName());

        if (!isServiceBound) {
            // Q: Should I bind it to the main activity or to the app?
            // A: It doesn't matter as long as you remember to shut the service down / destroy the Application
            // (for more info about this discussion go to: http://stackoverflow.com/questions/3154899/binding-a-service-to-an-android-app-activity-vs-binding-it-to-an-android-app-app)
            if (SHOULD_USE_MOCK) { // if 'true' only the first clause wll be compiled otherwise only the 'else' clause - thanks to the 'final' keyword
                bindService(new Intent(this, SensorServiceMock.class), serviceConnectionListener, Context.BIND_AUTO_CREATE);
            } else {
                Intent serviceIntent = new Intent(this, SensorService.class);
                bindService(serviceIntent, serviceConnectionListener, Context.BIND_AUTO_CREATE);

                // Nope, using 'startService' may throw an 'IllegalStateException' in Android O and above.
                //startService(serviceIntent);

                //startForegroundService(serviceIntent); // The app MUST present a local notification to show the user that the app is running

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (!SomeApplication.registerJobService()) {
                        AppLogger.error(TAG, "Failed to schedule background job!");
                    }
                }

            }
            // Now, this activity has its own bound service, which broadcasts its own info.
            // In this specific case, a fragment listens to the service's broadcast
        }
    }

    private void synchronizeAsynchronousOperationsV0() {
        SynchronizerV0 synchronizer = new SynchronizerV0(new Runnable() {
            @Override
            public void run() {
                AppLogger.log(TAG, "All async operations are done!");
            }
        });

        final int min = 1000;
        final int max = 5000;
        final Random random = new Random();
        for (int i = 0; i < 50; i++) {
            final SynchronizerV0.Holder taskHolder = synchronizer.createHolder();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final int randomWaitingTime = random.nextInt((max - min) + 1) + min;
                        Thread.sleep(randomWaitingTime);
                        AppLogger.log(TAG,  "Thread (" + Thread.currentThread().getName() + ") is waiting: " + randomWaitingTime + " milliseconds...");
                    } catch (InterruptedException e) {
                        AppLogger.error(TAG, e);
                    }
                    taskHolder.release();
                }
            }, "thread " + i).start();
        }
    }

    private void synchronizeAsynchronousOperations() {
        Synchronizer<Integer> synchronizer = new Synchronizer<>(new Synchronizer.SynchronizerCallback<Integer>() {
            @Override
            public void done(ArrayList<Integer> extra) {
                AppLogger.log(TAG, "All async operations are done! Extra: " + extra);
            }
        });

        final int min = 1000;
        final int max = 5000;
        final Random random = new Random();
        for (int i = 0; i < 50; i++) {
            final Synchronizer<Integer>.Holder taskHolder = synchronizer.createHolder();
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final int randomWaitingTime = random.nextInt((max - min) + 1) + min;
                        Thread.sleep(randomWaitingTime);
                        AppLogger.log(TAG,  "Thread (" + Thread.currentThread().getName() + ") is waiting: " + randomWaitingTime + " milliseconds...");
                    } catch (InterruptedException e) {
                        AppLogger.error(TAG, e);
                    }
                    if (finalI % 7 == 0) {
                        taskHolder.release(null);
                    } else {
                        taskHolder.release(finalI);
                    }
                }
            }, "thread " + i).start();
        }
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
        if (mGridLayout != null && mGridLayout.getParent() == boardLayout) {
            boardLayout.removeView(mGridLayout);
        }
        mGridLayout = createNewGrid(COLS_NUM, ROWS_NUM);
        boardLayout.addView(mGridLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private GridLayout createNewGrid(int colsNum, int rowsNum) {
        ViewGroup.LayoutParams gridLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setLayoutParams(gridLayoutParams);
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        gridLayout.setColumnCount(colsNum);
        gridLayout.setRowCount(rowsNum);
        gridLayout.setId(0);

        // Programmatically create the buttons layout
        for (int column = 0; column < colsNum; column++) {
            for (int row = 0; row < rowsNum; row++) {
                int fraction = 5;
                int screenWidth = PerrFuncs.screenWidthPixels();
                int screenHeight = PerrFuncs.screenHeightPixels();
                int theSmallerAxis = screenHeight < screenWidth ? screenHeight : screenWidth; // Equals: Math.min(screenHeight, screenWidth);

                int buttonWidth = theSmallerAxis / fraction;

                TicTacToeButton btnTicTacToe = new TicTacToeButton(this, column, row);
                btnTicTacToe.setLayoutParams(new ViewGroup.LayoutParams(buttonWidth, buttonWidth));
                btnTicTacToe.setOnClickListener(this);
                buttons[row + column * colsNum] = btnTicTacToe;
                gridLayout.addView(btnTicTacToe);
            }
        }

        return gridLayout;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        intentToHandle = intent;
    }

    @Override
    public void onResume() {
        // Starts interaction with the user
        super.onResume();

        putNewBoard();

        printAllViews();

        Intent intent = getIntent();
        if (intentToHandle == null && intent != null) {
            intentToHandle = intent;
        }
        handleIntent();
    }

    private void handleIntent() {
        if (intentToHandle == null || !intentToHandle.hasExtra(NotificationsActivity.EXTRA_NOTIFICATION_DATA_KEY)) return;

        String data = intentToHandle.getStringExtra(NotificationsActivity.EXTRA_NOTIFICATION_DATA_KEY);
        if (data != null && data.length() > 0) {
            PerrFuncs.toast("Got data from notification: " + data);

            Integer activityId = PerrFuncs.tryParseInt(data, 0);
            switch (activityId) {
                case 1:
                    presentMapActivity();
                    break;
                case 2:
                    presentAnimationsActivity();
                    break;
                case 3:
                    presentStorageActivity();
                    break;
            }
        }
        intentToHandle = null;
        setIntent(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // What if... we will put an animation inside the onPause event? Suggestions?
        explodeGrid();
//        OnlineSharedPreferences.getOnlineSharedPreferences(this).putObject("tempFloat",new Float(4)).commitInBackground();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (binder != null && binder.getService() != null) {
            binder.getService().setListener(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: activity destroyed");
        unbindService(serviceConnectionListener);
        localBroadcastReceiver.quit();
    }

    // The method that was defined from the hard coded XML file
    public void clicked(View v) {
        PerrFuncs.toast("clicked button, the method defined by layout's XML");
    }

    @Override
    public void onClick(View v) {
        //Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
        if (v instanceof TicTacToeButton) {
            TicTacToeButton clickedButton = (TicTacToeButton) v;
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
            Log.v(TAG, "clicked on a View which is not a '" + TicTacToeButton.class.getSimpleName() + "'");
        }
    }

    private void printAllViews() {
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        printSubviews(0, mainLayout);
    }

    /**
     * A recursive method that presents the composite pattern by iterating all Android's ViewGroup
     */
    private void printSubviews(int level, View viewToPrint) {
        StringBuilder tabsBuilder = new StringBuilder();
        for (int t = 0; t < level; t++) {
            tabsBuilder.append("\t");
        }
        String tabs = tabsBuilder.toString();
        Log.d(TAG, tabs + "<" + viewToPrint.getClass().getSimpleName() + ">");
        if (viewToPrint instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) viewToPrint;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = viewGroup.getChildAt(i);
                printSubviews(level + 1, child);
            }
        }
        Log.d(TAG, tabs + "</" + viewToPrint.getClass().getSimpleName() + ">");
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



                int timeFromNow = data.getIntExtra(NotificationsActivity.EXTRA_NOTIFICATION_DELAY_KEY, 0);
                PerrFuncs.toast("Will notify in " + timeFromNow / 1000 + " seconds...");
                default:
                    Log.e(TAG, "onActivityResult: Unknown request code");
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
                presentAnimationsActivity();
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
            case R.id.action_go_pager:
                startActivity(new Intent(this, PagerActivity.class));
                return true;
            case R.id.action_go_notification:
                startActivityForResult(new Intent(this, NotificationsActivity.class), NOTIFICATION_REQUEST_CODE);
                return true;
            case R.id.action_go_map:
                presentMapActivity();
                return true;
            case R.id.action_go_storage:
                presentStorageActivity();
                return true;
            case R.id.action_go_easy_list:
                // Experimenting - POC for starting activity via application context:
                SomeApplication.getContext().startActivity(new Intent(SomeApplication.getContext(), EasyListActivity.class));
                return true;
            case R.id.action_go_list:
                startActivity(new Intent(this, ListActivity.class));
                return true;
            case R.id.action_download_image:
                PerrFuncs.getTextFromUser(this, "Put a string for intent's extra data", new PerrFuncs.CallbacksHandler<String>() {
                    @Override
                    public void onCallback(String callbackObject) {
                        Intent otherActivityIntent = new Intent();
                        otherActivityIntent.setComponent(new ComponentName(MainActivity.this, ImageDownloadActivity.class));
                        otherActivityIntent.putExtra("data", callbackObject);
                        startActivity(otherActivityIntent);
                    }
                });
                return true;
            case R.id.action_make_phone_call:
                PerrFuncs.getTextFromUser(this, "What number should we call?", new PerrFuncs.CallbacksHandler<String>() {
                    @Override
                    public void onCallback(String callbackObject) {
                        PerrFuncs.callNumber(callbackObject);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void presentAnimationsActivity() {
        startActivity(new Intent(this, AnimationsActivity.class));
    }

    private void presentStorageActivity() {
        startActivity(new Intent(this, StorageActivity.class));
    }

    private void presentMapActivity() {
        startActivity(new Intent(this, SomeMapActivity.class));
    }

    @Override
    public void onSensorValuesChanged(SensorService sensorService, float[] values) {
        if (!SomeApplication.isInForeground()) return;
        Log.d(TAG, "onSensorValuesChanged: Accelerometer sensors state: " + Arrays.toString(values));
    }

    @Override
    public void valuesUpdated(SensorsFragment sensorsFragment, float[] someData) {
        // Do something with fragment's data
    }

    public enum TicTacToeButtonPlayer {
        None,
        xPlayer,
        oPlayer;
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnectionListener = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (SensorService.SensorServiceBinder) service;
            binder.getService().setListener(MainActivity.this);
            isServiceBound = true;
            notifyBoundService(SensorService.SensorServiceBinder.START_LISTENING);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
        }
    };

    void notifyBoundService(String massageFromActivity) {
        if (isServiceBound && binder != null) {
            binder.notifyService(massageFromActivity);
        }
    }
}