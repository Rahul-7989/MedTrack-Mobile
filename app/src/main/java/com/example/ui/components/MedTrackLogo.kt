package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Official MedTrack Logo component.
 * Displays the authentic MedTrack brand mark consisting of the interlocking
 * Warm Amber, Burnt Apricot, and Dusty Teal hexagons with the medication capsule.
 *
 * Bundled as an offline vector resource (R.drawable.ic_medtrack_logo) so it
 * functions seamlessly offline with zero network dependency.
 */
@Composable
fun MedTrackLogo(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 120.dp
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("medtrack_logo"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_medtrack_logo),
            contentDescription = "MedTrack Logo",
            modifier = Modifier.size(sizeDp),
            contentScale = ContentScale.Fit
        )
    }
}
