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
package org.jraf.android.cinetoday.app.main;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.wearable.view.CardFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.model.movie.Movie;
import org.jraf.android.cinetoday.model.movie.Showtime;

public class MovieCardFragment extends CardFragment {
    public enum CardType {
        MAIN,
        SYNOPSIS,
        SHOWTIMES,;

        public static CardType fromIndex(int columnIndex) {
            if (columnIndex == MovieFragmentGridPagerAdapter.INDEX_MAIN) {
                return MovieCardFragment.CardType.MAIN;
            } else if (columnIndex == MovieFragmentGridPagerAdapter.INDEX_SYNOPSIS) {
                return MovieCardFragment.CardType.SYNOPSIS;
            } else if (columnIndex >= MovieFragmentGridPagerAdapter.INDEX_SHOWTIMES) {
                return MovieCardFragment.CardType.SHOWTIMES;
            }
            throw new IllegalArgumentException();
        }
    }

    private static DateFormat sTimeFormat;


    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        CardType cardType = (CardType) args.getSerializable("cardType");
        assert cardType != null;
        Movie movie = args.getParcelable("movie");
        assert movie != null;
        int column = args.getInt("column");
        boolean round = false;

        View view = null;
        switch (cardType) {
            case MAIN:
                if (round) {
                    view = inflater.inflate(R.layout.movie_card_main_round, container, false);
                } else {
                    view = inflater.inflate(R.layout.movie_card_main_square, container, false);
                }

                // Title
                TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
                txtTitle.setText(movie.localTitle);

                // Description
                TextView txtDescription = (TextView) view.findViewById(R.id.txtDirectors);
                if (TextUtils.isEmpty((movie.directors))) {
                    txtDescription.setVisibility(View.GONE);
                } else {
                    txtDescription.setVisibility(View.VISIBLE);
                    txtDescription.setText(getHtml(R.string.movie_card_directors, movie.directors));
                }

                // Actors
                TextView txtActors = (TextView) view.findViewById(R.id.txtActors);
                if (TextUtils.isEmpty(movie.actors)) {
                    txtActors.setVisibility(View.GONE);
                } else {
                    txtActors.setVisibility(View.VISIBLE);
                    txtActors.setText(getHtml(R.string.movie_card_actors, movie.actors));
                }
                break;

            case SYNOPSIS:
                if (round) {
                    view = inflater.inflate(R.layout.movie_card_synopsis_round, container, false);
                } else {
                    view = inflater.inflate(R.layout.movie_card_synopsis_square, container, false);
                }

                // Genres
                TextView txtGenres = (TextView) view.findViewById(R.id.txtGenres);
                String genresStr = TextUtils.join(" · ", movie.genres);
                txtGenres.setText(genresStr);

                // Synopsis
                TextView txtSynopsis = (TextView) view.findViewById(R.id.txtSynopsis);
                txtSynopsis.setText(movie.synopsis);

                // Duration
                TextView txtDuration = (TextView) view.findViewById(R.id.txtDuration);
                if (movie.durationSeconds == null) {
                    txtDuration.setVisibility(View.GONE);
                } else {
                    txtDuration.setVisibility(View.VISIBLE);
                    String durationStr = formatDuration(movie.durationSeconds);
                    txtDuration.setText(getHtml(R.string.movie_card_duration, durationStr));
                }

                // Original title
                TextView txtOriginalTitle = (TextView) view.findViewById(R.id.txtOriginalTitle);
                if (movie.originalTitle.equals(movie.localTitle)) {
                    txtOriginalTitle.setVisibility(View.GONE);
                } else {
                    txtOriginalTitle.setText(getHtml(R.string.movie_card_originalTitle, movie.originalTitle));
                }
                break;

            case SHOWTIMES:
                if (round) {
                    view = inflater.inflate(R.layout.movie_card_showtimes_round, container, false);
                } else {
                    view = inflater.inflate(R.layout.movie_card_showtimes_square, container, false);
                }

                // Theater name
                TextView txtTheaterName = (TextView) view.findViewById(R.id.txtTheaterName);

                // Show times
                ViewGroup conShowtimes = (ViewGroup) view.findViewById(R.id.conShowtimes);
                addShowtimes(conShowtimes, movie, column, txtTheaterName, inflater);
                break;
        }

        return view;
    }

    private String formatDuration(Integer durationSeconds) {
        if (durationSeconds < 60 * 60) return getString(R.string.durationMinutes, durationSeconds / 60);
        int hours = durationSeconds / (60 * 60);
        int minutes = (durationSeconds % (60 * 60)) / 60;
        if (minutes == 0) return getResources().getQuantityString(R.plurals.durationHours, hours, hours);
        return getString(R.string.durationHoursMinutes, hours, minutes);
    }

    private void addShowtimes(ViewGroup conShowtimes, Movie movie, int column, TextView txtTheaterName, LayoutInflater inflater) {
        // Remove previous pages from the row index
        column = column - MovieFragmentGridPagerAdapter.INDEX_SHOWTIMES;

        // Find the correct key, based on the column
        ArrayList<String> orderedKeys = new ArrayList<>(movie.todayShowtimes.keySet());
        String key = orderedKeys.get(column);

        // Theater name
        String theaterName = key.split("/")[1];
        txtTheaterName.setText(theaterName);

        // Showtimes
        Calendar nowCalendar = Calendar.getInstance();
        List<Showtime> todayShowtimes = movie.todayShowtimes.get(key);
        assert todayShowtimes != null;
        for (Showtime todayShowtime : todayShowtimes) {
            boolean isTooLate = false;

            View conShowtimeItem = inflater.inflate(R.layout.movie_card_showtime_item, conShowtimes, false);
            TextView txtShowtime = (TextView) conShowtimeItem.findViewById(R.id.txtShowtime);
            txtShowtime.setText(getTimeFormat(getContext()).format(todayShowtime.time));
            TextView txtIs3d = (TextView) conShowtimeItem.findViewById(R.id.txtIs3d);
            if (todayShowtime.is3d) {
                txtIs3d.setVisibility(View.VISIBLE);
            } else {
                txtIs3d.setVisibility(View.GONE);
            }
            if (isTooLate) {
                conShowtimeItem.setAlpha(.33f);
            }
            conShowtimes.addView(conShowtimeItem);
        }
    }

    private static DateFormat getTimeFormat(Context context) {
        if (sTimeFormat == null) {
            sTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        }
        return sTimeFormat;
    }

    private CharSequence getHtml(@StringRes int stringResId, Object... args) {
        return Html.fromHtml(getString(stringResId, args));
    }

    public static MovieCardFragment create(CardType cardType, Movie movie, int column) {
        MovieCardFragment res = new MovieCardFragment();
//        Bundle args = new Bundle();
//        args.putSerializable("cardType", cardType);
//        args.putParcelable("movie", movie);
//        args.putInt("column", column);
//        res.setArguments(args);
        return res;
    }
}
