package io.github.josephx86.popularmovies.data.movies;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.FavoritesEntry;
import io.github.josephx86.popularmovies.data.FavoritesProvider;
import io.github.josephx86.popularmovies.data.Utils;

public class MovieThumbnailLoader {
    private MovieThumbnailLoader() {

    }

    public static void Load(final Movie movie, final ImageView posterImageView) {
        String imageUrl = Utils.getPosterUrl(movie.getPosterPath());
        Picasso
                .get()
                .load(imageUrl)
                .placeholder(R.drawable.movie_poster_placeholder)
                .into(posterImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) posterImageView.getDrawable();
                        if (bitmapDrawable != null) {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            bitmapDrawable.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            movie.setThumbnailBuffer(outputStream.toByteArray());
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // On error, try getting thumbnail from database.
                        // This condition will happen when device is offline.
                        // If movie is favorite, thumbnail will be in database.
                        ContentResolver resolver = posterImageView.getContext().getContentResolver();
                        Uri movieUri = ContentUris.withAppendedId(FavoritesProvider.FAVORITES_URI, movie.getId());
                        Cursor results = resolver.query(movieUri, null, null, null, null);
                        if ((results != null) && (results.getCount() > 0)) {
                            if (results.moveToFirst()) {
                                byte[] thumbnailBytes = results.getBlob(results.getColumnIndex(FavoritesEntry.COL_THUMBNAIL));
                                if (thumbnailBytes != null) {
                                    ByteArrayInputStream inputStream = new ByteArrayInputStream(thumbnailBytes);
                                    Drawable thumbnail = BitmapDrawable.createFromStream(inputStream, "");
                                    if (thumbnail != null) {
                                        posterImageView.setImageDrawable(thumbnail);
                                    }
                                }

                            }
                        }
                    }
                });
    }
}
