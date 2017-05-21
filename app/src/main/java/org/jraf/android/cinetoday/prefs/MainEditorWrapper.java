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
package org.jraf.android.cinetoday.prefs;

import java.util.Set;

import android.content.SharedPreferences;

import org.jraf.android.prefs.EditorWrapper;

public class MainEditorWrapper extends EditorWrapper {
    public MainEditorWrapper(SharedPreferences.Editor wrapped) {
        super(wrapped);
    }


    //================================================================================
    // region LastUpdateDate
    //================================================================================

    /**
     * Last time an update was successfully called.
     */
    public MainEditorWrapper putLastUpdateDate(Long lastUpdateDate) {
        if (lastUpdateDate == null) {
            remove("lastUpdateDate");
        } else {
            putLong("lastUpdateDate", lastUpdateDate);
        }
        return this;
    }

    /**
     * Last time an update was successfully called.
     */
    public MainEditorWrapper removeLastUpdateDate() {
        remove("lastUpdateDate");
        return this;
    }

    // endregion


    //================================================================================
    // region ShowNewReleasesNotification
    //================================================================================

    /**
     * Show a notification on new movie release day.
     */
    public MainEditorWrapper putShowNewReleasesNotification(Boolean showNewReleasesNotification) {
        if (showNewReleasesNotification == null) {
            remove("showNewReleasesNotification");
        } else {
            putBoolean("showNewReleasesNotification", showNewReleasesNotification);
        }
        return this;
    }

    /**
     * Show a notification on new movie release day.
     */
    public MainEditorWrapper removeShowNewReleasesNotification() {
        remove("showNewReleasesNotification");
        return this;
    }

    // endregion


    //================================================================================
    // region NotifiedMovieIds
    //================================================================================

    /**
     * List of movies that have been used in 'new release' notifications.
     */
    public MainEditorWrapper putNotifiedMovieIds(Set<String> notifiedMovieIds) {
        if (notifiedMovieIds == null) {
            remove("notifiedMovieIds");
        } else {
            putStringSet("notifiedMovieIds", notifiedMovieIds);
        }
        return this;
    }

    /**
     * List of movies that have been used in 'new release' notifications.
     */
    public MainEditorWrapper removeNotifiedMovieIds() {
        remove("notifiedMovieIds");
        return this;
    }

    // endregion
}