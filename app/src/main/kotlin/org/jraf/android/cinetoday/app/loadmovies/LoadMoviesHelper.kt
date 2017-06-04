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
package org.jraf.android.cinetoday.app.loadmovies

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.support.annotation.WorkerThread
import android.support.v7.graphics.Palette
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.app.main.MainActivity
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.network.api.Api
import org.jraf.android.cinetoday.prefs.MainPrefs
import org.jraf.android.util.handler.HandlerUtil
import org.jraf.android.util.log.Log
import org.jraf.android.util.ui.screenshape.ScreenShapeHelper
import java.util.Date
import java.util.SortedSet
import java.util.TreeSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class LoadMoviesHelper(private val mContext: Context, private val mMainPrefs: MainPrefs, private val mApi: Api, private val mAppDatabase: AppDatabase) {
    companion object {
        private const val NOTIFICATION_ID = 0
        private const val MIN_BANDWIDTH_KBPS = 320
    }

    @Volatile private var mWantStop: Boolean = false

    fun setWantStop(wantStop: Boolean) {
        mWantStop = wantStop
    }

    @WorkerThread
    private fun requestHighBandwidthNetwork(timeout: Long, unit: TimeUnit): Boolean {
        val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork == null || connectivityManager.getNetworkCapabilities(activeNetwork).linkDownstreamBandwidthKbps < MIN_BANDWIDTH_KBPS) {
            val res = AtomicBoolean(false)
            val countDownLatch = CountDownLatch(1)

            // Request a high-bandwidth network
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    connectivityManager.unregisterNetworkCallback(this)
                    res.set(connectivityManager.bindProcessToNetwork(network))
                    countDownLatch.countDown()
                }
            }
            val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

            Log.d("Requesting a high-bandwidth network")
            connectivityManager.requestNetwork(request, networkCallback)
            try {
                countDownLatch.await(timeout, unit)
            } catch (ignored: InterruptedException) {
            }

            return res.get()
        } else {
            // Already on a high-bandwidth network
            return true
        }
    }

    @WorkerThread
    @Throws(Exception::class)
    internal fun loadMovies() {
        mWantStop = false
        val loadMoviesListenerHelper = LoadMoviesListenerHelper.get()
        loadMoviesListenerHelper.onLoadMoviesStarted()

        // 0/ Try to connect to a fast network
        val highBandwidthNetworkSuccess = requestHighBandwidthNetwork(10, TimeUnit.SECONDS)
        Log.d("Fast network success=%s", highBandwidthNetworkSuccess)

        val movies = TreeSet<Movie>()
        try {
            // 1/ Retrieve list of movies (including showtimes), for all the theaters
            try {
                mAppDatabase.theaterDao.allTheaters().forEach { (theaterId) ->
                    mApi.getMovieList(movies, theaterId, Date())

                    if (mWantStop) {
                        loadMoviesListenerHelper.onLoadMoviesInterrupted()
                        return
                    }
                }
            } catch (e: Exception) {
                Log.e(e, "Could not load movies")
                loadMoviesListenerHelper.onLoadMoviesError(e)
                throw e
            }

            if (mWantStop) {
                loadMoviesListenerHelper.onLoadMoviesInterrupted()
                return
            }

            // 2/ Retrieve more details about each movie
            val size = movies.size
            var i = 0
            for (movie in movies) {
                loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle)

                // Check if we already have info for this movie
                if (mAppDatabase.movieDao.countMovieById(movie.id) == 0) {
                    movie.isNew = true

                    // Get movie info
                    try {
                        mApi.getMovieInfo(movie)
                        Log.d(movie.toString())
                    } catch (e: Exception) {
                        Log.e(e, "Could not load movie info: movie = %s", movie)
                        loadMoviesListenerHelper.onLoadMoviesError(e)
                        throw e
                    }
                }

                if (mWantStop) {
                    loadMoviesListenerHelper.onLoadMoviesInterrupted()
                    return
                }

                // Download the poster now
                var height = ScreenShapeHelper.get(mContext).height
                var width = (mContext.resources.getFraction(R.fraction.movie_list_item_poster, height, 1) + .5f).toInt()
                val border = mContext.resources.getDimensionPixelSize(R.dimen.movie_list_item_posterBorder)
                height -= border * 2
                width -= border * 2
                // Glide insists this is done on the main thread
                val finalWidth = width
                val finalHeight = height
                HandlerUtil.runOnUiThread {
                    Glide.with(mContext)
                            .load(movie.posterUri)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .listener(object : RequestListener<String, GlideDrawable> {
                                override fun onException(e: Exception, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                    return false
                                }

                                override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>,
                                                             isFromMemoryCache: Boolean,
                                                             isFirstResource: Boolean): Boolean {
                                    if (resource !is GlideBitmapDrawable) return false
                                    Palette.from(resource.bitmap)
                                            .generate { palette -> movie.color = palette.getDarkVibrantColor(mContext.getColor(R.color.movie_list_bg)) }
                                    return false
                                }
                            })
                            .preload(finalWidth, finalHeight)
                }

                i++
                loadMoviesListenerHelper.onLoadMoviesProgress(i, size, movie.localTitle)
            }

        } finally {
            // Release the high-bandwidth network
            if (highBandwidthNetworkSuccess) {
                val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.bindProcessToNetwork(null)
            }
        }

        // 3/ Save everything to the local db
        persist(movies)

        mMainPrefs.putLastUpdateDate(System.currentTimeMillis())
        loadMoviesListenerHelper.resetError()
        loadMoviesListenerHelper.onLoadMoviesSuccess()

        // 4/ Show a notification
        val newMovieTitles = ArrayList<String>(movies.size)
        for (movie in movies) {
            if (movie.isNew) newMovieTitles.add(movie.localTitle)
        }
        if (!newMovieTitles.isEmpty()) showNotification(newMovieTitles)
    }

    private fun persist(movies: SortedSet<Movie>) {
        // Delete all showtimes and movies
        mAppDatabase.showtimeDao.deleteAll()
        mAppDatabase.movieDao.deleteAll()

        // Insert movies
        mAppDatabase.movieDao.insert(movies.toList())

        // Insert showtimes
        for (movie in movies) {
            for (entry in movie.todayShowtimes) {
                mAppDatabase.showtimeDao.insert(entry.value)
            }
        }
    }

    private fun showNotification(newMovieTitles: ArrayList<String>) {
        Log.d()
        val mainNotifBuilder = Notification.Builder(mContext)

        // Small icon
        mainNotifBuilder.setSmallIcon(R.drawable.ic_notif)

        // Title
        val title = mContext.getString(R.string.notif_title)
        mainNotifBuilder.setContentTitle(title)

        // Text
        val bigTextStyle = Notification.BigTextStyle()
        val text = TextUtils.join(", ", newMovieTitles)
        bigTextStyle.bigText(text)
        mainNotifBuilder.setStyle(bigTextStyle)

        // Content intent
        val mainActivityIntent = Intent(mContext, MainActivity::class.java)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val mainActivityPendingIntent = PendingIntent.getActivity(mContext, 0, mainActivityIntent, 0)
        mainNotifBuilder.setContentIntent(mainActivityPendingIntent)

        //        // Wear specifics
        val wearableExtender = Notification.WearableExtender()
        //        wearableExtender.setBackground(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_notif));
        //        wearableExtender
        //                .addAction(new Notification.Action.Builder(Icon.createWithResource(mContext, R.mipmap.ic_launcher), "Open", mainActivityPendingIntent).build());
        //        wearableExtender.setContentAction(0);
        //
        //

        wearableExtender.hintContentIntentLaunchesActivity = true
        val wearableNotifBuilder = wearableExtender.extend(mainNotifBuilder)
        val notification = wearableNotifBuilder.build()
        //        Notification notification = mainNotifBuilder.build();


        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun startLoadMoviesIntentService() {
        val intent = Intent(mContext, LoadMoviesIntentService::class.java)
        intent.action = LoadMoviesIntentService.ACTION_LOAD_MOVIES
        mContext.startService(intent)
    }
}
