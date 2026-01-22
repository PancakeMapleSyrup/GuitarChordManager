package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.guitarchordmanager.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DDaySetupDialog(
    initialGoal: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, String) -> Unit
) {
    var goal by remember { mutableStateOf(initialGoal) }
    // 날짜 직접 입력 텍스트 상태 (예: "20251225")
    var dateInputText by remember { mutableStateOf("") }
    // DatePicker 상태 초기화
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    // 오늘의 날짜 "YYYYMMDD" 문자열로 만들기
    val todayPlaceholder = remember {
        val today = LocalDate.now()
        today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    // 달력을 클릭하면 -> 입력창(dateInputText)에 날짜 표시
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
            // 달력에서 선택한 날짜를 "20251225" 형식으로 변환해 입력창에 넣음
            val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            // 입력창이 비어있지 않고, 사용자가 타이핑 중이 아닐 때만 업데이트 (포커스 충돌 방지)
            if (dateInputText != formattedDate && dateInputText.isNotEmpty()) {
                dateInputText = formattedDate
            } else if (dateInputText.isEmpty()) {
                // 처음 켜졌을 때나 비어있을 때는 채워줌
                dateInputText = formattedDate
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("D-day 목표 설정", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                // 목표 입력
                TextFieldWithLabel(
                    label = "목표",
                    value = goal,
                    onValueChange = { goal = it },
                    placeholder = "예: 공연, 합주 날짜",
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 날짜 직접 입력
                TextFieldWithLabel(
                    label = "날짜 입력 (YYYYMMDD)",
                    value = dateInputText,
                    onValueChange = { input ->
                        // 숫자만 입력받기 (최대 8자리)
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            dateInputText = input

                            // 8자리가 완성되면 -> 달력(datePickerState) 업데이트
                            if (input.length == 8) {
                                try {
                                    val parsedDate = LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyyMMdd"))
                                    val millis = parsedDate
                                        .atStartOfDay(ZoneOffset.UTC)
                                        .toInstant()
                                        .toEpochMilli()
                                    datePickerState.selectedDateMillis = millis
                                } catch (e: Exception) {
                                    // 날짜 형식이 올바르지 않으면 무시 (예: 20251345)
                                }
                            }
                        }
                    },
                    clearOnFocus = true, // 창 안의 날짜가 삭제되게 만듦
                    placeholder = "$todayPlaceholder",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (dateInputText.isBlank()) {
                                onConfirm(LocalDate.now(), goal)
                            } else {
                                val selectedDateMillis = datePickerState.selectedDateMillis
                                if (selectedDateMillis != null && dateInputText.isNotBlank()) {
                                    val date = Instant.ofEpochMilli(selectedDateMillis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    onConfirm(date, goal)
                                }
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 날짜 선택기
                Text("날짜 선택", style = Typography.bodySmall, color = Gray400)
                Spacer(modifier = Modifier.height(8.dp))

                // DatePicker가 너무 크면 스크롤 가능하게 Box로 감쌉니다
                Box(modifier = Modifier.height(350.dp)) {
                    DatePicker(
                        state = datePickerState,
                        title = null,
                        headline = null,
                        showModeToggle = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val date = Instant.ofEpochMilli(selectedDateMillis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onConfirm(date, goal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("설정 완료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Gray400)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
