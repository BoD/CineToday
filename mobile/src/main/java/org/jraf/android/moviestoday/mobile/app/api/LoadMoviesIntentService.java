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
package org.jraf.android.moviestoday.mobile.app.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import org.jraf.android.moviestoday.common.model.movie.Movie;
import org.jraf.android.moviestoday.common.wear.WearHelper;
import org.jraf.android.moviestoday.mobile.api.Api;
import org.jraf.android.moviestoday.mobile.api.ImageCache;
import org.jraf.android.moviestoday.mobile.prefs.MainPrefs;
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
     * Starts this service to perform action ACTION_LOAD_MOVIES. If the service is already performing a task this action will be queued.
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
            final String action = intent.getAction();
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
    static void handleActionLoadMovies(Context context) throws Exception {
        LoadMoviesListenerHelper loadMoviesListenerHelper = LoadMoviesListenerHelper.get();
        loadMoviesListenerHelper.onLoadMoviesStarted();

        SortedSet<Movie> movies;
        try {
            String theaterId = MainPrefs.get(context).getTheaterId();
            movies = Api.get(context).getMovieList(theaterId, new Date());
        } catch (Exception e) {
            loadMoviesListenerHelper.onLoadMoviesError(e);
            throw e;
        }

        WearHelper wearHelper = WearHelper.get();
        wearHelper.connect(context);

        int size = movies.size();
        int i = 0;
        for (Movie movie : movies) {
            // Get movie info
            try {
                Api.get(context).getMovieInfo(movie);
                Log.d(movie.toString());
            } catch (Exception e) {
                loadMoviesListenerHelper.onLoadMoviesError(e);
                throw e;
            }

            // Get poster image
            Bitmap posterBitmap = ImageCache.get(context).getBitmap(movie.posterUri, POSTER_THUMBNAIL_WIDTH, POSTER_THUMBNAIL_HEIGHT);
            if (posterBitmap != null) {
                // Save it for Wear (only if not already there)
                Bitmap currentBitmap = wearHelper.getMoviePoster(movie);
                if (currentBitmap == null) {
                    wearHelper.putMoviePoster(movie, posterBitmap);
                }
            }
            i++;

            loadMoviesListenerHelper.onLoadMoviesProgress(i, size);
        }
        List<Movie> previousMovies = wearHelper.getMovies();
        wearHelper.putMovies(movies);

        MainPrefs.get(context).putLastUpdateDate(System.currentTimeMillis());

        if (previousMovies != null) {
            // Show notification
            showNotification(wearHelper, previousMovies, movies);
        }

        wearHelper.disconnect();

        loadMoviesListenerHelper.resetError();
        loadMoviesListenerHelper.onLoadMoviesFinished();
    }

    private static void showNotification(WearHelper wearHelper, Collection<Movie> previousMovies, Collection<Movie> currentMovies) {
        TreeSet<Movie> newMovies = new TreeSet<>(Movie.COMPARATOR);
        for (Movie currentMovie : currentMovies) {
            if (!previousMovies.contains(currentMovie)) newMovies.add(currentMovie);
        }

        if (newMovies.isEmpty()) {
            Log.d("No new movies: do not show a notifications");
            return;
        }

        ArrayList<String> firstThreeNewMovies = new ArrayList<>(3);
        int i = 0;
        for (Iterator<Movie> iterator = newMovies.iterator(); iterator.hasNext() && i < 3; i++) {
            Movie newMovie = iterator.next();
            firstThreeNewMovies.add(newMovie.localTitle);
        }
        wearHelper.putNotification(firstThreeNewMovies);
    }
}
