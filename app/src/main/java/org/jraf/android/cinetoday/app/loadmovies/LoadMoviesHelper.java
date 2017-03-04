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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.support.annotation.WorkerThread;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.api.Api;
import org.jraf.android.cinetoday.app.main.MainActivity;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.model.movie.Showtime;
import org.jraf.android.cinetoday.prefs.MainPrefs;
import org.jraf.android.cinetoday.provider.CineTodayProvider;
import org.jraf.android.cinetoday.provider.movie.MovieColumns;
import org.jraf.android.cinetoday.provider.movie.MovieContentValues;
import org.jraf.android.cinetoday.provider.movie.MovieCursor;
import org.jraf.android.cinetoday.provider.movie.MovieSelection;
import org.jraf.android.cinetoday.provider.showtime.ShowtimeColumns;
import org.jraf.android.cinetoday.provider.showtime.ShowtimeContentValues;
import org.jraf.android.cinetoday.provider.theater.TheaterCursor;
import org.jraf.android.cinetoday.provider.theater.TheaterSelection;
import org.jraf.android.util.handler.HandlerUtil;
import org.jraf.android.util.log.Log;
import org.jraf.android.util.ui.screenshape.ScreenShapeHelper;

public class LoadMoviesHelper {
    private static final LoadMoviesHelper INSTANCE = new LoadMoviesHelper();
    private static final int NOTIFICATION_ID = 0;
    private static final int MIN_BANDWIDTH_KBPS = 320;


    public static LoadMoviesHelper get() {
        return INSTANCE;
    }

    private volatile boolean mWantStop;

    public void setWantStop(boolean wantStop) {
        mWantStop = wantStop;
    }

