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
package org.jraf.android.cinetoday.app.theater.search;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.cinetoday.R;
import org.jraf.android.cinetoday.databinding.TheaterSearchListItemBinding;
import org.jraf.android.cinetoday.databinding.TheaterSearchListItemSearchBinding;
import org.jraf.android.cinetoday.model.theater.Theater;

public class TheaterSearchAdapter extends RecyclerView.Adapter<TheaterSearchAdapter.ViewHolder> implements TextWatcher {
    private static final int TYPE_SEARCH = 0;
    private static final int TYPE_LOADING = 1;
    private static final int TYPE_ITEM = 2;
    private static final int TYPE_EMPTY = 3;

    private final Context mContext;
    private TheaterSearchCallbacks mCallbacks;
    private final LayoutInflater mLayoutInflater;
    private List<Theater> mTheaters;
    private boolean mLoading;
    private String mSearchQuery;


    public TheaterSearchAdapter(Context context, TheaterSearchCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TheaterSearchListItemBinding itemBinding;
        public final TheaterSearchListItemSearchBinding searchBinding;

        public ViewHolder(TheaterSearchListItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
            searchBinding = null;
        }

        public ViewHolder(TheaterSearchListItemSearchBinding searchBinding) {
            super(searchBinding.getRoot());
            itemBinding = null;
            this.searchBinding = searchBinding;
        }

        public ViewHolder(View root) {
            super(root);
            itemBinding = null;
            searchBinding = null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_SEARCH;

            case 1:
                if (mLoading) return TYPE_LOADING;
                if (mTheaters.isEmpty()) return TYPE_EMPTY;
                return TYPE_ITEM;

            default:
                return TYPE_ITEM;
        }
    }

    @Override
    public TheaterSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_SEARCH:
                return new ViewHolder(
                        (TheaterSearchListItemSearchBinding) DataBindingUtil.inflate(mLayoutInflater, R.layout.theater_search_list_item_search, parent, false));

            case TYPE_LOADING:
                return new ViewHolder(mLayoutInflater.inflate(R.layout.theater_search_list_item_loading, parent, false));

            case TYPE_EMPTY:
                return new ViewHolder(mLayoutInflater.inflate(R.layout.theater_search_list_item_empty, parent, false));

            case TYPE_ITEM:
            default:
                return new ViewHolder(
                        (TheaterSearchListItemBinding) DataBindingUtil.inflate(mLayoutInflater, R.layout.theater_search_list_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(TheaterSearchAdapter.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_SEARCH:
                holder.searchBinding.setSearchQuery(mSearchQuery);
                holder.searchBinding.executePendingBindings();
                holder.searchBinding.edtSearch.removeTextChangedListener(this);
                holder.searchBinding.edtSearch.addTextChangedListener(this);
                break;

            case TYPE_ITEM:
                Theater theater = mTheaters.get(position - 1);
                holder.itemBinding.setTheater(theater);
                holder.itemBinding.setCallbacks(mCallbacks);
                holder.itemBinding.executePendingBindings();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mSearchQuery = s.toString();
        mCallbacks.onSearch(mSearchQuery);
    }

    @Override
    public int getItemCount() {
        if (mLoading) {
            // Search + loading
            return 2;
        } else if (mTheaters == null) {
            // Search
            return 1;
        } else if (mTheaters.isEmpty()) {
            // Search + empty
            return 2;
        } else {
            // Search + items
            return mTheaters.size() + 1;
        }
    }

    public void setResults(List<Theater> theaters) {
        if (mTheaters == null) {
            mTheaters = new ArrayList<>();
        } else {
            mTheaters.clear();
        }
        if (theaters != null) mTheaters.addAll(theaters);
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        mLoading = loading;
//        if (loading && mTheaters != null) mTheaters.clear();
        notifyDataSetChanged();
    }
}
