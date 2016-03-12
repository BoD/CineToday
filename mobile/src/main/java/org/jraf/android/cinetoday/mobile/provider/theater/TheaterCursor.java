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

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jraf.android.cinetoday.mobile.provider.base.AbstractCursor;

/**
 * Cursor wrapper for the {@code theater} table.
 */
public class TheaterCursor extends AbstractCursor implements TheaterModel {
    public TheaterCursor(Cursor cursor) {
        super(cursor);
    }

    /**
     * Primary key.
     */
    public long getId() {
        Long res = getLongOrNull(TheaterColumns._ID);
        if (res == null)
            throw new NullPointerException("The value of '_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Public id of this theater.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getPublicId() {
        String res = getStringOrNull(TheaterColumns.PUBLIC_ID);
        if (res == null)
            throw new NullPointerException("The value of 'public_id' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Name of this theater.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getName() {
        String res = getStringOrNull(TheaterColumns.NAME);
        if (res == null)
            throw new NullPointerException("The value of 'name' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * Address of this theater.
     * Cannot be {@code null}.
     */
    @NonNull
    public String getAddress() {
        String res = getStringOrNull(TheaterColumns.ADDRESS);
        if (res == null)
            throw new NullPointerException("The value of 'address' in the database was null, which is not allowed according to the model definition");
        return res;
    }

    /**
     * The uri of a picture for this theater.
     * Can be {@code null}.
     */
    @Nullable
    public String getPictureuri() {
        String res = getStringOrNull(TheaterColumns.PICTUREURI);
        return res;
    }
}
