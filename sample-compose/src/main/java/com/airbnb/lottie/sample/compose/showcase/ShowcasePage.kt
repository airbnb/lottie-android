package com.airbnb.lottie.sample.compose.showcase

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.viewModel
import androidx.fragment.app.Fragment
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.api.FeaturedAnimationsResponse
import com.airbnb.lottie.sample.compose.composables.LottieAnimation
import com.airbnb.lottie.sample.compose.composables.LottieAnimationSpec
import com.airbnb.lottie.sample.compose.composables.LottieComposeScaffoldView
import com.airbnb.lottie.sample.compose.findNavController
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.lottie.sample.compose.ui.textColorDark
import com.airbnb.mvrx.*
import dev.chrisbanes.accompanist.coil.CoilImage

class ShowcaseFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LottieComposeScaffoldView(requireContext()) {
            ShowcasePage()
        }
    }
}

@Composable
fun ShowcasePage() {
    val viewModel: ShowcaseViewModel = viewModel()
    val featuredAnimations = viewModel.featuredAnimations.collectAsState()
    ShowcasePage(featuredAnimations.value)
}

@Composable
fun ShowcasePage(featuredAnimations: Async<FeaturedAnimationsResponse>) {
    val scrollState = rememberScrollState()
    val navController = findNavController()
    Log.d("Gabe", "ShowcasePage: $featuredAnimations")
    Stack(
        modifier = Modifier.fillMaxSize()
    ) {
        ScrollableColumn(
            scrollState = scrollState
        ) {
            Marquee("Showcase")
            featuredAnimations()?.data?.forEach { data ->
                AnimationRow(
                    title = data.title,
                    previewUrl = data.preview_url ?: "",
                    previewBackgroundColor = data.bgColor,
                ) {
                    navController.navigate(R.id.player, data.asMavericksArgs())
                }
                Divider(color = Color.LightGray)
            }
        }
        if (featuredAnimations is Uninitialized || featuredAnimations is Loading) {
            Loader(modifier = Modifier.gravity(Alignment.Center))
        }
    }
}

@Composable
fun Marquee(text: String) {
    Text(
        text,
        fontSize = 16.sp,
        color = textColorDark,
        modifier = Modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
    )
}

@Composable
fun Loader(
    modifier: Modifier = Modifier
) {
    LottieAnimation(
        LottieAnimationSpec.RawRes(R.raw.loading),
        modifier = Modifier
            .preferredSize(100.dp)
            .then(modifier)
    )
}

@Composable
fun AnimationRow(
    title: String,
    previewUrl: String,
    previewBackgroundColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            CoilImage(
                data = previewUrl,
                modifier = Modifier
                    .preferredSize(40.dp)
                    .background(color = previewBackgroundColor)
                    .padding(end = 16.dp)
            )
            Text(
                title,
                fontSize = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LottieTheme {
        ShowcasePage()
    }
}