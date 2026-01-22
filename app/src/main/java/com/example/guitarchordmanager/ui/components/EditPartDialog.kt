package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.guitarchordmanager.ui.theme.Gray400
import com.example.guitarchordmanager.ui.theme.TossBlue
import com.example.guitarchordmanager.ui.theme.Typography
import com.example.guitarchordmanager.ui.theme.White

@Composable
fun EditPartDialog(
    initialName: String,
    initialMemo: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var memo by remember { mutableStateOf(initialMemo) }

    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("파트 정보 수정", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                // 파트 이름 입력
                TextFieldWithLabel(
                    label = "파트 이름",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "예: Verse 1, Chorus",
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 메모 입력
                TextFieldWithLabel(
                    label = "메모 (선택)",
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = "예: 조용하게 아르페지오",
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onConfirm(name, memo)
                            focusManager.clearFocus()
                        }
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, memo) },
                colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("수정 완료")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Gray400)
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(24.dp)
    )
}