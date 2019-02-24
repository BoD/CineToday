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
package org.jraf.android.cinetoday.app.theater.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.databinding.TheaterFavoritesBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.util.base.BaseFragment
import org.jraf.android.cinetoday.widget.RotaryPagerSnapHelper
import javax.inject.Inject

class TheaterFavoritesFragment : BaseFragment<TheaterFavoritesCallbacks>() {
    companion object {
        fun newInstance(): TheaterFavoritesFragment {
            return TheaterFavoritesFragment()
        }
    }

    @Inject
    lateinit var database: AppDatabase

    private lateinit var binding: TheaterFavoritesBinding

    private var adapter: TheaterFavoritesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)
        database.theaterDao.allTheatersLive().observe(this, Observer { if (it != null) onTheatersResult(it) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.theater_favorites, container, false)
        binding.callbacks = callbacks

        binding.rclList.setHasFixedSize(true)
        binding.rclList.layoutManager = LinearLayoutManager(context)
        RotaryPagerSnapHelper().attachToRecyclerView(binding.rclList)
        binding.rclList.addOnScrollListener(onScrollListener)

        return binding.root
    }

    private fun onTheatersResult(theaters: Array<Theater>) {
        binding.pgbLoading.visibility = View.GONE
        var adapter: TheaterFavoritesAdapter? = adapter
        if (adapter == null) {
            adapter = TheaterFavoritesAdapter(context!!)
            this.adapter = adapter
            binding.rclList.adapter = adapter
        }
        adapter.data = theaters
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) callbacks.onTheaterListScrolled()
        }
    }

    val currentVisibleTheater: Theater?
        get() = adapter?.let {
            val firstItemPosition =
                (binding.rclList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (firstItemPosition == RecyclerView.NO_POSITION) return null
            it.data[firstItemPosition]
        }
}
