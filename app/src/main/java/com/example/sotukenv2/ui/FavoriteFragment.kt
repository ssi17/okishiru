package com.example.sotukenv2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sotukenv2.MainActivity
import com.example.sotukenv2.databinding.FragmentFavoriteBinding
import com.example.sotukenv2.model.MainViewModel

class FavoriteFragment: Fragment() {

    private var binding: FragmentFavoriteBinding? = null
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentFavoriteBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.favoriteFragment = this

        sharedViewModel.getFavoriteArticle()
        setRecyclerView()

        if(sharedViewModel.startFlag) {
            requireActivity().let {
                if(it is MainActivity) {
                    it.setIcon()
                }
            }
        }
    }

    private fun setRecyclerView() {
        recyclerView = binding!!.recyclerView
        val adapter = RecyclerAdapter(sharedViewModel.favoriteArticles, sharedViewModel.flagList)
        recyclerView.adapter = adapter
        // お気に入り登録ボタンが押された時の処理
        adapter.favoriteButton = object : RecyclerAdapter.FavoriteButton {
            override fun pushFavoriteButton(id: Int) {
                sharedViewModel.changeFavoriteFlag(id)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}