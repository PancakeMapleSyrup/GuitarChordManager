package com.example.guitarchordmanager.songlist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale
import androidx.hilt.navigation.compose.hiltViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.example.guitarchordmanager.data.Song
import com.example.guitarchordmanager.ui.components.DDaySetupDialog
import com.example.guitarchordmanager.ui.components.EditSongDialog
import com.example.guitarchordmanager.ui.components.DeleteDialog
import com.example.guitarchordmanager.ui.components.SimpleTextField
import com.example.guitarchordmanager.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    viewModel: SongListViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dDayState by viewModel.dDayState.collectAsState()
    var editingSong by remember { mutableStateOf<Song?>(null) }
    var deletingSong by remember { mutableStateOf<Song?>(null) }

    // 플로팅 버튼(파트 추가) 애니메이션을 위한 코드
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "scale"
    )

    // 키보드 포커스 제어
    val focusManager = LocalFocusManager.current

    // D-day 다이얼로그 표시 상태
    var showDDayDialog by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromId = from.key as? String
        val toId = to.key as? String
        if (fromId != null && toId != null) {
            viewModel.reorderByKeys(fromId, toId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
        ) {
            // 헤더
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 헤더 텍스트 조건 (목표 내용이 비어있으면서 목표 날짜도 설정되어 있지 않다면 True)
                val isDefaultState = dDayState.targetDate == null ||
                        ((dDayState.dDayText == "Today" || dDayState.dDayText == "D-0") && dDayState.goal.isBlank())
                val headerText = if (!isDefaultState) { // 무언가 설정되어 있음
                    if (dDayState.goal.isNotBlank()) { // 목표 내용이 설정되어 있다면
                        "${dDayState.dDayText} | ${dDayState.goal}"
                    } else { // 목표 날짜만 설정되어 있다면
                        dDayState.dDayText
                    }
                } else { // 기본 상태
                    "🎼 플레이리스트"
                }

                Text(
                    text = headerText,
                    style = Typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )

                // D-day 설정 버튼
                IconButton(
                    onClick = { showDDayDialog = true },
                    modifier = Modifier
                        .background(Gray100, RoundedCornerShape(12.dp))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Set D-Day",
                        tint = TossBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 노래 추가 입력창
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 제목 입력
                    SimpleTextField(
                        value = uiState.inputTitle,
                        onValueChange = { viewModel.updateInputTitle(it) },
                        placeholder = "노래 제목 추가...",
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 가수 입력
                    SimpleTextField(
                        value = uiState.inputArtist,
                        onValueChange = { viewModel.updateInputArtist(it) },
                        placeholder = "가수 이름 추가...",
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.addSong()
                                focusManager.clearFocus()
                            }
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                // 추가 버튼
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .scale(scale) // 애니메이션 크기 적용
                        .clip(RoundedCornerShape(20.dp))
                        .background(TossBlue)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null, // 물결 효과 제거
                            onClick = {
                                viewModel.addSong()
                                focusManager.clearFocus()
                            }
                        ),
                    contentAlignment = Alignment.Center // 아이콘 중앙 정렬
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }

            // Box를 사용하여 LazyColumn과 안내 문구를 겹쳐서 배치
            Box(modifier = Modifier.fillMaxSize()) {

                // LazyColumn은 데이터가 있든 없든 항상 그려둡니다
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 즐겨찾기 섹션
                    if (uiState.favoriteSongs.isNotEmpty()) {
                        item {
                            Text(
                                "즐겨찾기",
                                color = Gray400,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(uiState.favoriteSongs, key = { it.id }) { song ->
                            SongItem(
                                song = song,
                                onClick = { onSongClick(song) },
                                onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                                onEditClick = { editingSong = song },
                                onDeleteClick = { viewModel.deleteSong(song.id) },
                                isDeletable = false,
                                isDraggable = false
                            )
                        }
                    }

                    // 일반 목록 섹션
                    if (uiState.normalSongs.isNotEmpty()) {
                        item {
                            Text(
                                "목록",
                                color = Gray400,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        itemsIndexed(uiState.normalSongs, key = { _, song -> song.id }) { index, song ->
                            ReorderableItem(reorderableState, key = song.id) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 10.dp else 0.dp, label = "elevation")

                                Box(
                                    modifier = Modifier
                                        .shadow(elevation, RoundedCornerShape(20.dp))
                                        .background(Color.Transparent)
                                ) {
                                    SongItem(
                                        song = song,
                                        onClick = { onSongClick(song) },
                                        onFavoriteClick = { viewModel.toggleFavorite(song.id) },
                                        onEditClick = { editingSong = song },
                                        onDeleteClick = { deletingSong = song },
                                        isDeletable = true,
                                        isDraggable = true,
                                        dragModifier = Modifier.draggableHandle()
                                    )
                                }
                            }
                        }
                    }
                }

                // 데이터가 진짜 없을 때만, 안내 문구를 화면 중앙에 띄웁니다.
                if (uiState.favoriteSongs.isEmpty() && uiState.normalSongs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "아직 추가된 노래가 없어요",
                            style = Typography.titleMedium,
                            color = Gray900,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "위 입력창에서 노래를 추가해보세요!",
                            style = Typography.bodyMedium,
                            color = Gray400
                        )
                    }
                }
            } // End of Box

            // 수정 창
            if (editingSong != null) {
                EditSongDialog(
                    initialTitle = editingSong!!.title,
                    initialArtist = editingSong!!.artist,
                    onDismiss = { editingSong = null },
                    onConfirm = { newTitle, newArtist ->
                        viewModel.updateSong(editingSong!!.id, newTitle, newArtist)
                        editingSong = null
                    }
                )
            }

            // 삭제 창
            if (deletingSong != null) {
                DeleteDialog(
                    title = "노래를 삭제할까요?",
                    description = "'${deletingSong!!.title}' 항목이\n영구적으로 삭제됩니다.",
                    onDismiss = { deletingSong = null },
                    onConfirm = {
                        viewModel.deleteSong(deletingSong!!.id)
                        deletingSong = null
                    }
                )
            }
        }
        // D-day 설정 다이얼로그
        if (showDDayDialog) {
            DDaySetupDialog(
                initialGoal = dDayState.goal,
                onDismiss = { showDDayDialog = false },
                onConfirm = { date, goal ->
                    viewModel.setDDay(date, goal)
                    showDDayDialog = false
                }
            )
        }
    }
}


@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeletable: Boolean = true,
    isDraggable: Boolean,
    dragModifier: Modifier = Modifier
) {
    // 배경색: 즐겨찾기는 약간 더 눈에 띄게, 일반은 회색
    val backgroundColor = if (song.isFavorite) Color(0xFFE8F3FF) else Gray100.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp)) // 둥글둥글하게
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // 드래그 핸들 (일반 목록에만 표시)
            if (isDraggable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag",
                    tint = Gray400,
                    modifier = dragModifier.size(24.dp) // 크기 지정
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 노래 정보
            Column {
                Text(
                    text = song.title,
                    style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Gray900,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    style = Typography.bodyLarge.copy(fontSize = 13.sp),
                    color = Gray400,
                    maxLines = 1
                )
            }
        }

        // 아이콘 모음 (수정, 삭제, 드래그)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // 삭제 아이콘 (isDeletable일 때만 보임)
            if (isDeletable) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Gray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 수정 아이콘 (연필)
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Gray400,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 즐겨찾기 별 아이콘
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = if (song.isFavorite) Color(0xFFFFD700) else Gray400, // 골드 or 회색
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
