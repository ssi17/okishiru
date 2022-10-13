package com.example.sotukenv2

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.room.Room
import com.example.sotukenv2.database.AppDatabase
import com.example.sotukenv2.json.Article
import com.example.sotukenv2.model.MainViewModel
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayDeque

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var navController: NavController
    private val sharedViewModel: MainViewModel by viewModels()
    var bgm: MediaPlayer = MediaPlayer()
    private lateinit var tts: TextToSpeech

    // 位置情報関係の変数
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var requestingLocationUpdates: Boolean = false

    var changeContents: Boolean = false

    //位置情報使用の権限許可を確認
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 使用が許可された
            requestingLocationUpdates = true
        } else {
            // それでも拒否された時の対応
            val toast = Toast.makeText(
                this,
                "これ以上なにもできません",
                Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }

    @SuppressLint("RestrictedApi", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ナビゲーションの設定
        // 下部ナビゲーション
        val bottomNavigation: BottomNavigationView= findViewById(R.id.bottom_navigation)

        // フラグメント間の遷移
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(bottomNavigation, navController)

        // 下部ナビゲーションの設定
        bottomNavigation.background = null
        bottomNavigation.menu.getItem(2).isEnabled = false

        // BGMの設定
        bgm = MediaPlayer.create(this, R.raw.bgm)
        bgm.isLooping = true    // ループ再生をON

        // TextToSpeechの初期化
        tts = TextToSpeech(this, this)

        // JSONファイルを取得し、ViewModelへ保存
        val assetManager = resources.assets

        // Contents.json
        val contentsFile = assetManager.open("Contents.json")
        var br = BufferedReader(InputStreamReader(contentsFile))
        val contentsArray = JSONArray(br.readText())

        // Articles.json
        val articlesFile = assetManager.open("Articles.json")
        br = BufferedReader(InputStreamReader(articlesFile))
        val articlesArray = JSONArray(br.readText())

        sharedViewModel.contentsArray = contentsArray
        sharedViewModel.articlesArray = articlesArray

        // データベース
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "app-database"
        )
            .createFromAsset("app-database.db")
            .build()
        sharedViewModel.db = db
        sharedViewModel.getAllFlag()

        // FABで再生ボタンの処理を行う
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            sharedViewModel.changeStartFlag()
            if(sharedViewModel.startFlag) {
                fab.setImageResource(R.drawable.ic_pause)
                if(sharedViewModel.bgmFlag.value!!) {
                    bgm.start()
                }
                if(sharedViewModel.contents.size != 0) {
                    startSpeech()
                }
            } else {
                if(sharedViewModel.bgmFlag.value!!) {
                    bgm.pause()
                }
                onPause()
                fab.setImageResource(R.drawable.ic_play)
            }
        }

        //位置情報の権限許可
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            requestingLocationUpdates = true
        }

        locationRequest = LocationRequest.create()
        locationRequest.setPriority(
            LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(30000).interval = 30000

        //位置情報に変更があったら呼び出される
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for(location in locationResult.locations) {
                    //address取得
                    getAddress(location.latitude, location.longitude)
                }
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    // 音声読み上げ機能
    @SuppressLint("NewApi")
    fun startSpeech() {
        if(tts.isSpeaking) {
            tts.stop()
        }

        val articles = sharedViewModel.articles
        val contents = sharedViewModel.contents

        val list: MutableList<Article> = sharedViewModel.displayArticles.value!!

        tts.setOnUtteranceProgressListener(object: UtteranceProgressListener() {
            override fun onDone(id: String) {
                // BGMの音量を戻す
                bgm.setVolume(1.0F, 1.0F)
                // 読み上げが終わったコンテンツのリストを更新
                sharedViewModel.doneContents.add(Integer.parseInt(id))
                if(changeContents) {
                    changeContents = false
                    startSpeech()
                }
            }

            override fun onError(id: String) {
            }

            override fun onStart(id: String) {
                // BGMの音量を下げる
                bgm.setVolume(0.1F, 0.1F)
                // 読み上げられているコンテンツの記事を記事リストから取得
                for(content in contents) {
                    // 読み上げているコンテンツを探索
                    if(content.id == Integer.parseInt(id)) {
                        for(article in articles) {
                            // 記事リストの中からこのコンテンツに関する記事を探索
                            if(content.articleId.contains(article.id)) {
                                // すでに同じ記事が表示されていればリストから削除
                                if(list.contains(article)) {
                                    list.remove(article)
                                }
                                // リストに記事を追加
                                list.add(0, article)
                                // リストのサイズが10を超えていたら最後の要素を削除
                                if(list.size > 10) {
                                    list.removeAt(10)
                                }
                            }
                        }
                        break
                    }
                }
                // 画面に表示する記事のリストを更新
                sharedViewModel.setDisplayArticles(list)
            }
        })

        for(content in sharedViewModel.contents) {
            // 読み上げが終わっているコンテンツなら処理をスキップ
            if(sharedViewModel.doneContents.contains(content.id)) {
                continue
            }
            // 音声データの取得
            val text = content.voice
            // 音声読み上げのキューに追加
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, "${content.id}")
        }
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS) {
            tts.let { tts ->
                val locale = Locale.getDefault()
                if(tts.isLanguageAvailable(locale) > TextToSpeech.LANG_AVAILABLE) {
                    tts.language = locale
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        tts.stop()
        bgm.setVolume(1.0F, 1.0F)
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    fun ttsState(): Boolean {
        return tts.isSpeaking
    }

    //緯度経度をもとに住所の取得
    private fun getAddress(lat: Double, lng: Double) {
        val geocoder = Geocoder(this)
        val address = geocoder.getFromLocation(lat, lng, 1)

        val city = address[0].locality.toString()

        if(sharedViewModel.city != city) {
            sharedViewModel.city = city
            sharedViewModel.getContents()
            if(sharedViewModel.startFlag) {
                changeContents = true
            }
        }
    }

    // -----位置情報-----
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if(requestingLocationUpdates) {
            startLocationUpdates()
        }
    }
    // -----位置情報-----
}