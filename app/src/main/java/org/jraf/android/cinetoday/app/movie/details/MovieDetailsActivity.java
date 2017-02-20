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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.MovieDetailsBinding;
import org.jraf.android.cinetoday.provider.showtime.ShowtimeCursor;
import org.jraf.android.cinetoday.provider.showtime.ShowtimeSelection;
import org.jraf.android.util.ui.animation.AnimationUtil;

public class MovieDetailsActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_MOVIE = 0;
    private static final int LOADER_SHOWTIMES = 1;

    private static DateFormat sTimeFormat;

    private MovieDetailsBinding mBinding;
    private ArrayList<TextView> mTxtTheaterNameList = new ArrayList<>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_details);
        getSupportLoaderManager().initLoader(LOADER_MOVIE, null, this);
        getSupportLoaderManager().initLoader(LOADER_SHOWTIMES, null, this);

        mBinding.conMovie.setOnScrollChangeListener(mOnScrollChangeListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri movieUri = getIntent().getData();
        switch (id) {
            case LOADER_MOVIE:
                return new CursorLoader(this, movieUri, null, null, null, null);

            case LOADER_SHOWTIMES:
                return new ShowtimeSelection()
                        .movieId(ContentUris.parseId(movieUri))
                        .orderByTheaterId()
                        .getCursorLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_MOVIE:
                mBinding.pgbLoading.setVisibility(View.GONE);
                mBinding.conMovie.setVisibility(View.VISIBLE);
                MovieViewModel movieViewModel = new MovieViewModel(this, data);
                movieViewModel.moveToFirst();
                mBinding.setMovie(movieViewModel);

                // Use the movie color in certain elements
                Integer color = movieViewModel.getColor();
                if (color == null) color = getColor(R.color.background);
                mBinding.getRoot().setBackgroundColor(color);
                mBinding.txtTheaterNameInvisible.setBackgroundColor(color);
                int gradientColors[] = {color, 0};
                GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, gradientColors);
                mBinding.vieTheaterNameGradient.setBackgroundDrawable(gradientDrawable);
                break;

            case LOADER_SHOWTIMES:
                ShowtimeCursor showtimeCursor = (ShowtimeCursor) data;
                showtimeCursor.moveToPosition(-1);
                long theaterId = -1;
                Calendar now = Calendar.getInstance();
                LayoutInflater inflater = LayoutInflater.from(this);
                while (showtimeCursor.moveToNext()) {
                    // Theater name
                    if (showtimeCursor.getTheaterId() != theaterId) {
                        theaterId = showtimeCursor.getTheaterId();
                        TextView txtTheaterName =
                                (TextView) inflater.inflate(R.layout.movie_details_theater_name, mBinding.conMovieDetails, false);
                        txtTheaterName.setText(showtimeCursor.getTheaterName());
                        mBinding.conMovieDetails.addView(txtTheaterName);

                        mTxtTheaterNameList.add(txtTheaterName);
                    }

                    // Time
                    boolean isTooLate = getTimeAsCalendar(showtimeCursor.getTime().getTime()).before(now);
                    View conShowtimeItem = inflater.inflate(R.layout.movie_card_showtime_item, mBinding.conMovieDetails, false);
                    TextView txtShowtime = (TextView) conShowtimeItem.findViewById(R.id.txtShowtime);
                    txtShowtime.setText(getTimeFormat(this).format(showtimeCursor.getTime()));
                    TextView txtIs3d = (TextView) conShowtimeItem.findViewById(R.id.txtIs3d);
                    if (showtimeCursor.getIs3d()) {
                        txtIs3d.setVisibility(View.VISIBLE);
                    } else {
                        txtIs3d.setVisibility(View.GONE);
                    }
                    if (isTooLate) {
                        conShowtimeItem.setAlpha(.33F);
                    }
                    mBinding.conMovieDetails.addView(conShowtimeItem);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    private static Calendar getTimeAsCalendar(long time) {
        Calendar res = Calendar.getInstance();
        res.set(Calendar.HOUR_OF_DAY, 0);
        res.set(Calendar.MINUTE, 0);
        res.set(Calendar.SECOND, 0);
        res.set(Calendar.MILLISECOND, 0);
        res.add(Calendar.MILLISECOND, (int) time);
        return res;
    }

    private static DateFormat getTimeFormat(Context context) {
        if (sTimeFormat == null) {
            sTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        }
        return sTimeFormat;
    }

    private View.OnScrollChangeListener mOnScrollChangeListener = new View.OnScrollChangeListener() {
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (scrollY < mTxtTheaterNameList.get(0).getY() - mBinding.txtTheaterName.getPaddingTop()) {
                mBinding.txtTheaterName.setVisibility(View.GONE);
                mBinding.conTheaterName.setVisibility(View.GONE);
                mTxtTheaterNameList.get(0).setVisibility(View.VISIBLE);
            } else {
                mBinding.txtTheaterName.setVisibility(View.VISIBLE);
                AnimationUtil.animateVisible(mBinding.conTheaterName);
                for (TextView textView : mTxtTheaterNameList) {
                    if (scrollY >= textView.getY() - mBinding.txtTheaterName.getPaddingTop()) {
                        mBinding.txtTheaterName.setText(textView.getText());
                        textView.setVisibility(View.INVISIBLE);
                    } else {
                        textView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };
}