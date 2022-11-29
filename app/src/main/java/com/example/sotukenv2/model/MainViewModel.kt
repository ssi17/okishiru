package com.example.sotukenv2.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sotukenv2.database.AppDatabase
import com.example.sotukenv2.database.Favorite
import com.example.sotukenv2.database.Setting
import com.example.sotukenv2.json.Article
import com.example.sotukenv2.json.Script
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
    var scriptsArray: JSONArray? = null
    var articlesArray: JSONArray? = null

    // コンテンツのリスト
    var scripts: MutableList<Script> = mutableListOf()
    // アーティクルのリスト
    var articles: MutableList<Article> = mutableListOf()
    // 画面に表示するアーティクルリスト
    private val _displayArticles: MutableLiveData<MutableList<Article>> = MutableLiveData<MutableList<Article>>(mutableListOf())
    val displayArticles: LiveData<MutableList<Article>> = _displayArticles
    // 読み上げ終わったコンテンツのIDのリスト
    val doneScripts: MutableList<Int> = mutableListOf()

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

    fun setting() {
        val dao = db!!.settingDao()
        viewModelScope.launch {
            val settingList: List<Setting> = dao.getAll()
            settingList.forEach{ list ->
                when (list.name) {
                    "bgm" -> _bgmFlag.value = list.flag == 1
                    "history" -> _historyFlag.value = list.flag == 1
                    "trivia" -> _triviaFlag.value = list.flag == 1
                    "restaurant" -> _restaurantFlag.value = list.flag == 1
                    "tourist" -> _touristSightFlag.value = list.flag == 1
                }
            }
        }
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
    fun getScripts() {
        val scriptsArray = scriptsArray!!
        val articlesArray = articlesArray!!

        // コンテンツリスト及びアーティクルリストを初期化
        scripts = mutableListOf()
        articles = mutableListOf()

        // アーティクルIDを格納するセット
        val articleIds: MutableSet<Int> = mutableSetOf()

        // JSONを扱うためのクラス
        val moshi1 = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        // JSONのデータを格納するクラスを指定
        val adapter1 = moshi1.adapter(Script::class.java)

        // コンテンツリストの作成
        for(i in 0 until scriptsArray.length()) {
            // 市町村名を取得
            val cityName = scriptsArray.getJSONObject(i).getString("city").toString()
            // 指定された市町村のコンテンツを取得
            if(cityName == city) {
                if(when(scriptsArray.getJSONObject(i).getString("category").toString()) {
                        "観光スポット" -> _touristSightFlag.value!!
                        "飲食店" -> _restaurantFlag.value!!
                        "歴史" -> _historyFlag.value!!
                        else -> _triviaFlag.value!!
                    }) {
                    // Scriptインスタンスを作成し、リストに保存
                    val obj = adapter1.fromJson(scriptsArray.getJSONObject(i).toString()) as Script
                    scripts.add(obj)

                    // アーティクルIDを取得
                    for(id in obj.articleId) {
                        articleIds.add(id)
                    }
                }
            }
        }

        // 音声読み上げの順序をシャッフル
        scripts.shuffle()

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
            if(flagList[i].flag == 1) {
                favoriteArticles.add(adapter.fromJson(articlesArray!!.getJSONObject(i).toString()) as Article)
            }
        }
    }

    fun setDisplayArticles(list: MutableList<Article>) {
        _displayArticles.postValue(list)
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
        val flag =
            if(flagList[id-1].flag == 0) {
                1
            } else {
                0
            }
        val dao = db!!.favoriteDao()
        viewModelScope.launch {
            dao.changeFlag(id, flag)
        }
    }
}