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

import android.app.LoaderManager
import android.content.Loader
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearSnapHelper
import android.support.wearable.view.DefaultOffsettingHelper
import android.support.wearable.view.WearableRecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.TheaterSearchListBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.util.base.BaseFragment
import org.jraf.android.util.ui.screenshape.ScreenShapeHelper

class TheaterSearchFragment : BaseFragment<TheaterSearchCallbacks>(), LoaderManager.LoaderCallbacks<List<Theater>> {
    private lateinit var mAdapter: TheaterSearchAdapter
    private lateinit var mBinding: TheaterSearchListBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate<TheaterSearchListBinding>(inflater, R.layout.theater_search_list, container, false)
        mBinding.rclList.setHasFixedSize(true)
        mBinding.rclList.centerEdgeItems = true

        // Apply an offset + scale on the items depending on their distance from the center (only for Round screens)
        if (ScreenShapeHelper.get(context).isRound) {
            mBinding.rclList.offsettingHelper = object : DefaultOffsettingHelper() {
                private val FACTOR = .75f

                override fun updateChild(child: View, parent: WearableRecyclerView) {
                    super.updateChild(child, parent)

                    val childTop = child.y + child.height / 2f
                    val childOffsetFromCenter = childTop - parent.height / 2f
                    val childOffsetFromCenterRatio = Math.abs(childOffsetFromCenter / parent.height)
                    val childOffsetFromCenterRatioNormalized = childOffsetFromCenterRatio * FACTOR

                    child.scaleX = 1 - childOffsetFromCenterRatioNormalized
                    child.scaleY = 1 - childOffsetFromCenterRatioNormalized
                }
            }

            // Also snaps
            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(mBinding.rclList)
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mAdapter = TheaterSearchAdapter(activity, callbacks)
        mBinding.rclList.adapter = mAdapter
    }

    fun search(query: String) {
        mAdapter.setLoading(true)
        val args = Bundle()
        args.putString("query", query)
        loaderManager.restartLoader(0, args, this)
    }


    //--------------------------------------------------------------------------
    // region LoaderCallbacks.
    //--------------------------------------------------------------------------

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<Theater>> {
        val query = args.getString("query")
        return TheaterSearchLoader(activity, query)
    }

    override fun onLoadFinished(loader: Loader<List<Theater>>, data: List<Theater>) {
        mAdapter.setLoading(false)
        mAdapter.setResults(data)
    }

    override fun onLoaderReset(loader: Loader<List<Theater>>) {
        mAdapter.setResults(null)
    }

    // endregion
}
