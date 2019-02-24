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
package org.jraf.android.cinetoday.network.api

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.annotation.WorkerThread
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.network.api.codec.movie.MovieCodec
import org.jraf.android.cinetoday.network.api.codec.showtime.ShowtimeCodec
import org.jraf.android.cinetoday.network.api.codec.theater.TheaterCodec
import org.jraf.android.util.log.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class Api(
    private val cachingOkHttpClient: OkHttpClient,
    private val movieCodec: MovieCodec,
    private val showTimeCodec: ShowtimeCodec,
    private val theaterCodec: TheaterCodec
) {

    @WorkerThread
    @Throws(IOException::class, ParseException::class)
    fun getMovieList(movies: MutableSet<Movie>, theaterId: String, date: Date) {
        val url = getBaseBuilder(PATH_SHOWTIMELIST)
            .addQueryParameter(QUERY_THEATERS_KEY, theaterId)
            .addQueryParameter(QUERY_DATE_KEY, SIMPLE_DATE_FORMAT.format(date))
            .build()
        val jsonStr = call(url, false)
        parseMovieList(movies, jsonStr, theaterId, date)
    }

    @VisibleForTesting(otherwise = PRIVATE)
    @Throws(ParseException::class)
    fun parseMovieList(movies: MutableSet<Movie>, jsonStr: String, theaterId: String, date: Date) {
        try {
            val jsonRoot = JSONObject(jsonStr)
            val jsonFeed = jsonRoot.getJSONObject("feed")
            val jsonTheaterShowtimes = jsonFeed.getJSONArray("theaterShowtimes")
            val jsonTheaterShowtime = jsonTheaterShowtimes.getJSONObject(0)
            val jsonMovieShowtimes = jsonTheaterShowtime.optJSONArray("movieShowtimes") ?: return
            val len = jsonMovieShowtimes.length()
            for (i in 0 until len) {
                val jsonMovieShowtime = jsonMovieShowtimes.getJSONObject(i)
                val jsonOnShow = jsonMovieShowtime.getJSONObject("onShow")
                val jsonMovie = jsonOnShow.getJSONObject("movie")
                var movie = Movie()

                // Movie (does not include showtimes, only the movie details)
                movieCodec.fill(movie, jsonMovie)
                // See if the movie was already in the set, if yes use this one, so the showtimes are merged
                if (movies.contains(movie)) {
                    // Already in the set: find it
                    for (m in movies) {
                        if (m == movie) {
                            // Found it: discard the new one, use the old one instead
                            movie = m
                            break
                        }
                    }
                }

                // Showtimes
                showTimeCodec.fill(movie, jsonMovieShowtime, theaterId, date)

                // If there is no showtimes for today, skip the movie
                if (movie.todayShowtimes.size == 0) {
                    Log.w("Movie %s has no showtimes: skip it", movie.id)
                } else {
                    movies.add(movie)
                }
            }
        } catch (e: JSONException) {
            throw ParseException(e)
        }

    }

    @WorkerThread
    @Throws(IOException::class, ParseException::class)
    fun getMovieInfo(movie: Movie) {
        val url = getBaseBuilder(PATH_MOVIE)
            .addQueryParameter(QUERY_STRIPTAGS_KEY, QUERY_STRIPTAGS_VALUE)
            .addQueryParameter(QUERY_CODE_KEY, movie.id)
            .build()
        val jsonStr = call(url, true)
        try {
            val jsonRoot = JSONObject(jsonStr)
            val jsonMovie = jsonRoot.getJSONObject("movie")
            movieCodec.fill(movie, jsonMovie)
        } catch (e: JSONException) {
            throw ParseException(e)
        }

    }

    @WorkerThread
    @Throws(IOException::class, ParseException::class)
    fun searchTheaters(query: String): List<Theater> {
        if (query.length < 3) return ArrayList()

        val url = getBaseBuilder(PATH_SEARCH)
            .addQueryParameter(QUERY_COUNT_KEY, QUERY_COUNT_VALUE)
            .addQueryParameter(QUERY_FILTER_KEY, QUERY_FILTER_VALUE)
            .addQueryParameter(QUERY_QUERY_KEY, query)
            .build()
        val jsonStr = call(url, true)
        try {
            val jsonRoot = JSONObject(jsonStr)
            val jsonFeed = jsonRoot.getJSONObject("feed")
            val totalResults = jsonFeed.getInt("totalResults")
            if (totalResults == 0) return ArrayList()

            val jsonTheaters = jsonFeed.getJSONArray("theater")
            val len = jsonTheaters.length()
            val res = mutableListOf<Theater>()
            for (i in 0..len - 1) {
                val jsonTheater = jsonTheaters.getJSONObject(i)
                val theater = Theater(
                    id = "",
                    name = "",
                    address = "",
                    pictureUri = null
                )

                // Theater
                theaterCodec.fill(theater, jsonTheater)
                res.add(theater)
            }
            return res
        } catch (e: JSONException) {
            throw ParseException(e)
        }
    }

    @WorkerThread
    @Throws(IOException::class)
    private fun call(url: HttpUrl, useCache: Boolean): String {
        Log.d("url=%s", url)
        val urlBuilder = Request.Builder().url(url)
        if (!useCache) urlBuilder.cacheControl(CacheControl.FORCE_NETWORK)
        val request = urlBuilder.build()
        val response = cachingOkHttpClient.newCall(request).execute()
        return response.body()?.string() ?: ""
    }

    private fun getBaseBuilder(path: String): HttpUrl.Builder {
        return HttpUrl.Builder()
            .scheme(SCHEME)
            .host(HOST)
            .addPathSegment(PATH_REST)
            .addPathSegment(PATH_V3)
            .addPathSegment(path)
            .addQueryParameter(QUERY_PARTNER_KEY, QUERY_PARTNER_VALUE)
            .addQueryParameter(QUERY_FORMAT_KEY, QUERY_FORMAT_VALUE)
    }

    companion object {
        val SIMPLE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        private const val SCHEME = "http"
        private const val HOST = "api.allocine.fr"
        private const val PATH_REST = "rest"
        private const val PATH_V3 = "v3"
        private const val QUERY_PARTNER_KEY = "partner"
        private const val QUERY_PARTNER_VALUE = "YW5kcm9pZC12M3M"
        private const val QUERY_FORMAT_KEY = "format"
        private const val QUERY_FORMAT_VALUE = "json"
        private const val PATH_SHOWTIMELIST = "showtimelist"
        private const val QUERY_THEATERS_KEY = "theaters"
        private const val QUERY_DATE_KEY = "date"
        private const val PATH_MOVIE = "movie"
        private const val QUERY_CODE_KEY = "code"
        private const val QUERY_STRIPTAGS_KEY = "striptags"
        private const val QUERY_STRIPTAGS_VALUE = "true"
        private const val PATH_SEARCH = "search"
        private const val QUERY_COUNT_KEY = "count"
        private const val QUERY_COUNT_VALUE = "15"
        private const val QUERY_QUERY_KEY = "q"
        private const val QUERY_FILTER_KEY = "filter"
        private const val QUERY_FILTER_VALUE = "theater"
    }
}
