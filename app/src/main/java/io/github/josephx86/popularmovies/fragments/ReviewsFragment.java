package io.github.josephx86.popularmovies.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.DetailsPagerFragment;
import io.github.josephx86.popularmovies.data.IWaitForReviews;
import io.github.josephx86.popularmovies.data.reviews.Review;
import io.github.josephx86.popularmovies.data.ReviewsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewsFragment extends DetailsPagerFragment {

    @BindView(R.id.reviews_rv)
    RecyclerView reviewsRecyclerView;

    @BindView(R.id.progressbar)
    ProgressBar progressBar;

    @BindView(R.id.no_reviews_tv)
    TextView noReviewsTextView;

    private ReviewsAdapter adapter = new ReviewsAdapter();

    private int scrollPosition = 0;

    public ReviewsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update the UI after DetailsActivity.onRestoreInstanceState(), which restores data for this fragment.
        updateUI();

        // Also set scroll position if not zero
        if ((reviewsRecyclerView != null) && (adapter != null) && (adapter.getItemCount() > 0) && (scrollPosition > 0)) {
            reviewsRecyclerView.smoothScrollToPosition(scrollPosition);
        }
    }

    private void updateUI() {
        // Update the UI accordingly
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if ((reviewsRecyclerView != null) && (noReviewsTextView != null)) {
            if ((adapter != null) && (adapter.getItemCount() == 0)) {
                reviewsRecyclerView.setVisibility(View.GONE);
                noReviewsTextView.setVisibility(View.VISIBLE);
            } else {
                reviewsRecyclerView.setVisibility(View.VISIBLE);
                noReviewsTextView.setVisibility(View.GONE);
            }

            RecyclerView.LayoutManager manager = reviewsRecyclerView.getLayoutManager();
            RecyclerView.Adapter adapter = reviewsRecyclerView.getAdapter();
            if ((adapter == null) || (manager == null)) {
                setupRecyclerView();
            }

            // Also set scroll position
            if ((adapter != null) && (adapter.getItemCount() > 0)) {
                reviewsRecyclerView.smoothScrollToPosition(scrollPosition);
            }
        }
    }

    private void setupRecyclerView() {
        if (reviewsRecyclerView != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(reviewsRecyclerView.getContext(), LinearLayoutManager.VERTICAL, false);
            reviewsRecyclerView.setLayoutManager(layoutManager);
            reviewsRecyclerView.setAdapter(adapter);
        }
    }

    public void setReviewsCaller(IWaitForReviews caller) {
        if (adapter != null) {
            adapter.setCaller(caller);
        }
    }

    public void addReviews(List<Review> reviews) {
        // NOTE: This method may be called before onCreateView()
        if (adapter != null) {
            adapter.addReviews(reviews);
            updateUI();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save reviews
        if (adapter != null) {
            String key = getString(R.string.parcelable_review_array_key);
            outState.putParcelableArrayList(key, adapter.getReviews());
        }

        // Save scroll posotion
        LinearLayoutManager manager = (LinearLayoutManager) reviewsRecyclerView.getLayoutManager();
        if (manager != null) {
            int position = manager.findFirstVisibleItemPosition();
            outState.putInt(getString(R.string.reviews_recyclerview_scroll_position), position);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            String key = getString(R.string.parcelable_review_array_key);
            if (savedInstanceState.containsKey(key)) {
                List<Review> reviews = savedInstanceState.getParcelableArrayList(key);
                if (reviews != null) {
                    adapter.addReviews(reviews);
                }
            }

            key = getString(R.string.reviews_recyclerview_scroll_position);
            if (savedInstanceState.containsKey(key)) {
                // Position will be restored in onResume()
                scrollPosition = savedInstanceState.getInt(key);
            }
        }
    }

    public ArrayList<Review> getReviews() {
        if (adapter != null) {
            return adapter.getReviews();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    protected String getTitle(Context context) {
        return context.getString(R.string.reviews);
    }
}
