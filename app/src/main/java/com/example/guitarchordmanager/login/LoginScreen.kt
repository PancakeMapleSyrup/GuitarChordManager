package com.example.guitarchordmanager.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.hilt.navigation.compose.hiltViewModel // Hilt 사용 시

import com.example.guitarchordmanager.ui.theme.TossBlue
import com.example.guitarchordmanager.ui.theme.White
import com.example.guitarchordmanager.ui.theme.Typography
import com.example.guitarchordmanager.ui.components.TextField
import com.example.guitarchordmanager.ui.components.PrimaryButton

@Composable
fun LoginScreen(
    // ViewModel 주입
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    // ViewModel의 상태를 관찰
    // uiState 값이 바뀌면 화면이 자동으로 다시 그려진다.
    val uiState by viewModel.uiState.collectAsState()
    // 포커스 이동을 위해 필요
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // 배경: 은은하게 움직이는 파란 물방울 (Liquid Effect)
        // 흰 배경 뒤에서 파란색이 번지는 느낌을 줍니다.
        CleanLiquidBackground()

        // 전면 UI: 여백을 충분히 활용
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp) // 좌우 여백 24dp (Toss 표준)
                .systemBarsPadding() // 상단 상태바 겹침 방지
                .imePadding(), // 키보드 높이만큼 패딩을 주어 입력창이 가려지지 않게 함
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎸 Guitar Chord Manager \n시작해볼까요?",
                style = Typography.headlineLarge,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // ViewModel에게 데이터 전달
            TextField(
                value = uiState.id, // ViewModel의 값 사용
                onValueChange = { viewModel.updateId(it) }, // ViewModel 함수 호출
                placeholder = "아이디를 입력해주세요",
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Glass/Flat Input Field (비밀번호)
            TextField(
                value = uiState.pw,
                onValueChange = { viewModel.updatePw(it) }, // ViewModel 함수 호출
                placeholder = "비밀번호를 입력해주세요",
                isPassword = true,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus() // 키보드 내리기
                        if (uiState.isButtonEnabled) viewModel.login(onLoginSuccess)
                    }
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 버튼 활성화 여부도 ViewModel이 결정해준 대로 따름
            PrimaryButton(
                text = if (uiState.isLoading) "잠시만요..." else "로그인",
                enabled = uiState.isButtonEnabled && !uiState.isLoading,
                onClick = { viewModel.login(onLoginSuccess) } // ViewModel 함수 호출
            )
        }
    }
}

// --------------------------------------------------
// Components
// --------------------------------------------------
@Composable
fun CleanLiquidBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")

    // 매우 천천히 움직이는 파란 원들
    val t by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "movement"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // Blur 효과: 원의 경계를 아주 흐릿하게 뭉개서 '빛'처럼 보이게 함
            .blur(60.dp)
            .alpha(0.6f) // 너무 진하지 않게 투명도 조절
    ) {
        val width = size.width
        val height = size.height

        // 첫 번째 원 (왼쪽 위에서 움직임) - Toss Blue
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(TossBlue.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(
                    x = width * 0.2f + (cos(t) * 100f),
                    y = height * 0.2f + (sin(t) * 100f)
                ),
                radius = 400f
            ),
            radius = 400f,
            center = Offset(
                x = width * 0.2f + (cos(t) * 100f),
                y = height * 0.2f + (sin(t) * 100f)
            )
        )

        // 두 번째 원 (오른쪽 아래에서 움직임) - 하늘색
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF48A6FF).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(
                    x = width * 0.8f - (sin(t) * 120f),
                    y = height * 0.8f - (cos(t) * 120f)
                ),
                radius = 500f
            ),
            radius = 500f,
            center = Offset(
                x = width * 0.8f - (sin(t) * 120f),
                y = height * 0.8f - (cos(t) * 120f)
            )
        )
    }
}
