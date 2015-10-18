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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.moviestoday.R;

public class MovieCardFragment extends CardFragment {
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.movie_card, container, false);
        Bundle args = this.getArguments();
        if (args != null) {
            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            txtTitle.setText(args.getCharSequence("title"));

            TextView txtDescription = (TextView) view.findViewById(R.id.txtDirectors);
            txtDescription.setText(getHtml(R.string.movie_card_directors, args.getCharSequence("directors")));

            TextView txtActors = (TextView) view.findViewById(R.id.txtActors);
            txtActors.setText(getHtml(R.string.movie_card_actors, args.getCharSequence("actors")));
        }
        return view;
    }

    private CharSequence getHtml(@StringRes int stringResId, Object... args) {
        return Html.fromHtml(getString(stringResId, args));
    }

    public static MovieCardFragment create(CharSequence title, CharSequence directors, CharSequence actors) {
        MovieCardFragment fragment = new MovieCardFragment();
        Bundle args = new Bundle();
        args.putCharSequence("title", title);
        args.putCharSequence("directors", directors);
        args.putCharSequence("actors", actors);
        fragment.setArguments(args);
        return fragment;
    }
}
