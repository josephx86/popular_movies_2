package io.github.josephx86.popularmovies.data;

import java.util.List;

import io.github.josephx86.popularmovies.data.reviews.Review;

public interface IWaitForReviews {

    void processReceivedReviews(List<Review> reviews);
}
