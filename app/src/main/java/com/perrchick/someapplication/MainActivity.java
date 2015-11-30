package com.perrchick.someapplication;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.perrchick.someapplication.uiexercises.AnimationsActivity;
import com.perrchick.someapplication.uiexercises.ImageDownload;
import com.perrchick.someapplication.utilities.PerrFuncs;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TicTacToeButton[] buttons = new TicTacToeButton[9];
    private final String TAG = MainActivity.class.getSimpleName();
    private boolean xTurn = true;
    private GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PerrFuncs.setApplicationContext(getApplicationContext());

        setContentView(R.layout.activity_main);

        // The main layout (vertical)
        LinearLayout boardLayout = (LinearLayout) findViewById(R.id.verticalLinearLayout);
        boardLayout.addView(createNewGrid(3, 3), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private GridLayout createNewGrid(int colsNum, int rowsNum) {
        ViewGroup.LayoutParams gridLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        gridLayout = new GridLayout(MainActivity.this);
        gridLayout.setLayoutParams(gridLayoutParams);
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        gridLayout.setColumnCount(colsNum);
        gridLayout.setRowCount(rowsNum);
        gridLayout.setId(0);

        // Programmatically create the buttons layout
        for (int column = 0; column < colsNum; column++) {
            for (int row = 0; row < rowsNum; row++) {
                TicTacToeButton btnTicTacToe = new TicTacToeButton(this,column, row);
                btnTicTacToe.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                btnTicTacToe.setOnClickListener(this);
                buttons[row + column * colsNum] = btnTicTacToe;
                gridLayout.addView(btnTicTacToe);
            }
        }

        return gridLayout;
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
                if (xTurn) {
                    clickedButton.setText("X");
                    clickedButton.setButtonPlayer(TicTacToeButtonPlayer.xPlayer);
                } else {
                    clickedButton.setText("O");
                    clickedButton.setButtonPlayer(TicTacToeButtonPlayer.oPlayer);
                }
                xTurn = !xTurn;

                checkWinner();
            }
        } else {
            Log.e(TAG, "clicked on a View which is not a '" + TicTacToeButton.class.getSimpleName() + "'!");
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
                //this.gridLayout.setAnimation(animHyperspaceJump);

                RotateAnimation rotateAnimation = new RotateAnimation(0f, -360f,gridLayout.getWidth() / 2.0f,gridLayout.getHeight() / 2.0f);
                rotateAnimation.setInterpolator(new AccelerateInterpolator());
                rotateAnimation.setDuration(2000);
                rotateAnimation.setFillAfter(true);
                this.gridLayout.startAnimation(rotateAnimation);
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
}