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
package org.jraf.android.cinetoday.app;

import javax.inject.Singleton;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

import org.jraf.android.cinetoday.app.loadmovies.LoadMoviesHelper;
import org.jraf.android.cinetoday.network.api.Api;
import org.jraf.android.cinetoday.prefs.MainPrefs;

@Module
public class ApplicationModule {
    private final Context mContext;

    public ApplicationModule(Context context) {
        mContext = context;
    }

    @Singleton
    @Provides
    public Context provideContext() {
        return mContext;
    }

    @Singleton
    @Provides
    public LoadMoviesHelper provideLoadMoviesHelper(Context context, MainPrefs mainPrefs, Api api) {
        return new LoadMoviesHelper(context, mainPrefs, api);
    }

    @Singleton
    @Provides
    public MainPrefs provideMainPrefs(Context context) {
        return MainPrefs.get(context);
    }
}
