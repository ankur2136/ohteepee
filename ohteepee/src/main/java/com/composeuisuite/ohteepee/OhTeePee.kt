package com.composeuisuite.ohteepee

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.input.KeyboardType
import com.composeuisuite.ohteepee.configuration.CellConfigurations
import com.composeuisuite.ohteepee.utils.EMPTY
import com.composeuisuite.ohteepee.utils.requestFocusSafely

private const val DEFAULT_PLACE_HOLDER = " "

@Composable
fun OhTeePee(
    cellsCount: Int,
    value: String,
    onValueChange: (String) -> Unit,
    cellConfigurations: CellConfigurations,
    modifier: Modifier = Modifier,
    isErrorOccurred: Boolean = false,
    obscureText: String = String.EMPTY,
    placeHolder: String = DEFAULT_PLACE_HOLDER,
    keyboardType: KeyboardType = KeyboardType.Number,
    enabled: Boolean = true,
    autoFocusByDefault: Boolean = true
) {
    require(placeHolder.length == 1) {
        "PlaceHolder must be a single character"
    }
    require(obscureText.length <= 1) {
        "obscureText can't be more then 1 characters"
    }

    val placeHolderAsChar = placeHolder.first()
    val otpValue by remember(value, isErrorOccurred, placeHolder) {
        val charList = CharArray(cellsCount) { index ->
            if (isErrorOccurred) {
                placeHolderAsChar
            } else {
                value.getOrElse(index) { placeHolderAsChar }
            }
        }
        mutableStateOf(charList)
    }
    val focusRequester = remember(cellsCount) { List(cellsCount) { FocusRequester() } }
    val transparentTextSelectionColors: TextSelectionColors = remember {
        TextSelectionColors(
            handleColor = Transparent,
            backgroundColor = Transparent
        )
    }

    fun requestFocus(index: Int) {
        val nextIndex = index.coerceIn(0, cellsCount - 1)
        focusRequester[nextIndex].requestFocusSafely()
    }

    LaunchedEffect(autoFocusByDefault) {
        if (autoFocusByDefault) focusRequester.first().requestFocus()
    }

    LaunchedEffect(isErrorOccurred) {
        if (isErrorOccurred) focusRequester.first().requestFocus()
    }

    OhTeePee(
        modifier = modifier,
        textSelectionColors = transparentTextSelectionColors,
        cellsCount = cellsCount,
        otpValue = otpValue,
        obscureText = obscureText,
        placeHolder = placeHolder,
        isErrorOccurred = isErrorOccurred,
        keyboardType = keyboardType,
        cellConfigurations = cellConfigurations,
        focusRequesters = focusRequester,
        enabled = enabled,
        onCellInputChange = onCellInputChange@{ index, newValue ->
            val currentCellText = otpValue[index].toString()
            val text = newValue.replace(placeHolder, String.EMPTY)
                .replace(obscureText, String.EMPTY)

            if (text == currentCellText) {
                requestFocus(index + 1)
                return@onCellInputChange
            }

            if (text.length == cellsCount) {
                onValueChange(text)
                focusRequester.last().requestFocusSafely()
                return@onCellInputChange
            }

            if (text.isNotEmpty()) {
                otpValue[index] = text.last()
                requestFocus(index + 1)
            } else if (currentCellText != placeHolder) {
                otpValue[index] = placeHolderAsChar
            } else {
                val previousIndex = (index - 1).coerceIn(0, cellsCount)
                otpValue[previousIndex] = placeHolderAsChar
                requestFocus(previousIndex)
            }
            onValueChange(otpValue.joinToString(""))
        },
    )
}

@Composable
private fun OhTeePee(
    modifier: Modifier = Modifier,
    textSelectionColors: TextSelectionColors,
    cellsCount: Int,
    otpValue: CharArray,
    obscureText: String,
    placeHolder: String,
    isErrorOccurred: Boolean,
    keyboardType: KeyboardType,
    cellConfigurations: CellConfigurations,
    focusRequesters: List<FocusRequester>,
    enabled: Boolean,
    onCellInputChange: (index: Int, value: String) -> Unit
) {
    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(cellsCount) { index ->
                val displayValue = getCellDisplayCharacter(
                    currentChar = otpValue[index],
                    obscureText = obscureText,
                    placeHolder = placeHolder
                )
                OhTeePeeCell(
                    value = displayValue,
                    isErrorOccurred = isErrorOccurred,
                    keyboardType = keyboardType,
                    modifier = cellConfigurations.modifier
                        .focusRequester(focusRequester = focusRequesters[index]),
                    enabled = enabled,
                    cellConfigurations = cellConfigurations,
                    isCurrentCharAPlaceHolder = displayValue == placeHolder,
                    onValueChange = {
                        onCellInputChange(index, it)
                    }
                )
            }
        }
    }
}

@Composable
private fun getCellDisplayCharacter(
    currentChar: Char,
    obscureText: String,
    placeHolder: String
): String = currentChar
    .toString()
    .replace(placeHolder, "")
    .let {
        if (it.isNotEmpty() && obscureText.isNotEmpty()) {
            obscureText
        } else {
            it
        }
    }
    .takeIf { it.isNotEmpty() } ?: placeHolder