package io.github.josephx86.popularmovies.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.josephx86.popularmovies.R;
import io.github.josephx86.popularmovies.data.movies.MoviesAdapter;

public class Utils {
    public static boolean isDeviceOnline(Context context) {
        boolean online = false;
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null) {
                online = networkInfo.isConnected();
            }
        }
        return online;
    }

    public static Date parseDate(String dateString) {
        // Parses date in yyyy-mm-dd format to Date object.
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static float getPosterImageViewWidth(Context context) {
        return context.getResources().getDimension(R.dimen.movie_poster_width);
    }

    public static int parseFirstPositiveIntegerFromString(String stringWithLeadingDigits) {
        int result = -1;
        // Gets the integer value of first digits found in a string
        if (stringWithLeadingDigits == null) {
            return result; // -1
        }
        StringBuilder buffer = new StringBuilder();
        boolean digitsFound = false;
        for (int i = 0; i < stringWithLeadingDigits.length(); i++) {
            char c = stringWithLeadingDigits.charAt(i);
            if (Character.isDigit(c)) {
                buffer.append(c);
                digitsFound = true;
            } else if (digitsFound) {
                // Some digits have already been found before this non-digit char.
                break;// Exit loop
            }
        }
        try {
            result = Integer.parseInt(buffer.toString());
        } catch (NumberFormatException nex) {
            // NumberFormatException will be thrown of string has 'int' that would be overflow 32 bits.
        }
        return result;
    }

    public static int calculatePosterGridLayoutColumns(Context context) {
        float posterWidth = getPosterImageViewWidth(context);
        return calculateGridLayoutColumns(context, posterWidth);
    }

    public static int calculateVideoGridLayoutColumns(Context context) {
        float videoWidth = context.getResources().getDimension(R.dimen.movie_trailer_width);
        return calculateGridLayoutColumns(context, videoWidth);
    }

    private static int calculateGridLayoutColumns(Context context, float childWith) {
        int deviceWidth = context.getResources().getDisplayMetrics().widthPixels;
        return (int) (deviceWidth / childWith);
    }

    public static String getPosterUrl(String imagePath) {
        if (imagePath == null) {
            imagePath = "";
        }
        return String.format(Locale.US, "%s%s%s",
                TMDBHelper.getBaseImageUrl(),
                TMDBHelper.getPosterSize(),
                imagePath);
    }

    public static String getBackdropUrl(String imagePath) {
        if (imagePath == null) {
            imagePath = "";
        }
        return String.format(Locale.US, "%s%s%s",
                TMDBHelper.getBaseImageUrl(),
                TMDBHelper.getBackdropSize(),
                imagePath);
    }

    public static void setRating(LinearLayout ratingLayout, double rating) {
        boolean hasHalfStar = false;
        int wholeStarCount = (int) rating;
        int count = 0;
        if (wholeStarCount != 10) {
            int fractionStarValue = (int) ((rating - wholeStarCount) * 10);
            hasHalfStar = fractionStarValue >= 5;
        }
        Context context = ratingLayout.getContext();
        if (ratingLayout.getChildCount() > 0) {
            ratingLayout.removeAllViews();
        }
        for (int i = 0; i < wholeStarCount; i++) {
            ImageView star = new ImageView(context);
            star.setImageResource(R.drawable.ic_star_12dp);
            ratingLayout.addView(star);
            count++;
        }
        if (hasHalfStar) {
            ImageView halfStar = new ImageView(context);
            halfStar.setImageResource(R.drawable.ic_star_half_12dp);
            ratingLayout.addView(halfStar);
            count++;
        }

        // The rest of the stars will be empty so that user will see rating is out of 10.
        while (count < 10) {
            ImageView emptyStar = new ImageView(context);
            emptyStar.setImageResource(R.drawable.ic_star_border_12dp);
            ratingLayout.addView(emptyStar);
            count++;
        }
    }

    public static MoviesAdapter.SortType getSortOrderPreference(Context context) {
        String key = context.getString(R.string.pref_sort);
        String orderByPopularity = context.getString(R.string.pref_sort_by_popularity);
        String orderByRating = context.getString(R.string.pref_sort_by_rating);
        String order = PreferenceManager.getDefaultSharedPreferences(context).getString(key, orderByPopularity);

        if (order.equals(orderByPopularity)) {
            return MoviesAdapter.SortType.Popularity;
        } else if (order.equals(orderByRating)) {
            return MoviesAdapter.SortType.Rating;
        } else {
            return MoviesAdapter.SortType.Popularity;
        }
    }

    public static void saveSortOrderPreference(Context context, MoviesAdapter.SortType sortType) {
        String key = context.getString(R.string.pref_sort);
        String orderByPopularity = context.getString(R.string.pref_sort_by_popularity);
        String orderByRating = context.getString(R.string.pref_sort_by_rating);
        String order = orderByPopularity; // Default.
        if (sortType == MoviesAdapter.SortType.Rating) {
            order = orderByRating;
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, order);
        editor.apply();
    }
}
