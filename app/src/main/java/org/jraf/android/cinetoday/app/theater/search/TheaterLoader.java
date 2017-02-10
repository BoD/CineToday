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
package org.jraf.android.cinetoday.app.theater.search;

import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.jraf.android.cinetoday.api.Api;
import org.jraf.android.cinetoday.model.theater.Theater;
import org.jraf.android.util.log.Log;

public class TheaterLoader extends AsyncTaskLoader<List<Theater>> {
    private final String mQuery;
    private List<Theater> mData;

    public TheaterLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    public List<Theater> loadInBackground() {
        Log.d();
        try {
            return Api.get(getContext()).searchTheaters(mQuery);
        } catch (Exception e) {
            Log.w(e, "Could not search for theaters");
            return null;
        }
    }

    @Override
    public void deliverResult(List<Theater> data) {
        Log.d();
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.d();
        if (mData != null) {
            deliverResult(mData);
        }

        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.d();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.d();
        onStopLoading();
        releaseResources();
    }

    @Override
    public void onCanceled(List<Theater> data) {
        Log.d();
        super.onCanceled(data);
        releaseResources();
    }

    private void releaseResources() {
        Log.d();
        mData = null;
    }
}
