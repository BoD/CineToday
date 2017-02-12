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
package org.jraf.android.cinetoday.glide;

import java.io.InputStream;

import android.content.Context;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.module.GlideModule;

import okhttp3.OkHttpClient;

import org.jraf.android.cinetoday.util.http.HttpUtil;

public class CineTodayGlideModule implements GlideModule {
    private static final int CACHE_SIZE_B = 5 * 1024 * 1024;
    private static final String CACHE_DIRECTORY_NAME = "images";

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // Disk cache
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, CACHE_DIRECTORY_NAME, CACHE_SIZE_B));

        // Memory cache / bitmap pool
        MemorySizeCalculator memorySizeCalculator = new MemorySizeCalculator(context);
        builder.setMemoryCache(new LruResourceCache(memorySizeCalculator.getMemoryCacheSize()));
        builder.setBitmapPool(new LruBitmapPool(memorySizeCalculator.getBitmapPoolSize()));

        // Decode format - RGB565 is enough
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlResizeModelLoaderFactory(HttpUtil.getNotCachingOkHttpClient(context)));
    }


    /**
     * Manipulate image uris to use the 'zimage.io' service that serves resized images.
     */
    private static class OkHttpUrlResizeModelLoaderFactory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private final OkHttpClient mOkHttpClient;

        public OkHttpUrlResizeModelLoaderFactory(OkHttpClient okHttpClient) {
            mOkHttpClient = okHttpClient;
        }

        @Override
        public ModelLoader<GlideUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new OkHttpUrlLoader(mOkHttpClient) {
                @Override
                public DataFetcher<InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
                    if (model != null) {
                        String uriStr = model.toStringUrl();
                        Uri uri = Uri.parse("http://edge.zimage.io");
                        uri = uri.buildUpon()
                                .appendQueryParameter("url", uriStr)
                                .appendQueryParameter("w", String.valueOf(width))
                                .appendQueryParameter("h", String.valueOf(height))
                                .appendQueryParameter("mode", "crop")
//                                .appendQueryParameter("format", "webp")
                                .build();
                        model = new GlideUrl(uri.toString());
                    }
                    return super.getResourceFetcher(model, width, height);
                }
            };
        }

        @Override
        public void teardown() {}
    }
}
