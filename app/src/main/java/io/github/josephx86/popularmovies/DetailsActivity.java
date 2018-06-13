package io.github.josephx86.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.josephx86.popularmovies.data.DetailsPagerAdapter;
import io.github.josephx86.popularmovies.data.IWaitForReviews;
import io.github.josephx86.popularmovies.data.IWaitForVideos;
import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.reviews.Review;
import io.github.josephx86.popularmovies.data.TMDBHelper;
import io.github.josephx86.popularmovies.data.Utils;
import io.github.josephx86.popularmovies.data.videos.Video;

public class DetailsActivity extends AppCompatActivity implements IWaitForReviews, IWaitForVideos {


    @BindView(R.id.movie_pager)
    ViewPager pager;

    @BindView(R.id.titles_tl)
    TabLayout titlesTabLayout;

    @BindView(R.id.backdrop_iv)
    ImageView backdropImageView;

    @BindView(R.id.coordinator)
    CoordinatorLayout rootLayout;

    DetailsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        // Set tool bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.details_activity_title));
        }

        pagerAdapter = new DetailsPagerAdapter(this);
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(0, true);
        titlesTabLayout.setupWithViewPager(pager, true);

        // Get the movie data from intent extra.
        Intent intent = getIntent();
        if (intent != null) {
            String key = getString(R.string.movie_object_key);
            Movie movie = intent.getParcelableExtra(key);
            setBackdrop(movie.getBackdropPath());
            if (pagerAdapter != null) {
                pagerAdapter.setMovie(movie);
            }

            // Get reviews
            int movieId = movie.getId();
            Review.setMovieId(movie.getId());
            TMDBHelper.getReviews(this, this, movieId, true);

            // Get videos
            TMDBHelper.getVideos(this, this, movieId);
        }

        registerAsReviewsCaller();
    }

    private void setBackdrop(String backdropPath) {
        // Show the backdrop in the background
        String imageUrl = Utils.getBackdropUrl(backdropPath);
        Picasso
                .get()
                .load(imageUrl)
                .into(backdropImageView);
    }

    @Override
    public void processReceivedReviews(List<Review> reviews) {
        if (pagerAdapter != null) {
            pagerAdapter.addReviews(reviews);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        registerAsReviewsCaller();
    }

    private void registerAsReviewsCaller() {
        // Register to receive reviews when more are fetched from server.
        if (pagerAdapter != null) {
            pagerAdapter.setReviewsCaller(this);
        }
    }

    @Override
    public void processReceivedVideos(List<Video> videos) {
        if (pagerAdapter != null) {
            pagerAdapter.addVideos(videos);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.ic_share) {
            Context context = rootLayout.getContext();
            Video video = pagerAdapter.getFirstVideo();
            if (video == null) {
                Snackbar.make(rootLayout, context.getString(R.string.no_share), Snackbar.LENGTH_SHORT).show();
            } else {
                String url = context.getString(R.string.youtube_url);
                url = String.format(Locale.ENGLISH, url, video.getKey());
                Intent videoIntent = new Intent(Intent.ACTION_SEND);
                videoIntent.setType("text/plain");
                videoIntent.putExtra(Intent.EXTRA_SUBJECT, video.getName());
                videoIntent.putExtra(Intent.EXTRA_TEXT, video.getName() + "\n\n" + url);
                PackageManager pm = context.getPackageManager();
                boolean canPlayYoutubeVideo = pm.queryIntentActivities(videoIntent, 0).size() > 0;
                if (canPlayYoutubeVideo) {
                    startActivity(Intent.createChooser(videoIntent, "Share with"));
                } else {
                    Snackbar.make(rootLayout, context.getString(R.string.no_share_app), Snackbar.LENGTH_SHORT).show();
                }
            }
            result = true;
        }
        return result;
    }
}
