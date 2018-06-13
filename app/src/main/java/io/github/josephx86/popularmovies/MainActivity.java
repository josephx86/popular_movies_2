package io.github.josephx86.popularmovies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.josephx86.popularmovies.data.BaseMovieAdapter;
import io.github.josephx86.popularmovies.data.IWaitForMovies;
import io.github.josephx86.popularmovies.data.TMDBHelper;
import io.github.josephx86.popularmovies.data.Utils;
import io.github.josephx86.popularmovies.data.favorites.FavoritesAdapter;
import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.movies.MoviesAdapter;
import io.github.josephx86.popularmovies.data.videos.Video;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements IWaitForMovies {

    @BindView(R.id.movies_rv)
    RecyclerView moviesRecyclerView;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    @BindView(R.id.no_movies_tv)
    TextView noMoviesTextView;

    @BindView(R.id.root_layout)
    CoordinatorLayout rootLayout;

    @BindView(R.id.btn_retry)
    Button retryButton;

    Menu appMenu = null;

    private int scrollPosition = 0;

    private boolean showFavoritesOnly = false;
    private List<Movie> movieBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Setup recyclerview
        int columnCount = Utils.calculatePosterGridLayoutColumns(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        moviesRecyclerView.setLayoutManager(layoutManager);
        MoviesAdapter moviesAdapter = new MoviesAdapter(this);
        moviesRecyclerView.setAdapter(moviesAdapter);
        moviesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                boolean isAtBottom = !recyclerView.canScrollVertically(1);
                boolean isIdle = (newState == RecyclerView.SCROLL_STATE_IDLE);
                boolean isOffline = (!Utils.isDeviceOnline(MainActivity.this));
                if (!showFavoritesOnly) {
                    if (isAtBottom && isIdle && isOffline) {
                        showOfflineSnackbar();
                    } else if (isAtBottom && isIdle) {
                        getMoviesAsync();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showFavoritesOnly) {
            // Use FavoritesAdapter
            getFavoritesAsync(); // Okay to call this in onResume; getFavoritesAsync() works async.
        } else {
            // Use MoviesAdapter
            MoviesAdapter moviesAdapter;
            if (moviesRecyclerView.getAdapter() == null) {
                moviesAdapter = new MoviesAdapter(this);
                moviesRecyclerView.setAdapter(moviesAdapter);
            } else {
                moviesAdapter = (MoviesAdapter) moviesRecyclerView.getAdapter();
                if (moviesAdapter == null) {
                    moviesAdapter = new MoviesAdapter(this);
                    moviesRecyclerView.setAdapter(moviesAdapter);
                }
            }

            int movieCount = 0;
            if (movieBuffer != null) {
                movieCount = movieBuffer.size();
            }

            if (movieCount == 0) {
                getMoviesAsync(); // Okay to call this in onResume; getMovies() works async.
            } else if (moviesRecyclerView != null) {
                if (movieBuffer != null) {
                    moviesAdapter.addMovies(movieBuffer);
                    movieBuffer.clear();
                }
                if (scrollPosition > 0) {
                    moviesRecyclerView.smoothScrollToPosition(scrollPosition);
                }
            }
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void getMoviesAsync() {
        TMDBHelper.discoverMoviesAsync(this, this);
    }

    private void showOfflineSnackbar() {
        Snackbar.make(rootLayout, MainActivity.this.getString(R.string.device_offline), Snackbar.LENGTH_LONG).show();
    }

    private void getFavoritesAsync() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, FavoritesAdapter> favoritesTask = new AsyncTask<Void, Void, FavoritesAdapter>() {
            @Override
            protected FavoritesAdapter doInBackground(Void... voids) {
                // Constructor of FavoritesAdapter queries database.
                return new FavoritesAdapter(MainActivity.this, MainActivity.this);
            }

            @Override
            protected void onPostExecute(FavoritesAdapter adapter) {
                super.onPostExecute(adapter);
                moviesRecyclerView.setAdapter(adapter);
                moviesRecyclerView.smoothScrollToPosition(scrollPosition);
                updateUI();
            }
        };
        favoritesTask.execute();
    }

    private void updateUI() {
        if ((moviesRecyclerView != null) && (progressBar != null)) {
            if (moviesRecyclerView.getAdapter().getItemCount() > 0) {
                moviesRecyclerView.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                noMoviesTextView.setVisibility(View.GONE);
            } else {
                moviesRecyclerView.setVisibility(View.GONE);
                if (showFavoritesOnly) {
                    setMessage(getString(R.string.no_favorites_found));
                    retryButton.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    noMoviesTextView.setVisibility(View.VISIBLE);
                } else {
                    if (Utils.isDeviceOnline(this)) {
                        retryButton.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        noMoviesTextView.setVisibility(View.GONE);
                    } else {
                        setMessage(getString(R.string.device_offline));
                        retryButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        noMoviesTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @OnClick(R.id.btn_retry)
    public void btnRetry_OnClick(View view) {
        getMoviesAsync();
    }

    @Override
    public void processReceivedMovies(List<Movie> movies) {
        MoviesAdapter moviesAdapter = null;
        RecyclerView.Adapter adapter = moviesRecyclerView.getAdapter();
        if ((adapter != null) && (adapter instanceof MoviesAdapter)) {
            moviesAdapter = (MoviesAdapter) moviesRecyclerView.getAdapter();
        }
        if (moviesAdapter == null) {
            moviesAdapter = new MoviesAdapter(this);
            moviesRecyclerView.setAdapter(moviesAdapter);
        }
        moviesAdapter.addMovies(movies);
        updateUI();
    }

    private void setMessage(String message) {
        if ((message != null) && (!message.isEmpty()) && (noMoviesTextView != null)) {
            noMoviesTextView.setText(message);
        }
    }

    @Override
    public void setMovieItemSelectedListener(final View view) {
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BaseMovieAdapter adapter = (BaseMovieAdapter) moviesRecyclerView.getAdapter();
                    if (adapter != null) {
                        Object idObject = v.getTag();
                        if (idObject instanceof Integer) {
                            int movieId = (int) idObject;
                            Movie m = adapter.findMovieById(movieId);
                            if (m != null) {
                                // Show movie details
                                Context context = view.getContext();
                                String key = context.getString(R.string.movie_object_key);
                                Intent intent = new Intent(context, DetailsActivity.class);
                                intent.putExtra(key, m);
                                context.startActivity(intent);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (moviesRecyclerView != null) {
            // Save the movies if not showing favorites only.
            // Favorites are stored in database and can be easily reloaded in onRestoreInstanceState()
            String key = getString(R.string.showing_favorites_only_key);
            outState.putBoolean(key, showFavoritesOnly);
            if (!showFavoritesOnly) {
                MoviesAdapter moviesAdapter = (MoviesAdapter) moviesRecyclerView.getAdapter();
                if (moviesAdapter != null) {
                    key = getString(R.string.parcelable_movie_array_key);
                    ArrayList<Movie> movies = moviesAdapter.getMovies();
                    outState.putParcelableArrayList(key, movies);
                }
            }

            // Save the position of the recycler view.
            GridLayoutManager manager = (GridLayoutManager) moviesRecyclerView.getLayoutManager();
            if (manager != null) {
                int position = manager.findFirstVisibleItemPosition();
                outState.putInt(getString(R.string.movies_recyclerview_scroll_position), position);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore view mode
            String key = getString(R.string.showing_favorites_only_key);
            if (savedInstanceState.containsKey(key)) {
                showFavoritesOnly = savedInstanceState.getBoolean(key);
            }

            // Restore movies if not favorites.
            // Favorites can be easily loaded from database later; they won't be fetched via HTTP
            if (!showFavoritesOnly) {
                key = getString(R.string.parcelable_movie_array_key);
                if (savedInstanceState.containsKey(key)) {
                    // Movies will be added to adapter in onResume()
                    movieBuffer = savedInstanceState.getParcelableArrayList(key);
                }
            }

            // Restore the scroll position
            key = getString(R.string.movies_recyclerview_scroll_position);
            if (savedInstanceState.containsKey(key)) {
                // Will scroll to position in onResume()
                scrollPosition = savedInstanceState.getInt(key);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        appMenu = menu;
        setSortOrderMenuItem();

        // Set correct label for switching view mode
        MenuItem favoritesMenuItem = menu.findItem(R.id.action_favorites);
        if (favoritesMenuItem != null) {
            String showFavoritesOnlyTitle = getString(R.string.show_favorites);
            String showAllMoviesTitle = getString(R.string.show_all);
            favoritesMenuItem.setTitle(showFavoritesOnly ? showAllMoviesTitle : showFavoritesOnlyTitle);
        }
        return true;
    }

    private void setSortOrderMenuItem() {
        // Set the saved sort order.
        if (appMenu != null) {
            MoviesAdapter.SortType sortType = Utils.getSortOrderPreference(this);
            MenuItem popularSortMenuItem = appMenu.findItem(R.id.action_sort_popularity);
            MenuItem ratingSortMenuItem = appMenu.findItem(R.id.action_sort_rating);
            if ((popularSortMenuItem != null) && (ratingSortMenuItem != null)) {
                if (sortType == MoviesAdapter.SortType.Popularity) {
                    popularSortMenuItem.setCheckable(true);
                    popularSortMenuItem.setChecked(true);
                    ratingSortMenuItem.setCheckable(false);
                } else if (sortType == MoviesAdapter.SortType.Rating) {
                    ratingSortMenuItem.setCheckable(true);
                    ratingSortMenuItem.setChecked(true);
                    popularSortMenuItem.setCheckable(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // When the different sort order is selected, there is a possibility that a movie with a
        // higher sort order index (using new order) might not be in the list.
        // To fix that, clear movies and fetch with new order.
        switch (item.getItemId()) {
            case R.id.action_sort_popularity:
                Utils.saveSortOrderPreference(this, MoviesAdapter.SortType.Popularity);
                setSortOrderMenuItem();
                if (showFavoritesOnly) {
                    // Sort favorites by popularity
                    FavoritesAdapter favoritesAdapter = (FavoritesAdapter) moviesRecyclerView.getAdapter();
                    if (favoritesAdapter != null) {
                        favoritesAdapter.sort(MoviesAdapter.SortType.Popularity);
                    }
                } else {
                    // Fetch movies sorted by popularity
                    scrollPosition = 0;
                    MoviesAdapter popularityAdapter = (MoviesAdapter) moviesRecyclerView.getAdapter();
                    if (popularityAdapter != null) {
                        popularityAdapter.clearMovies();
                    }
                    getMoviesAsync();
                }
                return true;
            case R.id.action_sort_rating:
                Utils.saveSortOrderPreference(this, MoviesAdapter.SortType.Rating);
                setSortOrderMenuItem();
                if (showFavoritesOnly) {
                    // Sort favorites by rating
                    FavoritesAdapter favoritesAdapter = (FavoritesAdapter) moviesRecyclerView.getAdapter();
                    if (favoritesAdapter != null) {
                        favoritesAdapter.sort(MoviesAdapter.SortType.Rating);
                    }
                } else {
                    // Fetch movies sorted by rating
                    scrollPosition = 0;
                    MoviesAdapter ratingAdapter = (MoviesAdapter) moviesRecyclerView.getAdapter();
                    if (ratingAdapter != null) {
                        ratingAdapter.clearMovies();
                    }
                    getMoviesAsync();
                }
                return true;
            case R.id.action_favorites:
                scrollPosition = 0;
                String itemText = item.getTitle().toString();
                String showFavoritesOnlyTitle = getString(R.string.show_favorites);
                String showAllMoviesTitle = getString(R.string.show_all);
                showFavoritesOnly = itemText.equals(showFavoritesOnlyTitle);
                if (showFavoritesOnly) {
                    item.setTitle(showAllMoviesTitle);
                    getFavoritesAsync();
                } else {
                    item.setTitle(showFavoritesOnlyTitle);

                    // Reset page counter in helper
                    TMDBHelper.resetCurrentMovieListPage();

                    // ... then fetch data.
                    getMoviesAsync();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


