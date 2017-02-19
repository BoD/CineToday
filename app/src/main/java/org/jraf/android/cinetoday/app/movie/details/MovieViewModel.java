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
package org.jraf.android.cinetoday.app.movie.details;

import android.content.Context;
import android.database.Cursor;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.provider.movie.MovieCursor;

public class MovieViewModel extends MovieCursor {
    private final Context mContext;

    public MovieViewModel(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    public String getDurationFormatted() {
        if (getDuration() == null) return null;
        return formatDuration(getDuration());
    }

    public String getGenresFormatted() {
        if (getGenres() == null) return null;
        return getGenres().replaceAll("\\|", " · ");
    }

    private String formatDuration(int durationSeconds) {
        if (durationSeconds < 60 * 60) return mContext.getString(R.string.durationMinutes, durationSeconds / 60);
        int hours = durationSeconds / (60 * 60);
        int minutes = (durationSeconds % (60 * 60)) / 60;
        if (minutes == 0) return mContext.getResources().getQuantityString(R.plurals.durationHours, hours, hours);
        return mContext.getString(R.string.durationHoursMinutes, hours, minutes);
    }
}
