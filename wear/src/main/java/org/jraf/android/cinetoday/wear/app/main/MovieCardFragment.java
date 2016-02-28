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
package org.jraf.android.cinetoday.wear.app.main;

import java.util.Calendar;

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
import org.jraf.android.cinetoday.common.model.movie.Movie;
import org.jraf.android.cinetoday.common.model.movie.Showtime;
import org.jraf.android.cinetoday.wear.app.Application;

public class MovieCardFragment extends CardFragment {
    public enum CardType {
        MAIN,
        SYNOPSIS,
        SHOWTIMES,
    }

    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        CardType cardType = (CardType) args.getSerializable("cardType");
        assert cardType != null;
        Movie movie = args.getParcelable("movie");
        assert movie != null;

        View view = null;
        switch (cardType) {
            case MAIN:
                if (Application.sIsRound) {
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
                if (movie.actors == null || movie.actors.isEmpty()) {
                    txtActors.setVisibility(View.GONE);
                } else {
                    txtActors.setVisibility(View.VISIBLE);
                    txtActors.setText(getHtml(R.string.movie_card_actors, movie.actors));
                }
                break;

            case SYNOPSIS:
                if (Application.sIsRound) {
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
                String durationStr = formatDuration(movie.durationSeconds);
                txtDuration.setText(getHtml(R.string.movie_card_duration, durationStr));

                // Original title
                TextView txtOriginalTitle = (TextView) view.findViewById(R.id.txtOriginalTitle);
                if (movie.originalTitle.equals(movie.localTitle)) {
                    txtOriginalTitle.setVisibility(View.GONE);
                } else {
                    txtOriginalTitle.setText(getHtml(R.string.movie_card_originalTitle, movie.originalTitle));
                }
                break;

            case SHOWTIMES:
                if (Application.sIsRound) {
                    view = inflater.inflate(R.layout.movie_card_showtimes_round, container, false);
                } else {
                    view = inflater.inflate(R.layout.movie_card_showtimes_square, container, false);
                }

                // Show times
                ViewGroup conShowtimes = (ViewGroup) view.findViewById(R.id.conShowtimes);
                addShowtimes(conShowtimes, movie, inflater);
                break;
        }

        return view;
    }

    private String formatDuration(int durationSeconds) {
        if (durationSeconds < 60 * 60) return getString(R.string.durationMinutes, durationSeconds / 60);
        int hours = durationSeconds / (60 * 60);
        int minutes = (durationSeconds % (60 * 60)) / 60;
        if (minutes == 0) return getResources().getQuantityString(R.plurals.durationHours, hours, hours);
        return getString(R.string.durationHoursMinutes, hours, minutes);
    }

    private void addShowtimes(ViewGroup conShowtimes, Movie movie, LayoutInflater inflater) {
        Calendar nowCalendar = Calendar.getInstance();
        Calendar showtimeCalendar = Calendar.getInstance();
        Showtime[] todayShowtimes = movie.todayShowtimes;
        for (Showtime todayShowtime : todayShowtimes) {
            String[] hourMinutes = todayShowtime.time.split(":");
            showtimeCalendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hourMinutes[0]));
            showtimeCalendar.set(Calendar.MINUTE, Integer.valueOf(hourMinutes[1]));
            boolean isTooLate = showtimeCalendar.before(nowCalendar);

            View conShowtimeItem = inflater.inflate(R.layout.movie_card_showtime_item, conShowtimes, false);
            TextView txtShowtime = (TextView) conShowtimeItem.findViewById(R.id.txtShowtime);
            txtShowtime.setText(todayShowtime.time);
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

    private CharSequence getHtml(@StringRes int stringResId, Object... args) {
        return Html.fromHtml(getString(stringResId, args));
    }

    public static MovieCardFragment create(CardType cardType, Movie movie) {
        MovieCardFragment res = new MovieCardFragment();
        Bundle args = new Bundle();
        args.putSerializable("cardType", cardType);
        args.putParcelable("movie", movie);
        res.setArguments(args);
        return res;
    }
}
