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

import androidx.lifecycle.LiveData
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.cinetoday.network.api.Api
import org.jraf.android.cinetoday.util.async.doAsync
import org.jraf.android.util.log.Log
import javax.inject.Inject

class TheaterSearchLiveData : LiveData<List<Theater>>() {
    var query: String? = null
        set(value) {
            field = value
            load()
        }

    @Inject
    lateinit var api: Api
    @Inject
    lateinit var database: AppDatabase

    init {
        Components.application.inject(this)
    }

    private fun load() {
        Log.d()

        doAsync {
            // Get the list of favorite theaters, so we can filter them out from the search results
            val favoriteTheaters = database.theaterDao.allTheaters()

            try {
                // API call (blocking)
                val res = api.searchTheaters(query ?: "").toMutableList()

                // Filter out favorite theaters
                res.removeAll { favoriteTheaters.contains(it) }
                postValue(res)
            } catch (e: Exception) {
                Log.w(e, "Could not search for theaters")
            }
        }
    }
}
