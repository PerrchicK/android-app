package com.perrchick.someapplication.uiexercises;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.transition.Visibility;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.yalantis.starwars.TilesFrameLayout;
import com.yalantis.starwars.interfaces.TilesFrameLayoutListener;

import java.util.Date;

import javax.xml.transform.Transformer;

public class AnimationsActivity extends AppCompatActivity implements TilesFrameLayoutListener, View.OnDragListener {
    private static final String TAG = AnimationsActivity.class.getSimpleName();
    private static final String SCALE_VALUE_TEXT_VIEW_TAG = "scale's value text";

    ImageView spinnerView;
    private RotateAnimation rotateAnimation;
    private TextView txtScaleValue;
    private SeekBar scaleSeekBar;
    private TextView shrinkingText;
    private ObjectAnimator shrinkingTextAnimator;
    private TilesFrameLayout mTilesFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        mTilesFrameLayout = (TilesFrameLayout) findViewById(R.id.tiles_frame_layout);
        mTilesFrameLayout.setOnAnimationFinishedListener(this);

        this.shrinkingText = (TextView) findViewById(R.id.txtShrinking);

        this.scaleSeekBar = (SeekBar) findViewById(R.id.seekBar);
        this.scaleSeekBar.setProgress(6);
        this.scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += 1;
                scaleImage(progress);

                ObjectAnimator fade = ObjectAnimator.ofFloat(txtScaleValue, "alpha", txtScaleValue.getAlpha(), (float) progress / 10.0f);
                long duration = 300;
                fade.setDuration(duration);
                fade.start();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        this.spinnerView = (ImageView) findViewById(R.id.spinnerImage);
        this.spinnerView.setImageResource(R.drawable.ic_spinner_image);
        this.spinnerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSpinnerAnimation();
            }
        });

        this.txtScaleValue = (TextView) findViewById(R.id.txtScaleValue);
        this.txtScaleValue.setTag(SCALE_VALUE_TEXT_VIEW_TAG);
        this.txtScaleValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerrFuncs.sayNo(v);
            }
        });
        this.txtScaleValue.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String viewTag = (String) v.getTag();
                Log.i(TAG, "viewTag = " + viewTag);
                ClipData.Item item = new ClipData.Item(viewTag);
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

                ClipData dragData = new ClipData(viewTag, mimeTypes, item);
                //ClipData dragData = ClipData.newPlainText("", "");
                View.DragShadowBuilder viewShadow = new View.DragShadowBuilder(txtScaleValue);

                v.startDrag(dragData, viewShadow, txtScaleValue, 0);
                return true;
            }
        });
        //this.txtScaleValue.setOnDragListener(this);
        findViewById(R.id.mainContainer).setOnDragListener(this);
        findViewById(R.id.spinnerContainer).setOnDragListener(this);
    }

    @Override
    public void onBackPressed() {
        //https://github.com/Yalantis/StarWars.Android
        //https://yalantis.com/blog/star-wars-the-force-awakens-or-how-to-crumble-view-into-tiny-pieces-on-android/
        mTilesFrameLayout.startAnimation();
    }

    private void scaleImage(float scaleSize) {
        if (scaleSize > 0) { // Check anyway to prevent exceptions
            txtScaleValue.setText("" + scaleSize);

            scaleSize *= 0.1;
            ObjectAnimator scaleImageX = ObjectAnimator.ofFloat(spinnerView, "scaleX", spinnerView.getScaleX(), scaleSize);
            scaleImageX.setDuration(200);
            scaleImageX.start();

            ObjectAnimator scaleImageY = ObjectAnimator.ofFloat(spinnerView, "scaleY", spinnerView.getScaleY(), scaleSize);
            scaleImageY.setDuration(200);
            scaleImageY.start();
        }
    }

    private void toggleSpinnerAnimation(boolean start) {
        if (start) {
            rotateAnimation = getRotateAnimation(this.spinnerView);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rotateAnimation = null;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            this.spinnerView.startAnimation(rotateAnimation);
        } else {
            rotateAnimation.setRepeatCount(0);
        }
    }

    private void toggleSpinnerAnimation() {
        this.toggleSpinnerAnimation(rotateAnimation == null);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTilesFrameLayout.onResume();

        this.shrinkingText.setText("Will shrink...");
        this.shrinkingTextAnimator = ObjectAnimator.ofFloat(this.shrinkingText, "scaleY", 1.0f, 0.0f);
        this.shrinkingTextAnimator.setDuration(1000);
        this.shrinkingTextAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                AnimationsActivity.this.shrinkingText.setText("... shrinking...");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AnimationsActivity.this.shrinkingText.setScaleY(0.0f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // Disappear the main layout
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mainLayout.setAlpha(0);

        // Prepare UI
        scaleImage(this.scaleSeekBar.getProgress());

        // Fade the main layout in
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mainLayout, "alpha", 0.0f, 1.0f);
        long duration = 4000;
        fadeIn.setDuration(duration);
        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AnimationsActivity.this.shrinkingTextAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        fadeIn.start();
    }

    @Override
    public void onPause() {
        super.onPause();

        mTilesFrameLayout.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_animations, menu);
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

    public RotateAnimation getRotateAnimation(View viewToRotate) {
        RotateAnimation rotateAnimation = new RotateAnimation(0f, 360f,viewToRotate.getWidth() / 2.0f,viewToRotate.getHeight() / 2.0f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(500);
        rotateAnimation.setFillAfter(true);

        return rotateAnimation;
    }

    @Override
    public void onAnimationFinished() {
        finish();
    }

    @Override
    public boolean onDrag(View draggingZone, DragEvent event) {
        View draggedView = (View) event.getLocalState();
        if (draggedView.getId() != txtScaleValue.getId()) {
            // During this DND operation, this View object won't receive anymore events until event ACTION_DRAG_ENDED will be sent.
            return false;
        }

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                draggingZone.setAlpha(0.5f);
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                draggingZone.setAlpha(1.0f);
                return true;
            case DragEvent.ACTION_DROP:
                draggingZone.setAlpha(1.0f);
                if (draggingZone.getParent() instanceof LinearLayout) {
                    // Then it's the spinner container
                    ((LinearLayout) draggingZone.getParent()).setAlpha(1.0f);
                }
                draggedView.setX(event.getX());
                draggedView.setY(event.getY());
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                break;
        }

        return false;
    }
}
