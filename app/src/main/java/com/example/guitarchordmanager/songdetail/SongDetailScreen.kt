package com.example.guitarchordmanager.songdetail

import android.R.attr.label
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.guitarchordmanager.data.Song
import com.example.guitarchordmanager.ui.components.EditSongInfoDialog
import com.example.guitarchordmanager.ui.components.TextField
import com.example.guitarchordmanager.ui.theme.*
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.math.ceil

@Composable
fun SongDetailScreen(
    title: String,
    artist: String,
    viewModel: SongDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val song = uiState.song

    // 다이얼로그 상태
    var showAddPartDialog by remember { mutableStateOf(false) }
    var showAddChordDialogForPartId by remember { mutableStateOf<String?>(null) } // 파트 ID 저장
    var showEditInfoDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
        ) {
            // 상단 네비게이션 바
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gray900)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column (
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showEditInfoDialog = true }
                        .padding(8.dp)
                ) {
                    Text(title, style = Typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text(artist, style = Typography.bodyMedium.copy(color = Gray400))

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InfoBadge(label = "BPM", value = song.bpm)
                        InfoBadge(label = "Capo", value = song.capo)
                        InfoBadge(label = "Tune", value = song.tuning)
                    }

                    // 오른쪽에 수정 아이콘을 작게 둬서 클릭 가능하다는 힌트 주기 (선택사항)
                    Icon(Icons.Default.Edit, contentDescription = "Edit Info", tint = Gray400, modifier = Modifier.size(16.dp))
                }
            }

            // 파트 리스트 (드래그 가능)
            val partListState = rememberLazyListState()
            val partReorderState = rememberReorderableLazyListState(partListState) { from, to ->
                val fromId = from.key as? String
                val toId = to.key as? String
                if (fromId != null && toId != null) {
                    viewModel.reorderParts(fromId, toId)
                }
            }

            LazyColumn(
                state = partListState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.parts, key = { it.id }) { part ->
                    ReorderableItem(partReorderState, key = part.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "partElev")

                        // 파트 아이템 (카드)
                        PartItem(
                            part = part,
                            elevation = elevation,
                            onAddChordClick = { showAddChordDialogForPartId = part.id },
                            onDeletePartClick = { viewModel.deletePart(part.id) },
                            onReorderChord = { from, to -> viewModel.reorderChords(part.id, from, to) },
                            onDeleteChord = { chordId -> viewModel.deleteChord(part.id, chordId) },
                            dragModifier = Modifier.draggableHandle() // 파트 드래그 핸들
                        )
                    }
                }
            }

            if (showEditInfoDialog) {
                EditSongInfoDialog(
                    initialTitle = song.title,
                    initialArtist = song.artist,
                    initialBpm = song.bpm,
                    initialCapo = song.capo,
                    initialTuning = song.tuning,
                    onDismiss = { showEditInfoDialog = false },
                    onConfirm = { t, a, b, c, tu ->
                        viewModel.updateSongInfo(t, a, b, c, tu)
                        showEditInfoDialog = false
                    }
                )
            }
        }

        // 플로팅 버튼 (파트 추가)
        ExtendedFloatingActionButton(
            onClick = { showAddPartDialog = true },
            containerColor = TossBlue,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            icon = {
                Icon(Icons.Default.Add, contentDescription = null)
            },
            text = {
                Text(text = "파트 추가", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            }
        )

        // --- 다이얼로그들 ---

        // 파트 추가 다이얼로그
        if (showAddPartDialog) {
            SimpleTextInputDialog(
                title = "새로운 파트 추가",
                placeholder = "예: Chorus, Verse 1",
                onDismiss = { showAddPartDialog = false },
                onConfirm = { name ->
                    viewModel.addPart(name)
                    showAddPartDialog = false
                }
            )
        }

        // 코드 추가 다이얼로그 (간단한 입력창 버전)
        if (showAddChordDialogForPartId != null) {
            SimpleTextInputDialog(
                title = "코드 추가",
                placeholder = "예: Am7, G/B",
                onDismiss = { showAddChordDialogForPartId = null },
                onConfirm = { chordName ->
                    viewModel.addChord(showAddChordDialogForPartId!!, chordName)
                    // showAddChordDialogForPartId = null // 계속 열어두려면 이 줄 삭제
                }
            )
        }
    }
}

