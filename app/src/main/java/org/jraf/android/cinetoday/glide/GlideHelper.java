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

import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jraf.android.util.log.Log;

public class GlideHelper {
    private static RequestListener<? super String, GlideDrawable> sRequestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            Log.w(e, "Could not load image " + model);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            return false;
        }
    };

    private static DrawableRequestBuilder<String> getRequestBuilder(String path, ImageView imageView) {
        return Glide.with(imageView.getContext())
                .load(path)
                .centerCrop();
    }

    public static void load(String path, ImageView imageView) {
        getRequestBuilder(path, imageView)
                .listener(sRequestListener)
                .into(imageView);
    }

    public static void load(String path, ImageView imageView, final RequestListener<? super String, GlideDrawable> listener) {
        getRequestBuilder(path, imageView)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        sRequestListener.onException(e, model, target, isFirstResource);
                        return listener.onException(e, model, target, isFirstResource);
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        sRequestListener.onResourceReady(resource, model, target, isFromMemoryCache, isFirstResource);
                        return listener.onResourceReady(resource, model, target, isFromMemoryCache, isFirstResource);
                    }
                })
                .into(imageView);
    }
}
