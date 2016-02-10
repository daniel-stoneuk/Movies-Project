package com.danielstone.moviesproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.danielstone.moviesproject.adapters.MovieGridAdapter;
import com.danielstone.moviesproject.data.MoviesContract;
import com.danielstone.moviesproject.service.FetchMovieService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final int MOVIE_LOADER = 0;

    //Projections for the movies shown on the main screen
    private static final String[] MOVIE_COLUMNS = {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };

    //indicies tied to MOVIE_COLUMNS
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_POSTER_PATH = 3;

    private int loadCount = 0;

    private static final String SORT_SETTING = "sort_setting";
    private static final String POPULARITY_DESC = "popular";
    private static final String RATING_DESC = "top_rated";

    private String mSortBy = POPULARITY_DESC;

    private GridView mGridView;

    private int mPosition;

    private MovieGridAdapter mMovieGridAdapter;

    public interface MainActivityFragmentCallback {
        void onItemSelected(Uri movieUri);
    }

    public MainActivityFragment() {
    }

    private void updateMovies() {
        Intent intent = new Intent(getActivity(), FetchMovieService.class);

        String[] fetchParams = {POPULARITY_DESC, RATING_DESC};
        intent.putExtra(FetchMovieService.MOVIE_QUERY_EXTRA,
                fetchParams);
        getActivity().startService(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //line for menu events
        setHasOptionsMenu(true);

        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mSortBy = sharedPref.getString(getString(R.string.pref_sort_option), POPULARITY_DESC);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieGridAdapter = new MovieGridAdapter(getActivity(), null, 0);


        mGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        mGridView.setAdapter(mMovieGridAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                Log.i(LOG_TAG, ""+ position);
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    ((MainActivityFragmentCallback) getActivity()).onItemSelected(MoviesContract.MovieEntry.buildMovieWithDbIdUri(cursor.getLong(COL_MOVIE_ID)));

                    Log.i(LOG_TAG, ""+MoviesContract.MovieEntry.buildMovieWithDbIdUri(cursor.getLong(COL_MOVIE_ID)));
                }
            }
        });

        updateMovies();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = "";

        switch (mSortBy) {
            case POPULARITY_DESC:
                sortOrder = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC " + " LIMIT 20";
                break;
            case RATING_DESC:
                sortOrder = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC " + " LIMIT 20";
        }

        if (!sortOrder.equals("")) {
            Uri moviesUri = MoviesContract.MovieEntry.buildMovieUri();

            return new CursorLoader(getActivity(),
                    moviesUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    sortOrder);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieGridAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieGridAdapter.swapCursor(null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu);

        MenuItem action_sort_by_popularity = (MenuItem) menu.findItem(R.id.action_sort_by_popularity);
        MenuItem action_sort_by_rating = (MenuItem) menu.findItem(R.id.action_sort_by_rating);

        switch (mSortBy) {
            case POPULARITY_DESC:
                if (!action_sort_by_popularity.isChecked()) {
                    action_sort_by_popularity.setChecked(true);
                }
                break;
            case RATING_DESC:
                if (!action_sort_by_rating.isChecked()) {
                    action_sort_by_rating.setChecked(true);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sort_by_popularity:
                if (!item.isChecked()) item.setChecked(true);
                return updateSortOption(POPULARITY_DESC);
            case R.id.action_sort_by_rating:
                if (!item.isChecked()) item.setChecked(true);
                return updateSortOption(RATING_DESC);
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private boolean updateSortOption(String sortBy) {
        mSortBy = sortBy;
        Log.d(LOG_TAG, mSortBy);
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_sort_option), mSortBy);
        editor.apply();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        return true;
    }

}
