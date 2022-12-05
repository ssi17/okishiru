package com.example.sotukenv2.ui.recycler

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sotukenv2.R
import com.example.sotukenv2.database.Favorite
import com.example.sotukenv2.json.Article
import com.example.sotukenv2.ui.holder.ArticleViewHolder

class ArticleRecyclerAdapter(
    private val articles: List<Article>,
    private val flagList: List<Favorite>
): RecyclerView.Adapter<ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_view_article, parent, false)
        return ArticleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val context = holder.item.context

        // サムネイル画像を設定
        val resId = if(articles[position].img != "none") {
            context.resources.getIdentifier(
                "article_img_" + articles[position].img,
                "drawable",
                context.packageName
            )
        } else {
            R.drawable.icon
        }
        holder.images.setImageResource(resId)

        // お気に入り登録ボタンの設定
        setFavoriteButton(holder, articles[position].id - 1)

        holder.favorites.setOnClickListener {
            val id = articles[position].id
            favoriteButton?.pushFavoriteButton(id)
            flagList[id-1].flag =
                if(flagList[id-1].flag == 0) {
                    1
                } else {
                    0
                }
            setFavoriteButton(holder, id - 1)
        }

        // タイトルを設定
        holder.titles.text = articles[position].name

        // 説明を設定
        holder.describes.text = articles[position].describe

        // ウェブページのURLを取得
        val pageUrl = articles[position].url
        // URLが無ければ隠す
        if(pageUrl == "") {
            holder.pageGroup.visibility = View.INVISIBLE
        } else {
            // ページへ
            holder.pageGroup.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pageUrl))
                context.startActivity(intent)
            }
        }


        // マップのURLを取得
        val mapUrl = articles[position].map
        // URLが無ければ隠す
        if(mapUrl == "") {
            holder.mapGroup.visibility = View.INVISIBLE
        } else {
            // マップを開く
            holder.mapGroup.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                context.startActivity(intent)
            }
        }
    }

    private fun setFavoriteButton(holder: ArticleViewHolder, id: Int) {
        if(flagList[id].flag == 1) {
            holder.favorites.setImageResource(R.drawable.ic_favorite)
        } else {
            holder.favorites.setImageResource(R.drawable.ic_not_favorite)
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
