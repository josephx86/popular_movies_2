package io.github.josephx86.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FavoritesProvider extends ContentProvider {

    private static final String PROVIDER_NAME = "io.github.josephx86.popularmovies.data.FavoritesProvider";
    private static final String FAVORITES_PATH = "favorites";
    private static final String FAVORITES_URL = "content://" + PROVIDER_NAME + "/" + FAVORITES_PATH;
    public static final Uri FAVORITES_URI = Uri.parse(FAVORITES_URL);
    private static UriMatcher uriMatcher;
    private static final int ALL_FAVORITES_URI_CODE = 100;
    private static final int SINGLE_FAVORITE_URI_CODE = 101;
    private SQLiteDatabase database;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, FAVORITES_PATH, ALL_FAVORITES_URI_CODE);
        uriMatcher.addURI(PROVIDER_NAME, FAVORITES_PATH + "/#", SINGLE_FAVORITE_URI_CODE);
    }

    public FavoritesProvider() {

    }

    @Override
    public boolean onCreate() {
        database = new FavoritesDbHelper(getContext()).getWritableDatabase();
        return database != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get all favorites
        Cursor result = null;
        switch (uriMatcher.match(uri)) {
            case SINGLE_FAVORITE_URI_CODE:
                long movieId = ContentUris.parseId(uri);
                selection = FavoritesEntry.COL_MOVIE_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(movieId)};
                result = database.query(FavoritesEntry.TABLE_NAME, null, selection, selectionArgs, null, null, FavoritesEntry.COL_TITLE);

                // Watch changes
                result.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case ALL_FAVORITES_URI_CODE:
                result = database.query(FavoritesEntry.TABLE_NAME, null, null, null, null, null, FavoritesEntry.COL_TITLE);

                // Watch changes
                result.setNotificationUri(getContext().getContentResolver(), uri);
                break;
        }
        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri result = null;
        switch (uriMatcher.match(uri)) {
            case ALL_FAVORITES_URI_CODE:
                // NOTE: movieId column has 'unique' attribute to avoid duplicate entries of the same movie.
                long insertedEntryId = database.insert(FavoritesEntry.TABLE_NAME, null, values);
                if (insertedEntryId != -1) {
                    result = ContentUris.withAppendedId(FAVORITES_URI, insertedEntryId);

                    // Notify observers that a row has changed
                    getContext().getContentResolver().notifyChange(result, null);
                }
                break;
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Delete single item
        int deleted = 0;
        switch (uriMatcher.match(uri)) {
            case SINGLE_FAVORITE_URI_CODE:
                long movieId = ContentUris.parseId(uri);
                selection = FavoritesEntry.COL_MOVIE_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(movieId)};
                deleted = database.delete(FavoritesEntry.TABLE_NAME, selection, selectionArgs);

                // Notify watchers
                getContext().getContentResolver().notifyChange(uri, null);
                break;
        }
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
