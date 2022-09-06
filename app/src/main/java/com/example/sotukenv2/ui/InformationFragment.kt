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
import com.example.sotukenv2.R
import com.example.sotukenv2.databinding.FragmentInformationBinding
import com.example.sotukenv2.json.Article
import com.example.sotukenv2.model.MainViewModel

class InformationFragment: Fragment() {

    private var binding: FragmentInformationBinding? = null
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentInformationBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.informationFragment = this
        binding?.lifecycleOwner = viewLifecycleOwner

        // 再生状態なら現在地をもとにコンテンツを取得
        // 再生状態でなければTopTitleを表示
        if (sharedViewModel.displayArticles.value!!.size == 0 && !sharedViewModel.startFlag) {
            binding!!.topTitle.setImageResource(R.drawable.top_title)
            binding!!.startText.setText(R.string.start_text)
        } else {
            setInformationTitle()
        }

        if (sharedViewModel.startFlag) {
            requireActivity().let {
                if (it is MainActivity) {
                    it.setIcon()
                }
            }
        }

        sharedViewModel.displayArticles.observe(viewLifecycleOwner) { it ->
            setRecyclerView(it)
            if (it.size != 0) {
                binding!!.topTitle.setImageDrawable(null)
                binding!!.startText.text = null
                setInformationTitle()
            }
        }
    }

    private fun setInformationTitle() {
        binding!!.informationTitle.setImageResource(R.drawable.header_title_information)
    }

    // リサイクラーを設定
    private fun setRecyclerView(articles: List<Article>) {
        recyclerView = binding!!.recyclerView
        val adapter = RecyclerAdapter(articles, sharedViewModel.flagList)
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