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
package org.jraf.android.cinetoday.mobile.api.http;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.content.Context;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class HttpUtil {
    private static final long CACHE_SIZE_BYTES = 10 * 1024 * 1024;

    private static OkHttpClient sOkHttpClient;

    public static OkHttpClient getOkHttpClient(Context context) {
        if (sOkHttpClient == null) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            okHttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
            okHttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
            File httpCacheDir = new File(context.getCacheDir(), "http");
            okHttpClientBuilder.cache(new Cache(httpCacheDir, CACHE_SIZE_BYTES));
            sOkHttpClient = okHttpClientBuilder.build();
        }
        return sOkHttpClient;
    }

}
