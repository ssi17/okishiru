package com.example.sotukenv2.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sotukenv2.database.AppDatabase
import com.example.sotukenv2.database.Favorite
import com.example.sotukenv2.json.Article
import com.example.sotukenv2.json.Content
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import org.json.JSONArray

class MainViewModel : ViewModel() {

    var startFlag = false

    // 設定画面のスイッチの状態を保持
    // BGM
    private val _bgmFlag = MutableLiveData<Boolean>()
    val bgmFlag: LiveData<Boolean> = _bgmFlag
    // 歴史
    private val _historyFlag = MutableLiveData<Boolean>()
    val historyFlag: LiveData<Boolean> = _historyFlag
    // 雑学
    private val _triviaFlag = MutableLiveData<Boolean>()
    val triviaFlag: LiveData<Boolean> = _triviaFlag
    // 飲食店
    private val _restaurantFlag = MutableLiveData<Boolean>()
    val restaurantFlag: LiveData<Boolean> = _restaurantFlag
    // 観光スポット
    private val _touristSightFlag = MutableLiveData<Boolean>()
    val touristSightFlag: LiveData<Boolean> = _touristSightFlag

    // jsonファイル
    var contentsArray: JSONArray? = null
    var articlesArray: JSONArray? = null

    // コンテンツのリスト
    var contents: MutableList<Content> = mutableListOf()
    // アーティクルのリスト
    var articles: MutableList<Article> = mutableListOf()
    // 画面に表示するアーティクルリスト
    private val _displayArticles: MutableLiveData<MutableList<Article>> = MutableLiveData<MutableList<Article>>(mutableListOf())
    val displayArticles: LiveData<MutableList<Article>> = _displayArticles
    // 読み上げ終わったコンテンツのIDのリスト
    val doneContents: MutableList<Int> = mutableListOf()

    // お気に入り画面用のアーティクルリスト
    var favoriteArticles: MutableList<Article> = mutableListOf()

    // データベースを操作するインスタンス
    var db: AppDatabase? = null
    // お気に入り登録状況を保持するリスト
    val flagList: MutableList<Favorite> = mutableListOf()

    // 現在の市町村情報
    var city: String = ""

    // 設定画面のスイッチの初期値
    init {
        _bgmFlag.value = true
        _touristSightFlag.value = false
        _restaurantFlag.value = false
        _historyFlag.value = true
        _triviaFlag.value = true
    }

    // 情報発信のON/OFF
    fun changeStartFlag() {
        startFlag = !startFlag
    }

    // BGMのON/OFF
    fun switchBgmFlag() {
        _bgmFlag.value = !_bgmFlag.value!!
    }

    // 歴史のON/OFF
    fun switchHistoryFlag() {
        _historyFlag.value = !_historyFlag.value!!
    }

    // 雑学のON/OFF
    fun switchTriviaFlag() {
        _triviaFlag.value = !_triviaFlag.value!!
    }

    // 飲食店のON/OFF
    fun switchRestaurantFlag() {
        _restaurantFlag.value = !_restaurantFlag.value!!
    }

    // 観光スポットのON/OFF
    fun switchTouristSightFlag() {
        _touristSightFlag.value = !_touristSightFlag.value!!
    }

    // JSONファイルから記事を取得する処理
    fun getContents() {
        val contentsArray = contentsArray!!
        val articlesArray = articlesArray!!

        // コンテンツリスト及びアーティクルリストを初期化
        contents = mutableListOf()
        articles = mutableListOf()

        // アーティクルIDを格納するセット
        val articleIds: MutableSet<Int> = mutableSetOf()

        // JSONを扱うためのクラス
        val moshi1 = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        // JSONのデータを格納するクラスを指定
        val adapter1 = moshi1.adapter(Content::class.java)

        // コンテンツリストの作成
        for(i in 0 until contentsArray.length()) {
            // 市町村名を取得
            val cityName = contentsArray.getJSONObject(i).getString("city").toString()
            // 指定された市町村のコンテンツを取得
            if(cityName == city) {
                if(when(contentsArray.getJSONObject(i).getString("category").toString()) {
                        "観光スポット" -> _touristSightFlag.value!!
                        "飲食店" -> _restaurantFlag.value!!
                        "歴史" -> _historyFlag.value!!
                        else -> _triviaFlag.value!!
                    }) {
                    // Contentインスタンスを作成し、リストに保存
                    val obj = adapter1.fromJson(contentsArray!!.getJSONObject(i).toString()) as Content
                    contents.add(obj)

                    // アーティクルIDを取得
                    for(id in obj.articleId) {
                        articleIds.add(id)
                    }
                }
            }
        }

        val moshi2 = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter2 = moshi2.adapter(Article::class.java)

        // アーティクルリストを作成
        for(i in 0 until articlesArray.length()) {
            val id = articlesArray.getJSONObject(i).getInt("id")
            // コンテンツに紐づけられたIDかどうかを判断
            if(articleIds.contains(id)) {
                // リストに保存
                articles.add(adapter2.fromJson(articlesArray.getJSONObject(i).toString()) as Article)
            }
        }
    }

    fun getFavoriteArticle() {
        // 初期化
        favoriteArticles = mutableListOf()

        // JSONからインスタンスを作成する準備
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(Article::class.java)

        for(i in 0 until flagList.size) {
            if(flagList[i].flag) {
                favoriteArticles.add(adapter.fromJson(articlesArray!!.getJSONObject(i).toString()) as Article)
            }
        }
    }

    fun setDisplayArticles(list: MutableList<Article>) {
        _displayArticles.postValue(list)
    }

    // お気に入り登録情報をデータベースに追加
    fun addFlag(id: Int) {
        val dao = db!!.favoriteDao()
        viewModelScope.launch {
            dao.addFlag(Favorite(id, false))
        }
    }

    // お気に入り登録情報をデータベースから取得
    fun getAllFlag() {
        val dao = db!!.favoriteDao()
        viewModelScope.launch {
            val list = dao.getAll()
            for(li in list) {
                flagList.add(li)
            }
        }
    }

    // データベースのお気に入り登録状況を更新
    fun changeFavoriteFlag(id: Int) {
        val flag = !flagList[id-1].flag
        val dao = db!!.favoriteDao()
        viewModelScope.launch {
            dao.changeFlag(id, flag)
        }
    }
}