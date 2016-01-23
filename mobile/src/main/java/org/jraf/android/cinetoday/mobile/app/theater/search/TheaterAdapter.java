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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.common.model.theater.Theater;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

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
        @Bind(R.id.txtName)
        public TextView txtName;

        @Bind(R.id.txtAddress)
        public TextView txtAddress;

        @Bind(R.id.imgThumbnail)
        public ImageView imgThumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    @Override
    public TheaterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mLayoutInflater.inflate(R.layout.theater_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TheaterAdapter.ViewHolder holder, int position) {
        Theater theater = mObjects.get(position);
        holder.txtName.setText(theater.name);
        holder.txtAddress.setText(theater.address);
        Picasso.with(mContext).load(theater.posterUri).fit().centerCrop().placeholder(R.drawable.theater_list_item_placeholder).error(
                R.drawable.theater_list_item_placeholder).noFade().into(holder.imgThumbnail);

        // Callback
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            Theater theater = mObjects.get(position);
            mCallbacks.onTheaterClicked(theater);
        }
    };

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
