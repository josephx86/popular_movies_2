package io.github.josephx86.popularmovies.data;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.reviews.Review;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewViewHolder> {
    private List<Review> reviews = new ArrayList<>();
    private IWaitForReviews caller;

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        if (position < reviews.size()) {
            Review review = reviews.get(position);
            if (review != null) {
                holder.setReview(position, review);
            }
        }

        if (position == (reviews.size() - 1)) {
            // When last review is bound, fetch more in the asynchronously
            // But only fetch if there are actually more reviews to fetch.
            if (reviews.size() < Review.getTotalReviews()) {
                TMDBHelper.getReviews(caller, holder.getContext(), Review.getMovieId(), false);
            }
        }
    }

    public void setCaller(IWaitForReviews caller) {
        // Save the caller so that it can be used to fetch more reviews if needed in onBindViewHolder()
        this.caller = caller;
    }

    public void addReviews(List<Review> newReviews) {
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        reviews.addAll(newReviews);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        return reviews.size();
    }

    public ArrayList<Review> getReviews() {
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        return (ArrayList<Review>) reviews;
    }
}
