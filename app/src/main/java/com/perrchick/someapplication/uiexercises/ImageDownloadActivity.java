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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class ImageDownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private String imageUrl = "http://static.srcdn.com/wp-content/uploads/the-simpsons-renewed-season-24-25.jpg";

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

        // Initialize clickable ImageView
        final Handler handler = new Handler();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setRotation(((float) (v.getRotation() + Math.PI)) % 360);
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_from_web:
                break;
            case R.id.dataText:
                break;
        }
    }
}
