package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.example.guitarchordmanager.ui.theme.*

@Composable
fun AddPartDialog(
    title: String,
    placeholder: String,
    existingPartNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    val presets = listOf("Intro", "Verse", "Chorus", "Bridge", "Interlude", "Outro")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // 파트 이름 입력 창
                SimpleTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = placeholder,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = { onConfirm(text) }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Gray100)
                Spacer(modifier = Modifier.height(12.dp))

                Text("빠른 선택", style = Typography.bodySmall, color = Gray400)
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { presetName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Gray100)
                                .clickable {
                                    val newName = generateNextPartName(presetName, existingPartNames)
                                    text = newName
                                    onConfirm(newName)
                                }
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = presetName,
                                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Gray900,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Gray400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Gray400)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

// 중복된 이름이 있으면 숫자를 붙여주는 함수
fun generateNextPartName(baseName: String, existingNames: List<String>): String {
    if (!existingNames.contains(baseName)) { // 기본 이름이 리스트에 없으면 그대로 사용
        return baseName
    }

    // 있다면 2부터 숫자를 붙여가며 빈 자리를 찾음
    var count = 2
    while (existingNames.contains("$baseName $count")) {
        count++
    }
    return "$baseName $count"
}