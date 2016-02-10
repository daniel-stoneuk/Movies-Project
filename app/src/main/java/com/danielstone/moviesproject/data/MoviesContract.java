package com.danielstone.moviesproject.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by danielstone on 06/02/16.
 */
public class MoviesContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.danielstone.moviesproject";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //Possible paths (URIs)
    public static final String PATH_MOVIE = "movie";


    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        //Table name
        public static final String TABLE_NAME = "movies";

        //MovieDB Movie id
        public static final String COLUMN_MOVIE_ID = "movie_id";

        //Poster path returned from API
        public static final String COLUMN_POSTER_PATH = "poster_path";
        //Backdrop path returned from API
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        //Overview
        public static final String COLUMN_OVERVIEW = "overview";
        //release date
        public static final String COLUMN_RELEASE_DATE = "release_date";
        //title
        public static final String COLUMN_TITLE = "title";
        //popularity
        public static final String COLUMN_POPULARITY = "popularity";
        //vote average
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        //vote count
        public static final String COLUMN_VOTE_COUNT = "vote_count";

        public static Uri buildMovieWithDbIdUri(long movieDbId) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_MOVIE_ID, Long.toString(movieDbId)).build();
        }
        public static Uri buildMovieWithIdUri(long movieId) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_MOVIE_ID, Long.toString(movieId)).build();
        }
        public static Uri buildMovieUri() {
            return CONTENT_URI;
        }

        public static Long getMovieIdFromUri(Uri uri) {
            if (uri.getQueryParameter(COLUMN_MOVIE_ID) != null) {
                return Long.parseLong(uri.getQueryParameter(COLUMN_MOVIE_ID));
            } else return null;
        }


    }

}
