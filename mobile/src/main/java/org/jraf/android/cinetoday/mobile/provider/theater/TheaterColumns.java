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
package org.jraf.android.cinetoday.mobile.provider.theater;

import android.net.Uri;
import android.provider.BaseColumns;

import org.jraf.android.cinetoday.mobile.provider.CineTodayProvider;

/**
 * A theater.
 */
public class TheaterColumns implements BaseColumns {
    public static final String TABLE_NAME = "theater";
    public static final Uri CONTENT_URI = Uri.parse(CineTodayProvider.CONTENT_URI_BASE + "/" + TABLE_NAME);

    /**
     * Primary key.
     */
    public static final String _ID = BaseColumns._ID;

    /**
     * Public id of this theater.
     */
    public static final String PUBLIC_ID = "public_id";

    /**
     * Name of this theater.
     */
    public static final String NAME = "name";

    /**
     * Address of this theater.
     */
    public static final String ADDRESS = "address";

    /**
     * The uri of a picture for this theater.
     */
    public static final String PICTUREURI = "pictureUri";


    public static final String DEFAULT_ORDER = TABLE_NAME + "." + _ID;

    // @formatter:off
    public static final String[] ALL_COLUMNS = new String[] {
            _ID,
            PUBLIC_ID,
            NAME,
            ADDRESS,
            PICTUREURI
    };
    // @formatter:on

    public static boolean hasColumns(String[] projection) {
        if (projection == null) return true;
        for (String c : projection) {
            if (c.equals(PUBLIC_ID) || c.contains("." + PUBLIC_ID)) return true;
            if (c.equals(NAME) || c.contains("." + NAME)) return true;
            if (c.equals(ADDRESS) || c.contains("." + ADDRESS)) return true;
            if (c.equals(PICTUREURI) || c.contains("." + PICTUREURI)) return true;
        }
        return false;
    }

}
