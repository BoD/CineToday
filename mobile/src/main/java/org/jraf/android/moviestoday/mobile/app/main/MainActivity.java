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
package org.jraf.android.moviestoday.mobile.app.main;

import java.util.Date;
import java.util.Set;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.jraf.android.moviestoday.R;
import org.jraf.android.moviestoday.common.model.movie.Movie;
import org.jraf.android.moviestoday.mobile.api.Api;
import org.jraf.android.moviestoday.mobile.api.CallResult;
import org.jraf.android.util.log.wrapper.Log;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnCall)
    protected void onCallClicked() {
        Api.get().getMovieList("C2954", new Date(), new CallResult<Set<Movie>>() {
            @Override
            public void onResult(Set<Movie> result) {
                for (Movie movie : result) {
                    Log.d(movie.toString());
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.e("Could not make call", error);
            }
        });
    }
}
