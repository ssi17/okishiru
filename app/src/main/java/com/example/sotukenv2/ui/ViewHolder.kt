package com.example.sotukenv2.ui

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sotukenv2.R

class ViewHolder(val item: View): RecyclerView.ViewHolder(item) {
    val images: ImageView = item.findViewById(R.id.item_image)
    val favorites: ImageButton = item.findViewById(R.id.favorite_button)
    val titles: TextView = item.findViewById(R.id.item_title)
    val describes: TextView = item.findViewById(R.id.item_describe)
    val pages: TextView = item.findViewById(R.id.item_page)
    val maps: TextView = item.findViewById(R.id.item_map)
}