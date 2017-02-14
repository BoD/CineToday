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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.SnapHelper;
import android.support.wearable.view.DefaultOffsettingHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.TheaterSearchListBinding;
import org.jraf.android.cinetoday.model.theater.Theater;
import org.jraf.android.cinetoday.util.ui.ScreenShapeHelper;
import org.jraf.android.util.app.base.BaseFragment;

public class TheaterSearchFragment extends BaseFragment<TheaterSearchCallbacks> implements LoaderManager.LoaderCallbacks<List<Theater>> {
    private TheaterSearchAdapter mAdapter;
    private TheaterSearchListBinding mBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.theater_search_list, container, false);
        mBinding.rclList.setHasFixedSize(true);
        mBinding.rclList.setCenterEdgeItems(true);

        // Apply an offset + scale on the items depending on their distance from the center (only for Round screens)
        if (ScreenShapeHelper.get().getIsRound()) {
            mBinding.rclList.setOffsettingHelper(new DefaultOffsettingHelper() {
                private static final float FACTOR = .75F;

                @Override
                public void updateChild(View child, WearableRecyclerView parent) {
                    super.updateChild(child, parent);

                    float childTop = child.getY() + child.getHeight() / 2F;
                    float childOffsetFromCenter = childTop - parent.getHeight() / 2F;
                    float childOffsetFromCenterRatio = Math.abs(childOffsetFromCenter / parent.getHeight());
                    float childOffsetFromCenterRatioNormalized = childOffsetFromCenterRatio * FACTOR;

                    child.setScaleX(1 - childOffsetFromCenterRatioNormalized);
                    child.setScaleY(1 - childOffsetFromCenterRatioNormalized);
                }
            });

            // Also snaps
            SnapHelper snapHelper = new LinearSnapHelper();
            snapHelper.attachToRecyclerView(mBinding.rclList);
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new TheaterSearchAdapter(getActivity(), getCallbacks());
        mBinding.rclList.setAdapter(mAdapter);
    }

    public void search(String query) {
        mAdapter.setLoading(true);
        Bundle args = new Bundle();
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
    }


    //--------------------------------------------------------------------------
    // region LoaderCallbacks.
    //--------------------------------------------------------------------------

    @Override
    public Loader<List<Theater>> onCreateLoader(int id, Bundle args) {
        String query = args.getString("query");
        return new TheaterSearchLoader(getActivity(), query);
    }

    @Override
    public void onLoadFinished(Loader<List<Theater>> loader, List<Theater> data) {
        mAdapter.setLoading(false);
        mAdapter.setResults(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Theater>> loader) {
        mAdapter.setResults(null);
    }

    // endregion
}
