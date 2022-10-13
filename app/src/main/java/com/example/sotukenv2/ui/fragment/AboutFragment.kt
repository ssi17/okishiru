package com.example.sotukenv2.ui.fragment

import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.sotukenv2.MainActivity
import com.example.sotukenv2.databinding.FragmentAboutBinding
import com.example.sotukenv2.model.MainViewModel
import kotlin.properties.Delegates

class AboutFragment: Fragment() {

    private var binding: FragmentAboutBinding? = null
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentAboutBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.aboutFragment = this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}