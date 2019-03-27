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
package org.jraf.android.cinetoday.app

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.StrictMode
import org.jraf.android.cinetoday.BuildConfig
import org.jraf.android.cinetoday.dagger.Components
import org.jraf.android.util.log.Log

class Application : android.app.Application() {

    companion object {
        private const val TAG = "CineToday"
    }

    override fun onCreate() {
        super.onCreate()
        // Log
        Log.init(this, TAG, BuildConfig.DEBUG_LOGS)

        // Strict mode
        if (BuildConfig.STRICT_MODE) setupStrictMode()

        // Dagger
        Components.init(this)

        // Sqlite debug logs (don't do it for >= P because it uses reflection, which shows a nasty dialog)
        if (BuildConfig.DEBUG_LOGS && Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) enableSqliteDebugLogs(false)
    }

    private fun setupStrictMode() {
        // Do this in a Handler.post because of this issue: http://code.google.com/p/android/issues/detail?id=35298
        Handler().post {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        }
    }

    @SuppressLint("PrivateApi")
    private fun enableSqliteDebugLogs(executionTime: Boolean) {
        try {
            val sqliteDebugClass = Class.forName("android.database.sqlite.SQLiteDebug")

            // Statements
            val debugStatementsField = sqliteDebugClass.getDeclaredField("DEBUG_SQL_STATEMENTS")
            debugStatementsField.isAccessible = true
            debugStatementsField.set(null, true)

            // Execution time (more verbose)
            if (executionTime) {
                val debugTimeField = sqliteDebugClass.getDeclaredField("DEBUG_SQL_TIME")
                debugTimeField.isAccessible = true
                debugTimeField.set(null, true)
            }
        } catch (t: Throwable) {
            Log.w(t, "Could not enable SQLiteDebug logging")
        }
    }
}
