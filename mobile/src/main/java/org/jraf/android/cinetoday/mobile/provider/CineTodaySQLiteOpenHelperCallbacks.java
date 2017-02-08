/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.mobile.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import org.jraf.android.cinetoday.BuildConfig;
import org.jraf.android.cinetoday.mobile.prefs.MainPrefs;
import org.jraf.android.cinetoday.mobile.provider.base.BaseSQLiteOpenHelperCallbacks;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterColumns;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterContentValues;

public class CineTodaySQLiteOpenHelperCallbacks extends BaseSQLiteOpenHelperCallbacks {
    private static final String TAG = CineTodaySQLiteOpenHelperCallbacks.class.getSimpleName();

    public void onPostCreate(final Context context, final SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(TAG, "onPostCreate");

        // The previous version of the app had one theater stored in shared preferences.
        // Now we have a database with multiple theaters.
        // We insert the theater from shared preferences into the db.
        MainPrefs prefs = MainPrefs.get(context);
        String theaterId = prefs.getTheaterId();
        if (!TextUtils.isEmpty(theaterId)) {
            TheaterContentValues contentValues = new TheaterContentValues();

            contentValues.putPublicId(theaterId);

            String theaterName = prefs.getTheaterName();
            if (!TextUtils.isEmpty(theaterName)) contentValues.putName(theaterName);

            String theaterAddress = prefs.getTheaterAddress();
            if (!TextUtils.isEmpty(theaterAddress)) contentValues.putAddress(theaterAddress);

            contentValues.putPictureUri(prefs.getTheaterPictureUri());

            db.insert(TheaterColumns.TABLE_NAME, null, contentValues.values());

            prefs.removeTheaterId();
            prefs.removeTheaterName();
            prefs.removeTheaterAddress();
            prefs.removeTheaterPictureUri();
        }
    }
}
