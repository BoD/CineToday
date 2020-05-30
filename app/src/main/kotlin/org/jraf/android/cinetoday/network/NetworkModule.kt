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
import org.jraf.android.util.log.Log
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


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
        @Named("CachingOkHttpClient") cachingOkHttpClient: OkHttpClient, movieCodec: MovieCodec,
        showtimeCodec: ShowtimeCodec,
        theaterCodec: TheaterCodec
    ): Api {
        return Api(cachingOkHttpClient, movieCodec, showtimeCodec, theaterCodec)
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
    @Named("CachingOkHttpClient")
    fun provideCachingOkHttpClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_S.toLong(), TimeUnit.SECONDS)
        val httpCacheDir = File(context.cacheDir, CACHE_DIRECTORY_NAME)
        builder.cache(Cache(httpCacheDir, CACHE_SIZE_B))

        // Proxy
//        if (BuildConfig.DEBUG) builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress("192.168.3.20", 8888)))

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
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.d(message)
                }
            }).setLevel(HttpLoggingInterceptor.Level.HEADERS)
        )


        return builder.build()
    }
}
