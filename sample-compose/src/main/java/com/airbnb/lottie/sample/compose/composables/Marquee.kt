package com.airbnb.lottie.sample.compose.composables

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.sample.compose.ui.TealDark

@Composable
fun Marquee(text: String) {
    Text(
        text,
        fontSize = 32.sp,
        color = TealDark,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
    )
}