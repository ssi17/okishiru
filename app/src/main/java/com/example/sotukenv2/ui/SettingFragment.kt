package com.example.sotukenv2.ui

import android.os.Bundle
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

    override fun onDestroyView() {
        super.onDestroyView()
        sharedViewModel.getContents()
        binding = null
    }
}