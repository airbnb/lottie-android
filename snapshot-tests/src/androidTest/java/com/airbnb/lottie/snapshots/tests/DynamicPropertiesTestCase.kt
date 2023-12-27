package com.airbnb.lottie.snapshots.tests

import android.graphics.Color
import android.graphics.PointF
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.withDrawable
import com.airbnb.lottie.value.LottieFrameInfo
import com.airbnb.lottie.value.LottieInterpolatedIntegerValue
import com.airbnb.lottie.value.LottieRelativeFloatValueCallback
import com.airbnb.lottie.value.LottieRelativePointValueCallback
import com.airbnb.lottie.value.LottieValueCallback
import com.airbnb.lottie.value.ScaleXY

class DynamicPropertiesTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        testDynamicProperty(
            "Fill color (Green)",
            KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
            LottieProperty.COLOR,
            LottieValueCallback(Color.GREEN)
        )

        testDynamicProperty(
            "Fill color (Yellow)",
            KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
            LottieProperty.COLOR,
            LottieValueCallback(Color.YELLOW)
        )

        testDynamicProperty(
            "Fill opacity",
            KeyPath("Shape Layer 1", "Rectangle", "Fill 1"),
            LottieProperty.OPACITY,
            LottieValueCallback(50)
        )

        testDynamicProperty(
            "Stroke color",
            KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
            LottieProperty.STROKE_COLOR,
            LottieValueCallback(Color.GREEN)
        )

        testDynamicProperty(
            "Stroke width",
            KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
            LottieProperty.STROKE_WIDTH,
            LottieRelativeFloatValueCallback(50f)
        )

        testDynamicProperty(
            "Stroke opacity",
            KeyPath("Shape Layer 1", "Rectangle", "Stroke 1"),
            LottieProperty.OPACITY,
            LottieValueCallback(50)
        )

        testDynamicProperty(
            "Transform anchor point",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_ANCHOR_POINT,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )

        testDynamicProperty(
            "Transform position",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_POSITION,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )


        testDynamicProperty(
            "Transform position X",
            KeyPath("Shape Layer 1"),
            LottieProperty.TRANSFORM_POSITION_X,
            object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>) = frameInfo.startValue
            },
            progress = 1f,
            assetName = "Tests/SplitPathTransform.json"
        )

        testDynamicProperty(
            "Transform position Y",
            KeyPath("Shape Layer 1"),
            LottieProperty.TRANSFORM_POSITION_Y,
            object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>) = frameInfo.startValue
            },
            progress = 1f,
            assetName = "Tests/SplitPathTransform.json"
        )

        testDynamicProperty(
            "Transform position (relative)",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_POSITION,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )

        testDynamicProperty(
            "Transform opacity",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_OPACITY,
            LottieValueCallback(50)
        )

        testDynamicProperty(
            "Transform rotation",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_ROTATION,
            LottieValueCallback(45f)
        )

        testDynamicProperty(
            "Transform scale",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_SCALE,
            LottieValueCallback(ScaleXY(0.5f, 0.5f))
        )

        testDynamicProperty(
            "Rectangle corner roundedness",
            KeyPath("Shape Layer 1", "Rectangle", "Rectangle Path 1"),
            LottieProperty.CORNER_RADIUS,
            LottieValueCallback(7f)
        )

        testDynamicProperty(
            "Rectangle position",
            KeyPath("Shape Layer 1", "Rectangle", "Rectangle Path 1"),
            LottieProperty.POSITION,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )

        testDynamicProperty(
            "Rectangle size",
            KeyPath("Shape Layer 1", "Rectangle", "Rectangle Path 1"),
            LottieProperty.RECTANGLE_SIZE,
            LottieRelativePointValueCallback(PointF(30f, 40f))
        )

        testDynamicProperty(
            "Ellipse position",
            KeyPath("Shape Layer 1", "Ellipse", "Ellipse Path 1"),
            LottieProperty.POSITION,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )

        testDynamicProperty(
            "Ellipse size",
            KeyPath("Shape Layer 1", "Ellipse", "Ellipse Path 1"),
            LottieProperty.ELLIPSE_SIZE,
            LottieRelativePointValueCallback(PointF(40f, 60f))
        )

        testDynamicProperty(
            "Star points",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POLYSTAR_POINTS,
            LottieValueCallback(8f)
        )

        testDynamicProperty(
            "Star rotation",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POLYSTAR_ROTATION,
            LottieValueCallback(10f)
        )

        testDynamicProperty(
            "Star position",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POSITION,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )

        testDynamicProperty(
            "Star inner radius",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POLYSTAR_INNER_RADIUS,
            LottieValueCallback(10f)
        )

        testDynamicProperty(
            "Star inner roundedness",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POLYSTAR_INNER_ROUNDEDNESS,
            LottieValueCallback(100f)
        )

        testDynamicProperty(
            "Star outer radius",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POLYSTAR_OUTER_RADIUS,
            LottieValueCallback(60f)
        )

        testDynamicProperty(
            "Star outer roundedness",
            KeyPath("Shape Layer 1", "Star", "Polystar Path 1"),
            LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS,
            LottieValueCallback(100f)
        )

        testDynamicProperty(
            "Polygon points",
            KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
            LottieProperty.POLYSTAR_POINTS,
            LottieValueCallback(8f)
        )

        testDynamicProperty(
            "Polygon rotation",
            KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
            LottieProperty.POLYSTAR_ROTATION,
            LottieValueCallback(10f)
        )

        testDynamicProperty(
            "Polygon position",
            KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
            LottieProperty.POSITION,
            LottieRelativePointValueCallback(PointF(20f, 20f))
        )

        testDynamicProperty(
            "Polygon radius",
            KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
            LottieProperty.POLYSTAR_OUTER_RADIUS,
            LottieRelativeFloatValueCallback(60f)
        )

        testDynamicProperty(
            "Polygon roundedness",
            KeyPath("Shape Layer 1", "Polygon", "Polystar Path 1"),
            LottieProperty.POLYSTAR_OUTER_ROUNDEDNESS,
            LottieValueCallback(100f)
        )

        testDynamicProperty(
            "Repeater transform position",
            KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
            LottieProperty.TRANSFORM_POSITION,
            LottieRelativePointValueCallback(PointF(100f, 100f))
        )

        testDynamicProperty(
            "Repeater contents",
            KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
            LottieProperty.TRANSFORM_POSITION,
            LottieRelativePointValueCallback(PointF(100f, 100f))
        )

        testDynamicProperty(
            "Repeater sub-contents",
            KeyPath("Shape Layer 1", "Repeater Shape", "Fill 1"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(SimpleColorFilter(Color.GREEN))
        )

        testDynamicProperty(
            "Repeater transform start opacity",
            KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
            LottieProperty.TRANSFORM_START_OPACITY,
            LottieValueCallback(25f)
        )

        testDynamicProperty(
            "Repeater transform end opacity",
            KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
            LottieProperty.TRANSFORM_END_OPACITY,
            LottieValueCallback(25f)
        )

        testDynamicProperty(
            "Repeater transform rotation",
            KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
            LottieProperty.TRANSFORM_ROTATION,
            LottieValueCallback(45f)
        )

        testDynamicProperty(
            "Repeater transform scale",
            KeyPath("Shape Layer 1", "Repeater Shape", "Repeater 1"),
            LottieProperty.TRANSFORM_SCALE,
            LottieValueCallback(ScaleXY(2f, 2f))
        )

        testDynamicProperty(
            "Time remapping",
            KeyPath("Circle 1"),
            LottieProperty.TIME_REMAP,
            LottieValueCallback(1f)
        )

        testDynamicProperty(
            "Color Filter",
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(SimpleColorFilter(Color.GREEN))
        )

        testDynamicProperty(
            "Null Color Filter",
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(null)
        )

        testDynamicProperty(
            "Opacity interpolation (0)",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_OPACITY,
            LottieInterpolatedIntegerValue(10, 100),
            0f
        )

        testDynamicProperty(
            "Opacity interpolation (0.5)",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_OPACITY,
            LottieInterpolatedIntegerValue(10, 100),
            0.5f
        )

        testDynamicProperty(
            "Opacity interpolation (1)",
            KeyPath("Shape Layer 1", "Rectangle"),
            LottieProperty.TRANSFORM_OPACITY,
            LottieInterpolatedIntegerValue(10, 100),
            1f
        )

        testDynamicProperty(
            "Drop Shadow Color",
            KeyPath("Shape Layer 1", "**"),
            LottieProperty.DROP_SHADOW_COLOR,
            LottieValueCallback(Color.RED),
            assetName = "Tests/AnimatedShadow.json"
        )

        testDynamicProperty(
            "Drop Shadow Distance",
            KeyPath("Shape Layer 1", "**"),
            LottieProperty.DROP_SHADOW_DISTANCE,
            LottieValueCallback(30f),
            assetName = "Tests/AnimatedShadow.json"
        )

        testDynamicProperty(
            "Drop Shadow Direction",
            KeyPath("Shape Layer 1", "**"),
            LottieProperty.DROP_SHADOW_DIRECTION,
            LottieValueCallback(30f),
            assetName = "Tests/AnimatedShadow.json"
        )

        testDynamicProperty(
            "Drop Shadow Radius",
            KeyPath("Shape Layer 1", "**"),
            LottieProperty.DROP_SHADOW_RADIUS,
            LottieValueCallback(40f),
            assetName = "Tests/AnimatedShadow.json"
        )

        testDynamicProperty(
            "Drop Shadow Opacity",
            KeyPath("Shape Layer 1", "**"),
            LottieProperty.DROP_SHADOW_OPACITY,
            LottieValueCallback(0.2f),
            assetName = "Tests/AnimatedShadow.json"
        )

        testDynamicProperty(
            "Solid Color",
            KeyPath("Cyan Solid 1", "**"),
            LottieProperty.COLOR,
            LottieValueCallback(Color.YELLOW),
            assetName = "Tests/SolidLayerTransform.json"
        )

        testDynamicProperty(
            "Solid Color w/ null",
            KeyPath("Cyan Solid 1", "**"),
            LottieProperty.COLOR,
            LottieValueCallback(null),
            assetName = "Tests/SolidLayerTransform.json"
        )

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Linear Gradient Fill") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int> {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Linear", "Rectangle", "Gradient Fill"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Radial Gradient Fill") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int> {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Radial", "Rectangle", "Gradient Fill"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Linear Gradient Stroke") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int> {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Linear", "Rectangle", "Gradient Stroke"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Colors", "Radial Gradient Stroke") { drawable ->
            val value = object : LottieValueCallback<Array<Int>>() {
                override fun getValue(frameInfo: LottieFrameInfo<Array<Int>>?): Array<Int> {
                    return arrayOf(Color.YELLOW, Color.GREEN)
                }
            }
            drawable.addValueCallback(KeyPath("Radial", "Rectangle", "Gradient Stroke"), LottieProperty.GRADIENT_COLOR, value)
        }

        withDrawable("Tests/DynamicGradient.json", "Gradient Opacity", "Linear Gradient Fill") { drawable ->
            val value = object : LottieValueCallback<Int>() {
                override fun getValue(frameInfo: LottieFrameInfo<Int>?) = 50
            }
            drawable.addValueCallback(KeyPath("Linear", "Rectangle", "Gradient Fill"), LottieProperty.OPACITY, value)
        }

        withDrawable("Tests/MatteTimeStretchScan.json", "Mirror animation", "Mirror animation") { drawable ->
            drawable.addValueCallback(KeyPath.COMPOSITION, LottieProperty.TRANSFORM_ANCHOR_POINT) {
                PointF(drawable.composition.bounds.width().toFloat(), 0f)
            }
            drawable.addValueCallback(KeyPath.COMPOSITION, LottieProperty.TRANSFORM_SCALE) {
                ScaleXY(-1.0f, 1.0f)
            }
        }

        withDrawable("Tests/TrackMattes.json", "Matte", "Matte property") { drawable ->
            val keyPath = KeyPath("Shape Layer 1", "Rectangle 1", "Rectangle Path 1")
            drawable.addValueCallback(keyPath, LottieProperty.RECTANGLE_SIZE, LottieValueCallback(PointF(50f, 50f)))
        }

        withDrawable("Tests/Text.json", "Text", "Text Fill (Blue -> Green)") { drawable ->
            val value = object : LottieValueCallback<Int>() {
                override fun getValue(frameInfo: LottieFrameInfo<Int>?) = Color.GREEN
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.COLOR, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Stroke (Red -> Yellow)") { drawable ->
            val value = object : LottieValueCallback<Int>() {
                override fun getValue(frameInfo: LottieFrameInfo<Int>?) = Color.YELLOW
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.STROKE_COLOR, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Stroke Width") { drawable ->
            val value = object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>?) = 200f
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.STROKE_WIDTH, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Tracking") { drawable ->
            val value = object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>?) = 20f
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.TEXT_TRACKING, value)
        }

        withDrawable("Tests/Text.json", "Text", "Text Size") { drawable ->
            val value = object : LottieValueCallback<Float>() {
                override fun getValue(frameInfo: LottieFrameInfo<Float>?) = 60f
            }
            drawable.addValueCallback(KeyPath("Text"), LottieProperty.TEXT_SIZE, value)
        }
    }

    private suspend fun <T> SnapshotTestCaseContext.testDynamicProperty(
        name: String,
        keyPath: KeyPath,
        property: T,
        callback: LottieValueCallback<T>,
        progress: Float = 0f,
        assetName: String = "Tests/Shapes.json",
    ) {
        withDrawable(assetName, "Dynamic Properties", name) { drawable ->
            drawable.addValueCallback(keyPath, property, callback)
            drawable.progress = progress
        }
    }
}
