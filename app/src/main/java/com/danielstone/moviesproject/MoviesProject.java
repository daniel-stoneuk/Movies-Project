package com.danielstone.moviesproject;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by danielstone on 06/02/16.
 */
public class MoviesProject extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
