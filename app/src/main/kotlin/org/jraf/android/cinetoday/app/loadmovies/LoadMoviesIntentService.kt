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
package org.jraf.android.cinetoday.app.loadmovies

import android.app.IntentService
import android.content.Intent

import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.util.log.Log

class LoadMoviesIntentService : IntentService(LoadMoviesIntentService::class.java.name) {

    companion object {
        val ACTION_LOAD_MOVIES = "${LoadMoviesIntentService::class.java.name}.LOAD_MOVIES"
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent?.action == ACTION_LOAD_MOVIES) {
            try {
                Components.application.loadMoviesHelper.loadMovies()
            } catch (e: Exception) {
                Log.e(e, "Could not load movies")
            }
        }
    }
}
