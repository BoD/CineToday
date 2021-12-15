/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.network

import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.apollographql.apollo3.network.okHttpClient
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jraf.android.cinetoday.BuildConfig
import org.jraf.android.cinetoday.network.api.Api
import org.jraf.android.cinetoday.network.api.codec.movie.MovieCodec
import org.jraf.android.cinetoday.network.api.codec.showtime.ShowtimeCodec
import org.jraf.android.cinetoday.network.api.codec.theater.TheaterCodec
import org.jraf.android.cinetoday.network.api.graphql.type.DateInterval
import org.jraf.android.cinetoday.network.api.graphql.type.DateTime
import org.jraf.android.util.log.Log
import java.io.File
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


@Module
class NetworkModule {

    companion object {
        private const val CACHE_DIRECTORY_NAME = "http"
        private const val CACHE_SIZE_B = (2 * 1024 * 1024).toLong()
        private const val TIMEOUT_S = 30
        private const val HEADER_ACCEPT = "Accept"
    }

    @Singleton
    @Provides
    fun provideApi(
        @Named("CachingOkHttpClient") cachingOkHttpClient: OkHttpClient,
        apolloClient: ApolloClient,
        movieCodec: MovieCodec,
        showtimeCodec: ShowtimeCodec,
        theaterCodec: TheaterCodec
    ): Api {
        return Api(cachingOkHttpClient, apolloClient, movieCodec, showtimeCodec, theaterCodec)
    }

    @Singleton
    @Provides
    fun provideMovieCodec(): MovieCodec {
        return MovieCodec()
    }

    @Singleton
    @Provides
    fun provideShowtimeCodec(): ShowtimeCodec {
        return ShowtimeCodec()
    }

    @Singleton
    @Provides
    fun provideTheaterCodec(): TheaterCodec {
        return TheaterCodec()
    }

    @Singleton
    @Provides
    fun provideApolloClient(@Named("CachingOkHttpClient") cachingOkHttpClient: OkHttpClient): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl(Api.GRAPHQL_URL)
            .addHttpHeader(
                Api.HEADER_AUTHORIZATION_KEY,
                Api.HEADER_AUTHORIZATION_VALUE
            )
            .addHttpHeader(
                Api.HEADER_AC_AUTH_TOKEN_KEY,
                Api.HEADER_AC_AUTH_TOKEN_VALUE
            )
            .addCustomScalarAdapter(DateInterval.type, object : Adapter<Long> {
                override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Long {
                    val isoDurationStr = reader.nextString()
                    val duration = Duration.parse(isoDurationStr)
                    return duration.get(ChronoUnit.SECONDS)
                }

                override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Long) {
                    throw UnsupportedOperationException()
                }
            })
            .addCustomScalarAdapter(DateTime.type, object : Adapter<Date> {
                private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

                override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date {
                    val isoDateStr = reader.nextString()!!
                    return DATE_FORMAT.parse(isoDateStr)!!
                }

                override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date) {
                    writer.value(DATE_FORMAT.format(value))
                }
            })
            .okHttpClient(cachingOkHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("CachingOkHttpClient")
    fun provideCachingOkHttpClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
        val httpCacheDir = File(context.cacheDir, CACHE_DIRECTORY_NAME)
        builder.cache(Cache(httpCacheDir, CACHE_SIZE_B))

        // Proxy
//        if (BuildConfig.DEBUG) builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("192.168.3.26", 8888)))

        // Logs
        if (BuildConfig.DEBUG_LOGS) builder.addInterceptor(
            HttpLoggingInterceptor { message -> Log.d(message) }.setLevel(HttpLoggingInterceptor.Level.BASIC)
        )

        // Ignore any SSL problem
        builder.ignoreAllSSLErrors()

        return builder.build()
    }

    @Singleton
    @Provides
    @Named("NotCachingOkHttpClient")
    fun provideNotCachingOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)

        // Proxy
//        if (BuildConfig.DEBUG) builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("192.168.3.20", 8888)))

        // Headers
        builder.addInterceptor { chain ->
            val request = chain.request()
            chain.proceed(
                request.newBuilder()
                    // Accept
                    .header(HEADER_ACCEPT, "image/webp")
                    .build()
            )
        }

        // Logs
        if (BuildConfig.DEBUG_LOGS) builder.addInterceptor(
            HttpLoggingInterceptor { message -> Log.d(message) }.setLevel(HttpLoggingInterceptor.Level.BASIC)
        )

        // Ignore any SSL problem
        builder.ignoreAllSSLErrors()

        return builder.build()
    }
}

private fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
    }

    val insecureSocketFactory = SSLContext.getInstance("TLS").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}
