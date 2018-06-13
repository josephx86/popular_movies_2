package io.github.josephx86.popularmovies.data;

import android.provider.BaseColumns;

public class FavoritesEntry implements BaseColumns {
    public static final String TABLE_NAME = "tbl_favorites";

    public static final String COL_MOVIE_ID = "movieId";
    public static final String COL_VOTE_COUNT = "vote_count";
    public static final String COL_VIDEO = "video";
    public static final String COL_ADULT = "adult";
    public static final String COL_VOTE_AVERAGE = "vote_average";
    public static final String COL_POPULARITY = "popularity";
    public static final String COL_TITLE = "title";
    public static final String COL_POSTER_PATH = "poster_path";
    public static final String COL_ORIGINAL_LANGUAGE = "original_language";
    public static final String COL_ORIGINAL_TITLE = "original_title";
    public static final String COL_BACKDROP_PATH = "backdrop_path";
    public static final String COL_OVERVIEW = "overview";
    public static final String COL_RELEASE_DATE = "release_date";
    public static final String COL_THUMBNAIL = "thumbnail";
}
