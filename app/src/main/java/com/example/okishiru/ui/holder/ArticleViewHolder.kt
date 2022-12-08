package com.example.okishiru.ui.holder

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.okishiru.R

class ArticleViewHolder(val item: View): RecyclerView.ViewHolder(item) {
    val images: ImageView = item.findViewById(R.id.itemImage)
    val favorites: ImageButton = item.findViewById(R.id.favoriteButton)
    val titles: TextView = item.findViewById(R.id.itemTitle)
    val describes: TextView = item.findViewById(R.id.itemDescribe)
    val pageGroup: LinearLayout = item.findViewById(R.id.page)
    val mapGroup: LinearLayout = item.findViewById(R.id.map)
}
