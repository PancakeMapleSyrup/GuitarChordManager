package com.example.guitarchordmanager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.guitarchordmanager.ui.theme.*


@Composable
fun AnimationButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() } // 사용자가 버튼을 누르고 있는지 감지
    val isPressed by interactionSource.collectIsPressedAsState()    // 버튼이 눌린 상태라면 true

    // isPressed가 true이면 크기가 96%로 줄어들고, 떼면 다시 100%로 돌아온다
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    // enabled 상태에 따라 배경색과 텍스트 색상이 부드럽게 전환
    val backgroundColor by animateColorAsState(if (enabled) TossBlue else Gray100, label = "bg")
    val contentColor by animateColorAsState(if (enabled) White else Gray400, label = "text")

    Box(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = Typography.labelLarge.copy(color = contentColor)
        )
    }
}