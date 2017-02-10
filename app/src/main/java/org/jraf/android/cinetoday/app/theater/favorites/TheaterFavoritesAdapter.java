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
package org.jraf.android.cinetoday.app.theater.favorites;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.TheaterFavoriteListItemBinding;
import org.jraf.android.cinetoday.provider.theater.TheaterCursor;

public class TheaterFavoritesAdapter extends RecyclerView.Adapter<TheaterFavoritesAdapter.ViewHolder> {
    private final Context mContext;
    private TheaterFavoritesCallbacks mCallbacks;
    private final LayoutInflater mLayoutInflater;
    private TheaterCursor mCursor;

    public TheaterFavoritesAdapter(Context context, TheaterFavoritesCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TheaterFavoriteListItemBinding itemBinding;

        public ViewHolder(TheaterFavoriteListItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }

    @Override
    public TheaterFavoritesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TheaterFavoriteListItemBinding binding = DataBindingUtil.inflate(mLayoutInflater, R.layout.theater_favorite_list_item, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TheaterFavoritesAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.itemBinding.setTheater(mCursor);
        holder.itemBinding.setCallbacks(mCallbacks);
        holder.itemBinding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(TheaterCursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
