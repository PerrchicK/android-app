package com.perrchick.someapplication.uiexercises;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.utilities.PerrFuncs;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class ImageDownloadActivity extends AppCompatActivity {

    private String imageUrl = "http://static.srcdn.com/wp-content/uploads/the-simpsons-renewed-season-24-25.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        // Get the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            String dataFromPrevieousActivity = intent.getStringExtra("data");
            if (dataFromPrevieousActivity != null && dataFromPrevieousActivity.length() > 0) {
                imageUrl = dataFromPrevieousActivity;
            }

            TextView dataLabel = (TextView) findViewById(R.id.dataText);
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

        // Initialize clickable ImageView
        final ImageView imageView = (ImageView) findViewById(R.id.image_from_web);
        final Handler handler = new Handler();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We're going to make a web request (network operation), we don't know how long is it going to take
                new Thread(new Runnable() {
                    public void run() {
                        // The "long operation"
                        PerrFuncs.performGetRequest(imageUrl, new PerrFuncs.CallbacksHandler() {
                            @Override
                            public void callbackWithObject(Object callbackObject) {
                                if (callbackObject instanceof Response) {
                                    Response response = (Response) callbackObject;
                                    try {
                                        final Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());

                                        // We shouldn't update UI in the worker thread, only on the UI Thread (main thread).
                                        // Let's do it with "Handler -> post(runnable)":
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                imageView.setImageBitmap(bmp);
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        PerrFuncs.toast("Error downloading the image");
                                    }
                                }
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
