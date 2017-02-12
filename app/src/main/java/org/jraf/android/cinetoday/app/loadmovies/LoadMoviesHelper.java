/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.jraf.android.cinetoday.api.Api;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.model.movie.Showtime;
import org.jraf.android.cinetoday.prefs.MainPrefs;
import org.jraf.android.cinetoday.provider.CineTodayProvider;
import org.jraf.android.cinetoday.provider.movie.MovieColumns;
import org.jraf.android.cinetoday.provider.movie.MovieContentValues;
import org.jraf.android.cinetoday.provider.showtime.ShowtimeColumns;
import org.jraf.android.cinetoday.provider.showtime.ShowtimeContentValues;
import org.jraf.android.cinetoday.provider.theater.TheaterCursor;
import org.jraf.android.cinetoday.provider.theater.TheaterSelection;
import org.jraf.android.util.log.Log;

public class LoadMoviesHelper {
    private static final LoadMoviesHelper INSTANCE = new LoadMoviesHelper();

    public static LoadMoviesHelper get() {
        return INSTANCE;
    }

    private volatile boolean mWantStop;

    public void setWantStop(boolean wantStop) {
        mWantStop = wantStop;
    }

    @WorkerThread
    public void loadMovies(Context context) throws Exception {
        mWantStop = false;
        LoadMoviesListenerHelper loadMoviesListenerHelper = LoadMoviesListenerHelper.get();
        loadMoviesListenerHelper.onLoadMoviesStarted();

        // 1/ Retrieve list of movies (including showtimes), for all the theaters
        SortedSet<Movie> movies = new TreeSet<>(Movie.COMPARATOR);
        try (TheaterCursor theaterCursor = new TheaterSelection().query(context)) {
            while (theaterCursor.moveToNext()) {
                Api.get(context).getMovieList(movies, theaterCursor.getPublicId(), new Date());

                if (mWantStop) {
                    loadMoviesListenerHelper.onLoadMoviesInterrupted();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(e, "Could not load movies");
            loadMoviesListenerHelper.onLoadMoviesError(e);
            throw e;
        }

        if (mWantStop) {
            loadMoviesListenerHelper.onLoadMoviesInterrupted();
            return;
        }

        // 2/ Retrieve more details about each movie
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

            if (mWantStop) {
                loadMoviesListenerHelper.onLoadMoviesInterrupted();
                return;
            }

            i++;
            loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle);
        }

        // 3/ Save everything to the local db
        persist(context, movies);

        MainPrefs.get(context).putLastUpdateDate(System.currentTimeMillis());
        loadMoviesListenerHelper.resetError();
        loadMoviesListenerHelper.onLoadMoviesSuccess();

        // TODO notification
    }

    private void persist(Context context, SortedSet<Movie> movies) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        // First, delete all the movies (and showtimes)
        operations.add(ContentProviderOperation.newDelete(MovieColumns.CONTENT_URI).build());

        // Get a map of theater internal ids to public ids
        HashMap<String, Long> theaterIds = new HashMap<>();
        try (TheaterCursor cursor = new TheaterSelection().query(context)) {
            while (cursor.moveToNext()) {
                theaterIds.put(cursor.getPublicId(), cursor.getId());
            }
        }

        for (Movie movie : movies) {
            // Movie
            MovieContentValues movieValues = new MovieContentValues()
                    .putPublicId(movie.id)
                    .putTitleOriginal(movie.originalTitle)
                    .putTitleLocal(movie.localTitle)
                    .putDirectors(movie.directors)
                    .putActors(movie.actors)
                    .putReleaseDate(movie.releaseDate)
                    .putDuration(movie.durationSeconds)
                    .putGenres(TextUtils.join("|", movie.genres))
                    .putPosterUri(movie.posterUri)
                    .putTrailerUri(movie.trailerUri)
                    .putWebUri(movie.webUri)
                    .putSynopsis(movie.synopsis);
            operations.add(ContentProviderOperation.newInsert(MovieColumns.CONTENT_URI).withValues(movieValues.values()).build());

            // Showtimes
            int movieIdResultIndex = operations.size() - 1;
            for (Map.Entry<String, List<Showtime>> entry : movie.todayShowtimes.entrySet()) {
                String theaterPublicId = entry.getKey();
                Long theaterId = theaterIds.get(theaterPublicId);
                List<Showtime> showtimes = entry.getValue();
                for (Showtime showtime : showtimes) {
                    ShowtimeContentValues showtimeValues = new ShowtimeContentValues()
                            .putTheaterId(theaterId)
                            .putTime(showtime.time)
                            .putIs3d(showtime.is3d);
                    ContentProviderOperation operation =
                            ContentProviderOperation.newInsert(ShowtimeColumns.CONTENT_URI)
                                    .withValues(showtimeValues.values())
                                    .withValueBackReference(ShowtimeColumns.MOVIE_ID, movieIdResultIndex)
                                    .build();
                    operations.add(operation);
                }
            }
        }

        // Apply the batch of operations
        try {
            context.getContentResolver().applyBatch(CineTodayProvider.AUTHORITY, operations);
        } catch (Exception e) {
            Log.e(e, "Could not apply batch");
        }
    }

    public void startLoadMoviesIntentService(Context context) {
        Intent intent = new Intent(context, LoadMoviesIntentService.class);
        intent.setAction(LoadMoviesIntentService.ACTION_LOAD_MOVIES);
        context.startService(intent);
    }
}
