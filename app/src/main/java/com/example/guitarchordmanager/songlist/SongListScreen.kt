package com.example.guitarchordmanager.songlist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.example.guitarchordmanager.data.Song
import com.example.guitarchordmanager.ui.components.TextField
import com.example.guitarchordmanager.ui.components.EditSongDialog
import com.example.guitarchordmanager.ui.components.DeleteDialog
import com.example.guitarchordmanager.ui.theme.*

@Composable
fun SongListScreen(
    viewModel: SongListViewModel = hiltViewModel(),
    onSongClick: (Song) -> Unit
) {
    // ViewModel의 통합된 UI 상태를 관찰
    val uiState by viewModel.uiState.collectAsState()

    // 수정 중인 노래를 저장하는 상태 (null이면 수정 안하는 중)
    var editingSong by remember { mutableStateOf<Song?>(null) }
    // 삭제 대기 중인 노래 상태 (null이면 팝업 안 뜸)
    var deletingSong by remember { mutableStateOf<Song?>(null) }

    // 드래그 앤 드롭 상태 설정
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
                .statusBarsPadding() // 상단 여백 확보
        ) {
            // 헤더
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "🎼 플레이리스트",
                style = Typography.headlineLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

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
                    // 제목 입력 창
                    TextField(
                        value = uiState.inputTitle,
                        onValueChange = { viewModel.updateInputTitle(it) },
                        placeholder = "노래 제목 추가..."
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // 입력창 사이 간격

                    // 가수 입력 창
                    TextField(
                        value = uiState.inputArtist,
                        onValueChange = { viewModel.updateInputArtist(it) },
                        placeholder = "가수 이름 추가..."
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                // 추가 버튼 (작은 원형)
                IconButton(
                    onClick = { viewModel.addSong() },
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .background(TossBlue, RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
            }

            // 리스트 (즐겨찾기 + 일반)
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // [섹션 1] 즐겨찾기 (고정됨)
                if (uiState.favoriteSongs.isNotEmpty()) {
                    item {
                        Text(
                            "즐겨찾기",
                            color = Gray400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(uiState.favoriteSongs, key = { it.id }) { song ->
                        // 즐겨찾기는 드래그 기능 없이 렌더링
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

                // [섹션 2] 일반 목록 (드래그 가능)
                if (uiState.normalSongs.isNotEmpty()) {
                    item {
                        Text(
                            "목록",
                            color = Gray400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    // itemsIndexed를 사용해야 Reorderable에서 정확한 위치 파악 가능
                    itemsIndexed(uiState.normalSongs, key = { _, song -> song.id }) { index, song ->
                        ReorderableItem(reorderableState, key = song.id) { isDragging ->
                            // 드래그 중일 때 약간 붕 뜨는 효과
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
                                    // 드래그 핸들 modifier 전달
                                    dragModifier = Modifier.draggableHandle()
                                )
                            }
                        }
                    }
                }
            }

            // 수정 창
            if (editingSong != null) {
                EditSongDialog(
                    initialTitle = editingSong!!.title,
                    initialArtist = editingSong!!.artist,
                    onDismiss = { editingSong = null }, // 취소하면 닫기
                    onConfirm = { newTitle, newArtist ->
                        viewModel.updateSong(editingSong!!.id, newTitle, newArtist)
                        editingSong = null // 수정 후 닫기
                    }
                )
            }

            // 삭제 창
            if (deletingSong != null) {
                DeleteDialog(
                    title = "노래를 삭제할까요?",
                    description = "'${deletingSong!!.title}' 항목이\n영구적으로 삭제됩니다.", // 취소하면 닫기만 함
                    onDismiss = { deletingSong = null },
                    onConfirm = {
                        viewModel.deleteSong(deletingSong!!.id)
                        deletingSong = null // 닫기
                    }
                )
            }
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
