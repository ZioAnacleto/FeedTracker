package com.zioanacleto.feedtracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zioanacleto.feedtracker.components.TitleWithName

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TitleWithName(
                    modifier = Modifier
                        .fillMaxWidth(),
                    name = "Costanza"
                )
            }

            FloatingActionButton(
                modifier = Modifier
                    .padding(end = 10.dp, bottom = 20.dp)
                    .align(Alignment.BottomEnd),
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = {  }
            ) {

            }
        }
    }
}