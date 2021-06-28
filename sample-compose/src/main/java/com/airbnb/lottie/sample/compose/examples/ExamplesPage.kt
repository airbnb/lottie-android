package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.Route
import com.airbnb.lottie.sample.compose.composables.Marquee
import com.airbnb.lottie.sample.compose.navigate

@Composable
fun ExamplesPage(navController: NavController) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Marquee(stringResource(R.string.examples_title))
        ListItem(
            text = { Text("Basic Usage") },
            secondaryText = { Text("Various example of simple Lottie usage") },
            modifier = Modifier
                .clickable { navController.navigate(Route.BasicUsageExamples) }
        )
        ListItem(
            text = { Text("Animatable Usage") },
            secondaryText = { Text("Usage of LottieAnimatable") },
            modifier = Modifier
                .clickable { navController.navigate(Route.AnimatableUsageExamples) }
        )
        ListItem(
            text = { Text("Transitions") },
            secondaryText = { Text("Sequencing segments of an animation based on state") },
            modifier = Modifier
                .clickable { navController.navigate(Route.TransitionsExamples) }
        )
        ListItem(
            text = { Text("View Pager") },
            secondaryText = { Text("Syncing a Lottie animation with a view pager") },
            modifier = Modifier
                .clickable { navController.navigate(Route.ViewPagerExample) }
        )
        ListItem(
            text = { Text("Network Animations") },
            secondaryText = { Text("Loading animations from a url") },
            modifier = Modifier
                .clickable { navController.navigate(Route.NetworkExamples) }
        )
        ListItem(
            text = { Text("Dynamic Properties") },
            secondaryText = { Text("Setting dynamic properties") },
            modifier = Modifier
                .clickable { navController.navigate(Route.DynamicProperties) }
        )
    }
}