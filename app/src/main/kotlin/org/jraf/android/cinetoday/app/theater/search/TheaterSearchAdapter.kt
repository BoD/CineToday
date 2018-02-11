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
package org.jraf.android.cinetoday.app.theater.search

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.TheaterSearchListItemBinding
import org.jraf.android.cinetoday.databinding.TheaterSearchListItemSearchBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.util.log.Log
import java.util.ArrayList


class TheaterSearchAdapter(context: Context, private val mCallbacks: TheaterSearchCallbacks) : RecyclerView.Adapter<TheaterSearchAdapter.ViewHolder>(), TextWatcher {
    companion object {
        private const val TYPE_SEARCH = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_ITEM = 2
        private const val TYPE_EMPTY = 3
    }

    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var mTheaters: MutableList<Theater>? = null
    private var mLoading: Boolean = false
    private var mSearchQuery: String? = null

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class SearchViewHolder(val searchBinding: TheaterSearchListItemSearchBinding) : ViewHolder(searchBinding.root)

        class ItemViewHolder(val itemBinding: TheaterSearchListItemBinding) : ViewHolder(itemBinding.root)

        class GenericViewHolder(view: View) : ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return TYPE_SEARCH

            1 -> {
                if (mLoading) return TYPE_LOADING
                if (mTheaters!!.isEmpty()) return TYPE_EMPTY
                return TYPE_ITEM
            }

            else -> return TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterSearchAdapter.ViewHolder {
        return when (viewType) {
            TYPE_SEARCH -> ViewHolder.SearchViewHolder(
                DataBindingUtil.inflate(
                    mLayoutInflater,
                    R.layout.theater_search_list_item_search,
                    parent,
                    false
                )
            )

            TYPE_LOADING -> ViewHolder.GenericViewHolder(mLayoutInflater.inflate(R.layout.theater_search_list_item_loading, parent, false))

            TYPE_EMPTY -> ViewHolder.GenericViewHolder(mLayoutInflater.inflate(R.layout.theater_search_list_item_empty, parent, false))

            TYPE_ITEM -> ViewHolder.ItemViewHolder(
                DataBindingUtil.inflate(
                    mLayoutInflater,
                    R.layout.theater_search_list_item,
                    parent,
                    false
                )
            )

            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: TheaterSearchAdapter.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when (viewType) {
            TYPE_SEARCH -> {
                holder as ViewHolder.SearchViewHolder
                holder.searchBinding.searchQuery = mSearchQuery
                holder.searchBinding.executePendingBindings()
                holder.searchBinding.edtSearch.removeTextChangedListener(this)
                holder.searchBinding.edtSearch.addTextChangedListener(this)
                holder.searchBinding.edtSearch.setOnEditorActionListener(mOnEditorActionListener)
            }

            TYPE_ITEM -> {
                holder as ViewHolder.ItemViewHolder
                val theater = mTheaters!![position - 1]
                holder.itemBinding.theater = theater
                holder.itemBinding.callbacks = mCallbacks
                holder.itemBinding.executePendingBindings()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        mSearchQuery = s.toString()
        mCallbacks.onSearch(s.toString())
    }

    private val mOnEditorActionListener = TextView.OnEditorActionListener { textView, _, _ ->
        Log.d()
        // Close keyboard
        val inputMethodManager = textView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)
        true
    }

    override fun getItemCount(): Int {
        if (mLoading) {
            // Search + loading
            return 2
        } else if (mTheaters == null) {
            // Search
            return 1
        } else if (mTheaters!!.isEmpty()) {
            // Search + empty
            return 2
        } else {
            // Search + items
            return mTheaters!!.size + 1
        }
    }

    fun setResults(theaters: List<Theater>?) {
        if (mTheaters == null) {
            mTheaters = ArrayList<Theater>()
        } else {
            mTheaters!!.clear()
        }
        if (theaters != null) mTheaters!!.addAll(theaters)
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        mLoading = loading
        //        if (loading && mTheaters != null) mTheaters.clear();
        notifyDataSetChanged()
    }
}
