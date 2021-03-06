package com.perrchick.someapplication.uiexercises.list;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.perrchick.someapplication.R;
import com.perrchick.someapplication.data.SomePojo;
import com.perrchick.someapplication.uiexercises.ImageDownloadActivity;
import com.perrchick.someapplication.utilities.AppLogger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by perrchick on 14/06/2017.
 */

class ListViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = ListViewHolder.class.getSimpleName();

    private final ImageView imageView;
    private TextView textView;
    @Nullable
    private SomePojo data;

    /**
     * Called only when the adapter creates this view holder.
     */
    ListViewHolder(View view) {
        super(view);

        textView = view.findViewById(R.id.text_view);
        imageView = view.findViewById(R.id.image_view);
    }

    /**
     * Used whenever the adapter is going to use the view of this holder.
     */
    void configure(SomePojo somePojo) {
        if (somePojo == null) return;

        textView.setText(somePojo.getName());

        this.data = somePojo;
        final String imageUrl = somePojo.getImageUrl();
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_language_swift).into(imageView);
        }
    }

    /**
     * Used for cleanup, called whenever the adapter gets an event that the view of this holder is no longer necessary.
     */
    void prepareForReuse() {
        textView.setText("");
        imageView.setImageBitmap(null);
        Picasso.get().cancelRequest(imageView);
        data = null;
    }
}
