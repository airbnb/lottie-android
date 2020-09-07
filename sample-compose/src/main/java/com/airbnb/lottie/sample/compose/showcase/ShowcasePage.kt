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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import androidx.ui.tooling.preview.Preview
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.composables.*
import com.airbnb.lottie.sample.compose.findNavController
import com.airbnb.lottie.sample.compose.ui.LottieTheme
import com.airbnb.mvrx.*

val ShowcaseStateAmbient = ambientOf { ShowcaseState() }

class ShowcaseFragment : Fragment(), MavericksView {
    private val viewModel by fragmentViewModel<ShowcaseFragment, ShowcaseViewModel, ShowcaseState>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LottieComposeScaffoldView(requireContext()) {
            val showcaseState = viewModel.stateFlow.collectAsState(ShowcaseState())
            Providers(ShowcaseStateAmbient provides showcaseState.value) {
                ShowcasePage()
            }
        }
    }

    override fun invalidate() {
    }
}


@Composable
fun ShowcasePage() {
    val featuredAnimations = ShowcaseStateAmbient.current.featuredAnimations
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LottieTheme {
        ShowcasePage()
    }
}