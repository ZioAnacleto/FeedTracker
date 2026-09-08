package com.zioanacleto.feedtracker.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.open_personal_settings
import org.jetbrains.compose.resources.stringResource

@Composable
fun UserAvatarButton(initials: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(Res.string.open_personal_settings)
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(width = 1.5.dp, color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
