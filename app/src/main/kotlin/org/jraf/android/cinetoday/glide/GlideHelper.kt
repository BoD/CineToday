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

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.jraf.android.util.log.Log

object GlideHelper {
    private val sRequestListener = object : RequestListener<Drawable> {
        override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            Log.w(e, "Could not load image " + model)
            return false
        }
    }

    private fun getRequestBuilder(path: String?, imageView: ImageView): GlideRequest<Drawable> {
        return GlideApp.with(imageView.context)
                .load(path)
                .centerCrop()
    }

    fun load(path: String, imageView: ImageView) {
        getRequestBuilder(path, imageView)
                .listener(sRequestListener)
                .into(imageView)
    }

    fun load(path: String?, imageView: ImageView, listener: RequestListener<Drawable>) {
        getRequestBuilder(path, imageView)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        sRequestListener.onLoadFailed(e, model, target, isFirstResource)
                        return listener.onLoadFailed(e, model, target, isFirstResource)
                    }

                    override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        sRequestListener.onResourceReady(resource, model, target, dataSource, isFirstResource)
                        return listener.onResourceReady(resource, model, target, dataSource, isFirstResource)
                    }
                })
                .into(imageView)
    }
}
