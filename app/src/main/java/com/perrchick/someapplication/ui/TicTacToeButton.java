package com.perrchick.someapplication.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.AppCompatButton;

import com.perrchick.someapplication.MainActivity;

public class TicTacToeButton extends AppCompatButton {
    private final int x;
    private final int y;
    private MainActivity.TicTacToeButtonPlayer buttonPlayer = MainActivity.TicTacToeButtonPlayer.None;

    public TicTacToeButton(Context context) {
        super(context);
        throw new RuntimeException("This class cannot be instantiated using this constructor!");
    }

    public TicTacToeButton(Context context, int x, int y) {
        super(context);
        this.x = x;
        this.y = y;

        setTag(1);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                // From: https://stackoverflow.com/questions/46111262/card-flip-animation-in-android

                final int alpha = (int) v.getTag() + 1;
                v.setTag(alpha);
                v.animate().withLayer().rotationY(90).setDuration(250).setInterpolator(new LinearInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        v.setAlpha((alpha % 2 == 0) ?  0.5f : 1);
                        v.animate().withLayer().rotationY(90).setDuration(250).setInterpolator(new LinearInterpolator()).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v.setRotationY(0);
                            }
                        }).start();
                    }
                }).start();

                return true;
            }
        });
    }

    public MainActivity.TicTacToeButtonPlayer getButtonPlayer() {
        return buttonPlayer;
    }

    public void setButtonPlayer(MainActivity.TicTacToeButtonPlayer buttonPlayer) {
        this.buttonPlayer = buttonPlayer;
    }
}