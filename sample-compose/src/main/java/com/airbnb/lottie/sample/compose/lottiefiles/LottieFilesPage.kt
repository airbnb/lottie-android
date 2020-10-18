package com.airbnb.lottie.sample.compose.lottiefiles

import androidx.annotation.StringRes
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.onGloballyPositioned
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.sample.compose.ComposeFragment
import com.airbnb.lottie.sample.compose.R
import com.airbnb.lottie.sample.compose.composables.Marquee

class LottieFilesFragment : ComposeFragment() {

    @Composable
    override fun root() {
        LottieFilesPage()
    }
}

enum class LottieFilesTab(@StringRes val stringRes: Int) {
    Recent(R.string.tab_recent),
    Popular(R.string.tab_popular),
    Search(R.string.tab_search)
}

@Composable
fun LottieFilesPage() {
    var tab by savedInstanceState { LottieFilesTab.Recent }

    Column {
        Marquee("LottieFiles")
        LottieFilesTabBar(
            selectedTab = tab,
            onTabSelected = { tab = it },
        )
        when (tab) {
            LottieFilesTab.Recent -> Text("Recent")
            LottieFilesTab.Popular -> Text("Popular")
            LottieFilesTab.Search -> LottieFilesSearchPage()
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
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.Start
    ) {
        for (tab in LottieFilesTab.values()) {
            LottieFilesTabBarTab(
                text = stringResource(tab.stringRes),
                isSelected = tab == selectedTab,
                onClick = { onTabSelected(tab) }
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
    val pxRatio = with(DensityAmbient.current) { 1.dp.toPx() }
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
        if (isSelected) {
            Box(
                modifier = Modifier
                    .preferredHeight(3.dp)
                    .background(MaterialTheme.colors.primary)
                    .preferredWidth((textWidth.value / pxRatio).dp)
            )
        }
    }
}