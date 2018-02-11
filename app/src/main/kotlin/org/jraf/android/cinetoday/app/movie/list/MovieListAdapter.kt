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

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.MovieListItemBinding
import org.jraf.android.cinetoday.glide.GlideApp
import org.jraf.android.cinetoday.glide.GlideHelper
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.util.log.Log

class MovieListAdapter(private val mContext: Context, private val mMovieListCallbacks: MovieListCallbacks, private val mPaletteListener: PaletteListener) : RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(mContext)

    var data: Array<Movie> = emptyArray()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(val itemBinding: MovieListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieListAdapter.ViewHolder {
        val binding =
            DataBindingUtil.inflate<MovieListItemBinding>(mLayoutInflater, R.layout.movie_list_item, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieListAdapter.ViewHolder, position: Int) {
        val movie = data[position]
        holder.itemBinding.movie = movie
        holder.itemBinding.callbacks = mMovieListCallbacks
        movie.color?.let {
            mPaletteListener.onPaletteAvailable(position, it, true, movie.id)
        }

        holder.itemBinding.executePendingBindings()

        GlideHelper.load(movie.posterUri, holder.itemBinding.imgPoster, object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                if (movie.color == null) {
                    Log.d("2")
                    // Generate the color
                    Palette.from((resource as BitmapDrawable).bitmap).generate { palette ->
                        val color = palette.getDarkVibrantColor(mContext.getColor(R.color.movie_list_bg))
                        mPaletteListener.onPaletteAvailable(position, color, false, movie.id)
                    }
                    Log.d("3")
                }

                // Preload the next image
                if (position + 1 in data.indices) {
                    val nextMovie = data[position + 1]
                    GlideApp.with(mContext).load(nextMovie.posterUri).centerCrop()
                            .preload(holder.itemBinding.imgPoster.width, holder.itemBinding.imgPoster.height)
                }

                return false
            }
        })
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int) = if (data.isEmpty()) RecyclerView.NO_ID else data[position].id.hashCode().toLong()
}
