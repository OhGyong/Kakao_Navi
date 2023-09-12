package com.vcudemo.ui.map

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.vcudemo.R
import com.vcudemo.base.BaseActivity
import com.vcudemo.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchActivity: BaseActivity() {
    companion object {
        const val TAG = "SearchActivity"
    }

    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel

    private var myLatitude = ""
    private var myLongitude = ""
    private var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        myLatitude = intent.getStringExtra("latitude").toString()
        myLongitude = intent.getStringExtra("longitude").toString()

        observeFlow()

        binding.etSearch.setOnEditorActionListener { textView, action, _ ->
            if(action == EditorInfo.IME_ACTION_SEARCH) {
                if(textView.text.isNullOrEmpty() || textView.text.isNullOrBlank()) {
                    return@setOnEditorActionListener false
                }

                searchText = textView.text.toString()

                viewModel.getSearchPlaceData(myLatitude, myLongitude)
            }
            false
        }
    }

    private fun observeFlow() {
        lifecycleScope.launch {
            viewModel.searchPlaceData.collectLatest {
                Log.d(TAG, "observeFlow $it")
            }
        }
    }
}