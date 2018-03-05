package com.airbnb.lottie.samples

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.lottie.samples.model.AnimationData
import com.airbnb.lottie.samples.model.CompositionArgs
import kotlinx.android.synthetic.main.item_view_animation.view.*

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class AnimationItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Observer<CompositionResult> {

    private var args: CompositionArgs? = null

    init {
        inflate(R.layout.item_view_animation)
    }

    @ModelProp
    fun setAnimationData(animationData: AnimationData) {
        args = CompositionArgs(animationData = animationData)
        animationView.background = ColorDrawable(animationData.bgColorInt())
        titleView.text = animationData.title
        authorView.text = animationData.userInfo.name
        animationView.setImageDrawable(null)
    }

    @ModelProp(options = [ModelProp.Option.DoNotHash])
    fun setClickListener(listener: View.OnClickListener) {
        clickOverlay.setOnClickListener(listener)
    }

    fun onBind() {
        animationView.playAnimation()
        CompositionCache.observe(args, context as LifecycleOwner, this)
    }

    fun onUnbind() {
        CompositionCache.removeObserver(args, this)
        animationView.cancelAnimation()
        animationView.setImageDrawable(null)
    }

    override fun onChanged(result: CompositionResult?) {
        result ?: return

        val composition = when (result) {
            is Loaded -> result.composition
            else -> null
        }
        if (composition == null) {
            animationView.setImageDrawable(null)
        } else {
            animationView.setComposition(composition)
            animationView.playAnimation()
        }
    }
}