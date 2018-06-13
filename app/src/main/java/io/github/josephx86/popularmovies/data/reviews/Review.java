package io.github.josephx86.popularmovies.data.reviews;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {
    private String author = "", content = "";

    // Reviews will only ever be shown in the details activity in reference to one movie.
    // So it is safe to use static variables for data that will be common to all reviews.
    private static int totalReviews = 0;
    private static int movieId = 0;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    protected Review(Parcel in) {
        author = in.readString();
        content = in.readString();
        totalReviews = in.readInt();
        movieId = in.readInt();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public static int getTotalReviews() {
        return totalReviews;
    }

    public static void setTotalReviews(int total) {
        totalReviews = total;
    }

    public static int getMovieId() {
        return movieId;
    }

    public static void setMovieId(int movieId) {
        Review.movieId = movieId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
        dest.writeInt(totalReviews);
        dest.writeInt(movieId);
    }
}
