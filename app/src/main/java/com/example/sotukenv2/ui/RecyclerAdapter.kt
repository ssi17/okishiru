package com.example.sotukenv2.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sotukenv2.R
import com.example.sotukenv2.database.Favorite
import com.example.sotukenv2.json.Article

class RecyclerAdapter(
    private val articles: List<Article>,
    private val flagList: List<Favorite>
): RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.item.context

        // サムネイル画像を設定
        val resId = context.resources.getIdentifier(articles[position].img, "drawable", context.packageName)
        holder.images.setImageResource(resId)

        // お気に入り登録ボタンの設定
        setFavoriteButton(holder, articles[position].id - 1)

        holder.favorites.setOnClickListener {
            val id = articles[position].id
            favoriteButton?.pushFavoriteButton(id)
            flagList[id-1].flag = !flagList[id-1].flag
            setFavoriteButton(holder, id - 1)
        }

        // タイトルを設定
        holder.titles.text = articles[position].name

        // 説明を設定
        holder.describes.text = articles[position].describe

        // ページへ
        holder.pages.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articles[position].url))
            context.startActivity(intent)
        }

        // マップを開く
        holder.maps.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articles[position].map))
            context.startActivity(intent)
        }
    }

    private fun setFavoriteButton(holder: ViewHolder, id: Int) {
        if(flagList[id].flag) {
            holder.favorites.setImageResource(R.drawable.favorite_button)
        } else {
            holder.favorites.setImageResource(R.drawable.not_favorite_button)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    var favoriteButton: FavoriteButton? = null
    interface FavoriteButton {
        fun pushFavoriteButton(id: Int)
    }
}