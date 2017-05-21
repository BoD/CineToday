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

import android.widget.ImageView

import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import org.jraf.android.util.log.Log

object GlideHelper {
    private val sRequestListener = object : RequestListener<String, GlideDrawable> {
        override fun onException(e: Exception, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
            Log.w(e, "Could not load image " + model)
            return false
        }

        override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
            return false
        }
    }

    private fun getRequestBuilder(path: String?, imageView: ImageView): DrawableRequestBuilder<String> {
        return Glide.with(imageView.context)
                .load(path)
                .centerCrop()
    }

    fun load(path: String, imageView: ImageView) {
        getRequestBuilder(path, imageView)
                .listener(sRequestListener)
                .into(imageView)
    }

    fun load(path: String?, imageView: ImageView, listener: RequestListener<in String, GlideDrawable>) {
        getRequestBuilder(path, imageView)
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                        sRequestListener.onException(e, model, target, isFirstResource)
                        return listener.onException(e, model, target, isFirstResource)
                    }

                    override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>, isFromMemoryCache: Boolean,
                                                 isFirstResource: Boolean): Boolean {
                        sRequestListener.onResourceReady(resource, model, target, isFromMemoryCache, isFirstResource)
                        return listener.onResourceReady(resource, model, target, isFromMemoryCache, isFirstResource)
                    }
                })
                .into(imageView)
    }
}
