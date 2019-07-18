package com.perrchick.someapplication.uiexercises;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.yalantis.starwars.TilesFrameLayout;
import com.yalantis.starwars.interfaces.TilesFrameLayoutListener;

import java.util.Locale;

import static android.opengl.GLES20.glGenTextures;

public class AnimationsActivity extends AppCompatActivity implements TilesFrameLayoutListener, View.OnDragListener {
    private static final String TAG = AnimationsActivity.class.getSimpleName();
    private static final String SCALE_VALUE_TEXT_VIEW_TAG = "scale's value text";

    private RelativeLayout spinnerContainer;
    private ImageView spinningView;
    private RotateAnimation rotateAnimation;
    private TextView txtScaleValue;
    private SeekBar scaleSeekBar;
    private TextView shrinkingText;
    private ObjectAnimator shrinkingTextAnimator;
    private FrameLayout mainLayout;
    @Nullable
    private TilesFrameLayout mTilesFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animations);

        mainLayout = findViewById(R.id.main_layout);
        if (!PerrFuncs.isRunningOnSimulator() && isStarWarsAnimationAvailable()) {
            mTilesFrameLayout = new TilesFrameLayout(this);
            mTilesFrameLayout.setOnAnimationFinishedListener(this);
            View innerLayout = findViewById(R.id.inner_layout);
            ((ViewGroup) innerLayout.getParent()).removeView(innerLayout);
            mTilesFrameLayout.addView(innerLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mainLayout.addView(mTilesFrameLayout);
        }

        shrinkingText = (TextView) findViewById(R.id.txtShrinking);

        scaleSeekBar = (SeekBar) findViewById(R.id.seekBar);
        scaleSeekBar.setProgress(6);
        scaleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += 1;
                scaleImage(progress);

                // Fade out
                PerrFuncs.animateProperty("alpha", txtScaleValue, txtScaleValue.getAlpha(), (float) progress / 10.0f, 300);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        spinningView = (ImageView) findViewById(R.id.spinnerImage);
        spinningView.setImageResource(R.drawable.ic_spinner_image);
        spinningView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSpinnerAnimation();
            }
        });

        txtScaleValue = (TextView) findViewById(R.id.txtScaleValue);
        txtScaleValue.setTag(SCALE_VALUE_TEXT_VIEW_TAG);
        txtScaleValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerrFuncs.animateNo(v);
            }
        });
        txtScaleValue.setOnLongClickListener(new View.OnLongClickListener() {
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

        findViewById(R.id.mainContainer).setOnDragListener(this);
        spinnerContainer = (RelativeLayout) findViewById(R.id.spinnerContainer);
        spinnerContainer.setOnDragListener(this);

        HandlerThread handlerThread = new HandlerThread("ValueAnimator example thread");
        handlerThread.start();
        Handler valueAnimatorHandler = new Handler(handlerThread.getLooper());
        valueAnimatorHandler.post(new Runnable() {
            @Override
            public void run() {
                // Preventing log info message: "I/Choreographer: Skipped XYZ frames!  The application may be doing too much work on its main thread."
                // FYI: android.util.AndroidRuntimeException: Animators may only be run on Looper threads
                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(10000);
                valueAnimator.setInterpolator(new AccelerateInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        String animatorCounterString = String.format(Locale.ENGLISH, "%.4f", (Float) valueAnimator.getAnimatedValue());
                        Log.d(TAG, "counter: " + animatorCounterString);
                    }
                });
                valueAnimator.start();
            }
        });
    }

    private boolean isStarWarsAnimationAvailable() {
        final int[] textureHandle = new int[1];
        glGenTextures(1, textureHandle, 0);
        return textureHandle[0] != 0;
    }

    @Override
    protected void onStart() {
        super.onStart();

        spinnerContainer.setVisibility(View.INVISIBLE); // What will happen in case we'll use 'View.GONE'?
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTilesFrameLayout != null) {
            mTilesFrameLayout.onResume();
        }

        shrinkingText.setText(R.string.shrink_text_action);

        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(1,0.1f);
        valueAnimator.setDuration(1000);
        valueAnimator.setObjectValues(shrinkingText);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                Log.d(TAG, "shrinkingText.textScaleX = " + valueAnimator.getAnimatedValue());
                AnimationsActivity.this.shrinkingText.setTextScaleX((Float)valueAnimator.getAnimatedValue());
