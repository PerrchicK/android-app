package com.perrchick.someapplication;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.perrchick.someapplication.utilities.NotificationPublisher;
import com.perrchick.someapplication.utilities.PerrFuncs;

import java.util.Date;

public class NotificationsActivity extends AppCompatActivity {

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
                mainActivityIntent.putExtra("data", notificationData.toString()); // Convert CharSequence  to String so using later with 'getString(...)'
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                int notificationId = (int) (System.currentTimeMillis() & 0xfffffff); // Convert long to int
                PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, mainActivityIntent, MODE_PRIVATE);
                NotificationCompat.Action notificationAction = new NotificationCompat.Action(R.drawable.ic_notification_icon,
                        "Open this app's landing screen", mainActivityPendingIntent);

                TimePicker timePicker = (TimePicker) findViewById(R.id.dateDispatchTime);
                long timeFromNow = PerrFuncs.getMillisFrom1970(timePicker);

                // Use 'NotificationManagerCompat' for maintaining compatibility on versions of
                // Android prior to 3.0 (API 11 / HONEYCOMB) that doesn't support newer features
                Notification notification = new NotificationCompat.Builder(getApplicationContext())
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .addAction(notificationAction).build(); // Builder Pattern

                // Dispatch now by calling: NotificationManagerCompat.from(getApplicationContext()).notify(notificationId, notification);
                int delay = (int) (timeFromNow - System.currentTimeMillis());
                PerrFuncs.toast("Will notify in " + delay / 1000 + " seconds...");
                scheduleNotification(notification, delay);
            }
        });
    }

    private void scheduleNotification(Notification notification, int delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, broadcastPendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_notifications, menu);
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
}
