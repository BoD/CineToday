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
package org.jraf.android.moviestoday.wear.app.main;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.wearable.view.CardFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.movie.Movie;

public class MovieCardFragment extends CardFragment {
    public enum CardType {
        MAIN,
        SYNOPSIS,
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
                view = inflater.inflate(R.layout.movie_card_main, container, false);

                TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
                txtTitle.setText(movie.localTitle);

                TextView txtDescription = (TextView) view.findViewById(R.id.txtDirectors);
                txtDescription.setText(getHtml(R.string.movie_card_directors, movie.directors));

                TextView txtActors = (TextView) view.findViewById(R.id.txtActors);
                txtActors.setText(getHtml(R.string.movie_card_actors, movie.actors));
                break;

            case SYNOPSIS:
                view = inflater.inflate(R.layout.movie_card_synopsis, container, false);

                TextView txtGenres = (TextView) view.findViewById(R.id.txtGenres);
                String genresStr = TextUtils.join(" · ", movie.genres);
                txtGenres.setText(genresStr);

                TextView txtSynopsis = (TextView) view.findViewById(R.id.txtSynopsis);
                txtSynopsis.setText(movie.synopsis);

                TextView txtOriginalTitle = (TextView) view.findViewById(R.id.txtOriginalTitle);
                txtOriginalTitle.setText(getHtml(R.string.movie_card_originalTitle, movie.originalTitle));
                break;
        }

        return view;
    }

    private CharSequence getHtml(@StringRes int stringResId, Object... args) {
        return Html.fromHtml(getString(stringResId, args));
    }

    public static MovieCardFragment create(CardType cardType, Movie movie) {
        MovieCardFragment fragment = new MovieCardFragment();
        Bundle args = new Bundle();
        args.putSerializable("cardType", cardType);
        args.putParcelable("movie", movie);
        fragment.setArguments(args);
        return fragment;
    }
}
