package com.perrchick.someapplication.utilities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * Created by perrchick on 10/23/15.
 */
public class PerrFuncs {
    private static PerrFuncs _perrFuncsInstance;
    private Activity _topActivity;
    private DisplayMetrics _metrics;
    private Context _applicationContext;

    /**
     * Returns the number of milliseconds since the Unix epoch (1.1.1970)
     * @return The number of milliseconds since the Unix epoch (1.1.1970)
     */
    public static long getMillisFrom1970() {
        return System.currentTimeMillis();
    }

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

    public interface CallbacksHandler {
        void callbackWithObject(Object callbackObject);
    }

    private static PerrFuncs getInstance() {
        if (_perrFuncsInstance == null) {
            _perrFuncsInstance = new PerrFuncs();
        }

        return _perrFuncsInstance;
    }

    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US);
        return (dateFormat.format(System.currentTimeMillis()));
    }

    public static void toast(String toastMessage) {
        PerrFuncs.toast(toastMessage, true);
    }

    public static void toast(final String toastMessage, final boolean shortDelay) {
        PerrFuncs.getInstance().getTopActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMessage, shortDelay ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
            }
        });
    }

    private static Context getApplicationContext() {
        return getInstance()._applicationContext;
    }

    public static void setApplicationContext(Context applicationContext) {
        getInstance()._applicationContext = applicationContext;
    }

    public static void callNumber(String phoneNumber) {
        callNumber(phoneNumber, getInstance().getTopActivity());
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
        activity.startActivity(phoneCallIntent);
    }

    public static void showDialog(final String dialogTitle, final String dialogMessage) {
        showDialog(dialogTitle, dialogMessage, PerrFuncs.getInstance().getTopActivity());
    }

    public static void showDialog(final String dialogTitle, final String dialogMessage, Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(PerrFuncs.getInstance().getTopActivity())
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

    public static void setTopActivity(Activity topActivity) {
        PerrFuncs.getInstance()._topActivity = topActivity;
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

    public Activity getTopActivity() {
        return PerrFuncs.getInstance()._topActivity;
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

    public static void sayNo(final View view) {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "translationX", 20f);
        animator1.setRepeatCount(0);
        animator1.setDuration(50);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "translationX", -20f);
        animator2.setRepeatCount(0);
        animator2.setDuration(50);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "translationX", 5f);
        animator3.setRepeatCount(0);
        animator3.setDuration(50);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animator1, animator2, animator3);
        set.start();
    }
}