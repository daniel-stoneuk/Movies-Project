package com.danielstone.moviesproject.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by danielstone on 06/02/16.
 */
public class MoviesProvider extends ContentProvider {

    private final String LOG_TAG = MoviesProvider.class.getSimpleName();


    //Uri Matcher
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;
    static final int MOVIE = 100;
    static final int MOVIE_WITH_MOVIE_ID = 101;


    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIE);

        return matcher;

    }

    private static final SQLiteQueryBuilder sMovieByMovieIdQueryBuilder;

    static {
        sMovieByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        sMovieByMovieIdQueryBuilder.setTables(
                MoviesContract.MovieEntry.TABLE_NAME
        );
    }

    private static final String sMovieIdSelection =
            MoviesContract.MovieEntry.TABLE_NAME +
                    "." + MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {

        long movieId = MoviesContract.MovieEntry.getMovieIdFromUri(uri);
        Log.d(LOG_TAG, "Movie ID: " + movieId);

        return sMovieByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieIdSelection, // Selection
                new String[] {Long.toString(movieId)}, // Selection args
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;

        Log.d(LOG_TAG, sUriMatcher.match(uri) + ": match code");
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                if (MoviesContract.MovieEntry.getMovieIdFromUri(uri) != null) {
                    retCursor = getMovieByMovieId(uri, projection, sortOrder);
                } else retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_NAME, projection, selection ,selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = MoviesContract.MovieEntry.buildMovieWithIdUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        int updated = insertOrUpdateById(db, uri, MoviesContract.MovieEntry.TABLE_NAME, value, MoviesContract.MovieEntry.COLUMN_MOVIE_ID);
                        returnCount = returnCount + updated;

//                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
//                        Log.d(LOG_TAG, "Id: " + _id);
//                        if (_id != -1) {
//                            returnCount++;
//                        }

                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * In case of a conflict when inserting the values, another update query is sent.
     *
     * @param db     Database to insert to.
     * @param uri    Content provider uri.
     * @param table  Table to insert to.
     * @param values The values to insert to.
     * @param column Column to identify the object.
     * @throws android.database.SQLException
     */
    private int insertOrUpdateById(SQLiteDatabase db, Uri uri, String table,
                                    ContentValues values, String column) throws SQLException {
        int updated = 0;
        try {
            long inserted = db.insertOrThrow(table, null, values);
            //Log.d(LOG_TAG, "Inserted id:" + inserted);
            updated++;
        } catch (SQLiteConstraintException e) {
            int nrRows = update(uri, values, column + "=?",
                    new String[]{values.getAsString(column)});
            //Log.d(LOG_TAG, "Updated: " + nrRows);
            updated++;
            if (nrRows == 0)
                throw e;
        }
        return updated;
    }
}
