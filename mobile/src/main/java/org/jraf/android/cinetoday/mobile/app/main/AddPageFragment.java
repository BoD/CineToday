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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jraf.android.cinetoday.R;
import org.jraf.android.util.app.base.BaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPageFragment extends BaseFragment<MainCallbacks> {
    @Bind(R.id.txtIntro)
    protected TextView mTxtIntro;

    @Bind(R.id.btnAdd)
    protected Button mBtnAdd;

    public static Fragment newInstance(boolean isEmpty) {
        AddPageFragment res = new AddPageFragment();
        Bundle args = new Bundle();
        args.putBoolean("isEmpty", isEmpty);
        res.setArguments(args);
        return res;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.main_page_add, container, false);
        ButterKnife.bind(this, res);
        Bundle args = getArguments();
        boolean isEmpty = args.getBoolean("isEmpty");
        if (isEmpty) {
            mBtnAdd.setText(R.string.main_page_add_btnAdd_empty);
            mTxtIntro.setText(R.string.main_page_add_intro_empty);
        } else {
            mBtnAdd.setText(R.string.main_page_add_btnAdd_additional);
            mTxtIntro.setText(R.string.main_page_add_intro_additional);
        }
        return res;
    }

    @OnClick(R.id.btnAdd)
    protected void onAddClicked() {
        getCallbacks().onAddTheater();
    }
}