// ------------------------------------
// 컴포넌트: 개별 파트 카드 (PartItem)
// ------------------------------------
@Composable
fun PartItem(
    part: SongPart,
    elevation: androidx.compose.ui.unit.Dp,
    onAddChordClick: () -> Unit,
    onDeletePartClick: () -> Unit,
    onReorderChord: (String, String) -> Unit,
    onDeleteChord: (String) -> Unit,
    dragModifier: Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Gray100.copy(alpha = 0.8f)), // 투명한 배경
        elevation = CardDefaults.cardElevation(elevation),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // [파트 헤더] 이름 + 드래그 핸들 + 삭제/추가 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 드래그 핸들
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Move Part",
                    tint = Gray400,
                    modifier = dragModifier
                )
                Spacer(modifier = Modifier.width(8.dp))

                // 파트 이름
                Text(
                    text = part.name,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TossBlue),
                    modifier = Modifier.weight(1f)
                )

                // 코드 추가 버튼 (작은 +)
                IconButton(
                    onClick = onAddChordClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Chord", tint = TossBlue, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 파트 삭제 버튼 (X)
                IconButton(
                    onClick = onDeletePartClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete Part", tint = Gray400, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BoxWithConstraints {
                val screenWidth = maxWidth
                val spacing = 8.dp
                val minCellWidth = 140.dp   // 코드 박스 최소 너비 (운지표 공간 확보)
                val cellHeight = 240.dp     // 코드 박스 높이 (운지표 공간 확보)

                // 한 줄에 들어갈 수 있는 개수 계산
                // (화면너비 + 간격) / (최소너비 + 간격)
                val columns = maxOf(1, ((screenWidth + spacing) / (minCellWidth + spacing)).toInt())

                // 필요한 행 갯 계산
                val rows = ceil(part.chords.size.toFloat() / columns).toInt()

                // 전체 그리드 높이 계산 (행 높이 + 간격)
                val gridHeight = if (rows > 0) {
                    (cellHeight * rows) + (spacing * (rows - 1))
                } else {
                    0.dp
                }

                // 빈 파트일 때 안내 문구
                if (part.chords.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("코드를 추가해주세요", style = Typography.bodySmall.copy(color = Gray400))
                    }
                } else {
                        val chordGridState = rememberLazyGridState()
                        val chordReorderState = rememberReorderableLazyGridState(chordGridState) { from, to ->
                            val fromId = from.key as? String
                            val toId = to.key as? String
                            if (fromId != null && toId != null) {
                                onReorderChord(fromId, toId)
                            }
                        }
                    LazyVerticalGrid(
                        state = chordGridState,
                        columns = GridCells.Fixed(columns), // 계산된 컬럼 수 고정
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        userScrollEnabled = false, // 중요: 내부 스크롤을 꺼야 외부 리스트(LazyColumn)가 스크롤 됨
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight) // 중요: 계산된 높이만큼 강제로 늘림
                    ) {
                        items(part.chords, key = { it.id }) { chord ->
                            ReorderableItem(chordReorderState, key = chord.id) { isDragging ->
                                val chordElevation by animateDpAsState(
                                    if (isDragging) 8.dp else 0.dp,
                                    label = "chord"
                                )

                                ChordChip(
                                    name = chord.name,
                                    elevation = chordElevation,
                                    modifier = Modifier.longPressDraggableHandle(true),
                                    onClick = { /* 수정 기능 연결 */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChordChip(
    name: String,
    elevation: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = elevation,
        modifier = modifier
            .height(240.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단: 코드 이름
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Gray100.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Gray900)
                )
            }

            // 하단: 운지표 들어갈 공간 (Placeholder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // 나중에 여기에 실제 운지표 그림(Canvas)을 넣으면 됩니다.
                // 지금은 가이드라인처럼 점선이나 텍스트를 표시
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HorizontalDivider(
                        modifier = Modifier.width(40.dp),
                        thickness = 1.dp,
                        color = Gray100
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.width(40.dp),
                        thickness = 1.dp,
                        color = Gray100
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.width(40.dp),
                        thickness = 1.dp,
                        color = Gray100
                    )
                }
            }
        }
    }
}

// ------------------------------------
// 다이얼로그 컴포넌트 (재사용)
// ------------------------------------
@Composable
fun SimpleTextInputDialog(
    title: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = placeholder
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
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

// ---------------------------
// 작은 정보 배지 컴포넌트
// ---------------------------
@Composable
fun InfoBadge(label: String, value: String) {
    Surface(
        color = Gray100,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = Typography.labelSmall.copy(color = Gray400, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, style = Typography.labelSmall.copy(color = Gray900, fontWeight = FontWeight.Bold))
        }
    }
}