/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.cinetoday.mobile.app.main;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.MainPageTheaterBinding;
import org.jraf.android.cinetoday.mobile.provider.theater.TheaterCursor;
import org.jraf.android.util.app.base.BaseFragment;
import org.jraf.android.util.dialog.AlertDialogFragment;
import org.jraf.android.util.dialog.AlertDialogListener;

import com.squareup.picasso.Picasso;

public class TheaterPageFragment extends BaseFragment<MainCallbacks> implements AlertDialogListener {
    private static final int DIALOG_DELETE_CONFIRM = 0;

    private long mId;

    public static Fragment newInstance(TheaterCursor theaterCursor) {
        TheaterPageFragment res = new TheaterPageFragment();
        Bundle args = new Bundle();
        args.putLong("id", theaterCursor.getId());
        args.putString("name", theaterCursor.getName());
        args.putString("address", theaterCursor.getAddress());
        args.putString("pictureUri", theaterCursor.getPictureUri());
        res.setArguments(args);
        return res;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainPageTheaterBinding binding = DataBindingUtil.inflate(inflater, R.layout.main_page_theater, container, false);
        binding.setController(this);
        Bundle args = getArguments();
        mId = args.getLong("id");
        binding.txtTheaterName.setText(args.getString("name"));
        binding.txtTheaterAddress.setText(args.getString("address"));
        String pictureUri = args.getString("pictureUri");
        Picasso.with(getContext()).load(pictureUri).placeholder(R.drawable.theater_list_item_placeholder).error(
                R.drawable.theater_list_item_placeholder).fit().centerCrop().noFade().into(binding.imgTheaterPicture);
        return binding.getRoot();
    }

    public void onNavigateClick(View v) {
        String address = getArguments().getString("address");
        try {
            address = URLEncoder.encode(address, "utf-8");
        } catch (UnsupportedEncodingException ignored) {}
        Uri uri = Uri.parse("http://maps.google.com/maps?f=d&daddr=" + address);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onWebSiteClick(View v) {
        String name = getArguments().getString("name");
        // Try to improve "I'm feeling ducky" results
        name = "cinema " + name;
        try {
            name = URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException ignored) {}
        Uri uri = Uri.parse("https://www.google.com/search?sourceid=navclient&btnI=I&q=" + name);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onDeleteClick(View v) {
        AlertDialogFragment.newInstance(DIALOG_DELETE_CONFIRM)
                .title(R.string.main_theater_delete_confirm_title)
                .message(R.string.main_theater_delete_confirm_message)
                .positiveButton(android.R.string.ok)
                .negativeButton(android.R.string.cancel)
                .show(this);
    }


    //--------------------------------------------------------------------------
    // region AlertDialogListener.
    //--------------------------------------------------------------------------

    @Override
    public void onDialogClickPositive(int tag, Object payload) {
        getCallbacks().onDeleteTheater(mId);
    }

    @Override
    public void onDialogClickNegative(int tag, Object payload) {}

    @Override
    public void onDialogClickListItem(int tag, int index, Object payload) {}

    // endregion
}

