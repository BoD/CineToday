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

import android.content.Context
import com.google.android.gms.gcm.GcmNetworkManager
import com.google.android.gms.gcm.GcmTaskService
import com.google.android.gms.gcm.PeriodicTask
import com.google.android.gms.gcm.TaskParams
import org.jraf.android.cinetoday.dagger.Components
import java.util.concurrent.TimeUnit

class LoadMoviesTaskService : GcmTaskService() {
    override fun onRunTask(taskParams: TaskParams): Int {
        try {
            Components.application.loadMoviesHelper.loadMovies()
        } catch (e: Exception) {
            return GcmNetworkManager.RESULT_RESCHEDULE
        }

        return GcmNetworkManager.RESULT_SUCCESS
    }

    override fun onInitializeTasks() {
        // This is called when the app is re-installed, after all the tasks have been canceled.
        // Simply re-schedule the task.
        scheduleTask(this)
    }

    companion object {

        fun scheduleTask(context: Context) {
            val periodSecs = TimeUnit.HOURS.toSeconds(12)
            val flexSecs = TimeUnit.HOURS.toSeconds(1)
            val tag = "dailyLoadMovies"
            val periodicTask = PeriodicTask.Builder()
                    .setTag(tag)
                    .setService(LoadMoviesTaskService::class.java!!)
                    .setPeriod(periodSecs)
                    .setFlex(flexSecs)
                    .setPersisted(true)
                    .build()
            GcmNetworkManager.getInstance(context).schedule(periodicTask)
        }
    }
}
