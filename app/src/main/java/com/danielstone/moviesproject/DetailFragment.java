package com.danielstone.moviesproject;


import android.app.Activity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.danielstone.moviesproject.data.MoviesContract;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 1;

    private static final String[] DETAIL_COLUMNS = {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_TITLE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_VOTE_COUNT
    };

    //indicies tied to MOVIE_COLUMNS
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_TITLE = 2;
    public static final int COL_MOVIE_POSTER_PATH = 3;
    public static final int COL_MOVIE_BACKDROP_PATH = 4;
    public static final int COL_MOVIE_OVERVIEW = 5;
    public static final int COL_VOTE_AVERAGE = 6;
    public static final int COL_VOTE_COUNT = 7;

    private TextView descriptionTextView;
    private TextView averageVoteTextView;
    private TextView totalVotesTextView;
    private ImageView posterImageView;



    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            Log.d(LOG_TAG, ""+ mUri);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail_activity, container, false);

        descriptionTextView = (TextView) rootView.findViewById(R.id.movie_description);
        posterImageView = (ImageView) rootView.findViewById(R.id.posterImageView);
        averageVoteTextView = (TextView) rootView.findViewById(R.id.averageVoteTextView);
        totalVotesTextView = (TextView) rootView.findViewById(R.id.totalVotesTextView);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            Log.d(LOG_TAG, "oncreateloader");
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            Log.d(LOG_TAG, "Cursor: " + DatabaseUtils.dumpCursorToString(data));
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            Log.i(LOG_TAG, data.getString(COL_MOVIE_TITLE));
            if (appBarLayout != null) {

                appBarLayout.setTitle(data.getString(COL_MOVIE_TITLE));
            }

            String url = "http://image.tmdb.org/t/p/w780" + data.getString(COL_MOVIE_BACKDROP_PATH);
            ImageView imgToolbar = (ImageView) activity.findViewById(R.id.imgToolbar);
            Glide.with(getActivity()).load(url).fitCenter().crossFade().into(imgToolbar);

            url = "http://image.tmdb.org/t/p/w500" + data.getString(COL_MOVIE_POSTER_PATH);
            Glide.with(getActivity()).load(url).placeholder(new ColorDrawable(Color.parseColor("#7986CB"))).fitCenter().crossFade().into(posterImageView);

            String movieDescription = data.getString(COL_MOVIE_OVERVIEW);
            descriptionTextView.setText(movieDescription);

            double averageVotes = data.getDouble(COL_VOTE_AVERAGE);
            averageVoteTextView.setText(Double.toString(averageVotes));

            int totalVotes = data.getInt(COL_VOTE_COUNT);
            totalVotesTextView.setText(Integer.toString(totalVotes));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
