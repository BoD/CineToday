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
package org.jraf.android.cinetoday.app.theater.favorites

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.jraf.android.cinetoday.R
import org.jraf.android.cinetoday.databinding.TheaterFavoriteListItemBinding
import org.jraf.android.cinetoday.model.theater.Theater

class TheaterFavoritesAdapter(context: Context) : RecyclerView.Adapter<TheaterFavoritesAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var data: Array<Theater> = emptyArray()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(val itemBinding: TheaterFavoriteListItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TheaterFavoritesAdapter.ViewHolder {
        val binding = DataBindingUtil.inflate<TheaterFavoriteListItemBinding>(
            layoutInflater,
            R.layout.theater_favorite_list_item,
            parent,
            false
        )!!
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TheaterFavoritesAdapter.ViewHolder, position: Int) {
        holder.itemBinding.theater = data[position]
        holder.itemBinding.executePendingBindings()
    }

    override fun getItemCount() = data.size

    override fun getItemId(position: Int) =
        if (data.isEmpty()) RecyclerView.NO_ID else data[position].id.hashCode().toLong()
}
