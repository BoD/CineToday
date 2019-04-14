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
package org.jraf.android.cinetoday.app

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesHelper
import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesListenerHelper
import org.jraf.android.cinetoday.database.AppDatabase
import org.jraf.android.cinetoday.database.V1ToV2Migration
import org.jraf.android.cinetoday.network.api.Api
import org.jraf.android.cinetoday.prefs.MainPrefs
import javax.inject.Singleton

@Module
class ApplicationModule(private val mContext: Context) {

    @Singleton
    @Provides
    fun provideContext(): Context {
        return mContext
    }

    @Singleton
    @Provides
    fun provideLoadMoviesHelper(
        context: Context,
        mainPrefs: MainPrefs,
        api: Api,
        appDatabase: AppDatabase,
        loadMoviesListenerHelper: LoadMoviesListenerHelper
    ): LoadMoviesHelper {
        return LoadMoviesHelper(context, mainPrefs, api, appDatabase, loadMoviesListenerHelper)
    }

    @Singleton
    @Provides
    fun provideMainPrefs(context: Context): MainPrefs {
        return MainPrefs(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(V1ToV2Migration())
            .build()
    }

    @Singleton
    @Provides
    fun provideLoadMoviesListenerHelper(): LoadMoviesListenerHelper {
        return LoadMoviesListenerHelper()
    }

}
