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

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.MoviePosterBinding;

public class PosterFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MoviePosterBinding binding = DataBindingUtil.inflate(inflater, R.layout.movie_poster, container, false);

        Bitmap posterBitmap = getArguments().getParcelable("poster");
        binding.imgPoster.setImageBitmap(posterBitmap);

        return binding.getRoot();
    }

    public static PosterFragment create(Bitmap poster) {
        PosterFragment res = new PosterFragment();
        Bundle args = new Bundle();
        args.putParcelable("poster", poster);
        res.setArguments(args);
        return res;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
