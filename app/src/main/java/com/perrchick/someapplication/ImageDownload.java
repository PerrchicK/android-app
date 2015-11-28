package com.perrchick.someapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.perrchick.someapplication.utilities.PerrFuncs;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ImageDownload extends AppCompatActivity {

    OkHttpClient client = new OkHttpClient(); // An open source project, downloaded from gradle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);

        // Get the intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra("data") != null) {
                TextView dataLabel = (TextView) findViewById(R.id.dataText);
                dataLabel.setText(intent.getStringExtra("data"));
            }

            Uri data = intent.getData();
            if (intent.getType() != null) {
                // Figure out what to do based on the intent type
                if (intent.getType().indexOf("image/") != -1) {
                    // Handle intents with image data ...
                } else if (intent.getType().equals("text/plain")) {
                    // Handle intents with text ...
                }
            }
        }

        // Initialize OkHttpClient
        client.setReadTimeout(20, TimeUnit.SECONDS);

        /** CLASS EXERCISE 05 - UPDATE UI AFTER NETWORK REQUEST **/

        // Initialize clickable ImageView
        final ImageView imageView = (ImageView) findViewById(R.id.image_from_web);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We're going to make a web request (network operation), we don't know how long is it going to take
                new Thread(new Runnable() {
                    public void run() {
                        // The "long operation" line
                        final Bitmap bmpImage = loadImageFromNetwork("http://www.comicsandmemes.com/wp-content/uploads/not-sure-fry-meme-pink-floyd-song.jpg");

                        if (bmpImage != null) {
                            // We shouldn't update UI in the worker thread, only on the UI Thread (main thread)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(bmpImage);
                                }
                            });
                        } else {
                            PerrFuncs.toast("Error downloading the image");
                        }
                    }
                }).start();
            }
        });

    }

    /**
     * Makes a network request and fetches an image from the specified URL.
     *
     * @param url The URL that represents the image's location on the web.
     * @return A Bitmap object if the request succeeded, otherwise it returns null.
     */
    private Bitmap loadImageFromNetwork(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            final Response response = client.newCall(request).execute();

            // The image's data is here
            return BitmapFactory.decodeStream(response.body().byteStream());
        } catch (IOException e) {
            e.printStackTrace(); // Will print the stack trace in the "locgcat"
        }

        // There was an error
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_another, menu);
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
