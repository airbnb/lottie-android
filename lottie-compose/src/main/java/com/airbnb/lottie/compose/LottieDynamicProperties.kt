package com.airbnb.lottie.compose

import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.PointF
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieFrameInfo
import com.airbnb.lottie.value.LottieValueCallback
import com.airbnb.lottie.value.ScaleXY

/**
 * Use this function when you want to apply one or more dynamic properties to an animation.
 * This takes a vararg of individual dynamic properties which should be created with [rememberLottieDynamicProperty].
 *
 * @see rememberLottieDynamicProperty
 */
@Composable
fun rememberLottieDynamicProperties(
    vararg properties: LottieDynamicProperty<*>,
): LottieDynamicProperties {
    return remember(properties) {
        LottieDynamicProperties(properties.toList())
    }
}

/**
 * Use this to create a single dynamic property for an animation.
 *
 * @param property should be one of [com.airbnb.lottie.LottieProperty].
 * @param value the desired value to use as this property's value.
 * @param keyPath the string parts of a [com.airbnb.lottie.model.KeyPath] that specify which animation element
 *                the property resides on.
 */
@Composable
fun <T> rememberLottieDynamicProperty(
    property: T,
    value: T,
    vararg keyPath: String,
): LottieDynamicProperty<T> {
    val keyPathObj = remember(keyPath) { KeyPath(*keyPath) }
    return remember(keyPathObj, property, value) {
        LottieDynamicProperty(property, keyPathObj, value)
    }
}

/**
 * Use this to create a single dynamic property for an animation.
 *
 * @param property Should be one of [com.airbnb.lottie.LottieProperty].
 * @param keyPath The string parts of a [com.airbnb.lottie.model.KeyPath] that specify which animation element
 *                the property resides on.
 * @param callback A callback that will be invoked during the drawing pass with current frame info. The frame
 *                 info can be used to alter the property's value based on the original animation data or it
 *                 can be completely ignored and an arbitrary value can be returned. In this case, you may want
 *                 the overloaded version of this function that takes a static value instead of a callback.
 */
@Composable
fun <T> rememberLottieDynamicProperty(
    property: T,
    vararg keyPath: String,
    callback: (frameInfo: LottieFrameInfo<T>) -> T,
): LottieDynamicProperty<T> {
    val keyPathObj = remember(keyPath) { KeyPath(*keyPath) }
    val callbackState by rememberUpdatedState(callback)
    return remember(keyPathObj, property) {
        LottieDynamicProperty(
            property,
            keyPathObj,
        ) { callbackState(it) }
    }
}

/**
 * @see rememberLottieDynamicProperty
 */
class LottieDynamicProperty<T> internal constructor(
    internal val property: T,
    internal val keyPath: KeyPath,
    internal val callback: (frameInfo: LottieFrameInfo<T>) -> T,
) {
    constructor(property: T, keyPath: KeyPath, value: T) : this(property, keyPath, { value })
}

/**
 * @see rememberLottieDynamicProperties
 */
class LottieDynamicProperties internal constructor(
    private val intProperties: List<LottieDynamicProperty<Int>>,
    private val pointFProperties: List<LottieDynamicProperty<PointF>>,
    private val floatProperties: List<LottieDynamicProperty<Float>>,
    private val scaleProperties: List<LottieDynamicProperty<ScaleXY>>,
    private val colorFilterProperties: List<LottieDynamicProperty<ColorFilter>>,
    // Java doesn't have reified types. All arrays are Integer[].
    private val intArrayProperties: List<LottieDynamicProperty<Array<*>>>,
    private val typefaceProperties: List<LottieDynamicProperty<Typeface>>,
    private val bitmapProperties: List<LottieDynamicProperty<Bitmap>>,
) {
    @Suppress("UNCHECKED_CAST")
    constructor(properties: List<LottieDynamicProperty<*>>) : this(
        properties.filter { it.property is Int } as List<LottieDynamicProperty<Int>>,
        properties.filter { it.property is PointF } as List<LottieDynamicProperty<PointF>>,
        properties.filter { it.property is Float } as List<LottieDynamicProperty<Float>>,
        properties.filter { it.property is ScaleXY } as List<LottieDynamicProperty<ScaleXY>>,
        properties.filter { it.property is ColorFilter } as List<LottieDynamicProperty<ColorFilter>>,
        properties.filter { it.property is Array<*> } as List<LottieDynamicProperty<Array<*>>>,
        properties.filter { it.property is Typeface } as List<LottieDynamicProperty<Typeface>>,
        properties.filter { it.property is Bitmap } as List<LottieDynamicProperty<Bitmap>>,
    )

    internal fun addTo(drawable: LottieDrawable) {
        intProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        pointFProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        floatProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        scaleProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        colorFilterProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        intArrayProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        typefaceProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }
        bitmapProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, p.callback.toValueCallback())
        }

    }

    internal fun removeFrom(drawable: LottieDrawable) {
        intProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<Int>?)
        }
        pointFProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<PointF>?)
        }
        floatProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<Float>?)
        }
        scaleProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<ScaleXY>?)
        }
        colorFilterProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<ColorFilter>?)
        }
        intArrayProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<Array<*>>?)
        }
        typefaceProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<Typeface>?)
        }
        bitmapProperties.forEach { p ->
            drawable.addValueCallback(p.keyPath, p.property, null as LottieValueCallback<Bitmap>?)
        }
    }
}

private fun <T> ((frameInfo: LottieFrameInfo<T>) -> T).toValueCallback() = object : LottieValueCallback<T>() {
    override fun getValue(frameInfo: LottieFrameInfo<T>): T {
        return invoke(frameInfo)
    }
}