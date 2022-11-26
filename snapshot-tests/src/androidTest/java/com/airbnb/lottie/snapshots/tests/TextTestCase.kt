package com.airbnb.lottie.snapshots.tests

import android.graphics.Typeface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.TextDelegate
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty
import com.airbnb.lottie.snapshots.LocalSnapshotReady
import com.airbnb.lottie.snapshots.SnapshotTestCase
import com.airbnb.lottie.snapshots.SnapshotTestCaseContext
import com.airbnb.lottie.snapshots.snapshotComposable
import com.airbnb.lottie.snapshots.withAnimationView

class TextTestCase : SnapshotTestCase {
    override suspend fun SnapshotTestCaseContext.run() {
        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Hello World") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "Hello World")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Hello World with getText") { animationView ->
            val textDelegate = object : TextDelegate(animationView) {
                override fun getText(input: String): String {
                    return when (input) {
                        "NAME" -> "Hello World"
                        else -> input
                    }
                }
            }
            animationView.setTextDelegate(textDelegate)
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Emoji") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥💪💯")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Taiwanese") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "我的密碼")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Fire Taiwanese") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥的A")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family man man girl boy") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family woman woman girl girl") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Brown Police Man") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC6E\uD83C\uDFFF\u200D♀️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family and Brown Police Man") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC6E\uD83C\uDFFF\u200D♀️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Family, Brown Police Man, emoji and chars") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥\uD83D\uDC68\u200D\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67\uD83D\uDC6E\uD83C\uDFFF\u200D♀的Aabc️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Fire English Fire Brown Police Man Fire") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "🔥c️🔥\uD83D\uDC6E\uD83C\uDFFF\u200D♀️\uD83D\uDD25")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "American Flag") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83C\uDDFA\uD83C\uDDF8")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Checkered Flag") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83C\uDFC1")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Pirate Flag") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83C\uDFF4\u200D☠️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "3 Oclock") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDD52")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Woman frowning") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDE4D\u200D♀️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Gay couple") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC68\u200D❤️\u200D\uD83D\uDC68️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Lesbian couple") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC69\u200D❤️\u200D\uD83D\uDC69️")
        }

        withAnimationView("Tests/DynamicText.json", "Dynamic Text", "Straight couple") { animationView ->
            val textDelegate = TextDelegate(animationView)
            animationView.setTextDelegate(textDelegate)
            textDelegate.setText("NAME", "\uD83D\uDC91")
        }

        snapshotComposable("Compose FontMap", "Text") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/Text.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            LottieAnimation(composition, { 0f }, fontMap = mapOf("Helvetica" to Typeface.SERIF))
        }

        snapshotComposable("Compose Dynamic Text", "Emoji") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/DynamicText.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.TEXT, "NAME") {
                    "🔥💪💯"
                },
            )
            LottieAnimation(composition, { 0f }, dynamicProperties = dynamicProperties)
        }

        snapshotComposable("Compose Dynamic Text", "Taiwanese") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/DynamicText.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.TEXT, "我的密碼", "NAME"),
            )
            LottieAnimation(composition, { 0f }, dynamicProperties = dynamicProperties)
        }

        snapshotComposable("Compose Dynamic Text", "Hindi") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/DynamicText.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.TEXT, "आपका लेख", "NAME"),
            )
            LottieAnimation(composition, { 0f }, dynamicProperties = dynamicProperties)
        }

        snapshotComposable("Compose Dynamic Text", "FrameInfo.startValue") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/DynamicText.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.TEXT, "NAME") { frameInfo ->
                    "${frameInfo.startValue}!!!"
                },
            )
            LottieAnimation(composition, { 0f }, dynamicProperties = dynamicProperties)
        }

        snapshotComposable("Compose Dynamic Text", "FrameInfo.endValue") {
            val composition by rememberLottieComposition(LottieCompositionSpec.Asset("Tests/DynamicText.json"))
            val snapshotReady = LocalSnapshotReady.current
            LaunchedEffect(snapshotReady, composition != null) {
                snapshotReady.value = composition != null
            }
            val dynamicProperties = rememberLottieDynamicProperties(
                rememberLottieDynamicProperty(LottieProperty.TEXT, "NAME") { frameInfo ->
                    "${frameInfo.endValue}!!!"
                },
            )
            LottieAnimation(composition, { 0f }, dynamicProperties = dynamicProperties)
        }
    }
}