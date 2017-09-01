package com.perrchick.someapplication.utilities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.perrchick.someapplication.Application;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by perrchick on 10/23/15.
 */
public class PerrFuncs {
    private static String TAG = PerrFuncs.class.getSimpleName();
    private static PerrFuncs _perrFuncsInstance;
    private final OkHttpClient httpClient;
    private DisplayMetrics _metrics;

    private static PerrFuncs getInstance() {
        if (_perrFuncsInstance == null) {
            _perrFuncsInstance = new PerrFuncs();
        }

        return _perrFuncsInstance;
    }

    private PerrFuncs() {
        // Initialize OkHttpClient
        httpClient = new OkHttpClient();
        httpClient.setReadTimeout(20, TimeUnit.SECONDS);
    }

    /**
     * Returns the number of milliseconds since the Unix epoch (1.1.1970)
     * @return The number of milliseconds since the Unix epoch (1.1.1970)
     */
    public static long getMillisFrom1970() {
        return System.currentTimeMillis();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.M)
    public static long getMillisFrom1970(TimePicker timePicker) {

        // Solves exception: java.lang.NoSuchMethodError
        int hour = 0;
        int minutes = 0;

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        // java.lang.NoSuchMethodError: No virtual method getHour()I in class Landroid/widget/TimePicker; or its super classes (declaration of 'android.widget.TimePicker' appears in /system/framework/framework.jar:classes2.dex)
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            hour = timePicker.getHour();
            minutes = timePicker.getMinute();
        } else {
            hour = timePicker.getCurrentHour();
            minutes = timePicker.getCurrentMinute();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                hour, minutes, 0);

