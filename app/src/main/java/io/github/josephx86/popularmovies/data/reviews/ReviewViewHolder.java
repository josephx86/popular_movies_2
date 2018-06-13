package io.github.josephx86.popularmovies.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.reviews.Review;

class ReviewViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.review_tv)
    TextView bodyTextView;

    @BindView(R.id.review_title_tv)
    TextView titleTextView;

    private Context context;

    ReviewViewHolder(View itemView) {
        super(itemView);
        context = itemView.getContext();
        ButterKnife.bind(this, itemView);
    }

    void setReview(int index, Review review) {
        if (review != null) {
            String title = String.format(Locale.US, "%d of %d by %s", ++index, Review.getTotalReviews(), review.getAuthor());
            setTitle(title);
            setReviewText(review.getContent());
        }
    }

    public Context getContext() {
        return context;
    }

    private void setTitle(String title) {
        if (titleTextView != null) {
            titleTextView.setText(title);
        }
    }

    private void setReviewText(String text) {
        if (bodyTextView != null) {
            bodyTextView.setText(text);
        }
    }
}
