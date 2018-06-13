package io.github.josephx86.popularmovies.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.movies.Movie;
import io.github.josephx86.popularmovies.data.videos.Video;
import io.github.josephx86.popularmovies.data.reviews.Review;
import io.github.josephx86.popularmovies.fragments.ReviewsFragment;
import io.github.josephx86.popularmovies.fragments.OverviewFragment;
import io.github.josephx86.popularmovies.fragments.VideosFragment;

public class DetailsPagerAdapter extends FragmentPagerAdapter {
    private DetailsPagerFragment[] fragments = new DetailsPagerFragment[]{
            new OverviewFragment(),
            new VideosFragment(),
            new ReviewsFragment()
    };

    private Context context;

    public DetailsPagerAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        context = activity;
    }

    public void setMovie(Movie movie) {
        OverviewFragment overviewFragment = (OverviewFragment) fragments[0];
        if (overviewFragment != null) {
            overviewFragment.setMovie(movie);
        }
    }

    public void setReviewsCaller(IWaitForReviews caller) {
        ReviewsFragment reviewsFragment = (ReviewsFragment) fragments[2];
        if (reviewsFragment != null) {
            reviewsFragment.setReviewsCaller(caller);
        }
    }

    public void addReviews(List<Review> reviews) {
        ReviewsFragment reviewsFragment = (ReviewsFragment) fragments[2];
        if (reviewsFragment != null) {
            reviewsFragment.addReviews(reviews);
        }
    }

    public Video getFirstVideo() {
        Video video = null;
        VideosFragment videosFragment = (VideosFragment) fragments[1];
        if (videosFragment != null) {
            video = videosFragment.getFirstVideo();
        }
        return video;
    }


    public void addVideos(List<Video> videos) {
        VideosFragment videosFragment = (VideosFragment) fragments[1];
        if (videosFragment != null) {
            videosFragment.addVideos(videos);
        }
    }

    public ArrayList<Review> getReviews() {
        ReviewsFragment reviewsFragment = (ReviewsFragment) fragments[2];
        if (reviewsFragment != null) {
            return reviewsFragment.getReviews();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Fragment getItem(int position) {
        // Keep position in range of array.
        position = position % fragments.length;
        return fragments[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        DetailsPagerFragment pagerFragment = (DetailsPagerFragment) getItem(position);
        String title = "";
        if (pagerFragment != null) {
            title = pagerFragment.getTitle(context);
        }
        return title;
    }

    @Override
    public int getCount() {
        return fragments.length;
    }
}
