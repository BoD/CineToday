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
package org.jraf.android.cinetoday.mobile.app.theater.search;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.common.model.theater.Theater;
import org.jraf.android.cinetoday.databinding.TheaterListItemBinding;

public class TheaterAdapter extends RecyclerView.Adapter<TheaterAdapter.ViewHolder> {
    private final Context mContext;
    private TheaterCallbacks mCallbacks;
    private final LayoutInflater mLayoutInflater;
    private List<Theater> mObjects = new ArrayList<>();


    public TheaterAdapter(Context context, TheaterCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TheaterListItemBinding binding;

        public ViewHolder(TheaterListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    @Override
    public TheaterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TheaterListItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.theater_list_item, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TheaterAdapter.ViewHolder holder, int position) {
        Theater theater = mObjects.get(position);
        holder.binding.setTheater(theater);
        holder.binding.setTheaterCallbacks(mCallbacks);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mObjects.size();
    }

    public void clear() {
        mObjects.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Theater> data) {
        mObjects.addAll(data);
        notifyDataSetChanged();
    }
}
