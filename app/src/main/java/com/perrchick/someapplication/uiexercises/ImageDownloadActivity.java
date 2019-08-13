package com.perrchick.someapplication.uiexercises;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.utilities.AppLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ImageDownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ImageDownloadActivity.class.getSimpleName();

    private String imageUrl = "http://static.srcdn.com/wp-content/uploads/the-simpsons-renewed-season-24-25.jpg";
    private final static boolean shouldAllowMemoryLeakExample = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);
        final ImageView imageView = (ImageView) findViewById(R.id.image_from_web);
        TextView dataLabel = (TextView) findViewById(R.id.dataText);

        imageView.setOnClickListener(this);
        dataLabel.setOnClickListener(this);

        // Get the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            String dataFromPrevieousActivity = intent.getStringExtra("data");
            if (dataFromPrevieousActivity != null && dataFromPrevieousActivity.length() > 0) {
                imageUrl = dataFromPrevieousActivity;
            }

            dataLabel.setText(imageUrl);

            //Uri data = intent.getData();
            if (intent.getType() != null) {
                // Figure out what to do based on the intent type
                if (intent.getType().contains("image/")) {
                    // Handle intents with image data ...
                } else if (intent.getType().equals("text/plain")) {
                    // Handle intents with text ...
                }
            }
        }

        /** CLASS EXERCISE 05 - UPDATE UI AFTER NETWORK REQUEST **/

        // Option 1
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                v.setRotation(((float) (v.getRotation() + Math.PI)) % 360);
//            }
//        });

        // Option 2
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_from_web: {
                v.setRotation(((float) (v.getRotation() + Math.PI)) % 360);

                final ImageView imageView = (ImageView) v;

                if (shouldAllowMemoryLeakExample) {
                    final Handler handler = new Handler();
                    // Perry: That is the least recommended way (by me) to make async operation, look for HandlerThread in this project for more details.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Bitmap bmpFromWeb = downloadBitmapFromUrl(imageUrl);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bmpFromWeb);
                                        AppLogger.log(TAG, "Do some UI work with " + bmpFromWeb);
                                    }
                                });
                            } catch (IOException e) {
                                AppLogger.error(TAG, e);
                            }
                        }
                    }).start();
                }
            }

            break;
            case R.id.dataText:
                break;
        }
    }

    /**
     * This method SYNCHRONOUSLY downloads an image from the provided URL.
     * @param imageUrl The image online address
     * @return A Bitmap object, in case the download was successful.
     * @throws IOException In case of an input / output (I/O) error.
     */
    public static Bitmap downloadBitmapFromUrl(String imageUrl) throws IOException {
        java.net.URL url = new java.net.URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        // Perry: Making a network request here, note that this activity is kept being held in memory until this call is done (GC won't let it go) therefore we are allowing leak here! (try to avoid it and use other ways)
        InputStream input = connection.getInputStream();
        final Bitmap bmpFromWeb = BitmapFactory.decodeStream(input);
        return bmpFromWeb;
    }
}