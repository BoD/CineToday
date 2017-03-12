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
package org.jraf.android.cinetoday.app.loadmovies;

import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.TaskParams;

import org.jraf.android.cinetoday.dagger.Components;

public class LoadMoviesTaskService extends GcmTaskService {
    @Override
    public int onRunTask(TaskParams taskParams) {
        try {
            Components.application.getLoadMoviesHelper().loadMovies();
        } catch (Exception e) {
            return GcmNetworkManager.RESULT_RESCHEDULE;
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @Override
    public void onInitializeTasks() {
        // This is called when the app is re-installed, after all the tasks have been canceled.
        // Simply re-schedule the task.
        scheduleTask(this);
    }

    public static void scheduleTask(Context context) {
        long periodSecs = TimeUnit.HOURS.toSeconds(12);
        long flexSecs = TimeUnit.HOURS.toSeconds(1);
        String tag = "dailyLoadMovies";
        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setTag(tag)
                .setService(LoadMoviesTaskService.class)
                .setPeriod(periodSecs)
                .setFlex(flexSecs)
                .setPersisted(true)
                .build();
        GcmNetworkManager.getInstance(context).schedule(periodicTask);
    }
}
