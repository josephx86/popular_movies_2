package io.github.josephx86.popularmovies.data;

import android.support.v7.widget.RecyclerView;

import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.movies.MovieViewHolder;

public abstract class BaseMovieAdapter extends RecyclerView.Adapter<MovieViewHolder> {
    public abstract Movie findMovieById(int movieId);
}
