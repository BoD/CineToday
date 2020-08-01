/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.util.handler.HandlerUtil
import org.jraf.android.util.log.Log
import java.util.concurrent.TimeUnit

class LoadMoviesWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d()
        try {
            Components.application.loadMoviesHelper.loadMovies()
        } catch (e: Exception) {
            Log.e(e, "Could not load movies")
            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        private val TAG = LoadMoviesWorker::class.java.simpleName

        fun scheduleTask(context: Context) {
            Log.d()
            val workRequest = PeriodicWorkRequestBuilder<LoadMoviesWorker>(12, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(TAG)
                .build()

            val operation = WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
            operation.state.observeAndLog()
        }

        private fun LiveData<Operation.State>.observeAndLog() {
            HandlerUtil.runOnUiThread {
                observeForever(object : Observer<Operation.State> {
                    override fun onChanged(t: Operation.State?) {
                        Log.d("State=$t")
                        if (t is Operation.State.SUCCESS || t is Operation.State.FAILURE) {
                            removeObserver(this)
                        }
                    }
                })
            }
        }
    }
}
