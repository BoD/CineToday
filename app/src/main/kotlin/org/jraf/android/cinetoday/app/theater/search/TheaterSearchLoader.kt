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

import android.content.AsyncTaskLoader
import android.content.Context
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.network.api.Api
import org.jraf.android.cinetoday.provider.theater.TheaterSelection
import org.jraf.android.util.log.Log
import java.util.*
import javax.inject.Inject

class TheaterSearchLoader(context: Context, private val mQuery: String) : AsyncTaskLoader<List<Theater>>(context) {
    @Inject lateinit var mApi: Api
    private var mData: List<Theater>? = null

    init {
        Components.application.inject(this)
    }

    override fun loadInBackground(): List<Theater>? {
        Log.d()
        // Get the list of favorite theaters, so we can filter them out from the search results
        val favoriteTheaterPublicIds = HashSet<String>()
        TheaterSelection().query(context).use { cursor ->
            while (cursor.moveToNext()) {
                favoriteTheaterPublicIds.add(cursor.getPublicId())
            }
        }
        try {
            // API call (blocking)
            val res = mApi!!.searchTheaters(mQuery).toMutableList()

            // Filter out favorite theaters
            val i = res.iterator()
            while (i.hasNext()) {
                if (favoriteTheaterPublicIds.contains(i.next().id)) i.remove()
            }
            return res
        } catch (e: Exception) {
            Log.w(e, "Could not search for theaters")
            return null
        }

    }

    override fun deliverResult(data: List<Theater>) {
        Log.d()
        mData = data

        if (isStarted) {
            super.deliverResult(data)
        }
    }

    override fun onStartLoading() {
        Log.d()
        val data = mData
        if (data != null) {
            deliverResult(data)
        }

        if (takeContentChanged() || data == null) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        Log.d()
        cancelLoad()
    }

    override fun onReset() {
        Log.d()
        onStopLoading()
        releaseResources()
    }

    override fun onCanceled(data: List<Theater>) {
        Log.d()
        super.onCanceled(data)
        releaseResources()
    }

    private fun releaseResources() {
        Log.d()
        mData = null
    }
}
