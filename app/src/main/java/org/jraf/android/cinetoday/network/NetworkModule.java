/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.network;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import org.jraf.android.cinetoday.BuildConfig;
import org.jraf.android.cinetoday.network.api.Api;
import org.jraf.android.cinetoday.network.api.codec.movie.MovieCodec;
import org.jraf.android.cinetoday.network.api.codec.showtime.ShowtimeCodec;
import org.jraf.android.cinetoday.network.api.codec.theater.TheaterCodec;

@Module
public class NetworkModule {
    private static final String CACHE_DIRECTORY_NAME = "http";
    private static final long CACHE_SIZE_B = 2 * 1024 * 1024;
    private static final int TIMEOUT_S = 30;

    @Singleton
    @Provides
    public Api provideApi(@Named("CachingOkHttpClient") OkHttpClient cachingOkHttpClient, MovieCodec movieCodec, ShowtimeCodec showtimeCodec,
                          TheaterCodec theaterCodec) {
        return new Api(cachingOkHttpClient, movieCodec, showtimeCodec, theaterCodec);
    }

    @Singleton
    @Provides
    public MovieCodec provideMovieCodec() {
        return new MovieCodec();
    }

    @Singleton
    @Provides
    public ShowtimeCodec provideShowtimeCodec() {
        return new ShowtimeCodec();
    }

    @Singleton
    @Provides
    public TheaterCodec provideTheaterCodec() {
        return new TheaterCodec();
    }

    @Singleton
    @Provides
    @Named("CachingOkHttpClient")
    public OkHttpClient provideCachingOkHttpClient(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(TIMEOUT_S, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT_S, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT_S, TimeUnit.SECONDS);
        File httpCacheDir = new File(context.getCacheDir(), CACHE_DIRECTORY_NAME);
        builder.cache(new Cache(httpCacheDir, CACHE_SIZE_B));

        if (BuildConfig.DEBUG) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.52", 8888)));
        }

        return builder.build();
    }

    @Singleton
    @Provides
    @Named("NotCachingOkHttpClient")
    public OkHttpClient provideNotCachingOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(TIMEOUT_S, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT_S, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT_S, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.52", 8888)));
        }

        return builder.build();
    }
}
