package com.example.sotukenv2.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.sotukenv2.MainActivity
import com.example.sotukenv2.database.AppDatabase
import com.example.sotukenv2.databinding.FragmentSettingBinding
import com.example.sotukenv2.model.MainViewModel
import kotlinx.coroutines.launch

class SettingFragment: Fragment() {

    private var binding: FragmentSettingBinding? = null
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentSettingBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            settingFragment = this@SettingFragment
            viewModel = sharedViewModel
        }
    }

    // BGMのON/OFF切り替え
    fun switchBgm() {
        // ONなのかOFF
        val checked = binding!!.soundSwitch.isChecked
        // 音声読み上げ中ならBGMの再生・停止処理を行う
        if(sharedViewModel.startFlag) {
            requireActivity().let {
                if(it is MainActivity) {
                    if(checked) {
                        it.bgm.start()
                    } else {
                        it.bgm.pause()
                    }
                }
            }
        }
        // ViewModelのBGMFlagの切り替え
        sharedViewModel.switchBgmFlag()

        // データベースのインスタンスを取得
        val db = AppDatabase.getInstance(requireContext())
        val dao = db.settingDao()
        // データベースのフラグを更新
        viewLifecycleOwner.lifecycleScope.launch {
            dao.changeFlag("bgm", getFlag(checked))
        }
    }

    // スイッチが押されたら呼び出される
    fun changeSetting(category: String) {
        // スイッチの状態をViewModelに反映
        when(category) {
            "history" -> sharedViewModel.switchHistoryFlag()
            "trivia" -> sharedViewModel.switchTriviaFlag()
            "restaurant" -> sharedViewModel.switchRestaurantFlag()
            "tourist" -> sharedViewModel.switchTouristSightFlag()
        }
        // コンテンツを再取得
        sharedViewModel.getScripts()

        // データベースのインスタンスを取得
        val db = AppDatabase.getInstance(requireContext())
        val dao = db.settingDao()

        // データベースに登録されているフラグを反転させる
        viewLifecycleOwner.lifecycleScope.launch {
            dao.changeFlag(category, getFlag(dao.getFlag(category) == 0))
        }

        // TextToSpeechに変更を知らせる
        requireActivity().let {
            if(it is MainActivity) {
                // 再生中なら条件に合わせて以下の処理を行う
                if(sharedViewModel.startFlag) {
                    // 音声読み上げを行っていないなら時間を空けて読み上げ開始
                    if(!it.ttsState()) {
                        // 遅延実行
                        Handler(Looper.getMainLooper()).postDelayed( {
                            // 音声読み上げを開始
                            it.startSpeech()
                        }, 5000)
                    }
                    // 音声読み上げ中ならコンテンツが変わったことを知らせる
                    else {
                        it.changeScripts = true
                    }
                }
            }
        }
    }

    // データベースに登録するフラグ(0 or 1)を取得
    private fun getFlag(flag: Boolean): Int {
        val result = if(flag) {
            1
        } else {
            0
        }
        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
