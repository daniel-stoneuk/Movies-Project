package com.danielstone.moviesproject.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.danielstone.moviesproject.BuildConfig;
import com.danielstone.moviesproject.R;
import com.danielstone.moviesproject.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;


public class FetchMovieService extends IntentService {

    String LOG_TAG = FetchMovieService.class.getSimpleName();

    public static final String MOVIE_QUERY_EXTRA = "mqe";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     **/
    public FetchMovieService() {
        super("FetchMovieService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //check last sync
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        long lastSync = sharedPreferences.getLong(this.getString(R.string.pref_last_sync), -1);
        Calendar calendar = Calendar.getInstance();
        int lastSyncDay = -1;
        if (lastSync != -1) {
            calendar.setTime(new Date(lastSync));
            lastSyncDay = calendar.get(Calendar.DATE);
        }

        calendar.setTimeInMillis(System.currentTimeMillis());
        int todayDay = calendar.get(Calendar.DATE);


        if (lastSyncDay == -1 || todayDay != lastSyncDay ) {

            String[] sortParams = intent.getStringArrayExtra(MOVIE_QUERY_EXTRA);

            for (String sortParam : sortParams) {

                // These two need to be declared outside the try/catch
                // so that they can be closed in the finally block.
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;

                // Will contain the raw JSON response as a string.
                String movieJsonStr = null;

                try {
                    // Construct the URL for the OpenWeatherMap query
                    // Possible parameters are avaiable at OWM's forecast API page, at
                    // http://openweathermap.org/API#forecast
                    final String POPULAR_BASE_URL =
                            "https://api.themoviedb.org/3/movie/";
                    final String API_KEY_PARAM = "api_key";

                    Uri builtUri = Uri.parse(POPULAR_BASE_URL).buildUpon()
                            .appendPath(sortParam)
                            .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                            .build();

                    URL url = new URL(builtUri.toString());

                    // Create the request to OpenWeatherMap, and open the connection
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    // Read the input stream into a String
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        // Nothing to do.
                        return;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                        // But it does make debugging a *lot* easier if you print out the completed
                        // buffer for debugging.
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        // Stream was empty.  No point in parsing.
                        return;
                    }
                    movieJsonStr = buffer.toString();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error ", e);
                    // If the code didn't successfully get the weather data, there's no point in attemping
                    // to parse it.
                    return;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(LOG_TAG, "Error closing stream", e);
                        }
                    }
                }

                try {
                    getMovieDataFromJson(movieJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                // This will only happen if there was an error getting or parsing the forecast.
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(this.getString(R.string.pref_last_sync), System.currentTimeMillis());
            editor.commit();

        } else {
            Log.d(LOG_TAG, "Sync already occurred today");
        }
    }

    private void getMovieDataFromJson(String movieJsonStr) throws JSONException {

        final String MDB_RESULTS_LIST = "results";
        final String MDB_MOVIE_ID = "id";
        final String MDB_TITLE = "title";
        final String MDB_OVERVIEW = "overview";
        final String MDB_RELEASE_DATE = "release_date";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_BACKDROP_PATH = "backdrop_path";
        final String MDB_POPULARITY = "popularity";
        final String MDB_VOTE_AVERAGE = "vote_average";
        final String MDB_VOTE_COUNT = "vote_count";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultArray = movieJson.getJSONArray(MDB_RESULTS_LIST);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(resultArray.length());

            for (int i = 0; i < resultArray.length(); i++) {
                int movieDbId;
                String title;
                String overview;
                String date;
                String posterPath;
                String backdropPath;
                double popularity;
                double voteAverage;
                int voteCount;

                JSONObject movieObject = resultArray.getJSONObject(i);

                movieDbId = movieObject.getInt(MDB_MOVIE_ID);
                title = movieObject.getString(MDB_TITLE);
                overview = movieObject.getString(MDB_OVERVIEW);
                date = movieObject.getString(MDB_RELEASE_DATE);
                posterPath = movieObject.getString(MDB_POSTER_PATH);
                backdropPath = movieObject.getString(MDB_BACKDROP_PATH);
                popularity = movieObject.getDouble(MDB_POPULARITY);
                voteAverage = movieObject.getDouble(MDB_VOTE_AVERAGE);
                voteCount = movieObject.getInt(MDB_VOTE_COUNT);

                ContentValues movieValues = new ContentValues();
                movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieDbId);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, date);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, backdropPath);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, voteAverage);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, voteCount);

                cVVector.add(movieValues);
            }

            int inserted = 0;
            int removed = 0;
            //add to database
            if (cVVector.size() > 0 ){
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = this.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "Movie Download Complete. " + inserted + " Inserted. " + removed + " Removed.");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

    }
}
