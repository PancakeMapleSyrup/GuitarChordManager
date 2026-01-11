package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.guitarchordmanager.ui.theme.*

@Composable
fun EditSongInfoDialog(
    initialTitle: String,
    initialArtist: String,
    initialBpm: String,
    initialCapo: String,
    initialTuning: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var artist by remember { mutableStateOf(initialArtist) }
    var bpm by remember {
        mutableStateOf(if (initialBpm == "-" || initialBpm == "0") "" else initialBpm)
    }
    var capo by remember {
        mutableStateOf(if (initialCapo == "None" || initialCapo == "0") "" else initialCapo)
    }
    var tuning by remember { mutableStateOf(initialTuning) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()) // 화면 작을 때 스크롤
            ) {
                Text("노래 정보 수정", style = Typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // 제목 & 가수
                SimpleTextField(value = title, onValueChange = { title = it }, label = "노래 제목")
                Spacer(modifier = Modifier.height(12.dp))
                SimpleTextField(value = artist, onValueChange = { artist = it }, label = "가수")

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(Modifier, DividerDefaults.Thickness, color = Gray100)
                Spacer(modifier = Modifier.height(12.dp))

                // 부가 정보 (가로 배치 or 세로 배치)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        SimpleTextField(
                            value = bpm,
                            onValueChange = { input ->
                                if (input.all {it.isDigit() }) bpm = input
                            },
                            label = "BPM",
                            placeholder = "-",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        SimpleTextField(
                            value = capo,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) capo = input
                            },
                            label = "Capo",
                            placeholder = "None",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SimpleTextField(value = tuning, onValueChange = { tuning = it }, label = "Tuning")

                Spacer(modifier = Modifier.height(24.dp))

                // 버튼
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("취소", color = Gray400) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(title, artist, bpm.ifBlank{ "-" }, capo.ifBlank{ "None" }, tuning) },
                        colors = ButtonDefaults.buttonColors(containerColor = TossBlue)
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }
}

// 라벨이 있는 텍스트 필드 헬퍼
@Composable
fun SimpleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    containerColor: Color = Gray100
) {
    Column {
        Text(label, style = Typography.bodySmall, color = Gray400)
        Spacer(modifier = Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(text = placeholder, color = Gray400.copy(alpha = 0.5f))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}