package com.airbnb.lottie.sample.compose.lottiefiles

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.composables.Marquee

enum class LottieFilesTab(@StringRes val stringRes: Int) {
    Recent(R.string.tab_recent),
    Popular(R.string.tab_popular),
    Search(R.string.tab_search)
}

@Composable
fun LottieFilesPage(navController: NavController) {
    var tab by rememberSaveable { mutableStateOf(LottieFilesTab.Recent) }

    Column {
        Marquee("LottieFiles")
        LottieFilesTabBar(
            selectedTab = tab,
            onTabSelected = { tab = it },
        )
        when (tab) {
            LottieFilesTab.Recent -> LottieFilesRecentAndPopularPage(navController, LottieFilesMode.Recent)
            LottieFilesTab.Popular -> LottieFilesRecentAndPopularPage(navController, LottieFilesMode.Popular)
            LottieFilesTab.Search -> LottieFilesSearchPage(navController)
        }
    }

}

@Composable
fun LottieFilesTabBar(
    selectedTab: LottieFilesTab,
    onTabSelected: (LottieFilesTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = modifier
            .fillMaxWidth(),
    ) {
        for (tab in LottieFilesTab.values()) {
            LottieFilesTabBarTab(
                text = stringResource(tab.stringRes),
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
            )
        }
    }
}

@Composable
fun LottieFilesTabBarTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textWidth = remember { mutableStateOf(0) }
    val pxRatio = with(LocalDensity.current) { 1.dp.toPx() }
    val tabWidth by animateDpAsState(if (isSelected) (textWidth.value / pxRatio).dp else 0.dp)
    val tabAlpha by animateFloatAsState(if (isSelected) 1f else 0f)
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            maxLines = 1,
            modifier = Modifier
                .onGloballyPositioned { textWidth.value = it.size.width }
                .wrapContentWidth()
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(3.dp)
                .background(MaterialTheme.colors.primary.copy(alpha = tabAlpha))
                .width(tabWidth)
        )
    }
}