        return calendar.getTimeInMillis();
    }

    public static boolean hasPermissionForLocationServices(Context context) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Because the user's permissions started only from Android M and on...
            return true;
        }

        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // The user blocked the location services of THIS app
            return false;
        }

        return true;
    }

    /**
     * For Android Marshmallow SDK, version 6.0 (API 23) and above. Checks the permissions in runtime.
     * @return Boolean that determines whether it is allowed to access the given permission
     */
    private boolean checkPermissionFor(String permission) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // More details on:
            // http://developer.android.com/training/permissions/best-practices.html
            // And on:
            // http://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en
        } else {
            // Pre-Marshmallow - not interesting...
        }

        return true;
    }

    public static void hideActionBarOfActivity(AppCompatActivity activity) {
        if (activity.getSupportActionBar() != null) { // Shouldn't have a problem here anyway
            activity.getSupportActionBar().hide();
        }
    }

    public static void animateRandomlyFlyingOut(View view, long duration) {
        Random random = new Random();
        int otherSide;

        otherSide = random.nextBoolean() ? 1 : -1;
        ObjectAnimator flyOutX = ObjectAnimator.ofFloat(view, "x", view.getX(), PerrFuncs.screenWidthPixels() * otherSide);
        flyOutX.setDuration(duration);
        flyOutX.setInterpolator(new DecelerateInterpolator());

        otherSide = random.nextBoolean() ? 1 : -1;
        ObjectAnimator flyOutY = ObjectAnimator.ofFloat(view, "y", view.getY(), PerrFuncs.screenWidthPixels() * otherSide);
        flyOutY.setDuration(duration);
        flyOutY.setInterpolator(new DecelerateInterpolator());

        otherSide = random.nextBoolean() ? 1 : -1;
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", otherSide == 1 ? 0f : 360f, otherSide == 1 ? 360f : 0f);
        rotate.setDuration(duration);
        rotate.setInterpolator(new DecelerateInterpolator());

        rotate.start();
        flyOutY.start();
        flyOutX.start();
    }

    public static Object getObjectProperty(Object object, String propertyName) {
        try {
            Field f = object.getClass().getDeclaredField(propertyName);
            f.setAccessible(true);
            return f.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Makes a network request and fetches an image from the specified URL.
     *
     * @param urlString The URL path to the web GET request.
     * @return A response object if the request succeeded, otherwise it returns null.
     */
    public static void performGetRequest(final String urlString, final PerrFuncs.CallbacksHandler callbacksHandler) {
        // An open source project, downloaded from gradle
        try {
            final Request request = new Request.Builder()
                    .url(urlString)
                    .build();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Response response = null;
                    try {
                        response = getInstance().httpClient.newCall(request).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "performGetRequest: Failed to perform request url from string '" + urlString + "', exception: " + e.toString());
                        if (callbacksHandler != null)
                            callbacksHandler.callbackWithObject(response);
                    }

                    if (callbacksHandler != null)
                        callbacksHandler.callbackWithObject(response);
                }
            }).start();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "performGetRequest: Failed to create request url from string '" + urlString + "'");
            if (callbacksHandler != null)
                callbacksHandler.callbackWithObject(null);
        }
    }

    public static void animateProperty(String whatProperty, Object ofWho, float from, float to, long millis, final CallbacksHandler onDoneHandler) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(ofWho, whatProperty, from, to);
        fadeOut.setDuration(millis);
        fadeOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (onDoneHandler != null){
                    onDoneHandler.callbackWithObject(animator);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        fadeOut.start();
    }

    public static void animateProperty(String whatProperty, Object ofWho, float from, float to, long millis) {
        animateProperty(whatProperty, ofWho, from, to, millis, null);
    }

    public interface CallbacksHandler {
        void callbackWithObject(Object callbackObject);
    }

    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
        return (dateFormat.format(System.currentTimeMillis()));
    }

    public static void toast(String toastMessage) {
        PerrFuncs.toast(toastMessage, true);
    }

    public static void toast(final String toastMessage, final boolean shortDelay) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMessage, shortDelay ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void runOnUiThread(Runnable runnable) {
        if (isRunningOnMainThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    private static boolean isRunningOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private static Context getApplicationContext() {
        return Application.getContext();
    }

    public static void callNumber(String phoneNumber) {
        callNumber(phoneNumber, Application.getTopActivity());
    }

    public static void callNumber(String phoneNumber, Activity activity) {
        // Example for implicit intent (the Android OS will choose the handler)
        Intent phoneCallIntent = new Intent(Intent.ACTION_CALL);
        phoneCallIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // Permission granted, making the call.
        getApplicationContext().startActivity(phoneCallIntent);
    }

    public static void showDialog(final String dialogTitle, final String dialogMessage) {
        showDialog(dialogTitle, dialogMessage, Application.getTopActivity());
    }

    public static void showDialog(final String dialogTitle, final String dialogMessage, Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle(dialogTitle)
                        .setMessage(dialogMessage)
                /*
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                */
                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    public static void askUser(Activity inActivity, String title, final CallbacksHandler callbacksHandler) {
        if (callbacksHandler == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(inActivity);
        builder.setTitle(title);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callbacksHandler.callbackWithObject(new Boolean(true));
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callbacksHandler.callbackWithObject(new Boolean(false));
            }
        });

        builder.show();
    }

    public static void getTextFromUser(Activity inActivity, String title, final CallbacksHandler callbacksHandler) {
        PerrFuncs.getTextFromUser(inActivity, title, "", callbacksHandler);
    }

    public static void getTextFromUser(Activity inActivity, String title, String defaultText, final CallbacksHandler callbacksHandler) {
        if (callbacksHandler == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(inActivity);
        builder.setTitle(title);

        // Set up the input control
        final EditText inputText = new EditText(inActivity);

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
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void getTextsFromUser(Activity inActivity, String title, final EditText[] textInputs, final CallbacksHandler callbacksHandler) {
        if (callbacksHandler == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(inActivity);
        builder.setTitle(title);

        // Set up the input control
        final LinearLayout inputTexts = new LinearLayout(inActivity);
        inputTexts.setOrientation(LinearLayout.VERTICAL);
        for (EditText editText:textInputs) {
            inputTexts.addView(editText);
        }

        // Specify the type of input expected; this, for example, add "| InputType.TYPE_TEXT_VARIATION_PASSWORD" and will mask the text
        builder.setView(inputTexts);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<String> texts = new ArrayList<>(2);
                for (EditText inputText:textInputs) {
                    texts.add(inputText.getText().toString());
                }
                callbacksHandler.callbackWithObject(texts);
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

    public static int screenWidthPixels() {
        return PerrFuncs.getInstance().getMetrics().widthPixels;
    }

    public static int screenHeightPixels() {
        return PerrFuncs.getInstance().getMetrics().heightPixels;
    }

    private DisplayMetrics getMetrics() {
        if (_metrics == null) {
            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            _metrics = new DisplayMetrics();
            display.getMetrics(_metrics);
        }

        return _metrics;
    }

    public static int getIndexOfItemInArray(Object item, Object[] arr) {
        int index = -1;
        int length = arr.length;

        for (int i =0; i < length; i++) {
            if (item == arr[i]) {
                index = i;
            }
        }

        return index;
    }

    /**
     * View says "no" using AnimatorSet
     * @param view    The view that will say "no"
     * */
    public static void animateNo(final View view) {
        final long duration = 50;
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationX", 20f);
        animator1.setDuration(duration / 2);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "translationX", -20f);
        animator2.setDuration(duration);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "translationX", 5f);
        animator3.setDuration(duration / 2);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator2).after(animator1).before(animator3);
        animatorSet.start();

        // Confused? ... So we are... http://img.youtube.com/vi/OSWOhGi_G90/mqdefault.jpg

        // This is a much simpler way:
//        AnimatorSet set = new AnimatorSet();
//        set.playSequentially(animator1, animator2, animator3);
//        set.start();
    }
}