package io.github.josephx86.popularmovies.fragments;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.DetailsPagerFragment;
import io.github.josephx86.popularmovies.data.FavoritesProvider;
import io.github.josephx86.popularmovies.data.Utils;
import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.movies.MovieThumbnailLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends DetailsPagerFragment {

    @BindView(R.id.poster_iv)
    ImageView posterImageView;

    @BindView(R.id.title_tv)
    TextView titleTextView;

    @BindView(R.id.release_date_tv)
    TextView releaseDateTextView;

    @BindView(R.id.rating_ll)
    LinearLayout ratingLayout;

    @BindView(R.id.overview_tv)
    TextView overviewTextView;

    @BindView(R.id.favorites_btn)
    FloatingActionButton favoritesButton;

    private Movie movie;
    private boolean isFavoriteMovie = false;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.bind(this, view);

        setMovieDetails();
        checkFavoriteAsync();

        return view;
    }

    @OnClick(R.id.favorites_btn)
    public void favoriteButton_OnClick(View view) {
        String message;
        Context context = view.getContext();
        ContentResolver resolver = context.getContentResolver();
        if (isFavoriteMovie) {
            // Remove from favorites
            // Save movie to favorites
            message = context.getString(R.string.failed_to_remove_from_favorites);
            if (movie != null) {
                Uri movieUri = ContentUris.withAppendedId(FavoritesProvider.FAVORITES_URI, movie.getId());
                int deleted = resolver.delete(movieUri, null, null);
                if (deleted > 0) {
                    message = context.getString(R.string.removed_from_favorites);
                    favoritesButton.setImageResource(R.drawable.add_48dp);
                    isFavoriteMovie = false;
                }
            }
        } else {
            // Save movie to favorites
            message = context.getString(R.string.failed_to_save_to_favorites);
            if (movie != null) {
                ContentValues contentValues = movie.getContentValues();
                Uri resultUri = resolver.insert(FavoritesProvider.FAVORITES_URI, contentValues);
                if (resultUri != null) {
                    long id = ContentUris.parseId(resultUri);
                    if (id >= 0) {
                        message = context.getString(R.string.saved_to_favorites);
                        favoritesButton.setImageResource(R.drawable.remove_48dp);
                        isFavoriteMovie = true;
                    }
                }
            }
        }

        Snackbar.make(favoritesButton, message, Snackbar.LENGTH_LONG).show();
    }

    private void checkFavoriteAsync() {
        final ContentResolver resolver = getContext().getContentResolver();
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> checkFavoriteTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                // Check if any movies with the same ID already exist in favorites database.
                Uri movieUri = ContentUris.withAppendedId(FavoritesProvider.FAVORITES_URI, movie.getId());
                Cursor cursor = resolver.query(movieUri, null, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() != 0) {
                        isFavoriteMovie = true;
                    }
                    cursor.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                favoritesButton.setImageResource(isFavoriteMovie ? R.drawable.remove_48dp : R.drawable.add_48dp);
            }
        };
        checkFavoriteTask.execute();
    }

    private void setMovieDetails() {
        if (movie != null) {
           MovieThumbnailLoader.Load(movie, posterImageView);

            String originalTitle = movie.getOriginalTitle();
            String title = movie.getTitle();
            titleTextView.setText(movie.getOriginalTitle());
            if (!originalTitle.equals(title)) {
                titleTextView.append("\n(" + title + ")");
            }
            String releaseDate = DateFormat.format("MMMM dd, yyyy", movie.getReleaseDate()).toString();
            releaseDateTextView.setText(releaseDate);
            Utils.setRating(ratingLayout, movie.getVoteAverage());

            overviewTextView.setText(movie.getOverview());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save movie data
        if (movie != null) {
            String key = getString(R.string.parcelable_movie_key);
            outState.putParcelable(key, movie);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            String key = getString(R.string.parcelable_movie_key);
            if (savedInstanceState.containsKey(key)) {
                Movie savedMovie = (Movie) savedInstanceState.getParcelable(key);
                if (savedMovie != null) {
                    movie = savedMovie;
                    setMovieDetails();
                }
            }
        }
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    @Override
    protected String getTitle(Context context) {
        return context.getString(R.string.overview);
    }
}
