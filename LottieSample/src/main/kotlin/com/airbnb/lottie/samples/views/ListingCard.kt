package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.R
import kotlinx.android.synthetic.main.listing_card.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListingCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.listing_card, this)
    }

    @CallbackProp
    fun setClickListener(listener: View.OnClickListener?) {
        wishListIcon.setOnClickListener(listener)
    }
}