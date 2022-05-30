package com.airbnb.lottie.sample.compose.examples

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.sample.compose.R

@Composable
fun ContentScaleExamplesPage() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gradient))
    var alignment by remember { mutableStateOf(Alignment.Center) }
    var contentScale by remember { mutableStateOf(ContentScale.Fit) }

    UsageExamplePageScaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OptionsRow(
                alignment,
                contentScale,
                onAlignmentChanged = { alignment = it },
                onContentScaleChanged = { contentScale = it },
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(2.dp, Color.Green)
                    .padding(2.dp)
            ) {
                LottieAnimation(
                    composition,
                    progress = { 0f },
                    alignment = alignment,
                    contentScale = contentScale,
                )
            }
        }
    }
}
@Composable
private fun OptionsRow(
    alignment: Alignment,
    contentScale: ContentScale,
    onContentScaleChanged: (ContentScale) -> Unit,
    onAlignmentChanged: (Alignment) -> Unit,
) {
    var scaleExpanded by remember { mutableStateOf(false) }
    var alignmentExpanded by remember { mutableStateOf(false) }

    val onContentScaleChangedState by rememberUpdatedState(onContentScaleChanged)
    val onAlignmentChangedState by rememberUpdatedState(onAlignmentChanged)

    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    scaleExpanded = true
                    alignmentExpanded = false
                }
                .padding(horizontal = 4.dp, vertical = 16.dp)
        ){
            Text(
                ContentScales[contentScale] ?: "Unknown Content Scale",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 128.dp)
            )
            Icon(
                imageVector = if (scaleExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
            DropdownMenu(
                scaleExpanded,
                onDismissRequest = { scaleExpanded = false },
            ) {
                ContentScales.forEach { (cs, label) ->
                    DropdownMenuItem(
                        onClick = { onContentScaleChangedState(cs) },
                    ) {
                        Text(label)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .clickable {
                    alignmentExpanded = true
                    scaleExpanded = false
                }
                .padding(horizontal = 4.dp, vertical = 16.dp)
        ) {
            Text(
                Alignments[alignment] ?: "Unknown Alignment",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .defaultMinSize(minWidth = 128.dp)
            )
            Icon(
                imageVector = if (alignmentExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
            )
            DropdownMenu(
                alignmentExpanded,
                onDismissRequest = { alignmentExpanded = false }
            ) {
                Alignments.forEach { (a, label) ->
                    DropdownMenuItem(
                        onClick = { onAlignmentChangedState(a) },
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

private val Alignments = mapOf(
    Alignment.TopStart to "TopStart",
    Alignment.TopCenter to "TopCenter",
    Alignment.TopEnd to "TopEnd",
    Alignment.CenterStart to "CenterStart",
    Alignment.Center to "Center",
    Alignment.CenterEnd to "CenterEnd",
    Alignment.BottomStart to "BottomStart",
    Alignment.BottomCenter to "BottomCenter",
    Alignment.BottomEnd to "BottomEnd",
)

private val ContentScales = mapOf(
    ContentScale.Fit to "Fit",
    ContentScale.Crop to "Crop",
    ContentScale.Inside to "Inside",
    ContentScale.None to "None",
    ContentScale.FillBounds to "FillBounds",
    ContentScale.FillHeight to "FillHeight",
    ContentScale.FillWidth to "FillWidth",
)
