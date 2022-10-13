package com.example.sotukenv2.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sotukenv2.MainActivity
import com.example.sotukenv2.databinding.FragmentSettingBinding
import com.example.sotukenv2.model.MainViewModel

class SettingFragment: Fragment() {

    private var binding: FragmentSettingBinding? = null
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        if(sharedViewModel.startFlag) {
            requireActivity().let {
                if(it is MainActivity) {
                    if(binding!!.soundSwitch.isChecked) {
                        it.bgm.start()
                    } else {
                        it.bgm.pause()
                    }
                }
            }
        }
        sharedViewModel.switchBgmFlag()
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
        sharedViewModel.getContents()

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
                        it.changeContents = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}