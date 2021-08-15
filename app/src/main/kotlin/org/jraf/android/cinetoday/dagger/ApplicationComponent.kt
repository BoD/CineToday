/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2017-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.dagger

import dagger.Component
import okhttp3.OkHttpClient
import org.jraf.android.cinetoday.app.ApplicationModule
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesHelper
import org.jraf.android.cinetoday.app.main.MainActivity
import org.jraf.android.cinetoday.app.movie.details.MovieDetailsActivity
import org.jraf.android.cinetoday.app.movie.list.MovieListFragment
import org.jraf.android.cinetoday.app.preferences.PreferencesFragment
import org.jraf.android.cinetoday.app.theater.favorites.TheaterFavoritesFragment
import org.jraf.android.cinetoday.app.theater.search.TheaterSearchLiveData
import org.jraf.android.cinetoday.app.tile.MoviesTodayTile
import org.jraf.android.cinetoday.network.NetworkModule
import org.jraf.android.cinetoday.prefs.MainPrefs
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        NetworkModule::class
    ]
)
interface ApplicationComponent {
    val loadMoviesHelper: LoadMoviesHelper

    @get:Named("NotCachingOkHttpClient")
    val notCachingOkHttpClient: OkHttpClient

    val mainPrefs: MainPrefs

    fun inject(mainActivity: MainActivity)

    fun inject(preferencesFragment: PreferencesFragment)

    fun inject(theaterFavoritesFragment: TheaterFavoritesFragment)

    fun inject(movieListFragment: MovieListFragment)

    fun inject(movieDetailsActivity: MovieDetailsActivity)

    fun inject(theaterSearchLiveData: TheaterSearchLiveData)

    fun inject(moviesTodayTile: MoviesTodayTile)
}
