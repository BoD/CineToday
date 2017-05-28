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

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.databinding.TheaterFavoritesBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.util.base.BaseFragment
import javax.inject.Inject

class TheaterFavoritesFragment : BaseFragment<TheaterFavoritesCallbacks>() {
    companion object {
        fun newInstance(): TheaterFavoritesFragment {
            return TheaterFavoritesFragment()
        }
    }

    @Inject lateinit var mDatabase: AppDatabase

    private lateinit var mBinding: TheaterFavoritesBinding

    private var mAdapter: TheaterFavoritesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Components.application.inject(this)
        mDatabase.theaterDao.allTheatersLive().observe(this, Observer { if (it != null) onTheatersResult(it) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate<TheaterFavoritesBinding>(inflater, R.layout.theater_favorites, container, false)
        mBinding.callbacks = callbacks

        mBinding.rclList.setHasFixedSize(true)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(mBinding.rclList)

        mBinding.rclList.addOnScrollListener(mOnScrollListener)

        return mBinding.root
    }

    private fun onTheatersResult(theaters: Array<Theater>) {
        mBinding.pgbLoading.visibility = View.GONE
        var adapter: TheaterFavoritesAdapter? = mAdapter
        if (adapter == null) {
            adapter = TheaterFavoritesAdapter(context)
            mAdapter = adapter
            mBinding.rclList.adapter = adapter
        }
        adapter.data = theaters
    }

    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) callbacks.onTheaterListScrolled()
        }
    }

    val currentVisibleTheater: Theater?
        get() = mAdapter?.let {
            val firstItemPosition = (mBinding.rclList.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (firstItemPosition == RecyclerView.NO_POSITION) return null
            it.data[firstItemPosition]
        }
}
