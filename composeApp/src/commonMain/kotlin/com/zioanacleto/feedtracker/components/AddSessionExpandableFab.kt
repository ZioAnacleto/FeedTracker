package com.zioanacleto.feedtracker.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.add
import feedtracker.composeapp.generated.resources.close_add_session_menu
import feedtracker.composeapp.generated.resources.log_past_session
import feedtracker.composeapp.generated.resources.start_new_session
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddSessionExpandableFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onNewSessionClick: () -> Unit,
    onPastSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FabMenuItem(
                    icon = Icons.Filled.DateRange,
                    label = stringResource(Res.string.log_past_session),
                    onClick = {
                        onExpandedChange(false)
                        onPastSessionClick()
                    },
                )
                FabMenuItem(
                    icon = Icons.Filled.Add,
                    label = stringResource(Res.string.start_new_session),
                    onClick = {
                        onExpandedChange(false)
                        onNewSessionClick()
                    },
                )
            }
        }
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = stringResource(
                    if (expanded) {
                        Res.string.close_add_session_menu
                    } else {
                        Res.string.add
                    },
                ),
            )
        }
    }
}

@Composable
private fun FabMenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}
