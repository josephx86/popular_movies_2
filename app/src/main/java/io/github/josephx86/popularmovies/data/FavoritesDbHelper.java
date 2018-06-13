package io.github.josephx86.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Locale;

public class FavoritesDbHelper extends SQLiteOpenHelper {
    private static int DB_VERSION = 1;
    private static final String DB_NAME = "popular_movies.db";

    public FavoritesDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String sql = "create table if not exists `" + FavoritesEntry.TABLE_NAME + "` (" +
                "`" + FavoritesEntry.COL_MOVIE_ID + "` integer not null unique, " +
                "`" + FavoritesEntry.COL_VOTE_COUNT + "` integer not null, " +
                "`" + FavoritesEntry.COL_VIDEO + "` integer not null, " +
                "`" + FavoritesEntry.COL_ADULT + "`	integer not null, " +
                "`" + FavoritesEntry.COL_VOTE_AVERAGE + "` real not null, " +
                "`" + FavoritesEntry.COL_POPULARITY + "` real not null, " +
                "`" + FavoritesEntry.COL_TITLE + "` text not null, " +
                "`" + FavoritesEntry.COL_POSTER_PATH + "` text not null, " +
                "`" + FavoritesEntry.COL_ORIGINAL_LANGUAGE + "` text not null, " +
                "`" + FavoritesEntry.COL_ORIGINAL_TITLE + "` text not null, " +
                "`" + FavoritesEntry.COL_BACKDROP_PATH + "` text not null, " +
                "`" + FavoritesEntry.COL_OVERVIEW + "` text not null, " +
                "`" + FavoritesEntry.COL_RELEASE_DATE + "` integer not null, " +
                "`" + FavoritesEntry.COL_THUMBNAIL + "` blob);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String sql = String.format(Locale.ENGLISH, "drop table if exists `%s`;", FavoritesEntry.TABLE_NAME);
        db.execSQL(sql);
        onCreate(db);
    }
}
