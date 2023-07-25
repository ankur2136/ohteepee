package com.composeuisuite.ohteepee.configuration

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

data class CellConfigurations(
    val modifier: Modifier,
    val elevation: Dp,
    val activeCellConfig: SingleCellConfiguration,
    val errorCellConfig: SingleCellConfiguration,
    val emptyCellConfig: SingleCellConfiguration,
    val filledCellConfig: SingleCellConfiguration,
)

