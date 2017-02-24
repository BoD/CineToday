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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.jraf.android.cinetoday.api.Api;
import org.jraf.android.cinetoday.model.theater.Theater;
import org.jraf.android.cinetoday.provider.theater.TheaterCursor;
import org.jraf.android.cinetoday.provider.theater.TheaterSelection;
import org.jraf.android.util.log.Log;

public class TheaterSearchLoader extends AsyncTaskLoader<List<Theater>> {
    private final String mQuery;
    private List<Theater> mData;

    public TheaterSearchLoader(Context context, String query) {
        super(context);
        mQuery = query;
    }

    @Override
    public List<Theater> loadInBackground() {
        Log.d();
        // Get the list of favorite theaters, so we can filter them out from the search results
        HashSet<String> favoriteTheaterPublicIds = new HashSet<>();
        try (TheaterCursor cursor = new TheaterSelection().query(getContext())) {
            while (cursor.moveToNext()) {
                favoriteTheaterPublicIds.add(cursor.getPublicId());
            }
        }
        try {
            // API call (blocking)
            List<Theater> res = Api.get(getContext()).searchTheaters(mQuery);

            // Filter out favorite theaters
            Iterator<Theater> i = res.iterator();
            while (i.hasNext()) {
                if (favoriteTheaterPublicIds.contains(i.next().id)) i.remove();
            }
            return res;
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
