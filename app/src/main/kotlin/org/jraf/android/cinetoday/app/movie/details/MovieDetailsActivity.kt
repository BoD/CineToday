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
package org.jraf.android.cinetoday.app.movie.details

import android.app.Activity
import android.app.LoaderManager
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.MovieDetailsBinding
import org.jraf.android.cinetoday.provider.showtime.ShowtimeCursor
import org.jraf.android.cinetoday.provider.showtime.ShowtimeSelection
import org.jraf.android.util.ui.animation.AnimationUtil
import java.text.DateFormat
import java.util.*

class MovieDetailsActivity : Activity(), LoaderManager.LoaderCallbacks<Cursor> {

    private var mBinding: MovieDetailsBinding? = null
    private val mTxtTheaterNameList = ArrayList<TextView>(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView<MovieDetailsBinding>(this, R.layout.movie_details)
        loaderManager.initLoader(LOADER_MOVIE, null, this)
        loaderManager.initLoader(LOADER_SHOWTIMES, null, this)

        mBinding!!.conMovie.setOnScrollChangeListener(mOnScrollChangeListener)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor>? {
        val movieUri = intent.data
        when (id) {
            LOADER_MOVIE -> return CursorLoader(this, movieUri, null, null, null, null)

            LOADER_SHOWTIMES -> return ShowtimeSelection()
                    .movieId(ContentUris.parseId(movieUri))
                    .orderByTheaterId()
                    .getCursorLoader(this)
        }
        return null
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        when (loader.id) {
            LOADER_MOVIE -> {
                mBinding!!.pgbLoading.visibility = View.GONE
                mBinding!!.conMovie.visibility = View.VISIBLE
                val movieViewModel = MovieViewModel(this, data)
                movieViewModel.moveToFirst()
                mBinding!!.movie = movieViewModel

                // Use the movie color in certain elements
                var color = movieViewModel.color
                if (color == null) color = getColor(R.color.background)
                mBinding!!.root.setBackgroundColor(color)
                mBinding!!.txtTheaterNameInvisible.setBackgroundColor(color)
                val gradientColors = intArrayOf(color, 0)
                val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors)
                mBinding!!.vieTheaterNameGradient.background = gradientDrawable
            }

            LOADER_SHOWTIMES -> {
                mTxtTheaterNameList.clear()
                mBinding!!.conShowtimes.removeAllViews()

                val showtimeCursor = data as ShowtimeCursor
                showtimeCursor.moveToPosition(-1)
                var theaterId: Long = -1
                val now = Calendar.getInstance()
                val inflater = LayoutInflater.from(this)
                while (showtimeCursor.moveToNext()) {
                    // Theater name
                    if (showtimeCursor.theaterId != theaterId) {
                        theaterId = showtimeCursor.theaterId
                        val txtTheaterName = inflater.inflate(R.layout.movie_details_theater_name, mBinding!!.conShowtimes, false) as TextView
                        txtTheaterName.text = showtimeCursor.theaterName
                        mBinding!!.conShowtimes.addView(txtTheaterName)

                        mTxtTheaterNameList.add(txtTheaterName)
                    }

                    // Time
                    val isTooLate = getTimeAsCalendar(showtimeCursor.time.time).before(now)
                    val conShowtimeItem = inflater.inflate(R.layout.movie_details_showtime, mBinding!!.conShowtimes, false)
                    val txtShowtime = conShowtimeItem.findViewById(R.id.txtShowtime) as TextView
                    txtShowtime.text = getTimeFormat(this).format(showtimeCursor.time)
                    val txtIs3d = conShowtimeItem.findViewById(R.id.txtIs3d) as TextView
                    if (showtimeCursor.is3d) {
                        txtIs3d.visibility = View.VISIBLE
                    } else {
                        txtIs3d.visibility = View.GONE
                    }
                    if (isTooLate) {
                        conShowtimeItem.alpha = .33f
                    }
                    mBinding!!.conShowtimes.addView(conShowtimeItem)
                }
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

    private val mOnScrollChangeListener = View.OnScrollChangeListener { _, scrollX, scrollY, oldScrollX, oldScrollY ->
        if (scrollY < mBinding!!.conShowtimes.y + mTxtTheaterNameList[0].y - mBinding!!.txtTheaterName.paddingTop) {
            mBinding!!.txtTheaterName.visibility = View.GONE
            mBinding!!.conTheaterName.visibility = View.GONE
            mTxtTheaterNameList[0].visibility = View.VISIBLE
        } else {
            mBinding!!.txtTheaterName.visibility = View.VISIBLE
            AnimationUtil.animateVisible(mBinding!!.conTheaterName)
            for (textView in mTxtTheaterNameList) {
                if (scrollY >= textView.y - mBinding!!.txtTheaterName.paddingTop + mBinding!!.conShowtimes.y) {
                    mBinding!!.txtTheaterName.text = textView.text
                    mBinding!!.txtTheaterNameInvisible.text = textView.text
                    textView.visibility = View.INVISIBLE
                } else {
                    textView.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private val LOADER_MOVIE = 0
        private val LOADER_SHOWTIMES = 1

        private var sTimeFormat: DateFormat? = null

        private fun getTimeAsCalendar(time: Long): Calendar {
            val res = Calendar.getInstance()
            res.set(Calendar.HOUR_OF_DAY, 0)
            res.set(Calendar.MINUTE, 0)
            res.set(Calendar.SECOND, 0)
            res.set(Calendar.MILLISECOND, 0)
            res.add(Calendar.MILLISECOND, time.toInt())
            return res
        }

        private fun getTimeFormat(context: Context): DateFormat {
            var res = sTimeFormat
            if (res == null) {
                res = android.text.format.DateFormat.getTimeFormat(context)
                sTimeFormat = res
                return res
            }
            return res
        }
    }
}