//                AnimationsActivity.this.shrinkingText.setTextScaleY(Float.parseFloat(valueAnimator.getValues()[0].toString()));
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                AnimationsActivity.this.shrinkingText.setText(R.string.shrinking_title);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                spinnerContainer.setTranslationY(-PerrFuncs.screenHeightPixels());
                spinnerContainer.setVisibility(View.VISIBLE);
                spinnerContainer.invalidate();
                PerrFuncs.animateProperty("alpha", AnimationsActivity.this.shrinkingText, 1, 0, 300, new PerrFuncs.CallbacksHandler<Animator>() {
                    @Override
                    public void onCallback(Animator callbackObject) {
                        AnimationsActivity.this.shrinkingText.setVisibility(View.INVISIBLE); // What will happen in case we'll use 'View.GONE'?
                        // An example for ViewPropertyAnimator usage:
                        spinnerContainer.animate()
                                .translationY(0)
                                .setDuration(400)
                                .setStartDelay(500)
                                .start();
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        this.shrinkingTextAnimator = ObjectAnimator.ofFloat(this.shrinkingText, "x", 1.0f, 0.0f);
        this.shrinkingTextAnimator.setDuration(1000);
        this.shrinkingTextAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                AnimationsActivity.this.shrinkingText.setText(R.string.shrinking_title);
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
        mainLayout.setAlpha(0);

        // Prepare UI
        scaleImage(scaleSeekBar.getProgress());

        // Fade the main layout in
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mainLayout, "alpha", 0.0f, 1.0f);
        long duration = 2000;
        fadeIn.setDuration(duration);
        fadeIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                valueAnimator.start();
//                AnimationsActivity.this.shrinkingTextAnimator.start();
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

        if (mTilesFrameLayout != null) {
            mTilesFrameLayout.onPause();
        }

        spinnerContainer.animate().cancel();
    }

    @Override
    public void onBackPressed() {
        //https://github.com/Yalantis/StarWars.Android
        //https://yalantis.com/blog/star-wars-the-force-awakens-or-how-to-crumble-view-into-tiny-pieces-on-android/
        if (mTilesFrameLayout != null) {
            mTilesFrameLayout.startAnimation();
        } else {
            super.onBackPressed();
        }
    }

    private void scaleImage(float scaleSize) {
        if (scaleSize > 0) { // Check anyway to prevent exceptions
//            txtScaleValue.setText(String.format("%f2.0", scaleSize));
            txtScaleValue.setText(String.format(new Locale(Locale.ENGLISH.getLanguage()),"%.1f", scaleSize));

            scaleSize *= 0.1;
            ObjectAnimator scaleImageX = ObjectAnimator.ofFloat(spinningView, "scaleX", spinningView.getScaleX(), scaleSize);
            scaleImageX.setDuration(200);
            scaleImageX.start();

            ObjectAnimator scaleImageY = ObjectAnimator.ofFloat(spinningView, "scaleY", spinningView.getScaleY(), scaleSize);
            scaleImageY.setDuration(200);
            scaleImageY.start();
        }
    }

    private void toggleSpinnerAnimation(boolean start) {
        if (start) {
            rotateAnimation = getRotateAnimation(spinningView);
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

            spinningView.startAnimation(rotateAnimation);
        } else {
            rotateAnimation.setRepeatCount(0);
        }
    }

    private void toggleSpinnerAnimation() {
        toggleSpinnerAnimation(rotateAnimation == null);
//        compile "com.andkulikov:transitionseverywhere:1.7.9"
    }

    void flySpinnerToCorner() {
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(spinningView, "x", -spinningView.getWidth() / 2);
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(spinningView, "y", -spinningView.getHeight() / 2);

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new BounceInterpolator());
        set.play(objectAnimatorX).with(objectAnimatorY);
        set.setDuration(500);
        set.start();
    }


    public RotateAnimation getRotateAnimation(View viewToRotate) {
        RotateAnimation rotateAnimation = new RotateAnimation(0f, 360f, viewToRotate.getWidth() / 2.0f, viewToRotate.getHeight() / 2.0f);
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
