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
package org.jraf.android.cinetoday.util.async

import android.annotation.SuppressLint
import android.os.AsyncTask
import org.jraf.android.util.log.Log

@SuppressLint("StaticFieldLeak")
inline fun <T> doAsync(crossinline doInBackground: () -> T, crossinline onPostExecute: (T) -> Unit) {
    object : AsyncTask<Unit, Unit, T>() {
        override fun doInBackground(vararg params: Unit?): T {
            return doInBackground()
        }

        override fun onPostExecute(result: T) {
            onPostExecute(result)
        }
    }.execute()
}

inline fun <T> doAsync(crossinline doInBackground: () -> T) {
    val origin = Exception()
    AsyncTask.execute {
        try {
            doInBackground()
        } catch (t: Throwable) {
            Log.e(origin, "Caught an exception in doAsync")
            throw t
        }
    }
}