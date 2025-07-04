package com.example.aimodelbench.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.aimodelbench.model.AIModelEnum
import com.example.aimodelbench.model.FunctionItemUIModel
import com.example.aimodelbench.ui.theme.Blue40
import com.example.aimodelbench.ui.theme.Dimens


@Composable
fun GridView(
    items: List<FunctionItemUIModel>,
    onItemClick: (AIModelEnum) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridSpacing),
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridSpacing)
    ) {
        items(items) { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.CardCornerRadius))
                    .background(Blue40)
                    .clickable { onItemClick(item.enumCode) }
                    .padding(Dimens.CardPadding)
            ) {
                Text(
                    text = item.title,
                    fontSize = Dimens.TextSize_16,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}