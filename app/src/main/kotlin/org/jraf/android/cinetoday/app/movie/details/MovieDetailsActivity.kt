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

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.database.ShowtimeWithTheater
import org.jraf.android.cinetoday.databinding.MovieDetailsBinding
import org.jraf.android.cinetoday.model.movie.Movie
import org.jraf.android.cinetoday.util.base.BaseActivity
import org.jraf.android.cinetoday.util.uri.contentId
import org.jraf.android.util.ui.animation.AnimationUtil
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class MovieDetailsActivity : BaseActivity() {

    @Inject
    lateinit var database: AppDatabase

    private lateinit var binding: MovieDetailsBinding
    private val txtTheaterNameList = ArrayList<TextView>(3)
    private val timeFormat: DateFormat by lazy {
        android.text.format.DateFormat.getTimeFormat(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.movie_details)
        binding.conMovie.setOnScrollChangeListener(mOnScrollChangeListener)

        val movieId = intent.data!!.contentId
        database.movieDao.movieByIdLive(movieId).observe(this, Observer { if (it != null) onMovieResult(it) })
        database.showtimeDao.showtimesWithTheaterByMovieIdLive(movieId)
            .observe(this, Observer { if (it != null) onShowtimesResult(it) })
    }

    private fun onMovieResult(movie: Movie) {
        binding.pgbLoading.visibility = View.GONE
        binding.conMovie.visibility = View.VISIBLE
        // Needed for the rotary input to work
        binding.conMovie.requestFocus()

        val movieViewModel = MovieViewModel(movie, this)
        binding.movie = movieViewModel

        // Use the movie color in certain elements
        val color = movie.color ?: getColor(R.color.background)
        binding.root.setBackgroundColor(color)
        binding.txtTheaterNameInvisible.setBackgroundColor(color)
        val gradientColors = intArrayOf(color, 0)
        val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors)
        binding.vieTheaterNameGradient.background = gradientDrawable
    }

    private fun onShowtimesResult(showtimes: Array<ShowtimeWithTheater>) {
        txtTheaterNameList.clear()
        binding.conShowtimes.removeAllViews()

        var theaterId: String? = null
        val now = Date()
        val inflater = LayoutInflater.from(this)
        for (showtime in showtimes) {
            // Theater name
            if (showtime.theaterId != theaterId) {
                theaterId = showtime.theaterId
                val txtTheaterName =
                    inflater.inflate(R.layout.movie_details_theater_name, binding.conShowtimes, false) as TextView
                txtTheaterName.text = showtime.theaterName
                binding.conShowtimes.addView(txtTheaterName)

                txtTheaterNameList.add(txtTheaterName)
            }

            // Time
            val isTooLate = showtime.time.resetDate().before(now.resetDate())
            val conShowtimeItem = inflater.inflate(R.layout.movie_details_showtime, binding.conShowtimes, false)
            val txtShowtime = conShowtimeItem.findViewById<TextView>(R.id.txtShowtime)
            txtShowtime.text = timeFormat.format(showtime.time)
            val txtIs3d = conShowtimeItem.findViewById<TextView>(R.id.txtIs3d)
            txtIs3d.visibility = if (showtime.is3d) View.VISIBLE else View.GONE
            if (isTooLate) conShowtimeItem.alpha = .33f
            binding.conShowtimes.addView(conShowtimeItem)
        }
    }


    private val mOnScrollChangeListener = View.OnScrollChangeListener { _, _, scrollY, _, _ ->
        if (scrollY < binding.conShowtimes.y + txtTheaterNameList[0].y - binding.txtTheaterName.paddingTop) {
            binding.txtTheaterName.visibility = View.GONE
            binding.conTheaterName.visibility = View.GONE
            txtTheaterNameList[0].visibility = View.VISIBLE
        } else {
            binding.txtTheaterName.visibility = View.VISIBLE
            AnimationUtil.animateVisible(binding.conTheaterName)
            for (textView in txtTheaterNameList) {
                if (scrollY >= textView.y - binding.txtTheaterName.paddingTop + binding.conShowtimes.y) {
                    binding.txtTheaterName.text = textView.text
                    binding.txtTheaterNameInvisible.text = textView.text
                    textView.visibility = View.INVISIBLE
                } else {
                    textView.visibility = View.VISIBLE
                }
            }
        }
    }
}

private fun Date.resetDate(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(1970, Calendar.JANUARY, 1)
    return cal.time
}
