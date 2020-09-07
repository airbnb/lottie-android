package com.airbnb.lottie.sample.compose

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.api.AnimationResponseV2
import com.airbnb.lottie.sample.compose.showcase.ShowcasePage
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.ui.textColorDark
import dev.chrisbanes.accompanist.coil.CoilImage

class ComposeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LottieTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ShowcasePage()
                }
            }
        }
    }
}