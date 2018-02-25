/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.app.theater.search

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.support.wear.widget.CurvingLayoutCallback
import android.support.wear.widget.WearableLinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.TheaterSearchListBinding
import org.jraf.android.cinetoday.util.base.BaseFragment
import org.jraf.android.util.ui.screenshape.ScreenShapeHelper

class TheaterSearchFragment : BaseFragment<TheaterSearchCallbacks>() {
    private lateinit var adapter: TheaterSearchAdapter
    private lateinit var binding: TheaterSearchListBinding

    private val theaterSearchLiveData: TheaterSearchLiveData by lazy { TheaterSearchLiveData() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.theater_search_list, container, false)!!
        binding.rclList.setHasFixedSize(true)
        binding.rclList.isEdgeItemsCenteringEnabled = true

        // Apply an offset + scale on the items depending on their distance from the center (only for Round screens)
        if (ScreenShapeHelper.get(context).isRound) {
            binding.rclList.layoutManager =
                    WearableLinearLayoutManager(context, object : CurvingLayoutCallback(context) {
                        private val FACTOR = .75f

                        override fun onLayoutFinished(child: View, parent: RecyclerView) {
                            super.onLayoutFinished(child, parent)

                            val childTop = child.y + child.height / 2f
                            val childOffsetFromCenter = childTop - parent.height / 2f
                            val childOffsetFromCenterRatio = Math.abs(childOffsetFromCenter / parent.height)
                            val childOffsetFromCenterRatioNormalized = childOffsetFromCenterRatio * FACTOR

                            child.scaleX = 1 - childOffsetFromCenterRatioNormalized
                            child.scaleY = 1 - childOffsetFromCenterRatioNormalized
                        }
                    })

            // Also snaps
            LinearSnapHelper().attachToRecyclerView(binding.rclList)
        } else {
            // Square screen: no scale effect and no snapping
            binding.rclList.layoutManager = WearableLinearLayoutManager(context)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = TheaterSearchAdapter(activity!!, callbacks)
        binding.rclList.adapter = adapter
    }

    fun search(query: String) {
        adapter.setLoading(true)
        val args = Bundle()
        args.putString("query", query)
        theaterSearchLiveData.query = query
        theaterSearchLiveData.observe(this, Observer {
            adapter.setLoading(false)
            adapter.setResults(it)
        })
    }

}