    @WorkerThread
    private boolean requestHighBandwidthNetwork(Context context, long timeout, TimeUnit unit) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null || connectivityManager.getNetworkCapabilities(activeNetwork).getLinkDownstreamBandwidthKbps() < MIN_BANDWIDTH_KBPS) {
            final AtomicBoolean res = new AtomicBoolean(false);
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            // Request a high-bandwidth network
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    connectivityManager.unregisterNetworkCallback(this);
                    res.set(connectivityManager.bindProcessToNetwork(network));
                    countDownLatch.countDown();
                }
            };
            NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();

            Log.d("Requesting a high-bandwidth network");
            connectivityManager.requestNetwork(request, networkCallback);
            try {
                countDownLatch.await(timeout, unit);
            } catch (InterruptedException ignored) {}

            return res.get();
        } else {
            // Already on a high-bandwidth network
            return true;
        }
    }

    @WorkerThread
    /* package */ void loadMovies(final Context context) throws Exception {
        mWantStop = false;
        LoadMoviesListenerHelper loadMoviesListenerHelper = LoadMoviesListenerHelper.get();
        loadMoviesListenerHelper.onLoadMoviesStarted();

        // 0/ Try to connect to a fast network
        boolean highBandwidthNetworkSuccess = requestHighBandwidthNetwork(context, 10, TimeUnit.SECONDS);
        Log.d("Fast network success=%s", highBandwidthNetworkSuccess);

        SortedSet<Movie> movies = new TreeSet<>(Movie.COMPARATOR);
        try {
            // 1/ Retrieve list of movies (including showtimes), for all the theaters
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
            for (final Movie movie : movies) {
                loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle);

                // Check if we already have info for this movie
                if (new MovieSelection().publicId(movie.id).count(context) == 0) {
                    movie.isNew = true;

                    // Get movie info
                    try {
                        Api.get(context).getMovieInfo(movie);
                        Log.d(movie.toString());
                    } catch (Exception e) {
                        Log.e(e, "Could not load movie info: movie = %s", movie);
                        loadMoviesListenerHelper.onLoadMoviesError(e);
                        throw e;
                    }
                }

                if (mWantStop) {
                    loadMoviesListenerHelper.onLoadMoviesInterrupted();
                    return;
                }

                // Download the poster now
                int height = ScreenShapeHelper.get(context).height;
                int width = (int) (context.getResources().getFraction(R.fraction.movie_list_item_poster, height, 1) + .5F);
                int border = context.getResources().getDimensionPixelSize(R.dimen.movie_list_item_posterBorder);
                height -= border * 2;
                width -= border * 2;
                // Glide insists this is done on the main thread
                final int finalWidth = width;
                final int finalHeight = height;
                HandlerUtil.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(context)
                                .load(movie.posterUri)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target,
                                                                   boolean isFromMemoryCache,
                                                                   boolean isFirstResource) {
                                        if (!(resource instanceof GlideBitmapDrawable)) return false;
                                        GlideBitmapDrawable glideBitmapDrawable = (GlideBitmapDrawable) resource;
                                        Palette.from(glideBitmapDrawable.getBitmap()).generate(new Palette.PaletteAsyncListener() {
                                            @Override
                                            public void onGenerated(Palette p) {
                                                movie.color = p.getDarkVibrantColor(context.getColor(R.color.movie_list_bg));
                                            }
                                        });
                                        return false;
                                    }
                                })
                                .preload(finalWidth, finalHeight);
                    }
                });

                i++;
                loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle);
            }

        } finally {
            // Releasing the high-bandwidth network
            if (highBandwidthNetworkSuccess) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                connectivityManager.bindProcessToNetwork(null);
            }
        }

        // 3/ Save everything to the local db
        persist(context, movies);

        MainPrefs.get(context).putLastUpdateDate(System.currentTimeMillis());
        loadMoviesListenerHelper.resetError();
        loadMoviesListenerHelper.onLoadMoviesSuccess();

        // 4/ Show a notification
        ArrayList<String> newMovieTitles = new ArrayList<>(movies.size());
        for (Movie movie : movies) {
            if (movie.isNew) newMovieTitles.add(movie.localTitle);
        }
        if (!newMovieTitles.isEmpty()) showNotification(context, newMovieTitles);
    }

    private void persist(Context context, SortedSet<Movie> movies) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        // First, delete all the showtimes
        operations.add(ContentProviderOperation.newDelete(ShowtimeColumns.CONTENT_URI).build());

        // Get a map of theater public ids to internal ids
        HashMap<String, Long> theaterIds = new HashMap<>();
        try (TheaterCursor cursor = new TheaterSelection().query(context)) {
            while (cursor.moveToNext()) {
                theaterIds.put(cursor.getPublicId(), cursor.getId());
            }
        }

        // Get a map of movie public ids to internal ids
        HashMap<String, Long> movieIds = new HashMap<>();
        try (MovieCursor cursor = new MovieSelection().query(context)) {
            while (cursor.moveToNext()) {
                movieIds.put(cursor.getPublicId(), cursor.getId());
            }
        }

        for (Movie movie : movies) {
            // Movie
            if (movie.isNew) {
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
                        .putSynopsis(movie.synopsis)
                        .putColor(movie.color);
                operations.add(ContentProviderOperation.newInsert(MovieColumns.CONTENT_URI).withValues(movieValues.values()).build());
            }

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
                    if (!movie.isNew) {
                        showtimeValues.putMovieId(movieIds.get(movie.id));
                    }

                    ContentProviderOperation.Builder operationBuilder = ContentProviderOperation.newInsert(ShowtimeColumns.CONTENT_URI)
                            .withValues(showtimeValues.values());
                    if (movie.isNew) {
                        operationBuilder.withValueBackReference(ShowtimeColumns.MOVIE_ID, movieIdResultIndex);
                    }

                    ContentProviderOperation operation = operationBuilder.build();
                    operations.add(operation);
                }
            }
        }

        // Delete movies that have no show times
        MovieSelection movieSelection = new MovieSelection();
        movieSelection.addRaw("(select "
                + " count(" + ShowtimeColumns.TABLE_NAME + "." + ShowtimeColumns._ID + ")"
                + " from " + ShowtimeColumns.TABLE_NAME
                + " where " + ShowtimeColumns.TABLE_NAME + "." + ShowtimeColumns.MOVIE_ID
                + " = " + MovieColumns.TABLE_NAME + "." + MovieColumns._ID
                + " ) = 0");
        operations.add(ContentProviderOperation.newDelete(MovieColumns.CONTENT_URI)
                .withSelection(movieSelection.sel(), movieSelection.args()).build());

        // Apply the batch of operations
        try {
            context.getContentResolver().applyBatch(CineTodayProvider.AUTHORITY, operations);
        } catch (Exception e) {
            Log.e(e, "Could not apply batch");
        }
    }

    private void showNotification(Context context, ArrayList<String> newMovieTitles) {
        Log.d();
        Notification.Builder mainNotifBuilder = new Notification.Builder(context);

        // Small icon
        mainNotifBuilder.setSmallIcon(R.drawable.ic_notif);

        // Title
        String title = context.getString(R.string.notif_title);
        mainNotifBuilder.setContentTitle(title);

        // Text
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        String text = TextUtils.join(", ", newMovieTitles);
        bigTextStyle.bigText(text);
        mainNotifBuilder.setStyle(bigTextStyle);

        // Content intent
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        mainNotifBuilder.setContentIntent(mainActivityPendingIntent);

//        // Wear specifics
        Notification.WearableExtender wearableExtender = new Notification.WearableExtender();
//        wearableExtender.setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notif));
//        wearableExtender
//                .addAction(new Notification.Action.Builder(Icon.createWithResource(context, R.mipmap.ic_launcher), "Open", mainActivityPendingIntent).build());
//        wearableExtender.setContentAction(0);
//
//

        wearableExtender.setHintContentIntentLaunchesActivity(true);
        Notification.Builder wearableNotifBuilder = wearableExtender.extend(mainNotifBuilder);
        Notification notification = wearableNotifBuilder.build();
//        Notification notification = mainNotifBuilder.build();


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void startLoadMoviesIntentService(Context context) {
        Intent intent = new Intent(context, LoadMoviesIntentService.class);
        intent.setAction(LoadMoviesIntentService.ACTION_LOAD_MOVIES);
        context.startService(intent);
    }
}
