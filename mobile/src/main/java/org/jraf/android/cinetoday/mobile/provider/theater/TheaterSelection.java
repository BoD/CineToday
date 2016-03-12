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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.jraf.android.cinetoday.mobile.provider.base.AbstractSelection;

/**
 * Selection for the {@code theater} table.
 */
public class TheaterSelection extends AbstractSelection<TheaterSelection> {
    @Override
    protected Uri baseUri() {
        return TheaterColumns.CONTENT_URI;
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param contentResolver The content resolver to query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code TheaterCursor} object, which is positioned before the first entry, or null.
     */
    public TheaterCursor query(ContentResolver contentResolver, String[] projection) {
        Cursor cursor = contentResolver.query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new TheaterCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(contentResolver, null)}.
     */
    public TheaterCursor query(ContentResolver contentResolver) {
        return query(contentResolver, null);
    }

    /**
     * Query the given content resolver using this selection.
     *
     * @param context The context to use for the query.
     * @param projection A list of which columns to return. Passing null will return all columns, which is inefficient.
     * @return A {@code TheaterCursor} object, which is positioned before the first entry, or null.
     */
    public TheaterCursor query(Context context, String[] projection) {
        Cursor cursor = context.getContentResolver().query(uri(), projection, sel(), args(), order());
        if (cursor == null) return null;
        return new TheaterCursor(cursor);
    }

    /**
     * Equivalent of calling {@code query(context, null)}.
     */
    public TheaterCursor query(Context context) {
        return query(context, null);
    }


    public TheaterSelection id(long... value) {
        addEquals("theater." + TheaterColumns._ID, toObjectArray(value));
        return this;
    }

    public TheaterSelection idNot(long... value) {
        addNotEquals("theater." + TheaterColumns._ID, toObjectArray(value));
        return this;
    }

    public TheaterSelection orderById(boolean desc) {
        orderBy("theater." + TheaterColumns._ID, desc);
        return this;
    }

    public TheaterSelection orderById() {
        return orderById(false);
    }

    public TheaterSelection publicId(String... value) {
        addEquals(TheaterColumns.PUBLIC_ID, value);
        return this;
    }

    public TheaterSelection publicIdNot(String... value) {
        addNotEquals(TheaterColumns.PUBLIC_ID, value);
        return this;
    }

    public TheaterSelection publicIdLike(String... value) {
        addLike(TheaterColumns.PUBLIC_ID, value);
        return this;
    }

    public TheaterSelection publicIdContains(String... value) {
        addContains(TheaterColumns.PUBLIC_ID, value);
        return this;
    }

    public TheaterSelection publicIdStartsWith(String... value) {
        addStartsWith(TheaterColumns.PUBLIC_ID, value);
        return this;
    }

    public TheaterSelection publicIdEndsWith(String... value) {
        addEndsWith(TheaterColumns.PUBLIC_ID, value);
        return this;
    }

    public TheaterSelection orderByPublicId(boolean desc) {
        orderBy(TheaterColumns.PUBLIC_ID, desc);
        return this;
    }

    public TheaterSelection orderByPublicId() {
        orderBy(TheaterColumns.PUBLIC_ID, false);
        return this;
    }

    public TheaterSelection name(String... value) {
        addEquals(TheaterColumns.NAME, value);
        return this;
    }

    public TheaterSelection nameNot(String... value) {
        addNotEquals(TheaterColumns.NAME, value);
        return this;
    }

    public TheaterSelection nameLike(String... value) {
        addLike(TheaterColumns.NAME, value);
        return this;
    }

    public TheaterSelection nameContains(String... value) {
        addContains(TheaterColumns.NAME, value);
        return this;
    }

    public TheaterSelection nameStartsWith(String... value) {
        addStartsWith(TheaterColumns.NAME, value);
        return this;
    }

    public TheaterSelection nameEndsWith(String... value) {
        addEndsWith(TheaterColumns.NAME, value);
        return this;
    }

    public TheaterSelection orderByName(boolean desc) {
        orderBy(TheaterColumns.NAME, desc);
        return this;
    }

    public TheaterSelection orderByName() {
        orderBy(TheaterColumns.NAME, false);
        return this;
    }

    public TheaterSelection address(String... value) {
        addEquals(TheaterColumns.ADDRESS, value);
        return this;
    }

    public TheaterSelection addressNot(String... value) {
        addNotEquals(TheaterColumns.ADDRESS, value);
        return this;
    }

    public TheaterSelection addressLike(String... value) {
        addLike(TheaterColumns.ADDRESS, value);
        return this;
    }

    public TheaterSelection addressContains(String... value) {
        addContains(TheaterColumns.ADDRESS, value);
        return this;
    }

    public TheaterSelection addressStartsWith(String... value) {
        addStartsWith(TheaterColumns.ADDRESS, value);
        return this;
    }

    public TheaterSelection addressEndsWith(String... value) {
        addEndsWith(TheaterColumns.ADDRESS, value);
        return this;
    }

    public TheaterSelection orderByAddress(boolean desc) {
        orderBy(TheaterColumns.ADDRESS, desc);
        return this;
    }

    public TheaterSelection orderByAddress() {
        orderBy(TheaterColumns.ADDRESS, false);
        return this;
    }

    public TheaterSelection pictureUri(String... value) {
        addEquals(TheaterColumns.PICTURE_URI, value);
        return this;
    }

    public TheaterSelection pictureUriNot(String... value) {
        addNotEquals(TheaterColumns.PICTURE_URI, value);
        return this;
    }

    public TheaterSelection pictureUriLike(String... value) {
        addLike(TheaterColumns.PICTURE_URI, value);
        return this;
    }

    public TheaterSelection pictureUriContains(String... value) {
        addContains(TheaterColumns.PICTURE_URI, value);
        return this;
    }

    public TheaterSelection pictureUriStartsWith(String... value) {
        addStartsWith(TheaterColumns.PICTURE_URI, value);
        return this;
    }

    public TheaterSelection pictureUriEndsWith(String... value) {
        addEndsWith(TheaterColumns.PICTURE_URI, value);
        return this;
    }

    public TheaterSelection orderByPictureUri(boolean desc) {
        orderBy(TheaterColumns.PICTURE_URI, desc);
        return this;
    }

    public TheaterSelection orderByPictureUri() {
        orderBy(TheaterColumns.PICTURE_URI, false);
        return this;
    }
}
