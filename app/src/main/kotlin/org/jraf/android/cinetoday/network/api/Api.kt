/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import android.util.Base64
import androidx.annotation.WorkerThread
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import okhttp3.CacheControl
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.model.showtime.Showtime
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.network.api.codec.movie.MovieCodec
import org.jraf.android.cinetoday.network.api.codec.showtime.ShowtimeCodec
import org.jraf.android.cinetoday.network.api.codec.theater.TheaterCodec
import org.jraf.android.cinetoday.network.api.graphql.MovieShowtimesQuery
import org.jraf.android.cinetoday.util.datetime.atMidnight
import org.jraf.android.cinetoday.util.datetime.nextDay
import org.jraf.android.cinetoday.util.sha1.sha1
import org.jraf.android.util.log.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch

class Api(
    private val cachingOkHttpClient: OkHttpClient,
    private val apolloClient: ApolloClient,
    private val movieCodec: MovieCodec,
    private val showTimeCodec: ShowtimeCodec,
    private val theaterCodec: TheaterCodec
) {

    private fun <T> ApolloCall<T>.blockingAwait(): Response<T> {
        val countDownLatch = CountDownLatch(1)
        var callbackResponse: Response<T>? = null
        var callbackException: ApolloException? = null

        enqueue(object : ApolloCall.Callback<T>() {
            override fun onResponse(response: Response<T>) {
                callbackResponse = response
                countDownLatch.countDown()
            }

            override fun onFailure(e: ApolloException) {
                callbackException = e
                countDownLatch.countDown()
            }
        })
        countDownLatch.await()
        callbackException?.let { throw it }
        return callbackResponse!!
    }

    @WorkerThread
    fun getMovieList(movies: MutableSet<Movie>, theaterId: String, date: Date) {
        val todayAtMidnight = date.atMidnight()
        val tomorrowAtMidnight = todayAtMidnight.nextDay()
        val response: Response<MovieShowtimesQuery.Data> = apolloClient
            .query(MovieShowtimesQuery(theaterCodec.toGraphqlTheaterId(theaterId), todayAtMidnight, tomorrowAtMidnight))
            .blockingAwait()
        if (response.hasErrors()) throw ParseException()
        for (movieShowtimeEdge in response.data!!.movieShowtimeList!!.edges!!) {
            val graphqlMovie = movieShowtimeEdge!!.node!!.movie!!
            // Reuse existing movie if present (from previous Theater call), otherwise create one now
            val movie = movies.find { it.id == graphqlMovie.id } ?: Movie().apply {
                movieCodec.fill(this, graphqlMovie)
            }

            val graphqlShowtimes = movieShowtimeEdge.node!!.showtimes!!
            val showtimes = mutableListOf<Showtime>()
            for (graphqlShowtime in graphqlShowtimes) {
                showtimes += showTimeCodec.convert(graphqlShowtime!!, movieId = graphqlMovie.id, theaterId = theaterId)
            }
            movie.todayShowtimes[theaterId] = showtimes
            movies.add(movie)
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
            for (i in 0 until len) {
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
        Log.d("url=$url")
        val method = url.encodedPathSegments.lastOrNull()
        val paramList = url.encodedQuery
        val signatureBase = method + paramList + SIGNATURE_SECRET
        val signatureSha1 = sha1(signatureBase)
        val signatureBase64 = Base64.encodeToString(signatureSha1, Base64.NO_WRAP)

        Log.d("method=$method paramList=$paramList signatureBase=$signatureBase signatureBase64=$signatureBase64")

        val urlWithSignature = url.newBuilder()
            .addQueryParameter(QUERY_SIGNATURE_KEY, signatureBase64)
            .build()
        Log.d("urlWithSignature=$urlWithSignature")

        val urlBuilder = Request.Builder().url(urlWithSignature)
        if (!useCache) urlBuilder.cacheControl(CacheControl.FORCE_NETWORK)
        val request = urlBuilder.build()
        val response = cachingOkHttpClient.newCall(request).execute()
        return response.body?.string() ?: ""
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
            .addQueryParameter(QUERY_SED_DATE_KEY, SED_DATE_FORMAT.format(Date()))
    }

    companion object {
        val MAIN_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private val SED_DATE_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.US)

        private const val SIGNATURE_SECRET = "1a1ed8c1bed24d60ae3472eed1da33eb"

        private const val SCHEME = "http"
        private const val HOST = "api.allocine.fr"
        private const val PATH_REST = "rest"
        private const val PATH_V3 = "v3"

        private const val QUERY_PARTNER_KEY = "partner"
        private const val QUERY_PARTNER_VALUE = "100ED1DA33EB"

        private const val QUERY_FORMAT_KEY = "format"
        private const val QUERY_FORMAT_VALUE = "json"

        private const val QUERY_SIGNATURE_KEY = "sig"

        private const val QUERY_SED_DATE_KEY = "sed"

        // GraphQL
        const val GRAPHQL_URL = "https://graph.allocine.fr/v1/mobile"
        const val HEADER_AUTHORIZATION_KEY = "Authorization"

        // Unfortunately this will expire in June 2023 (see https://jwt.io/)
        const val HEADER_AUTHORIZATION_VALUE =
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE1NzE4NDM5NTcsInVzZXJuYW1lIjoiYW5vbnltb3VzIiwiYXBwbGljYXRpb25fbmFtZSI6Im1vYmlsZSIsInV1aWQiOiJmMDg3YTZiZi05YTdlLTQ3YTUtYjc5YS0zMDNiNWEwOWZkOWYiLCJzY29wZSI6bnVsbCwiZXhwIjoxNjg2NzAwNzk5fQ.oRS_jzmvfFAQ47wH0pU3eKKnlCy93FhblrBXxPZx2iwUUINibd70MBkI8C8wmZ-AeRhVCR8kavW8dLIqs5rUfA6piFwdYpt0lsAhTR417ABOxVrZ8dv0FX3qg1JLIzan-kSN4TwUZ3yeTjls0PB3OtSBKzoywGvFAu2jMYG1IZyBjxnkfi1nf1qGXbYsBfEaSjrj-LDV6Jjq_MPyMVvngNYKWzFNyzVAKIpAZ-UzzAQujAKwNQcg2j3Y3wfImydZEOW_wqkOKCyDOw9sWCWE2D-SObbFOSrjqKBywI-Q9GlfsUz-rW7ptea_HzLnjZ9mymXc6yq7KMzbgG4W9CZd8-qvHejCXVN9oM2RJ7Xrq5tDD345NoZ5plfCmhwSYA0DSZLw21n3SL3xl78fMITNQqpjlUWRPV8YqZA1o-UNgwMpOWIoojLWx-XBX33znnWlwSa174peZ1k60BQ3ZdCt9A7kyOukzvjNn3IOIVVgS04bBxl4holc5lzcEZSgjoP6dDIEJKib1v_AAxA34alVqWngeDYhd0wAO-crYW1HEd8ogtCoBjugwSy7526qrh68mSJxY66nr4Cle21z1wLC5lOsex0FbuwvOeFba0ycaI8NJPTUriOdvtHAjhDRSem4HjypGvKs5AzlZ3LAJACCHICNwo3NzYjcxfT4Wo1ur-M"
        const val HEADER_AC_AUTH_TOKEN_KEY = "AC-Auth-Token"

        // This value was found by looking at the official app's network
        const val HEADER_AC_AUTH_TOKEN_VALUE =
            "fRCoWAfDyLs:APA91bF0V8MX1qMRDgG51FLWSZOYzec9vqTR74iWZdcrRUs-VeDF1LZoRmHcDhdNOr-7Z0WNnUi5TBTncvyRse4XbkpiEjvMgvVpBgAmeMMtW6wa8bKEcEUuXEw6xbW3ddhnrrpCYOrx"


        // Theater search
        private const val PATH_SEARCH = "search"

        private const val QUERY_COUNT_KEY = "count"
        private const val QUERY_COUNT_VALUE = "15"

        private const val QUERY_FILTER_KEY = "filter"
        private const val QUERY_FILTER_VALUE = "theater"

        private const val QUERY_QUERY_KEY = "q"
    }
}
