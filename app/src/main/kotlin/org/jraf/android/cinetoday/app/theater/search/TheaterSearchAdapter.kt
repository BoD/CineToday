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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.TheaterSearchListItemBinding
import org.jraf.android.cinetoday.databinding.TheaterSearchListItemSearchBinding
import org.jraf.android.cinetoday.model.theater.Theater
import org.jraf.android.util.log.Log
import java.util.ArrayList

class TheaterSearchAdapter(context: Context, private val callbacks: TheaterSearchCallbacks) :
    RecyclerView.Adapter<TheaterSearchAdapter.ViewHolder>(), TextWatcher {
    companion object {
        private const val TYPE_SEARCH = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_ITEM = 2
        private const val TYPE_EMPTY = 3
    }

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var theaters: MutableList<Theater>? = null
    private var loading: Boolean = false
    private var searchQuery: String? = null

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class SearchViewHolder(val searchBinding: TheaterSearchListItemSearchBinding) : ViewHolder(searchBinding.root)

        class ItemViewHolder(val itemBinding: TheaterSearchListItemBinding) : ViewHolder(itemBinding.root)

        class GenericViewHolder(view: View) : ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return TYPE_SEARCH

            1 -> {
                if (loading) return TYPE_LOADING
                if (theaters!!.isEmpty()) return TYPE_EMPTY
                return TYPE_ITEM
            }

            else -> return TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterSearchAdapter.ViewHolder {
        return when (viewType) {
            TYPE_SEARCH -> ViewHolder.SearchViewHolder(
                DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.theater_search_list_item_search,
                    parent,
                    false
                )
            )

            TYPE_LOADING -> ViewHolder.GenericViewHolder(
                layoutInflater.inflate(
                    R.layout.theater_search_list_item_loading,
                    parent,
                    false
                )
            )

            TYPE_EMPTY -> ViewHolder.GenericViewHolder(
                layoutInflater.inflate(
                    R.layout.theater_search_list_item_empty,
                    parent,
                    false
                )
            )

            TYPE_ITEM -> ViewHolder.ItemViewHolder(
                DataBindingUtil.inflate(
                    layoutInflater,
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
                holder.searchBinding.searchQuery = searchQuery
                holder.searchBinding.executePendingBindings()
                holder.searchBinding.edtSearch.removeTextChangedListener(this)
                holder.searchBinding.edtSearch.addTextChangedListener(this)
                holder.searchBinding.edtSearch.setOnEditorActionListener(onEditorActionListener)
            }

            TYPE_ITEM -> {
                holder as ViewHolder.ItemViewHolder
                val theater = theaters!![position - 1]
                holder.itemBinding.theater = theater
                holder.itemBinding.callbacks = callbacks
                holder.itemBinding.executePendingBindings()
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        searchQuery = s.toString()
        callbacks.onSearch(s.toString())
    }

    private val onEditorActionListener = TextView.OnEditorActionListener { textView, _, _ ->
        Log.d()
        // Close keyboard
        val inputMethodManager = textView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(textView.windowToken, 0)
        true
    }

    override fun getItemCount(): Int {
        if (loading) {
            // Search + loading
            return 2
        } else if (theaters == null) {
            // Search
            return 1
        } else if (theaters!!.isEmpty()) {
            // Search + empty
            return 2
        } else {
            // Search + items
            return theaters!!.size + 1
        }
    }

    fun setResults(theaters: List<Theater>?) {
        if (this.theaters == null) {
            this.theaters = ArrayList<Theater>()
        } else {
            this.theaters!!.clear()
        }
        if (theaters != null) this.theaters!!.addAll(theaters)
        notifyDataSetChanged()
    }

    fun setLoading(loading: Boolean) {
        this.loading = loading
        notifyDataSetChanged()
    }
}
