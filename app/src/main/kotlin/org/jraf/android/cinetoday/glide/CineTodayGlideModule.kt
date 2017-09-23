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
package org.jraf.android.cinetoday.glide

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GenericLoaderFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.module.GlideModule
import okhttp3.OkHttpClient
import org.jraf.android.cinetoday.dagger.Components
import java.io.InputStream

class CineTodayGlideModule : GlideModule {
    companion object {
        private const val CACHE_SIZE_B = 5 * 1024 * 1024
        private const val CACHE_DIRECTORY_NAME = "images"
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Disk cache
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, CACHE_DIRECTORY_NAME, CACHE_SIZE_B))

        // Memory cache / bitmap pool
        val memorySizeCalculator = MemorySizeCalculator(context)
        builder.setMemoryCache(LruResourceCache(memorySizeCalculator.memoryCacheSize))
        builder.setBitmapPool(LruBitmapPool(memorySizeCalculator.bitmapPoolSize))

        // Decode format - RGB565 is enough
        builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565)
    }

    override fun registerComponents(context: Context, glide: Glide) {
        val okHttpClient = Components.application.notCachingOkHttpClient
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlResizeModelLoaderFactory(okHttpClient))
    }


    /**
     * Manipulate image uris to use the 'zimage.io' service that serves resized images.
     */
    private class OkHttpUrlResizeModelLoaderFactory(private val mOkHttpClient: OkHttpClient) : ModelLoaderFactory<GlideUrl, InputStream> {

        override fun build(context: Context, factories: GenericLoaderFactory): ModelLoader<GlideUrl, InputStream> {
            return object : OkHttpUrlLoader(mOkHttpClient) {
                override fun getResourceFetcher(model: GlideUrl?, width: Int, height: Int): DataFetcher<InputStream> {
                    val zimageModel = model?.let {
                        val uriStr = model.toStringUrl()
                        var uri = Uri.parse("http://edge.zimage.io")
                        uri = uri.buildUpon()
                                .appendQueryParameter("url", uriStr)
                                .appendQueryParameter("w", width.toString())
                                .appendQueryParameter("h", height.toString())
                                .appendQueryParameter("mode", "crop")
                                // Webp should work but doesn't :(
                                // .appendQueryParameter("format", "webp")
                                .build()
                        GlideUrl(uri.toString())
                    }
                    return super.getResourceFetcher(zimageModel, width, height)
                }
            }
        }

        override fun teardown() {}
    }
}
