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
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import org.jraf.android.cinetoday.BuildConfig
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.util.log.Log
import java.io.InputStream

@GlideModule
class CineTodayGlideModule : AppGlideModule() {
    companion object {
        private const val CACHE_SIZE_B = 5 * 1024 * 1024L
        private const val CACHE_DIRECTORY_NAME = "images"
        private const val CLOUD_IMG_URL = "https://ce8eb4b9c.cloudimg.io/crop"
        private const val CLOUD_IMG_FORMAT = "twebp"
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val memorySizeCalculator = MemorySizeCalculator.Builder(context).build()

        builder
            // Disk cache
            .setDiskCache(InternalCacheDiskCacheFactory(context, CACHE_DIRECTORY_NAME, CACHE_SIZE_B))

            // Memory cache / bitmap pool
            .setMemoryCache(LruResourceCache(memorySizeCalculator.memoryCacheSize.toLong()))
            .setBitmapPool(LruBitmapPool(memorySizeCalculator.bitmapPoolSize.toLong()))

            .setDefaultRequestOptions(
                RequestOptions()
                    // Decode format - RGB565 is enough
                    .format(DecodeFormat.PREFER_RGB_565)
                    // Disable this optimization because we need to access pixels (because we use Palette on them)
                    .disallowHardwareConfig()
            )

            // Logs
            .setLogLevel(if (BuildConfig.DEBUG_LOGS) android.util.Log.VERBOSE else android.util.Log.WARN)
    }

    override fun isManifestParsingEnabled() = false

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val okHttpClient = Components.application.notCachingOkHttpClient
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlResizeModelLoaderFactory(okHttpClient))
    }


    /**
     * Manipulate image uris to use the 'zimage.io' service that serves resized images.
     */
    private class OkHttpUrlResizeModelLoaderFactory(private val okHttpClient: OkHttpClient) :
        ModelLoaderFactory<GlideUrl, InputStream> {

        override fun build(factories: MultiModelLoaderFactory): ModelLoader<GlideUrl, InputStream> {
            return object : OkHttpUrlLoader(okHttpClient) {
                override fun buildLoadData(
                    model: GlideUrl,
                    width: Int,
                    height: Int,
                    options: Options
                ): ModelLoader.LoadData<InputStream>? {
                    var uri = Uri.parse(CLOUD_IMG_URL)
                    uri = uri.buildUpon()
                        .appendPath("${width}x$height")
                        .appendPath(CLOUD_IMG_FORMAT)
                        .appendPath(model.toStringUrl())
                        .build()
                    val cloudImgUrl = GlideUrl(uri.toString())
                    Log.d("cloudImgUrl=$cloudImgUrl")
                    return super.buildLoadData(cloudImgUrl, width, height, options)
                }
            }
        }

        override fun teardown() {}
    }
}
