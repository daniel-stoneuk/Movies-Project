package com.danielstone.moviesproject.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.danielstone.moviesproject.MainActivityFragment;
import com.danielstone.moviesproject.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by danielstone on 06/02/16.
 */
public class MovieGridAdapter extends CursorAdapter {

    Map<Long, Palette.Swatch> paletteSwatchMap;


    public MovieGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        paletteSwatchMap = new HashMap<>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        //Get movie item and add it to parent
        int layoutId = R.layout.movie_item;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (view != null){

            final long movieId = cursor.getLong(MainActivityFragment.COL_MOVIE_ID);


            String url = "http://image.tmdb.org/t/p/w342" + cursor.getString(MainActivityFragment.COL_MOVIE_POSTER_PATH);
            final ImageView posterImageView = (ImageView) view.findViewById(R.id.movie_image);
            posterImageView.setImageDrawable(new ColorDrawable(Color.parseColor("#7986CB")));

            final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.frameLayout);

            final TextView titleView = (TextView) view.findViewById(R.id.titleView);
            titleView.setText(cursor.getString(MainActivityFragment.COL_MOVIE_TITLE));
            final Context mContext = context;

            titleView.setBackgroundColor(Color.parseColor("#212121"));
            titleView.setTextColor(Color.parseColor("#FFFFFF"));


            Glide.with(context).load(url).asBitmap().fitCenter().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    posterImageView.setAlpha(0f);
                    posterImageView.setImageBitmap(resource);
                    posterImageView.animate().setDuration(200l).alpha(1f);

//                    Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
//                        @Override
//                        public void onGenerated(Palette palette) {
//                            Palette.Swatch vibrant =
//                                    palette.getVibrantSwatch();
//                            if (vibrant != null) {
//
//                                int colorFrom = Color.parseColor("#212121");
//                                int colorTo = vibrant.getRgb();
//                                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
//                                colorAnimation.setDuration(100); // milliseconds
//                                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//                                    @Override
//                                    public void onAnimationUpdate(ValueAnimator animator) {
//                                        titleView.setBackgroundColor((int) animator.getAnimatedValue());
//                                    }
//
//                                });
//                                colorAnimation.start();
//
//                                titleView.setTextColor(vibrant.getBodyTextColor());
//                            }
//                        }
//                    });

                }
            });

        }
    }

    public static int darker (int color, float factor) {
        int a = Color.alpha( color );
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb( a,
                Math.max( (int)(r * factor), 0 ),
                Math.max( (int)(g * factor), 0 ),
                Math.max( (int)(b * factor), 0 ) );
    }

}
