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
package org.jraf.android.moviestoday.mobile.app.api;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class LoadMoviesIntentService extends IntentService {
    private static final String PREFIX = LoadMoviesIntentService.class.getName() + ".";
    private static final String ACTION_LOAD_MOVIES = PREFIX + "LOAD_MOVIES";

    public LoadMoviesIntentService() {
        super(LoadMoviesIntentService.class.getName());
    }

    /**
     * Starts this service to perform action ACTION_LOAD_MOVIES. If the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionLoadMovies(Context context) {
        Intent intent = new Intent(context, LoadMoviesIntentService.class);
        intent.setAction(ACTION_LOAD_MOVIES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_MOVIES.equals(action)) {
                handleActionLoadMovies();
            }
        }
    }

    /**
     * Handle action ACTION_LOAD_MOVIES in the provided background thread.
     */
    private void handleActionLoadMovies() {
        LoadMoviesHelper.get().onLoadMoviesStarted();
        SystemClock.sleep(8000);
        LoadMoviesHelper.get().onLoadMoviesFinished();
    }
}
