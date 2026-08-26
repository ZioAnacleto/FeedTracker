package com.zioanacleto.feedtracker.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

@Composable
fun Modifier.hideKeyboardOnTouch(): Modifier {
    val localFocusManager = LocalFocusManager.current

    return this
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                localFocusManager.clearFocus()
            })
        }
}
