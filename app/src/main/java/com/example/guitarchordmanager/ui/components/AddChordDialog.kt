package com.example.guitarchordmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.example.guitarchordmanager.ui.theme.*
import kotlin.math.roundToInt

// [디자인 상수]
private val BOARD_COLOR = Gray900
private val DISABLED_COLOR = Gray400 // 비활성화된 화살표 색상
private val ROOT_COLOR = Color.Red
private val LINE_THICKNESS = 2.dp
private val MARKER_THICKNESS = 4.dp
private val DOT_RADIUS = 10.dp
private val NUT_THICKNESS = 6.dp

@Composable
fun AddChordDialog(
    title: String = "코드 생성",
    onDismiss: () -> Unit,
    onConfirm: (String, List<Int>) -> Unit
) {
    // 텍스트 필드에 들어갈 코드 이름 (예: "Cm7(b5)")
    var chordNameInput by remember { mutableStateOf("") }
    // 운지표 (기본값은 빈 상태)
    var fretPositions by remember { mutableStateOf(listOf(-1, -1, -1, -1, -1, -1)) }
    // 코드 이름 한 글자 지우기 로직
    val onDeleteChar = {
        if (chordNameInput.isNotEmpty()) {
            chordNameInput = chordNameInput.dropLast(1)
        }
    }
    // 시작 프렛 번호를 저장하는 상태
    var startFret by remember { mutableStateOf(1) }

    // 상태 관리 (테스트용)
    // 기본값: 모두 X (-1)
    var positions by remember { mutableStateOf(listOf(-1, -1, -1, -1, -1, -1)) }

    // 텍스트가 바뀔 때마다, 알고 있는 코드라면 운지표를 자동으로 업데이트 (편의 기능)
    LaunchedEffect(chordNameInput) {
        val standard = findStandardVoicing(chordNameInput)
        if (standard.count { it == -1 } != 6) {
            fretPositions = standard
            // TODO: 표준 운지법에 따라 startFret도 업데이트하는 로직이 필요할 수 있음
            // 예를 들어 하이코드의 경우 startFret은 1이 아님
            // 현재는 기본값 1로 유지.
            startFret = 1
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)  // 화면을 넓게 쓰기 위함
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(633.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // 헤더 (제목 + 닫기)
                Text(
                    text = title,
                    style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Gray900,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 메인 컨텐츠 (좌: 선택기, 우: 운지표)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // 좌측 코드 선택 패널
                    Column(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .padding(end = 16.dp)
                    ){
                        //  사용자 직접 입력 칸
                        SimpleTextField(
                            value = chordNameInput,
                            onValueChange = { chordNameInput = it },
                            placeholder = "직접 입력 또는 아래 버튼 클릭"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Root (C D E F G A B)
                        Text("Root", style = Typography.labelMedium, color = Gray400)
                        Spacer(modifier = Modifier.height(8.dp))
                        val roots = listOf("C", "D", "E", "F", "G", "A", "B")
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(roots) { root ->
                                InputChip(label = root, onClick = { chordNameInput += root }, backgroundColor = Color.White)
                            }
                            // 지우기 버튼
                            item {
                                InputChip(
                                    label = "⌫",
                                    onClick = onDeleteChar,
                                    backgroundColor = Gray100, // 약간 더 어두운 색으로 강조
                                    contentColor = Color(0xFFFF3553)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Accidental (#, b)
                        Text("Accidental", style = Typography.labelMedium, color = Gray400)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            InputChip(
                                label = "#",
                                onClick = { chordNameInput += "#" },
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFEEF2F9)
                            )
                            InputChip(
                                label = "b",
                                onClick = { chordNameInput += "b" },
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFEEF2F9)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quality
                        Text("Quality", style = Typography.labelMedium, color = Gray400)
                        Spacer(modifier = Modifier.height(8.dp))

                        // 숫자 줄 (2, 3, 4, 5, 6, 7, 9, 11)
                        val numbers = listOf("2", "3", "4", "5", "6", "7", "9", "11")
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(8),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(numbers) { num ->
                                InputChip(
                                    label = num,
                                    onClick = { chordNameInput += num },
                                    fontSize = 12.sp,
                                    backgroundColor = Color(0xFFEEF2F9)
                                )
                            }
                        }

                        // 기호 줄 (M, m, sus, add, dim, aug, (, ), /)
                        val symbols = listOf("M", "m", "sus", "add", "dim", "aug", "(", ")", "/")
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(symbols) { sym ->
                                InputChip(
                                    label = sym,
                                    onClick = { chordNameInput += sym },
                                    fontSize = 12.sp,
                                    backgroundColor = Color(0xFFE8F3FF)
                                )
                            }
                        }
                    }

                    // 구분선
                    VerticalDivider(color = Gray100, modifier = Modifier.padding(horizontal = 8.dp))

                    // 우측 운지표 프리뷰
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center // 카드 수직 중앙 정렬
                    ) {
                        // ChordChip 디자인을 그대로 옮겨온 Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // 그림자 효과
                            modifier = Modifier
                                .width(200.dp) // 캔버스(160dp)보다 약간 넓게
                                .wrapContentHeight() // 내용물 크기에 맞게 높이 조절
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 상단: 코드 이름 (회색 타이틀 바)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(Gray100.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chordNameInput.ifBlank { "이름 없음" },
                                        style = Typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Gray900
                                        )
                                    )
                                }

                                // 하단: 운지표 (Interactive Canvas)
                                Box(
                                    modifier = Modifier.padding(8.dp), // ChordChip과 동일한 패딩
                                    contentAlignment = Alignment.Center
                                ) {
                                    InteractiveFretboard(
                                        modifier = Modifier.size(300.dp, 300.dp),
                                        positions = fretPositions,
                                        onPositionChanged = { fretPositions = it },
                                        startFret = startFret,
                                        onStartFretChanged = { newFret -> startFret = newFret}
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "운지표를 터치해 수정하세요",
                            style = Typography.labelSmall.copy(color = Gray400),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 하단 버튼 (취소 / 추가)
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("취소", color = Gray400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(chordNameInput, fretPositions) }, // TODO: fretPositions도 같이 넘겨야 함
                        colors = ButtonDefaults.buttonColors(containerColor = TossBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = chordNameInput.isNotBlank() // 이름 없으면 버튼 비활성
                    ) {
                        Text("코드 추가", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun InputChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    backgroundColor: Color = Gray100, // 배경색 커스텀 가능하게 추가
    contentColor: Color = Gray900
) {
    // 햅틱 피드백을 위해 현재 View 가져오기
    val view = LocalView.current

    Card(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) // 탭 피드백
            onClick()
        },
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // 그림자 추가
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}


// 인터랙티브 운지표
@Composable
fun InteractiveFretboard(
    modifier: Modifier = Modifier,
    positions: List<Int>,   // 외부에서 받는 운지 상태 (0~5번줄)
    onPositionChanged: (List<Int>) -> Unit, // 상태 변경 시 호출할 콜백
    startFret: Int = 1,   // 시작 프렛 번호 (기본값: 1)
    onStartFretChanged: (Int) -> Unit
) {
    // 상수 정의
    val stringCount = 6 // 줄 개수 6개
    val fretCount = 5   // 프랫 5칸 (너트는 따로 구현되어있음)

    // 텍스트를 그리기 위한 도구(TextMeasurer)
    val textMeasurer = rememberTextMeasurer()
    val view = LocalView.current // 햅틱 피드백용

    // 근음 찾기: 활성화된 줄 중 가장 낮은 줄
    // 0번 인덱스(6번줄)부터 확인해서 처음으로 -1이 아닌 줄을 찾는다.
    val rootStringIndex = positions.indexOfFirst { it != -1 }

    // 최신 상태 참조 (클로저 캡처 문제 방지)
    val currentPositions by rememberUpdatedState(positions)
    val currentOnChanged by rememberUpdatedState(onPositionChanged)
    val currentStartFret by rememberUpdatedState(startFret)
    val currentOnStartFretChanged by rememberUpdatedState(onStartFretChanged)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

                    val width = size.width
                    val height = size.height

                    // 레이아웃 계산
                    val fretLabelWidth = width * 0.15f
                    val padding = 16.dp.toPx()  // 두 영역 사이의 안전 거리 생성
                    val rightBuffer = LINE_THICKNESS.toPx() // 오른쪽 끝 선이 잘리지 않게 여유 공간 확보
                    val boardStartX = fretLabelWidth + padding
                    val boardEndX = width - rightBuffer // 지판이 끝나는 실제 위치
                    val boardWidth = boardEndX - boardStartX

                    val stringSpacing = boardWidth / (stringCount - 1)
                    val fretSpacing = height / (fretCount + 1)
                    val topMargin = fretSpacing * 0.8f

                    // 화살표 영역 좌표 계산
                    val labelCenterY = topMargin + (fretSpacing * 0.5f)

                    // 왼쪽 사이드바 (화살표 영역) 터치?
                    if (tapOffset.x < fretLabelWidth) {
                        // 위쪽 화살표 영역 (중심 Y보다 위쪽)
                        if (tapOffset.y < labelCenterY) {
                            if (currentStartFret < 20) { // 20보다 작을 때만 동작
                                currentOnStartFretChanged(currentStartFret + 1)
                            }
                        } else { // 아래쪽 화살표 영역 (중심 Y보다 아래쪽)
                            if (currentStartFret > 1) { // 1보다 클 때만 동작
                                currentOnStartFretChanged(currentStartFret - 1)
                            }
                        }
                        return@detectTapGestures    // 화살표를 눌렀다면 지판 로직은 실행 X
                    }

                    // 지판 영역 터치
                    val adjustedX = tapOffset.x - boardStartX
                    // 지판 오른쪽 밖 터치 무시
                    if (adjustedX > boardWidth + 20f) return@detectTapGestures

                    // 몇 번 줄을 눌렀는지 계산 (반올림하여 가장 가까운 줄 찾기)
                    val clickedStringIndex = (adjustedX / stringSpacing).times(1).let {
                        it.roundToInt()
                    }.coerceIn(0, stringCount - 1)

                    // 현재 상태 변경을 위해 리스트 복사
                    val newPositions = currentPositions.toMutableList()
                    val currentState = newPositions[clickedStringIndex]

                    // 너트 위쪽(상단)을 눌렀는지 확인
                    if (tapOffset.y < topMargin) {
                        // 현재 X(-1)이면 -> O(0)
                        // 현재 O(0)이면 -> X(-1)
                        // 현재 프렛(1~)이 눌려있으면 -> X(-1)로 초기화 (0으로 초기화하고 싶음)
                        // 여기서는 X <-> O 토글로 구현
                        if (currentState == -1) {
                            newPositions[clickedStringIndex] = 0 // Open
                        } else {
                            newPositions[clickedStringIndex] = -1 // Mute
                        }
                    } else {
                        // [하단 터치] 프렛 계산 (1~5)
                        val clickedFret = ((tapOffset.y - topMargin) / fretSpacing).toInt() + 1

                        // 범위를 벗어난 터치 무시 (혹시 모를 버그 방지)
                        if (clickedFret in 1..fretCount) {
                            if (currentState == clickedFret) {
                                // 이미 눌린 곳 다시 클릭 -> Open(0)으로 변경
                                newPositions[clickedStringIndex] = 0
                            } else {
                                // 새로운 곳 클릭 -> 해당 프렛으로 변경
                                newPositions[clickedStringIndex] = clickedFret
                            }
                        }
                    }
                    // 부모에게 변경된 상태 전달
                    currentOnChanged(newPositions)
                }
            }
    ) { // 그리기 로직
        val width = size.width
        val height = size.height    // --- 캔버스 크기
        val fretLabelWidth = width * 0.15f  // 왼쪽의 15%는 프렛 번호와 화살표를 위한 공간
        val padding = 16.dp.toPx()  // 왼쪽 부분과 오른쪽 부분의 여백
        val rightBuffer = LINE_THICKNESS.toPx() // 오른쪽 여유 공간
        val boardStartX = fretLabelWidth + padding   // 지판이 시작되는 X 위치
        val boardEndX = width - rightBuffer
        val boardWidth = boardEndX - boardStartX
        val stringSpacing = boardWidth / (stringCount - 1)  // 가로 간격
        val fretSpacing = height / (fretCount + 1)  // 세로 간격
        val topMargin = fretSpacing * 0.8f  // 너트의 시작 Y 위치

        // 세로줄 그리기
        for (i in 0 until stringCount) {
            val x = boardStartX + (i * stringSpacing)
            drawLine(
                color = BOARD_COLOR,
                start = Offset(x, topMargin), // 너트 위치에서 시작
                end = Offset(x, topMargin + (fretCount * fretSpacing)), // 마지막 프렛까지
                strokeWidth = LINE_THICKNESS.toPx()
            )
        }
        // 가로줄 그리기
        drawLine( // 너트
            color = BOARD_COLOR,
            start = Offset(boardStartX, topMargin),
            end = Offset(boardEndX, topMargin),
            strokeWidth = NUT_THICKNESS.toPx()
        )
        // 나머지 프렛 바 (1번 ~ 5번)
        for (i in 1..fretCount) {
            val y = topMargin + (i * fretSpacing)
            drawLine(
                color = BOARD_COLOR,
                start = Offset(boardStartX, y),
                end = Offset(boardEndX, y),
                strokeWidth = LINE_THICKNESS.toPx()
            )
        }

        // 프렛 번호와 화살표
        val labelCenterY = topMargin + (fretSpacing * 0.5f) // 첫 칸의 중간 높이
        val labelCenterX = fretLabelWidth / 2               // 왼쪽 영역의 가로 가운데
        // 텍스트 그리기 (숫자)
        val textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(startFret.toString()),
            style = TextStyle(
                color = BOARD_COLOR,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
        // 텍스트를 중앙에 배치하기 위한 좌표 계산
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = labelCenterX - (textLayoutResult.size.width / 2),
                y = labelCenterY - (textLayoutResult.size.height / 2)
            )
        )
        // 화살표 색상 계산
        val upArrowColor = if (startFret < 20) BOARD_COLOR else DISABLED_COLOR
        val downArrowColor = if (startFret > 1) BOARD_COLOR else DISABLED_COLOR
        val arrowSize = 8.dp.toPx()
        // 위 화살표 (▲) 그리기
        val upArrowY = labelCenterY - (textLayoutResult.size.height / 2) - 12.dp.toPx()
        drawPath(
            path = Path().apply {
                moveTo(labelCenterX, upArrowY - arrowSize)
                lineTo(labelCenterX - arrowSize, upArrowY)
                lineTo(labelCenterX + arrowSize, upArrowY)
                close()
            },
            color = upArrowColor
        )
        // 아래쪽 화살표 (▼)
        val downArrowY = labelCenterY + (textLayoutResult.size.height / 2) + 12.dp.toPx()
        drawPath(
            path = Path().apply {
                moveTo(labelCenterX, downArrowY + arrowSize)
                lineTo(labelCenterX - arrowSize, downArrowY)
                lineTo(labelCenterX + arrowSize, downArrowY)
                close()
            },
            color = downArrowColor
        )

        // 마커(점, X, O) 그리기
        val markerY = topMargin - 15.dp.toPx() // 마커 위치 (너트 위)
        val markerRadius = 8.dp.toPx()

        positions.forEachIndexed { index, state ->
            val x = boardStartX + (index * stringSpacing)

            // 색상 결정: 근음(rootStringIndex)이면 빨간색, 아니면 기본색
            val itemColor = if (index == rootStringIndex) ROOT_COLOR else BOARD_COLOR

            when (state) {
                -1 -> { // X (mute)
                    val xSize = markerRadius
                    drawLine(
                        color = itemColor,
                        start = Offset(x - xSize, markerY - xSize),
                        end = Offset(x + xSize, markerY + xSize),
                        strokeWidth = MARKER_THICKNESS.toPx()
                    )
                    drawLine(
                        color = itemColor,
                        start = Offset(x + xSize, markerY - xSize),
                        end = Offset(x - xSize, markerY + xSize),
                        strokeWidth = MARKER_THICKNESS.toPx()
                    )
                }

                0 -> { // O (open)
                    drawCircle(
                        color = itemColor,
                        radius = markerRadius,
                        center = Offset(x, markerY),
                        style = Stroke(width = MARKER_THICKNESS.toPx())
                    )
                }

                else -> { // 1~5 (프렛 누름) -> 점 그리기
                    val y = topMargin + (state * fretSpacing) - (fretSpacing / 2)

                    drawCircle(
                        color = itemColor,
                        radius = DOT_RADIUS.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

// 텍스트 기반으로 기본 운지법 찾기 (이름 매칭)
fun findStandardVoicing(chordName: String): List<Int> {
    // 띄어쓰기 제거나 대소문자 처리가 필요할 수 있음
    return when (chordName) {
        "C" -> listOf(-1, 3, 2, 0, 1, 0)
        "Cm" -> listOf(-1, 3, 5, 5, 4, 3) // 바레 코드 예시
        "C7" -> listOf(-1, 3, 2, 3, 1, 0)
        "D" -> listOf(-1, -1, 0, 2, 3, 2)
        "Dm" -> listOf(-1, -1, 0, 2, 3, 1)
        "E" -> listOf(0, 2, 2, 1, 0, 0)
        "Em" -> listOf(0, 2, 2, 0, 0, 0)
        "F" -> listOf(1, 3, 3, 2, 1, 1)
        "G" -> listOf(3, 2, 0, 0, 0, 3)
        "A" -> listOf(-1, 0, 2, 2, 2, 0)
        "Am" -> listOf(-1, 0, 2, 2, 1, 0)
        // 여기에 없는 복잡한 코드는 사용자가 직접 그려야 함
        else -> listOf(-1, -1, -1, -1, -1, -1) // 매칭 안되면 다 비움
    }
}