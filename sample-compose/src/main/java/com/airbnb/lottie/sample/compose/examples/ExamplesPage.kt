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
import com.airbnb.lottie.sample.compose.utils.drawBottomBorder

@Composable
fun ExamplesPage(navController: NavController) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Marquee(stringResource(R.string.examples_title))
        ExampleListItem(
            navController,
            "Basic Usage",
            "Various example of simple Lottie usage.",
            Route.BasicUsageExamples,
        )
        ExampleListItem(
            navController,
            "Transitions",
            "Sequencing segments of an animation based on state.",
            Route.TransitionsExamples,
        )
        ExampleListItem(
            navController,
            "Network Animations",
            "Loading animations from a url",
            Route.NetworkExamples,
        )
        ExampleListItem(
            navController,
            "Dynamic Properties",
            "Dynamic Properties",
            Route.DynamicPropertiesExamples,
        )
    }
}

@Composable
private fun ExampleListItem(navController: NavController, title: String, description: String, route: Route) {
    ListItem(
        text = { Text(title) },
        secondaryText = { Text(description) },
        modifier = Modifier
            .clickable { navController.navigate(route) }
            .drawBottomBorder()
    )
}