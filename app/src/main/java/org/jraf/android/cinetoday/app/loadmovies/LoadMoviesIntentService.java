/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.cinetoday.app.loadmovies;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;

import org.jraf.android.cinetoday.api.Api;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.prefs.MainPrefs;
import org.jraf.android.cinetoday.provider.theater.TheaterCursor;
import org.jraf.android.cinetoday.provider.theater.TheaterSelection;
import org.jraf.android.util.log.Log;

public class LoadMoviesIntentService extends IntentService {
    private static final String PREFIX = LoadMoviesIntentService.class.getName() + ".";
    private static final String ACTION_LOAD_MOVIES = PREFIX + "LOAD_MOVIES";

    private static final int POSTER_THUMBNAIL_WIDTH = 240;
    private static final int POSTER_THUMBNAIL_HEIGHT = 240;


    public LoadMoviesIntentService() {
        super(LoadMoviesIntentService.class.getName());
    }

    /**
     * Starts this service to perform action ACTION_LOAD_MOVIES.
     * If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionLoadMovies(Context context) {
        Intent intent = new Intent(context, LoadMoviesIntentService.class);
        intent.setAction(ACTION_LOAD_MOVIES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_LOAD_MOVIES.equals(action)) {
                try {
                    handleActionLoadMovies(this);
                } catch (Exception ignore) {
                    // Do nothing, it was already handled inside handleActionLoadMovies
                }
            }
        }
    }

    /**
     * Handle action ACTION_LOAD_MOVIES in the provided background thread.
     */
    @WorkerThread
    static void handleActionLoadMovies(Context context) throws Exception {
        LoadMoviesHelper.get().setWantStop(false);
        LoadMoviesListenerHelper loadMoviesListenerHelper = LoadMoviesListenerHelper.get();
        loadMoviesListenerHelper.onLoadMoviesStarted();

        // Retrieve list of movies, for all the theaters
        SortedSet<Movie> movies = new TreeSet<>(Movie.COMPARATOR);
        TheaterSelection theaterSelection = new TheaterSelection();
        TheaterCursor theaterCursor = theaterSelection.query(context);
        try {
            while (theaterCursor.moveToNext()) {
                String theaterId = theaterCursor.getPublicId();
                String theaterName = theaterCursor.getName();
                Api.get(context).getMovieList(movies, theaterId, theaterName, theaterCursor.getPosition(), new Date());

                if (LoadMoviesHelper.get().isWantStop()) {
                    loadMoviesListenerHelper.onLoadMoviesInterrupted();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(e, "Could not load movies");
            loadMoviesListenerHelper.onLoadMoviesError(e);
            throw e;
        } finally {
            theaterCursor.close();
        }

        if (LoadMoviesHelper.get().isWantStop()) {
            loadMoviesListenerHelper.onLoadMoviesInterrupted();
            return;
        }

        int size = movies.size();
        int i = 0;
        for (Movie movie : movies) {
            loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle);

            // Get movie info
            try {
                Api.get(context).getMovieInfo(movie);
                Log.d(movie.toString());
            } catch (Exception e) {
                Log.e(e, "Could not load movie info: movie = %s", movie);
                loadMoviesListenerHelper.onLoadMoviesError(e);
                throw e;
            }

            if (LoadMoviesHelper.get().isWantStop()) {
                loadMoviesListenerHelper.onLoadMoviesInterrupted();
                return;
            }

            if (LoadMoviesHelper.get().isWantStop()) {
                loadMoviesListenerHelper.onLoadMoviesInterrupted();
                return;
            }

            i++;
            loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle);
        }
        MainPrefs.get(context).putLastUpdateDate(System.currentTimeMillis());

        loadMoviesListenerHelper.resetError();
        loadMoviesListenerHelper.onLoadMoviesSuccess();

        // TODO notification
    }
}
