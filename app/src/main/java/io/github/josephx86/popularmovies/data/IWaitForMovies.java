package io.github.josephx86.popularmovies.data;

import android.view.View;

import io.github.josephx86.popularmovies.data.movies.Movie;

import java.util.List;

public interface IWaitForMovies {

    void getMoviesAsync();

    void processReceivedMovies(List<Movie> nowPlayingResults);

    void setMovieItemSelectedListener(View view);
}
