package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.airbnb.lottie.samples.databinding.ItemViewSearchInputBinding
import com.airbnb.lottie.samples.utils.viewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchInputItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ItemViewSearchInputBinding by viewBinding()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    init {
        binding.searchEditText.doAfterTextChanged { text ->
            _query.value = text?.toString() ?: ""
        }
    }
}