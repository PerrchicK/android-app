package com.perrchick.someapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.perrchick.someapplication.utilities.NotificationPublisher;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.squareup.okhttp.Response;

import java.util.HashMap;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = NotificationsActivity.class.getSimpleName();

    public static final String EXTRA_NOTIFICATION_TITLE_KEY = "notification_title";
    public static final String EXTRA_NOTIFICATION_DELAY_KEY = "notification_delay";
    private static final int MY_NOTIFICATION_ID = 100;
    public static final String EXTRA_NOTIFICATION_DATA_KEY = "data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        PerrFuncs.hideActionBarOfActivity(this);

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence notificationTitle = ((EditText) findViewById(R.id.txtNotificationTitle)).getText();
                CharSequence notificationText = ((EditText) findViewById(R.id.txtNotificationText)).getText();
                CharSequence notificationData = ((EditText) findViewById(R.id.txtNotificationData)).getText();

                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                mainActivityIntent.putExtra(EXTRA_NOTIFICATION_DATA_KEY, notificationData.toString()); // Convert CharSequence  to String so using later with 'getString(...)'
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);// What will happen if I'll comment this out?

                int notificationId = /* MY_NOTIFICATION_ID */ (int) (System.currentTimeMillis() & 0xfffffff); // Convert long to int
                PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Action notificationAction = new NotificationCompat.Action(R.drawable.ic_notification_icon,
                        "Open this app's landing screen", mainActivityPendingIntent);

                TimePicker timePicker = (TimePicker) findViewById(R.id.dateDispatchTime);
                long scheduledTime = PerrFuncs.getMillisFrom1970(timePicker);

                // Use 'NotificationManagerCompat' for maintaining compatibility on versions of
                // Android prior to 3.0 (API 11 / HONEYCOMB) that doesn't support newer features

                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#setChannelId(java.lang.String)
                String channelId = SomeApplication.getContext().getString(R.string.app_name);
                Notification notification = new NotificationCompat.Builder(SomeApplication.getContext(), channelId)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setPriority(Notification.PRIORITY_MAX) // Determines how "naggy" will the notification be
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        //.setWhen(scheduledTime) // doesn't really do the job
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .addAction(notificationAction).build(); // Builder Pattern

                notification.defaults |= Notification.DEFAULT_LIGHTS;

                notification.ledARGB = 0xff00ff00;
                notification.ledOnMS = 100;
                notification.ledOffMS = 50;
                notification.flags |= Notification.FLAG_SHOW_LIGHTS;

                //notification.defaults |= Notification.DEFAULT_VIBRATE;

                // Keep playing till treated
                //notification.flags |= Notification.FLAG_INSISTENT;

                // Dispatch now by calling: NotificationManagerCompat.from(getApplicationContext()).notify(notificationId, notification);
                int timeFromNow = (int) (scheduledTime - System.currentTimeMillis());
                scheduleNotification(notification, notificationId, timeFromNow);
                //showNowNotification(notification, notificationId);

                // Respond to the "starting activity" with result
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_NOTIFICATION_TITLE_KEY, notificationTitle);
                resultIntent.putExtra(EXTRA_NOTIFICATION_DELAY_KEY, timeFromNow);
                setResult(Activity.RESULT_OK, resultIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        String fcmToken = "fJs_6MQ04-I:APA91bEzO5TPZsw56qIq2bSInMJbuKqD_4GaSN8wNDZb1qO9GtogJYO257KYC2I9GIvtNGqbzOq9g65UjIHGqwlo5C68u4FRyQWo6vRsWaoH03kOtF7mTLK3aJwHdkUUZ_MD3-iMwSgV";
        HashMap<String, String> data = new HashMap<>();
        data.put("more data", "some ID");
        sendFcmNotificationUsingUrlRequest(generateNotificationPayload("Test", "From Android"), data, new String[]{fcmToken}, new PerrFuncs.CallbacksHandler<Response>() {
            @Override
            public void onCallback(Response response) {
                Log.d(TAG, "sendFcmNotificationUsingUrlRequest: " + response);
            }
        });
    }

    private HashMap<String, String> generateNotificationPayload(String title, String body) {
        HashMap<String, String> notificationDictionary = new HashMap<>();
        notificationDictionary.put("alert", title);
        notificationDictionary.put("title", title);
        notificationDictionary.put("body", body);
        notificationDictionary.put("icon", "app-icon");
        notificationDictionary.put("sound", "default.aiff");

        return notificationDictionary;
    }

    private void showNowNotification(Notification notification, int notificationId) {
        NotificationManagerCompat.from(getApplicationContext()).notify(notificationId, notification);
    }

    private void scheduleNotification(Notification notification, int notificationId, int delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, broadcastPendingIntent);
    }

    /**
     * This method demonstrates how to send a notification using a simple URL request.
     * @param notificationDictionary A dictionary (HashMap) of the visual notification. It should include: "title", "body", "icon" (optional), "sound" (optional). If not used: the notification will be "silent" - it won't be presented automatically by FCM but the data's payload will arrive to the firebase service.
     * @param dataDictionary         A dictionary (HashMap) of the payload data. It should include any custom data you want (key-value pairs), if you want.
     * @param registrationIds        An array of recipients.
     * @param callbacksHandler       The callback to invoke after the process is finished.
     */
    private void sendFcmNotificationUsingUrlRequest(HashMap<String, String> notificationDictionary, HashMap<String, String> dataDictionary, String[] registrationIds, PerrFuncs.CallbacksHandler<Response> callbacksHandler) {
        HashMap<String, Object> jsonDictionary = new HashMap<>();
        jsonDictionary.put("registration_ids", registrationIds); // or use 'to' for ony one registration ID (without using an array)
        jsonDictionary.put("notification", notificationDictionary);
        jsonDictionary.put("data", dataDictionary);

        HashMap<String, String> httpHeaders = new HashMap<>();
        String secretKey = "your secret key from FCM"; // I extremely recommend NOT TO USE your secret key in the client side. Execute this request on your server side instead.
        httpHeaders.put("Authorization", "key= " + secretKey);

        PerrFuncs.makePostRequest(new Gson().toJson(jsonDictionary), "https://fcm.googleapis.com/fcm/send", httpHeaders, callbacksHandler);
    }
}
