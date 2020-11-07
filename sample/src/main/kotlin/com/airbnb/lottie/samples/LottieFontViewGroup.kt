package com.airbnb.lottie.samples

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import java.util.*

class LottieFontViewGroup @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val views = ArrayList<View>()

    private val cursorView: LottieAnimationView by lazy { LottieAnimationView(context) }

    init {
        isFocusableInTouchMode = true
        LottieCompositionFactory.fromAsset(context, "Mobilo/BlinkingCursor.json")
                .addListener {
                    cursorView.layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    cursorView.setComposition(it)
                    cursorView.repeatCount = LottieDrawable.INFINITE
                    cursorView.playAnimation()
                    addView(cursorView)
                }
    }

    private fun addSpace() {
        val index = indexOfChild(cursorView)
        addView(createSpaceView(), index)
    }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        if (index == -1) {
            views.add(child)
        } else {
            views.add(index, child)
        }
    }

    private fun removeLastView() {
        if (views.size > 1) {
            val position = views.size - 2
            removeView(views[position])
            views.removeAt(position)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (views.isEmpty()) {
            return
        }
        var currentX = paddingTop
        var currentY = paddingLeft

        for (i in views.indices) {
            val view = views[i]
            if (!fitsOnCurrentLine(currentX, view)) {
                if (view.tag != null && view.tag == "Space") {
                    continue
                }
                currentX = paddingLeft
                currentY += view.measuredHeight
            }
            currentX += view.width
        }

        setMeasuredDimension(measuredWidth, currentY + views[views.size - 1].measuredHeight * 2)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (views.isEmpty()) {
            return
        }
        var currentX = paddingTop
        var currentY = paddingLeft

        for (i in views.indices) {
            val view = views[i]
            if (!fitsOnCurrentLine(currentX, view)) {
                if (view.tag != null && view.tag == "Space") {
                    continue
                }
                currentX = paddingLeft
                currentY += view.measuredHeight
            }
            view.layout(currentX, currentY, currentX + view.measuredWidth,
                    currentY + view.measuredHeight)
            currentX += view.width
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val fic = BaseInputConnection(this, false)
        outAttrs.actionLabel = null
        outAttrs.inputType = InputType.TYPE_NULL
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NEXT
        return fic
    }

    override fun onCheckIsTextEditor(): Boolean {
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            addSpace()
            return true
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            removeLastView()
            return true
        }

        if (!isValidKey(event)) {
            return super.onKeyUp(keyCode, event)
        }


        val letter = "" + Character.toUpperCase(event.unicodeChar.toChar())
        // switch (letter) {
        //     case ",":
        //         letter = "Comma";
        //         break;
        //     case "'":
        //         letter = "Apostrophe";
        //         break;
        //     case ";":
        //     case ":":
        //         letter = "Colon";
        //         break;
        // }
        val fileName = "Mobilo/$letter.json"
        LottieCompositionFactory.fromAsset(context, fileName)
                .addListener { addComposition(it) }

        return true
    }

    private fun isValidKey(event: KeyEvent): Boolean {
        if (!event.hasNoModifiers()) {
            return false
        }
        if (event.keyCode >= KeyEvent.KEYCODE_A && event.keyCode <= KeyEvent.KEYCODE_Z) {
            return true
        }

        // switch (keyCode) {
        //     case KeyEvent.KEYCODE_COMMA:
        //     case KeyEvent.KEYCODE_APOSTROPHE:
        //     case KeyEvent.KEYCODE_SEMICOLON:
        //         return true;
        // }
        return false
    }

    private fun addComposition(composition: LottieComposition) {
        val lottieAnimationView = LottieAnimationView(context)
        lottieAnimationView.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lottieAnimationView.setComposition(composition)
        lottieAnimationView.playAnimation()
        val index = indexOfChild(cursorView)
        addView(lottieAnimationView, index)
    }

    private fun fitsOnCurrentLine(currentX: Int, view: View): Boolean {
        return currentX + view.measuredWidth < width - paddingRight
    }

    private fun createSpaceView(): View {
        val spaceView = View(context)
        spaceView.layoutParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.font_space_width),
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        spaceView.tag = "Space"
        return spaceView
    }
}
