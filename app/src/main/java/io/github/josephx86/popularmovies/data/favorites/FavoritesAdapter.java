package io.github.josephx86.popularmovies.data.favorites;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.BaseMovieAdapter;
import io.github.josephx86.popularmovies.data.FavoritesEntry;
import io.github.josephx86.popularmovies.data.FavoritesProvider;
import io.github.josephx86.popularmovies.data.IWaitForMovies;
import io.github.josephx86.popularmovies.data.Utils;
import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.movies.MovieViewHolder;
import io.github.josephx86.popularmovies.data.movies.MoviesAdapter;

public class FavoritesAdapter extends BaseMovieAdapter {
    private List<Movie> favorites = new ArrayList<>();

    private IWaitForMovies waiter;

    public FavoritesAdapter(Context context, IWaitForMovies waiter) {
        this.waiter = waiter;

        // Fetch favorites from database.
        Uri favoritesUri = FavoritesProvider.FAVORITES_URI;

        String sortyBy = (Utils.getSortOrderPreference(context) == MoviesAdapter.SortType.Rating)
                ? FavoritesEntry.COL_VOTE_AVERAGE
                : FavoritesEntry.COL_POPULARITY;

        Cursor cursor = context.getContentResolver().query(favoritesUri, null, null, null, sortyBy);
        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                Movie movie = new Movie();

                movie.setFavorite(true);

                int id = cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COL_MOVIE_ID));
                movie.setId(id);

                int votes = cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COL_VOTE_COUNT));
                movie.setVoteCount(votes);

                boolean video = cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COL_VIDEO)) == 1;
                movie.setVideo(video);

                double voteAverage = cursor.getDouble(cursor.getColumnIndex(FavoritesEntry.COL_VOTE_AVERAGE));
                movie.setVoteAverage(voteAverage);

                String title = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COL_TITLE));
                movie.setTitle(title);

                double popularity = cursor.getDouble(cursor.getColumnIndex(FavoritesEntry.COL_POPULARITY));
                movie.setPopularity(popularity);

                String posterPath = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COL_POSTER_PATH));
                movie.setPosterPath(posterPath);

                String originalTitle = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COL_ORIGINAL_TITLE));
                movie.setOriginalTitle(originalTitle);

                String originalLanguage = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COL_ORIGINAL_LANGUAGE));
                movie.setOriginalLanguage(originalLanguage);

                String backdropPath = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COL_BACKDROP_PATH));
                movie.setBackdropPath(backdropPath);

                boolean adult = cursor.getInt(cursor.getColumnIndex(FavoritesEntry.COL_ADULT)) == 1;
                movie.setAdult(adult);

                String overview = cursor.getString(cursor.getColumnIndex(FavoritesEntry.COL_OVERVIEW));
                movie.setOverview(overview);

                long releaseDate = cursor.getLong(cursor.getColumnIndex(FavoritesEntry.COL_RELEASE_DATE));
                movie.setReleaseDate(new Date(releaseDate));

                favorites.add(movie);
            } while (cursor.moveToNext());
            cursor.close();
        }

        sort(Utils.getSortOrderPreference(context));
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_movie_item, parent, false);
        if (waiter != null) {
            waiter.setMovieItemSelectedListener(view);
        }

        return new MovieViewHolder(view);
    }

    public void sort(final MoviesAdapter.SortType sortBy) {
        if (favorites != null) {
            Movie[] movies = favorites.toArray(new Movie[favorites.size()]);
            Arrays.sort(movies, new Comparator<Movie>() {
                @Override
                public int compare(Movie first, Movie second) {
                    if (sortBy == MoviesAdapter.SortType.Rating) {
                        return Double.compare(second.getVoteAverage(), first.getVoteAverage());
                    } else {
                        return Double.compare(second.getPopularity(), first.getPopularity());
                    }
                }
            });
            favorites.clear();
            favorites.addAll(Arrays.asList(movies));
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        int lastItemPosition = getItemCount() - 1;
        if (position <= lastItemPosition) {
            Movie m = favorites.get(position);
            holder.setMovieData(m);
        }
    }

    @Override
    public int getItemCount() {
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        return favorites.size();
    }

    @Override
    public Movie findMovieById(int id) {
        Movie movie = null;

        if (favorites != null) {
            for (Movie m : favorites) {
                if (m.getId() == id) {
                    movie = m;
                    break;
                }
            }
        }
        return movie;
    }
}
