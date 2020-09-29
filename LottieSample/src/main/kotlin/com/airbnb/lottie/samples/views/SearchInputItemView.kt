package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.inflate
import kotlinx.android.synthetic.main.item_view_search_input.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class SearchInputItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.item_view_search_input)
        searchEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && event?.action == KeyEvent.ACTION_DOWN) {
                searchButton.callOnClick()
                return@setOnEditorActionListener true
            } else if (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                searchButton.callOnClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setSearchClickListener(listener: (String) -> Unit) {
        searchButton.setOnClickListener {
            val inputMethodManager = context.getSystemService<InputMethodManager>()!!
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            listener(searchEditText.text.toString())
        }
    }
}