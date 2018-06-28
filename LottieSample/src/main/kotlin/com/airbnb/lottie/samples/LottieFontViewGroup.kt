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
import com.airbnb.lottie.LottieDrawable
import java.util.*

class LottieFontViewGroup @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val compositionMap by lazy { HashMap<String, LottieComposition>() }
    private val views by lazy { ArrayList<View>() }
    private val cursorView: LottieAnimationView by lazy { LottieAnimationView(context) }

    init {
        isFocusableInTouchMode = true
        LottieComposition.Factory.fromAssetFileName(context, "Mobilo/BlinkingCursor.json"
        ) { composition ->
            if (composition == null) {
                return@fromAssetFileName
            }
            cursorView.apply {
                layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setComposition(composition)
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
            }.let { addView(it) }
        }
    }

    private fun addSpace() {
        addView(createSpaceView(), indexOfChild(cursorView))
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
        (views.size - 2).takeIf { it >= 0 }?.let {
            removeView(views[it])
            views.removeAt(it)
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
                if (view.tag == "Space") {
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
        outAttrs.run {
            actionLabel = null
            inputType = InputType.TYPE_NULL
            imeOptions = EditorInfo.IME_ACTION_NEXT
        }
        return BaseInputConnection(this, false)
    }

    override fun onCheckIsTextEditor(): Boolean = true

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
        val composition = compositionMap[fileName]
        if (composition == null) {
            LottieComposition.Factory.fromAssetFileName(context, fileName) {
                if (it == null) {
                    return@fromAssetFileName
                }
                compositionMap[fileName] = it
                addComposition(it)
            }
        } else {
            addComposition(composition)
        }

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
        LottieAnimationView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            setComposition(composition)
            playAnimation()
        }.let { addView(it, indexOfChild(cursorView)) }
    }

    private fun fitsOnCurrentLine(currentX: Int, view: View): Boolean {
        return currentX + view.measuredWidth < width - paddingRight
    }

    private fun createSpaceView() = View(context).apply {
        layoutParams = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.font_space_width),
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tag = "Space"
    }
}
