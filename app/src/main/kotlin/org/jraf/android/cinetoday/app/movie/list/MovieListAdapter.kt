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
package org.jraf.android.cinetoday.app.movie.list

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.MovieListItemBinding
import org.jraf.android.cinetoday.glide.GlideHelper
import org.jraf.android.cinetoday.provider.movie.MovieCursor

class MovieListAdapter(private val mContext: Context, private val mMovieListCallbacks: MovieListCallbacks, private val mPaletteListener: PaletteListener) : RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)
    private var mCursor: MovieCursor? = null

    class ViewHolder(val itemBinding: MovieListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListAdapter.ViewHolder {
        val binding = DataBindingUtil.inflate<MovieListItemBinding>(mLayoutInflater, R.layout.movie_list_item, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieListAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        mCursor!!.moveToPosition(position)
        holder.itemBinding.movie = mCursor
        holder.itemBinding.movieId = mCursor!!.id
        holder.itemBinding.callbacks = mMovieListCallbacks
        val id = mCursor!!.id
        val hasColor = mCursor!!.color != null
        if (hasColor) mPaletteListener.onPaletteAvailable(position, mCursor!!.color!!, true, id)

        holder.itemBinding.executePendingBindings()

        GlideHelper.load(mCursor!!.posterUri, holder.itemBinding.imgPoster, object : RequestListener<String, GlideDrawable> {
            override fun onException(e: Exception, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>, isFromMemoryCache: Boolean,
                                         isFirstResource: Boolean): Boolean {
                // Generate the color
                if (!hasColor) {
                    val glideBitmapDrawable = resource as GlideBitmapDrawable
                    Palette.from(glideBitmapDrawable.bitmap).generate { palette ->
                        val color = palette.getDarkVibrantColor(mContext.getColor(R.color.movie_list_bg))
                        mPaletteListener.onPaletteAvailable(position, color, false, id)
                    }
                }

                // Preload the next image
                if (mCursor!!.moveToPosition(position + 1)) {
                    Glide.with(mContext).load(mCursor!!.posterUri).centerCrop()
                            .preload(holder.itemBinding.imgPoster.width, holder.itemBinding.imgPoster.height)
                }

                return false
            }
        })
    }

    override fun getItemCount(): Int {
        if (mCursor == null) return 0
        return mCursor!!.count
    }

    override fun getItemId(position: Int): Long {
        if (mCursor == null) return RecyclerView.NO_ID
        if (!mCursor!!.moveToPosition(position)) return RecyclerView.NO_ID
        return mCursor!!.id
    }

    fun swapCursor(cursor: MovieCursor?) {
        mCursor = cursor
        notifyDataSetChanged()
    }
}
