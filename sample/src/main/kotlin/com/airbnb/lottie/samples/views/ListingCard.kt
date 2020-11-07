package com.airbnb.lottie.samples.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.OnViewRecycled
import com.airbnb.lottie.samples.databinding.ListingCardBinding
import com.airbnb.lottie.samples.utils.viewBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListingCard @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ListingCardBinding by viewBinding()

    @CallbackProp
    fun onToggled(listener: ((isWishListed: Boolean) -> Unit)?) {
        binding.wishListIcon.setOnClickListener(when (listener) {
            null -> null
            else -> { _ ->
                listener(binding.wishListIcon.progress == 0f)
            }
        })
    }

    @ModelProp
    fun isWishListed(isWishListed: Boolean) {
        val targetProgress = if (isWishListed) 1f else 0f
        binding.wishListIcon.speed = if (isWishListed) 1f else -1f
        if (binding.wishListIcon.progress != targetProgress) {
            binding.wishListIcon.playAnimation()
        }
    }

    @OnViewRecycled
    fun onRecycled() {
        binding.wishListIcon.pauseAnimation()
        binding.wishListIcon.progress = 0f
    }
}