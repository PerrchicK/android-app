package com.perrchick.someapplication;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence notificationTitle = ((EditText) findViewById(R.id.txtNotificationTitle)).getText();
                CharSequence notificationText = ((EditText) findViewById(R.id.txtNotificationText)).getText();

                int notificationId = 0;
                NotificationCompat.Builder notificationsBuilder = new NotificationCompat.Builder(getApplicationContext());
                notificationsBuilder.setContentTitle(notificationTitle);
                notificationsBuilder.setContentText(notificationText);
                notificationsBuilder.setSmallIcon(R.drawable.ic_notification_icon);
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), notificationId, mainActivityIntent, MODE_PRIVATE);
                android.support.v4.app.NotificationCompat.Action notificationAction = new android.support.v4.app.NotificationCompat.Action(R.drawable.ic_notification_icon,
                        "Open this app's landing screen", pendingIntent);
                notificationsBuilder.addAction(notificationAction);
                // Use 'NotificationManagerCompat' for maintaining compatibility on versions of
                // Android prior to 3.0 (API 11?) that doesn't support newer features
                NotificationManagerCompat.from(getApplicationContext())
                        .notify(notificationId, notificationsBuilder.build());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
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
