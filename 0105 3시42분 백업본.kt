package com.example.guitarchordmaker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.guitarchordmaker.ui.theme.GuitarChordMakerTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.DateRange
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.DragHandle // 선 3개 아이콘
import androidx.compose.material.icons.filled.Person


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 테마 이름이 다를 경우 기본 MaterialTheme으로 감싸셔도 됩니다.
            GuitarChordMakerTheme {
                GuitarChordApp()
            }
        }
    }
}

// [핵심] 앱의 메인 진입점 (여기서 데이터 로드/저장 관리)
// [수정] 노래 수정/삭제 기능 연결
@Composable
fun GuitarChordApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val songList = remember {
        mutableStateListOf<Song>().apply {
            addAll(DataManager.loadSongs(context))
        }
    }

    fun saveAll() {
        DataManager.saveSongs(context, songList)
    }

    NavHost(navController = navController, startDestination = "song_list") {
        composable("song_list") {
            SongListScreen(
                songs = songList,
                onAddSong = { title, artist -> songList.add(Song(title, artist))
                    saveAll()
                },
                onSongClick = { song ->
                    song.lastPlayed = System.currentTimeMillis()
                    saveAll()
                    val index = songList.indexOf(song)
                    if (index != -1) {
                        navController.navigate("song_detail/$index")
                    }
                },
                // [추가] 정보 수정(메모/즐겨찾기) 시 저장
                onUpdateSong = { saveAll() },
                // [추가] 팝업에서 삭제 버튼 눌렀을 때 처리
                onDeleteSong = { song ->
                    songList.remove(song)
                    saveAll()
                },
                // [추가] 순서 변경 시 데이터 리스트에서도 위치 변경 및 저장
                onMove = { fromIndex, toIndex ->
                    songList.move(fromIndex, toIndex)
                    saveAll()
                }
            )
        }

        // [변경] 식별자를 index로 변경 (제목 수정 시 식별자가 바뀌는 문제 해결)
        composable("song_detail/{songIndex}") { backStackEntry ->
            val indexStr = backStackEntry.arguments?.getString("songIndex") ?: "-1"
            val index = indexStr.toIntOrNull() ?: -1

            if (index in songList.indices) {
                val targetSong = songList[index]
                SongDetailScreen(
                    navController = navController,
                    song = targetSong,
                    onSave = { saveAll() },
                    onUpdateInfo = { newTitle, newArtist ->
                        // [추가] 정보 수정 시 리스트 갱신 및 저장
                        targetSong.title = newTitle
                        targetSong.artist = newArtist
                        saveAll()
                    },
                    onDelete = {
                        // [추가] 삭제 시 리스트에서 제거, 저장, 뒤로가기
                        songList.removeAt(index)
                        saveAll()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// [화면 1] 노래 목록 리스트 화면
// [수정] 가수 이름 입력 및 표시 추가
// [수정] 마지막 연습 날짜 표시 기능 추가
// [수정] 'ⓘ' 버튼 추가 및 상세 정보(메모, 즐겨찾기, 삭제) 팝업 구현
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    songs: List<Song>,
    onAddSong: (String, String) -> Unit,
    onSongClick: (Song) -> Unit,
    onUpdateSong: () -> Unit = {},
    onDeleteSong: (Song) -> Unit = {},
    onMove: (Int, Int) -> Unit // "모든 노래"용 이동 콜백
) {
    // [추가] Context와 SharedPreferences 가져오기
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("d_day_prefs", android.content.Context.MODE_PRIVATE)
    }

    // 화면 강제 갱신을 위한 변수
    var refreshTrigger by remember { mutableStateOf(0) }

    // 기존 변수들
    var showAddDialog by remember { mutableStateOf(false) }
    var newSongTitle by remember { mutableStateOf("") }
    var newArtistName by remember { mutableStateOf("") }

    // 팝업 관련 상태
    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedSongForInfo by remember { mutableStateOf<Song?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // 기타 다이얼로그 상태 (D-day, 개발자 정보 등)
    var showDdayDialog by remember { mutableStateOf(false) }
    var showDeveloperDialog by remember { mutableStateOf(false) }

    // [수정] D-day 데이터: 저장된 값 불러오기 (없으면 기본값 0L / "")
    var dDayTimestamp by remember {
        mutableStateOf(sharedPreferences.getLong("timestamp", 0L))
    }
    var dDayTitle by remember {
        mutableStateOf(sharedPreferences.getString("title", "") ?: "")
    }
    // 다이얼로그에서 수정 중일 때 쓸 임시 변수
    var tempDDayTitle by remember { mutableStateOf("") }

    // [추가] D-day 저장 함수
    fun saveDDay(timestamp: Long, title: String) {
        sharedPreferences.edit().apply {
            putLong("timestamp", timestamp)
            putString("title", title)
            apply()
        }
        dDayTimestamp = timestamp
        dDayTitle = title
    }

    val dateFormatter = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())

    // [추가] 드롭다운 메뉴 상태 변수
    var showMenu by remember { mutableStateOf(false) }

    // [1] 즐겨찾기 목록 (refreshTrigger로 갱신)
    val favoriteSongs = remember(songs, refreshTrigger, songs.size) {
        songs.filter { it.isFavorite }
    }

    // [2] "모든 노래"용 드래그 상태
    val dragDropState = rememberDragDropState(rememberLazyListState(), songs.size) { from, to ->
        onMove(from, to)
    }

    // [3] "즐겨찾기"용 드래그 상태 (새로 추가됨)
    val favDragDropState = rememberDragDropState(dragDropState.state, favoriteSongs.size) { from, to ->
        // 즐겨찾기 리스트 내에서 from -> to 로 이동
        if (from in favoriteSongs.indices && to in favoriteSongs.indices) {
            val fromSong = favoriteSongs[from]
            val toSong = favoriteSongs[to]

            // 원본 리스트(songs)에서의 실제 인덱스를 찾음
            val realFromIndex = songs.indexOf(fromSong)
            val realToIndex = songs.indexOf(toSong)

            if (realFromIndex != -1 && realToIndex != -1) {
                // 원본 리스트에서 위치 교환
                onMove(realFromIndex, realToIndex)
                // 화면 갱신 유도
                refreshTrigger++
            }
        }
    }

    val listState = dragDropState.state // 같은 스크롤 상태 공유

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "추가")
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                // [수정] 제목을 앱 이름으로 복구
                title = { Text("🎸 Guitar Chord Maker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // [수정] 점 3개 메뉴 아이콘 및 드롭다운 메뉴 복구
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "메뉴")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("D-day 설정") },
                                onClick = {
                                    showMenu = false
                                    showDdayDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("제작자 정보") },
                                onClick = {
                                    showMenu = false
                                    showDeveloperDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                // [주의] 드래그 컨테이너는 하나만 있어도 동작하지만,
                // 여기서는 전체 리스트 영역을 감싸되 상태는 각각의 아이템 핸들에서 처리함
                .dragContainer(dragDropState)
        ) {
            // 1. D-Day 섹션
            if (dDayTitle.isNotEmpty()) {
                item {
                    val diff = dDayTimestamp - System.currentTimeMillis()
                    val days = diff / (1000 * 60 * 60 * 24)
                    val dDayText = if (days > 0) "D-$days" else if (days == 0L) "D-Day" else "D+${-days}"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(dDayTitle, style = MaterialTheme.typography.titleMedium)
                            Text(dDayText, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            Text(
                                dateFormatter.format(java.util.Date(dDayTimestamp)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // 2. 즐겨찾기 섹션
            if (favoriteSongs.isNotEmpty()) {
                item {
                    Text(
                        "즐겨찾기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // [즐겨찾기 리스트]
                itemsIndexed(
                    items = favoriteSongs,
                    key = { _, song -> "fav_${song.hashCode()}" }
                ) { index, song ->
                    // [수정] 현재 아이템이 드래그 중인지 확인
                    val isDragging = index == favDragDropState.draggingItemIndex
                    val modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)

                    SongListItem(
                        modifier = modifier,
                        song = song,
                        dateFormatter = dateFormatter,
                        onClick = { onSongClick(song) },
                        onInfoClick = { selectedSongForInfo = song; showInfoDialog = true },
                        isDraggable = true,
                        dragModifier = Modifier.draggableHandle(favDragDropState, index),
                        isDragging = isDragging // [추가] 드래그 상태 전달
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
            }

            // 3. 모든 노래 섹션
            item {
                Text(
                    "모든 노래",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // [모든 노래 리스트]
            itemsIndexed(
                items = songs,
                key = { _, song -> "all_${song.hashCode()}" }
            ) { index, song ->
                // [수정] 현재 아이템이 드래그 중인지 확인
                val isDragging = index == dragDropState.draggingItemIndex
                val modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)

                SongListItem(
                    modifier = modifier,
                    song = song,
                    dateFormatter = dateFormatter,
                    onClick = { onSongClick(song) },
                    onInfoClick = { selectedSongForInfo = song; showInfoDialog = true },
                    isDraggable = true,
                    dragModifier = Modifier.draggableHandle(dragDropState, index),
                    isDragging = isDragging // [추가] 드래그 상태 전달
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // === 다이얼로그 모음 (기존과 동일) ===

        // 1. 노래 추가 다이얼로그
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("새 노래 추가") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newSongTitle,
                            onValueChange = { newSongTitle = it },
                            label = { Text("노래 제목") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newArtistName,
                            onValueChange = { newArtistName = it },
                            label = { Text("가수 이름 (선택)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newSongTitle.isNotBlank()) {
                                    onAddSong(newSongTitle, newArtistName)
                                    showAddDialog = false
                                }
                            })
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newSongTitle.isNotBlank()) {
                            onAddSong(newSongTitle, newArtistName)
                            showAddDialog = false
                        }
                    }) { Text("추가") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("취소") }
                }
            )
        }

        // 2. 상세 정보 팝업
        if (showInfoDialog && selectedSongForInfo != null) {
            val song = selectedSongForInfo!!
            var memo by remember { mutableStateOf(song.memo) }

            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("노래 정보", style = MaterialTheme.typography.titleLarge)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (song.artist.isNotBlank()) {
                            Text("가수: ${song.artist}", fontSize = 14.sp, color = Color.Gray)
                        }
                        HorizontalDivider()

                        // 즐겨찾기 토글
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    song.isFavorite = !song.isFavorite
                                    onUpdateSong()
                                    refreshTrigger++
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                if (song.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (song.isFavorite) Color(0xFFFFD700) else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (song.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가")
                        }

                        HorizontalDivider()

                        OutlinedTextField(
                            value = memo,
                            onValueChange = { memo = it },
                            label = { Text("메모") },
                            placeholder = { Text("연습 포인트, 주법 등을 기록하세요") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    Row {
                        TextButton(
                            onClick = {
                                showDeleteConfirmDialog = true
                                showInfoDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) { Text("삭제") }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            song.memo = memo
                            onUpdateSong()
                            showInfoDialog = false
                        }) { Text("저장") }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInfoDialog = false }) { Text("닫기") }
                }
            )
        }

        // 3. 삭제 확인 다이얼로그 (생략 없이 유지)
        if (showDeleteConfirmDialog && selectedSongForInfo != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("노래 삭제") },
                text = { Text("'${selectedSongForInfo!!.title}' 노래를 정말 삭제하시겠습니까?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDeleteSong(selectedSongForInfo!!)
                            showDeleteConfirmDialog = false
                            selectedSongForInfo = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("삭제") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("취소") }
                }
            )
        }

        // 4. D-day 설정 다이얼로그 (간격 문제 수정)
        if (showDdayDialog) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = if (dDayTimestamp != 0L) dDayTimestamp else System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDdayDialog = false },
                confirmButton = {
                    Button(onClick = {
                        // [수정] 저장 함수 호출 (화면 갱신 + 내부 저장소 저장)
                        val selectedDate = datePickerState.selectedDateMillis ?: 0L
                        saveDDay(selectedDate, tempDDayTitle)
                        showDdayDialog = false
                    }) { Text("저장") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        // [수정] 삭제 시 데이터 초기화 및 저장
                        saveDDay(0L, "")
                        showDdayDialog = false
                    }) { Text("삭제") }
                }
            ) {
                // [수정] Column의 전체 패딩(.padding(16.dp))을 제거하여 DatePicker 가로 공간 확보
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    // 제목과 입력창에만 별도로 패딩 적용
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    ) {
                        Text("목표 날짜 설정", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tempDDayTitle,
                            onValueChange = { tempDDayTitle = it },
                            label = { Text("목표 내용") },
                            modifier = Modifier.fillMaxWidth(),
                            // [수정] 한 줄 입력 설정 (엔터 키가 줄바꿈 대신 완료 동작을 하도록 함)
                            singleLine = true,
                            // [수정] 키보드 동작 설정: 엔터 키를 'Done(완료)'으로 변경
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            ),
                            // [수정] 완료 키 눌렀을 때 포커스 해제 (키보드 닫힘)
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    defaultKeyboardAction(androidx.compose.ui.text.input.ImeAction.Done)
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // [핵심] DatePicker는 패딩 없이 가로 꽉 차게 렌더링 (간격 좁아짐 해결)
                    DatePicker(
                        state = datePickerState,
                        title = null, // 불필요한 내부 타이틀 제거 (공간 확보)
                        headline = null,
                        showModeToggle = false
                    )
                }
            }
        }

        if (showDeveloperDialog) {
            AlertDialog(
                onDismissRequest = { showDeveloperDialog = false },
                title = { Text("앱 제작자") },
                text = { Text("만든이: 벅만슈\n이메일: qkralstn0820@gmail.com") },
                confirmButton = { Button(onClick = { showDeveloperDialog = false }) { Text("닫기") } }
            )
        }
    }
}

@Composable
fun SongListItem(
    modifier: Modifier = Modifier,
    song: Song,
    dateFormatter: java.text.SimpleDateFormat,
    onClick: () -> Unit,
    onInfoClick: () -> Unit,
    isDraggable: Boolean = false,
    dragModifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    // 드래그 중일 때 적용할 시각적 효과
    val elevation = if (isDragging) 8.dp else 2.dp // 드래그 중일 때만 높게 설정
    val borderColor = if (isDragging) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)
    val containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            // [수정] 드래그 중일 때만 맨 위로 오게 하고, 끝나면 즉시 원래대로(0f) 복구
            .zIndex(if (isDragging) 1f else 0f)
            .clickable(onClick = onClick),
        // [수정] graphicsLayer 없이 Card 자체 elevation만 사용 (그림자 잘림 방지)
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = androidx.compose.foundation.BorderStroke(if (isDragging) 2.dp else 0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isDraggable) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "순서 변경",
                    tint = if (isDragging) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = dragModifier
                        .padding(end = 12.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (song.isFavorite) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.Star, contentDescription = "즐겨찾기", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    }
                    if (song.memo.isNotBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Edit, contentDescription = "메모 있음", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    }
                }

                if (song.artist.isNotBlank()) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                if (song.lastPlayed > 0) {
                    Text(
                        text = "마지막 연습: ${dateFormatter.format(java.util.Date(song.lastPlayed))}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onInfoClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Info, contentDescription = "정보", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SongDetailScreen(
    navController: NavController,
    song: Song,
    onSave: () -> Unit,
    onUpdateInfo: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    // 팝업 상태 변수들
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddPartDialog by remember { mutableStateOf(false) }
    var showChordDialog by remember { mutableStateOf(false) }

    // 상단 메뉴 및 추가 설정 팝업들
    var showMenu by remember { mutableStateOf(false) }
    var showSongSettingDialog by remember { mutableStateOf(false) }
    var showYouTubeDialog by remember { mutableStateOf(false) }

    // 파트 이름 수정 팝업 관련
    var showEditPartNameDialog by remember { mutableStateOf(false) }
    var editingPartName by remember { mutableStateOf("") }

    // [신규] 파트 삭제 확인 팝업 관련 변수
    var showDeletePartDialog by remember { mutableStateOf(false) }
    var partToDeleteIndex by remember { mutableStateOf(-1) }

    // 수정 중인 데이터
    var editTitle by remember { mutableStateOf(song.title) }
    var editArtist by remember { mutableStateOf(song.artist) }

    // 노래 설정 임시 저장 변수
    var tempTuningName by remember { mutableStateOf("") }
    val tempTuningNotes = remember { mutableStateListOf<String>() }
    var tempCapoText by remember { mutableStateOf("") }
    var tempBpmText by remember { mutableStateOf("") }

    var tempYouTubeUrl by remember { mutableStateOf("") }

    // 화면 갱신용
    var refreshKey by remember { mutableStateOf(0) }
    val displayedParts = remember(refreshKey) { song.parts.toList() }

    // 작업 위치
    var currentPartIndex by remember { mutableStateOf(-1) }
    var currentChordIndex by remember { mutableStateOf(-1) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),

                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                title = {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            editTitle = song.title
                            editArtist = song.artist
                            showInfoDialog = true
                        }, contentAlignment = Alignment.CenterStart) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1)

                                // 노래 설정 정보 표시 (튜닝 -> 카포 -> BPM)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 1. Tuning
                                    if (song.tuning != "Standard" && song.tuning.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        InfoBadge(text = song.tuning)
                                    }
                                    // 2. Capo
                                    if (song.capo > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        InfoBadge(text = "Capo ${song.capo}")
                                    }
                                    // 3. BPM
                                    if (song.bpm > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        InfoBadge(text = "${song.bpm} BPM")
                                    }
                                }
                            }
                            if (song.artist.isNotBlank()) Text(song.artist, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                actions = {
                    // 1. 유튜브 아이콘
                    IconButton(onClick = {
                        if (song.youtubeUrl.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(song.youtubeUrl))
                            intent.setPackage("com.google.android.youtube")
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(song.youtubeUrl)))
                                } catch (e2: Exception) { }
                            }
                        } else {
                            tempYouTubeUrl = ""
                            showYouTubeDialog = true
                        }
                    }) {
                        val tintColor = if (song.youtubeUrl.isNotBlank()) Color.Unspecified else Color.Gray
                        Icon(
                            painter = painterResource(id = R.drawable.youtube),
                            contentDescription = "유튜브 연결",
                            tint = tintColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 2. 삭제 버튼
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                    }

                    // 3. 더보기 메뉴
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "더보기")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("노래 설정 (튜닝/카포/BPM)") },
                            onClick = {
                                tempTuningName = song.tuning
                                tempTuningNotes.clear()
                                if (song.tuningNotes.isNotEmpty()) {
                                    tempTuningNotes.addAll(song.tuningNotes)
                                } else {
                                    tempTuningNotes.addAll(listOf("E", "A", "D", "G", "B", "E"))
                                }
                                tempCapoText = if(song.capo == 0) "" else song.capo.toString()
                                tempBpmText = if(song.bpm == 0) "" else song.bpm.toString()

                                showSongSettingDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (song.youtubeUrl.isBlank()) "유튜브 링크 연결" else "유튜브 링크 수정") },
                            onClick = {
                                tempYouTubeUrl = song.youtubeUrl
                                showYouTubeDialog = true
                                showMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showAddPartDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    icon = { Icon(Icons.Default.Add, "파트 추가") },
                    text = { Text("파트 추가") }
                )
                ExtendedFloatingActionButton(
                    onClick = {
                        var targetPartIndex = song.parts.indexOfFirst { it.name == "Unknown" }
                        if (targetPartIndex == -1) {
                            song.parts.add(SongPart(name = "Unknown"))
                            targetPartIndex = song.parts.lastIndex
                            refreshKey++
                        }
                        currentPartIndex = targetPartIndex
                        currentChordIndex = -1
                        showChordDialog = true
                    },
                    icon = { Icon(Icons.Default.Add, "코드 추가") },
                    text = { Text("코드 추가") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(items = displayedParts, key = { _, part -> part.id }) { partIdx, part ->
                if (part.name == "Unknown") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            part.chords.forEachIndexed { chordIdx, chord ->
                                Box(modifier = Modifier.clickable {
                                    currentPartIndex = partIdx
                                    currentChordIndex = chordIdx
                                    showChordDialog = true
                                }) { ChordCard(chord) }
                            }
                        }
                    }
                } else {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = null,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = part.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            currentPartIndex = partIdx
                                            editingPartName = part.name
                                            showEditPartNameDialog = true
                                        }
                                )
                                IconButton(
                                    onClick = {
                                        if (partIdx > 0) {
                                            val prev = song.parts[partIdx - 1]
                                            song.parts[partIdx - 1] = part
                                            song.parts[partIdx] = prev
                                            onSave()
                                            refreshKey++
                                        }
                                    },
                                    modifier = Modifier.size(24.dp),
                                    enabled = partIdx > 0
                                ) { Icon(Icons.Default.KeyboardArrowUp, "위로", tint = if (partIdx > 0) Color.Gray else Color.LightGray) }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (partIdx < song.parts.size - 1) {
                                            val next = song.parts[partIdx + 1]
                                            song.parts[partIdx + 1] = part
                                            song.parts[partIdx] = next
                                            onSave()
                                            refreshKey++
                                        }
                                    },
                                    modifier = Modifier.size(24.dp),
                                    enabled = partIdx < song.parts.size - 1
                                ) { Icon(Icons.Default.KeyboardArrowDown, "아래로", tint = if (partIdx < song.parts.size - 1) Color.Gray else Color.LightGray) }
                                Spacer(modifier = Modifier.width(16.dp))

                                // [수정] 파트 삭제 버튼: 바로 삭제하지 않고 팝업 띄우기
                                IconButton(
                                    onClick = {
                                        partToDeleteIndex = partIdx
                                        showDeletePartDialog = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) { Icon(Icons.Default.Close, "삭제", tint = Color.Red.copy(alpha = 0.6f)) }
                            }
                            HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                part.chords.forEachIndexed { chordIdx, chord ->
                                    Box(modifier = Modifier.clickable {
                                        currentPartIndex = partIdx
                                        currentChordIndex = chordIdx
                                        showChordDialog = true
                                    }) { ChordCard(chord) }
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(100.dp, 160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                                        .clickable {
                                            currentPartIndex = partIdx
                                            currentChordIndex = -1
                                            showChordDialog = true
                                        }
                                ) { Icon(Icons.Default.Add, "코드 추가", tint = Color.LightGray) }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(160.dp)) }
        }
    }

    // === 다이얼로그 모음 ===
    // 1. 노래 제목 수정 다이얼로그 (수정됨)
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            // [수정] 아이콘 제거, "노래 제목 수정" 텍스트 좌측 정렬 (기본값이 좌측 정렬임)
            title = {
                Text(
                    text = "노래 제목 수정",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("노래 제목") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editArtist,
                        onValueChange = { editArtist = it },
                        label = { Text("가수 이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateInfo(editTitle, editArtist)
                    showInfoDialog = false
                }) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("취소") }
            }
        )
    }

    // 노래 설정 (튜닝 상세 설정 포함 - 드롭다운 & 샵/플랫 전환)
    if (showSongSettingDialog) {
        var tuningDropdownExpanded by remember { mutableStateOf(false) }
        val stringDropdownExpanded = remember { mutableStateListOf(false, false, false, false, false, false) }
        var useSharp by remember { mutableStateOf(true) }

        val tuningPresets = mapOf(
            "Standard" to listOf("E", "A", "D", "G", "B", "E"),
            "Half Step Down" to listOf("Eb", "Ab", "Db", "Gb", "Bb", "Eb"),
            "Drop D" to listOf("D", "A", "D", "G", "B", "E"),
            "DADGAG" to listOf("D", "A", "D", "G", "A", "G")
        )

        val notesSharp = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val notesFlat = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")
        val currentNoteOptions = if (useSharp) notesSharp else notesFlat

        fun toggleNoteFormat(note: String): String {
            val sharpIndex = notesSharp.indexOf(note)
            if (sharpIndex != -1) return notesFlat[sharpIndex]
            val flatIndex = notesFlat.indexOf(note)
            if (flatIndex != -1) return notesSharp[flatIndex]
            return note
        }

        AlertDialog(
            onDismissRequest = { showSongSettingDialog = false },
            title = { Text("노래 설정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. 튜닝 이름 설정
                    Box {
                        OutlinedTextField(
                            value = tempTuningName,
                            onValueChange = { tempTuningName = it },
                            label = { Text("튜닝 이름") },
                            placeholder = { Text("Standard, Drop D ...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { tuningDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "튜닝 목록")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = tuningDropdownExpanded,
                            onDismissRequest = { tuningDropdownExpanded = false }
                        ) {
                            tuningPresets.forEach { (name, notes) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        tempTuningName = name
                                        tempTuningNotes.clear()
                                        val convertedNotes = notes.map { note ->
                                            if (useSharp) {
                                                if (notesFlat.contains(note)) notesSharp[notesFlat.indexOf(note)] else note
                                            } else {
                                                if (notesSharp.contains(note)) notesFlat[notesSharp.indexOf(note)] else note
                                            }
                                        }
                                        tempTuningNotes.addAll(convertedNotes)
                                        tuningDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 샵/플랫 전환 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("각 줄의 음을 설정하세요 (6번 → 1번)", fontSize = 12.sp, color = Color.Gray)
                        TextButton(
                            onClick = {
                                useSharp = !useSharp
                                for (i in tempTuningNotes.indices) {
                                    tempTuningNotes[i] = toggleNoteFormat(tempTuningNotes[i])
                                }
                            },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (useSharp) "현재: ♯ (샵)" else "현재: ♭ (플랫)", fontSize = 12.sp)
                        }
                    }

                    // 2. 6줄 튜닝 입력칸 (드롭다운 방식)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 6) {
                            val stringNum = 6 - i
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("${stringNum}번", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)

                                Box {
                                    OutlinedTextField(
                                        value = if(i < tempTuningNotes.size) tempTuningNotes[i] else "",
                                        onValueChange = {},
                                        readOnly = true,
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 2.dp)
                                            .clickable { stringDropdownExpanded[i] = true },
                                        enabled = false,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { stringDropdownExpanded[i] = true }
                                    )
                                    DropdownMenu(
                                        expanded = stringDropdownExpanded[i],
                                        onDismissRequest = { stringDropdownExpanded[i] = false },
                                        modifier = Modifier.heightIn(max = 200.dp)
                                    ) {
                                        currentNoteOptions.forEach { note ->
                                            DropdownMenuItem(
                                                text = { Text(note, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                                                onClick = {
                                                    if (i < tempTuningNotes.size) {
                                                        tempTuningNotes[i] = note

                                                        val currentNotes = tempTuningNotes.toList()
                                                        val currentSharpVer = currentNotes.map { n ->
                                                            if (notesFlat.contains(n)) notesSharp[notesFlat.indexOf(n)] else n
                                                        }
                                                        val isPreset = tuningPresets.any { (name, pNotes) ->
                                                            val presetSharpVer = pNotes.map { n ->
                                                                if (notesFlat.contains(n)) notesSharp[notesFlat.indexOf(n)] else n
                                                            }
                                                            name == tempTuningName && presetSharpVer == currentSharpVer
                                                        }
                                                        if (!isPreset) {
                                                            tempTuningName = "사용자 지정"
                                                        }
                                                    }
                                                    stringDropdownExpanded[i] = false
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = tempCapoText,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 2) tempCapoText = it },
                            label = { Text("카포") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tempBpmText,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 3) tempBpmText = it },
                            label = { Text("BPM") },
                            placeholder = { Text("120") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    song.tuning = tempTuningName
                    song.tuningNotes = tempTuningNotes.toMutableList()
                    song.capo = tempCapoText.toIntOrNull() ?: 0
                    song.bpm = tempBpmText.toIntOrNull() ?: 0
                    onSave()
                    showSongSettingDialog = false
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { showSongSettingDialog = false }) { Text("취소") } }
        )
    }

    if (showYouTubeDialog) {
        AlertDialog(
            onDismissRequest = { showYouTubeDialog = false },
            title = { Text("유튜브 링크 연결") },
            text = {
                Column {
                    Text("연습할 때 참고할 영상 주소를 입력하세요.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempYouTubeUrl,
                        onValueChange = { tempYouTubeUrl = it },
                        label = { Text("URL 붙여넣기") },
                        placeholder = { Text("https://youtu.be/...") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    song.youtubeUrl = tempYouTubeUrl
                    onSave()
                    showYouTubeDialog = false
                }) { Text("저장") }
            },
            dismissButton = { TextButton(onClick = { showYouTubeDialog = false }) { Text("취소") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("노래 삭제") },
            text = { Text("정말 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteConfirm = false; navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("삭제") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") } }
        )
    }

    // [신규] 파트 삭제 확인 다이얼로그
    if (showDeletePartDialog && partToDeleteIndex >= 0) {
        AlertDialog(
            onDismissRequest = { showDeletePartDialog = false },
            title = { Text("파트 삭제") },
            text = {
                // 안전한 접근을 위해 인덱스 체크
                if (partToDeleteIndex < song.parts.size) {
                    Text("정말 '${song.parts[partToDeleteIndex].name}' 파트를 삭제하시겠습니까?\n내부에 저장된 코드들도 모두 삭제됩니다.")
                } else {
                    Text("파트를 삭제하시겠습니까?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (partToDeleteIndex < song.parts.size) {
                            song.parts.removeAt(partToDeleteIndex)
                            onSave()
                            refreshKey++
                        }
                        showDeletePartDialog = false
                        partToDeleteIndex = -1
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePartDialog = false }) { Text("취소") }
            }
        )
    }

    if (showAddPartDialog) {
        var newPartName by remember { mutableStateOf("") }
        val partOptions = listOf("Intro", "Verse", "Chorus", "Bridge", "Interlude", "Outro")

        AlertDialog(
            onDismissRequest = { showAddPartDialog = false },
            title = { Text("파트 추가") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // A. 직접 이름 입력해서 추가
                    Column {
                        Text("직접 입력", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newPartName,
                                onValueChange = { newPartName = it },
                                placeholder = { Text("예: Solo, Breakdown") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newPartName.isNotBlank()) {
                                        song.parts.add(SongPart(name = newPartName))
                                        onSave()
                                        refreshKey++
                                        showAddPartDialog = false
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text("추가")
                            }
                        }
                    }
                    HorizontalDivider()
                    // B. 자주 쓰는 이름 선택
                    Column {
                        Text("빠른 선택", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        partOptions.forEach { name ->
                            TextButton(
                                onClick = {
                                    val count = song.parts.count { it.name.startsWith(name) }
                                    val finalName = if (count == 0) name else "$name ${count + 1}"
                                    song.parts.add(SongPart(name = finalName))
                                    onSave()
                                    refreshKey++
                                    showAddPartDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(12.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Text(name, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.Add, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showChordDialog && currentPartIndex >= 0) {
        val targetPart = song.parts[currentPartIndex]
        val targetChord = if (currentChordIndex >= 0) targetPart.chords[currentChordIndex] else null
        AddChordDialog(
            initialChord = targetChord,
            onDismiss = { showChordDialog = false },
            onChordSelected = { newChord ->
                if (currentChordIndex >= 0) targetPart.chords[currentChordIndex] = newChord
                else targetPart.chords.add(newChord)
                onSave()
                refreshKey++
                showChordDialog = false
            },
            onDelete = if (currentChordIndex >= 0) { {
                targetPart.chords.removeAt(currentChordIndex)
                onSave()
                refreshKey++
                showChordDialog = false
            } } else null
        )
    }

    if (showEditPartNameDialog && currentPartIndex >= 0) {
        AlertDialog(
            onDismissRequest = { showEditPartNameDialog = false },
            title = { Text("파트 이름 수정") },
            text = {
                Column {
                    Text("변경할 이름을 입력하세요.", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingPartName,
                        onValueChange = { editingPartName = it },
                        label = { Text("파트 이름") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (editingPartName.isNotBlank()) {
                        song.parts[currentPartIndex].name = editingPartName
                        onSave()
                        refreshKey++
                    }
                    showEditPartNameDialog = false
                }) { Text("저장") }
            },
            dismissButton = {
                TextButton(onClick = { showEditPartNameDialog = false }) { Text("취소") }
            }
        )
    }
}

// [보조 UI] 노래 설정 정보를 깔끔하게 보여주는 뱃지 컴포저블
@Composable
fun InfoBadge(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

// [수정] 배경을 흰색으로 고정하여 검은색 그림이 잘 보이도록 변경
@Composable
fun ChordCard(chord: GuitarChord) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.size(width = 100.dp, height = 160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val dynamicFontSize = when {
                chord.name.length >= 5 -> 18.sp
                chord.name.length >= 4 -> 22.sp
                else -> 30.sp
            }

            Text(
                text = chord.name,
                fontSize = dynamicFontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            ChordDiagram(
                frets = chord.frets,
                modifier = Modifier.size(60.dp, 80.dp)
            )
        }
    }
}

// [새로 추가] 터치로 운지법을 수정하는 전용 다이얼로그
// [수정] 코드 이름 입력 추가 + 점 크기 확대
// [수정] 시작 프렛 설정 기능 추가 (하이코드 지원)
// [수정] 깔끔한 디자인: 텍스트 겹침 해결, 시작 프렛 숫자 강조
@Composable
fun FretboardEditorDialog(
    initialFrets: List<Int>,    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, List<Int>) -> Unit
) {
    // 초기 프렛 데이터 분석
    val minFret = initialFrets.filter { it > 0 }.minOrNull() ?: 1
    val initialBaseFret = if (minFret > 1) minFret else 1

    // 화면용 프렛 (0~5칸)
    var currentBaseFret by remember { mutableStateOf(initialBaseFret) }
    var visualFrets by remember(initialFrets) {
        mutableStateOf(
            initialFrets.map { f ->
                if (f <= 0) f else (f - (initialBaseFret - 1)).coerceIn(1, 5)
            }.toMutableList()
        )
    }
    var currentName by remember { mutableStateOf(initialName) }

    // [추가] 포커스 매니저
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // [추가] 키보드 컨트롤러 (강제로 키보드 숨기기용)
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("운지법 직접 입력") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. 코드 이름 입력
                OutlinedTextField(
                    value = currentName,
                    onValueChange = { currentName = it },
                    label = { Text("코드 이름") },
                    singleLine = true, // [필수] 엔터를 '완료'로 인식
                    modifier = Modifier.fillMaxWidth(),

                    // [중요] 옵션을 한 번에 정의
                    keyboardOptions = KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),

                    // [중요] 액션을 한 번에 정의
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide() // 키보드 숨기기
                            focusManager.clearFocus()  // 포커스 해제
                        }
                    )
                )


                Spacer(modifier = Modifier.height(24.dp))
                // 2. 지판 영역 (숫자 + 지판)
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // [수정 1] 왼쪽 프렛 번호 위치 조정
                    // 단순 padding 대신 지판의 행 높이(50dp)에 맞춰 정확히 배치
                    if (currentBaseFret > 1) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // 맨 윗줄(개방현/뮤트 영역) 높이인 50dp만큼 빈 공간을 둠
                            Spacer(modifier = Modifier.height(50.dp))

                            // 그 다음 줄(실제 첫 번째 프렛) 높이인 50dp의 중앙에 숫자를 배치
                            Box(
                                contentAlignment = Alignment.CenterEnd,
                                modifier = Modifier.height(50.dp) // 프렛 한 칸 높이
                            ) {
                                Text(
                                    text = "$currentBaseFret",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // 숫자가 없을 때도 공간을 맞춰줌
                        Spacer(modifier = Modifier.width(24.dp))
                    }

                    // [오른쪽] 터치 가능한 기타 지판
                    Box(
                        modifier = Modifier
                            .size(220.dp, 300.dp) // 높이 300dp -> 6칸이므로 1칸당 50dp
                            .background(Color.White)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val stringIdx =
                                        (offset.x / (size.width / 6)).toInt().coerceIn(0, 5)
                                    val stringIndex = stringIdx

                                    val fretHeight = size.height / 6
                                    val fretIdx = (offset.y / fretHeight).toInt().coerceIn(0, 5)

                                    val updated = visualFrets.toMutableList()
                                    if (updated[stringIndex] == fretIdx) {
                                        updated[stringIndex] = -1
                                    } else {
                                        updated[stringIndex] = fretIdx
                                    }
                                    visualFrets = updated
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val colWidth = width / 6f
                            val rowHeight = height / 6f

                            // [수정 2] 개방현/뮤트 영역(맨 윗줄) 배경색 칠하기
                            // 사용자가 헷갈리지 않게 0번 행(헤드 부분)에 회색 배경 추가
                            drawRect(
                                color = Color(0xFFEEEEEE), // 연한 회색
                                topLeft = Offset(0f, 0f),
                                size = Size(width, rowHeight)
                            )

                            // 1) 프렛 바 (가로선)
                            for (i in 0..5) {
                                val y = i * rowHeight
                                // 시작 프렛이 1일 때만 첫 줄(너트)을 굵게 표시
                                val isNut = (i == 1 && currentBaseFret == 1)
                                val stroke = if (isNut) 8f else 3f

                                drawLine(Color.LightGray, Offset(0f, y), Offset(width, y), strokeWidth = stroke)
                            }
                            drawLine(Color.LightGray, Offset(0f, height), Offset(width, height), 3f)

                            // 2) 기타 줄 (세로선)
                            for (i in 0 until 6) {
                                val x = i * colWidth + (colWidth / 2)
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(x, 0f),
                                    end = Offset(x, height),
                                    strokeWidth = 2f
                                )
                            }

                            // 3) 점 찍기
                            // [추가] 근음 찾기 로직: 6번줄(index 0)부터 1번줄(index 5) 순서로 검사하여
                            // 처음으로 유효한(뮤트(-1)가 아닌) 줄의 인덱스를 찾습니다.
                            val rootStringIndex = visualFrets.indexOfFirst { it != -1 }

                            visualFrets.forEachIndexed { index, fretVal ->
                                val x = index * colWidth + (colWidth / 2)

                                // 0.85배 축소된 크기
                                val markerRadius = rowHeight * 0.25f
                                val muteSize = rowHeight * 0.20f

                                // [추가] 현재 줄이 근음 줄인지 확인 (근음이면 빨간색, 아니면 검은색)
                                val markerColor = if (index == rootStringIndex) Color.Red else Color.Black

                                if (fretVal == -1) {
                                    // 뮤트 (X)
                                    val y = rowHeight / 2
                                    val stroke = 6f

                                    drawLine(
                                        color = Color.Black, // 뮤트는 검은색 유지
                                        start = Offset(x - muteSize, y - muteSize),
                                        end = Offset(x + muteSize, y + muteSize),
                                        strokeWidth = stroke
                                    )
                                    drawLine(
                                        color = Color.Black,
                                        start = Offset(x + muteSize, y - muteSize),
                                        end = Offset(x - muteSize, y + muteSize),
                                        strokeWidth = stroke
                                    )
                                } else if (fretVal == 0) {
                                    // 개방현 (O)
                                    val y = rowHeight / 2
                                    drawCircle(
                                        color = markerColor, // 근음이면 빨간색 테두리
                                        radius = markerRadius,
                                        center = Offset(x, y),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
                                    )
                                } else {
                                    // 실제 프렛 (원)
                                    val y = (fretVal * rowHeight) + (rowHeight / 2)
                                    drawCircle(
                                        color = markerColor, // 근음이면 빨간색 원
                                        radius = markerRadius,
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }
                    }
                } // Row 끝

                Spacer(modifier = Modifier.height(16.dp))

                // 3. 하단 프렛 조절 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { if (currentBaseFret > 1) currentBaseFret-- },
                        enabled = currentBaseFret > 1
                    ) {
                        Text("- 프렛 이동")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    OutlinedButton(
                        onClick = { if (currentBaseFret < 12) currentBaseFret++ },
                        enabled = currentBaseFret < 12 // [수정] 12프렛 도달 시 버튼 비활성화
                    ) {
                        Text("+ 프렛 이동")
                }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val realFrets = visualFrets.map { f ->
                    if (f <= 0) f else (f + (currentBaseFret - 1))
                }
                onConfirm(currentName, realFrets)
            }) {
                Text("적용")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}

// [수정] 프렛 번호와 기타 지판의 위치를 완벽하게 맞춘 함수
@Composable
fun ChordDiagram(frets: List<Int>, modifier: Modifier = Modifier) {
    // 1. 유효한 프렛(0과 -1 제외)만 골라냄
    val validFrets = frets.filter { it > 0 }
    // 가장 높은 프렛 번호를 찾음 (없으면 0)
    val maxFret = if (validFrets.isEmpty()) 0 else validFrets.max()
    val minFret = if (validFrets.isEmpty()) 1 else validFrets.min()

    // [핵심 수정 로직]
    // 가장 높은 프렛이 5 이하라면? -> 굳이 이동하지 말고 1프렛부터 보여줌 (B코드, F#m 등)
    // 5를 넘어가는 높은 프렛이 있다면? -> 이동해서 보여줌 (최소 프렛 기준)
    val startFret = if (maxFret <= 5) 1 else minFret

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 왼쪽 숫자 공간 확보 (전체 너비의 15% 정도)
        // 만약 1프렛 시작이면 숫자 공간 없이 꽉 채움
        val sidePadding = if (startFret > 1) width * 0.15f else 0f

        // 실제 기타가 그려질 영역 너비
        val boardWidth = width - sidePadding

        val stringGap = boardWidth / 5  // 줄 간격
        val fretGap = height / 5        // 프렛 간격

        // 0. 프렛 번호 그리기 (startFret > 1 일 때만 왼쪽 공간에 그림)
        if (startFret > 1) {
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 30f // 글자 크기 조절 (필요하면 숫자 변경)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            // 첫 번째 프렛 칸의 정중앙 높이 계산 (fretGap의 절반)
            // 텍스트 베이스라인 보정값 (대략 10f 정도 내려야 수직 중앙이 맞음)
            val textY = (fretGap / 2) + 10f

            drawContext.canvas.nativeCanvas.drawText(
                "$startFret",
                sidePadding - 5f, // 기타 지판 바로 왼쪽
                textY,
                textPaint
            )
        }

        // 근음 줄 찾기
        val rootStringIndex = frets.indexOfFirst { it != -1 }

        // 1. 세로줄 (기타 줄) - sidePadding 만큼 오른쪽으로 밀어서 그림
        for (i in 0..5) {
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(sidePadding + (i * stringGap), 0f),
                end = androidx.compose.ui.geometry.Offset(sidePadding + (i * stringGap), height),
                strokeWidth = 2f
            )
        }

        // 2. 가로줄 (프렛) - sidePadding 부터 시작
        // 기존 0..4에서 0..5로 변경하여 마지막 줄까지 그립니다.
        for (i in 0..5) {
            // startFret이 1일 때만 너트(첫 줄, i=0)를 굵게 처리
            val stroke = if (i == 0 && startFret == 1) 6f else 2f

            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(sidePadding, i * fretGap),
                end = androidx.compose.ui.geometry.Offset(width, i * fretGap), // 끝은 전체 width까지
                strokeWidth = stroke
            )
        }


        // 3. 점 찍기
        frets.forEachIndexed { index, fret ->
            val xPos = sidePadding + (index * stringGap) // 점 위치도 밀어야 함
            val dotColor = if (index == rootStringIndex) Color.Red else Color.Black

            if (fret > 0) {
                // 상대적 프렛 위치 계산
                val relativeFret = fret - startFret + 1

                if (relativeFret in 1..5) {
                    val yPos = (relativeFret * fretGap) - (fretGap / 2)
                    drawCircle(
                        color = dotColor,
                        radius = stringGap / 2.5f,
                        center = androidx.compose.ui.geometry.Offset(xPos, yPos)
                    )
                }
            } else if (fret == 0) {
                // 개방현
                drawCircle(
                    color = dotColor,
                    radius = stringGap / 4f,
                    center = androidx.compose.ui.geometry.Offset(xPos, -15f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
            } else if (fret == -1) {
                // X 표시 (뮤트)
                val xSize = stringGap / 4
                val xCenterY = -15f
                drawLine(Color.Gray,
                    start = androidx.compose.ui.geometry.Offset(xPos - xSize, xCenterY - xSize),
                    end = androidx.compose.ui.geometry.Offset(xPos + xSize, xCenterY + xSize),
                    strokeWidth = 2f
                )
                drawLine(Color.Gray,
                    start = androidx.compose.ui.geometry.Offset(xPos + xSize, xCenterY - xSize),
                    end = androidx.compose.ui.geometry.Offset(xPos - xSize, xCenterY + xSize),
                    strokeWidth = 2f
                )
            }
        }
    }
}

// [수정] 코드 추가/수정/삭제 팝업 창
// [수정] 텍스트 입력 제거 -> 버튼식 그래픽 에디터 적용
// [수정] 지판 에디터에서 '적용' 시 즉시 저장되도록 로직 변경
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChordDialog(
    initialChord: GuitarChord? = null,
    onDismiss: () -> Unit,
    onChordSelected: (GuitarChord) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // === 파싱 로직 (기존 코드 이름을 분해해서 버튼 상태 복구) ===
    val parsed = remember(initialChord) {
        if (initialChord != null) {
            val name = initialChord.name
            val root = name.take(1)
            var rest = name.drop(1)
            val acc = if (rest.startsWith("#")) "#" else if (rest.startsWith("b")) "b" else ""
            if (acc.isNotEmpty()) rest = rest.drop(1)

            val qualityMap = mapOf("Major" to "maj", "Min" to "m", "7" to "7", "sus4" to "sus4")

            val qualityKey = when {
                rest == "Major" -> "maj"
                rest == "m" -> "m"
                rest == "7" -> "7"
                rest == "M7" -> "maj7"
                rest == "sus4" -> "sus4"
                else -> ""
            }
            Triple(root, acc, qualityKey)
        } else {
            Triple("C", "", "")
        }
    }

    var selectedRoot by remember { mutableStateOf(parsed.first) }
    var selectedAccidental by remember { mutableStateOf(parsed.second) }
    var selectedQuality by remember { mutableStateOf(parsed.third) }

    // 이름 조합
    val qualityDisplayMap = mapOf("" to "", "maj" to "Major", "m" to "m", "7" to "7", "maj7" to "M7", "sus4" to "sus4")
    val displayQuality = qualityDisplayMap[selectedQuality] ?: ""
    val previewName = "$selectedRoot$selectedAccidental$displayQuality"

    // 운지법 상태
    var currentFrets by remember {
        mutableStateOf(initialChord?.frets ?: listOf(-1, -1, -1, -1, -1, -1))
    }

    // 이름 변경 시 자동 업데이트 (수정 모드 진입 직후가 아닐 때만)
    val isFirstLoad = remember { mutableStateOf(true) }
    LaunchedEffect(previewName) {
        if (!isFirstLoad.value || initialChord == null) {
            currentFrets = ChordDictionary.getFrets(previewName)
        }
        isFirstLoad.value = false
    }

    // 운지법 에디터 팝업 표시 여부
    var showFretEditor by remember { mutableStateOf(false) }

    // 메인 다이얼로그 (코드 설정)
    if (!showFretEditor) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = if (initialChord == null) "코드 추가" else "코드 수정") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 미리보기 카드
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = previewName,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(24.dp))
                            ChordDiagram(
                                frets = currentFrets,
                                modifier = Modifier
                                    .size(60.dp, 80.dp)
                                    .padding(top = 10.dp)
                            )
                        }
                    }

                    Text("1. 기본 구성", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val roots = listOf("C", "D", "E", "F", "G", "A", "B")
                        items(roots) { root -> SelectableButton(text = root, isSelected = selectedRoot == root) { selectedRoot = root } }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("" to "-", "#" to "#", "b" to "b").forEach { (v, l) ->
                            SelectableButton(text = l, isSelected = selectedAccidental == v) { selectedAccidental = v }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val qualities = listOf("" to "Basic", "maj" to "Major", "m" to "Min", "7" to "7", "maj7" to "M7", "sus4" to "sus4")
                        items(qualities) { (v, l) ->
                            SelectableButton(text = l, isSelected = selectedQuality == v) { selectedQuality = v }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("운지법이 다른가요?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showFretEditor = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("운지법 직접 수정하기")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onChordSelected(GuitarChord(previewName, currentFrets)) }) {
                    Text(if (initialChord == null) "추가" else "수정 완료")
                }
            },
            dismissButton = {
                if (onDelete != null) {
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("삭제")
                    }
                }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        )
    }

    // [핵심 변경] 에디터에서 완료 시 -> 즉시 저장하고 종료
    if (showFretEditor) {
        FretboardEditorDialog(
            initialFrets = currentFrets,
            initialName = previewName, // 현재 버튼으로 조합된 이름을 기본값으로 전달
            onDismiss = { showFretEditor = false },
            onConfirm = { newName, newFrets ->
                // 에디터에서 입력한 이름(newName)과 운지법(newFrets)으로
                // 즉시 GuitarChord 객체를 생성하여 저장(onChordSelected) 호출

                // 만약 사용자가 에디터에서 이름을 안 적었으면 기존 previewName 유지
                val finalName = if (newName.isNotBlank()) newName else previewName

                onChordSelected(GuitarChord(finalName, newFrets))

                // 에디터 닫기 (메인 다이얼로그도 onChordSelected에 의해 닫힘)
                showFretEditor = false
            }
        )
    }
}

// 선택 가능한 버튼 디자인 (선택되면 색깔이 바뀜)
@Composable
fun SelectableButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp) // 버튼 크기 최소화
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}

// [새로 추가] 기타 코드 데이터 클래스
// frets: 6번줄(가장 굵은 줄)부터 1번줄까지 잡아야 할 프렛 번호 (-1: 뮤트/X, 0: 개방현)
data class GuitarChord(    val name: String,
                           val frets: List<Int> = listOf(-1, -1, -1, -1, -1, -1) // 기본값은 아무것도 안 잡음
)

// [수정] 확장된 코드 사전 (하이코드/바레코드 포함)
object ChordDictionary {
    private val chords = mapOf(
        // === Open Chords (기본) ===
        "C" to listOf(-1, 3, 2, 0, 1, 0),
        "D" to listOf(-1, -1, 0, 2, 3, 2),
        "E" to listOf(0, 2, 2, 1, 0, 0),
        "F" to listOf(1, 3, 3, 2, 1, 1), // F는 1프렛 바레
        "G" to listOf(3, 2, 0, 0, 0, 3),
        "A" to listOf(-1, 0, 2, 2, 2, 0),
        "B" to listOf(-1, 2, 4, 4, 4, 2), // B는 2프렛 바레

        // === Minor ===
        "Dm" to listOf(-1, -1, 0, 2, 3, 1),
        "Em" to listOf(0, 2, 2, 0, 0, 0),
        "Am" to listOf(-1, 0, 2, 2, 1, 0),
        "Bm" to listOf(-1, 2, 4, 4, 3, 2), // 2프렛 바레
        "Fm" to listOf(1, 3, 3, 1, 1, 1),
        "F#m" to listOf(2, 4, 4, 2, 2, 2), // 2프렛 바레
        "Gm" to listOf(3, 5, 5, 3, 3, 3),  // 3프렛 바레
        "Cm" to listOf(-1, 3, 5, 5, 4, 3), // 3프렛 바레 (A형)

        // === 7th ===
        "C7" to listOf(-1, 3, 2, 3, 1, 0),
        "D7" to listOf(-1, -1, 0, 2, 1, 2),
        "E7" to listOf(0, 2, 0, 1, 0, 0),
        "G7" to listOf(3, 2, 0, 0, 0, 1),
        "A7" to listOf(-1, 0, 2, 0, 2, 0),
        "B7" to listOf(-1, 2, 1, 2, 0, 2),

        // === Major 7 (Maj7) ===
        "Cmaj7" to listOf(-1, 3, 2, 0, 0, 0),
        "Dmaj7" to listOf(-1, -1, 0, 2, 2, 2),
        "Fmaj7" to listOf(-1, -1, 3, 2, 1, 0), // 약식
        "Gmaj7" to listOf(3, 2, 0, 0, 0, 2),
        "Amaj7" to listOf(-1, 0, 2, 1, 2, 0),

        // === Sharp/Flat Examples ===
        "C#" to listOf(-1, 4, 6, 6, 6, 4), // C# (A형 4프렛)
        "Eb" to listOf(-1, 6, 8, 8, 8, 6), // Eb (A형 6프렛)
        "Bb" to listOf(-1, 1, 3, 3, 3, 1)  // Bb (A형 1프렛)
    )

    fun getFrets(chordName: String): List<Int> {
        // 사전에 없으면 모두 뮤트(-1) 처리
        return chords[chordName] ?: listOf(-1, -1, -1, -1, -1, -1)
    }
}

// [새로 추가] 드래그 앤 드롭 기능을 위한 확장 함수
@Composable
fun Modifier.dragAndDrop(
    index: Int,
    itemWidth: Int, // 아이템 하나의 대략적인 너비 (dp가 아닌 픽셀 단위지만 비율로 계산)
    onSwap: (Int, Int) -> Unit,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit
): Modifier {
    var offsetX by remember { mutableStateOf(0f) }

    return this
        .zIndex(if (isDragging) 1f else 0f) // 드래그 중인 놈을 제일 위로 띄움
        .graphicsLayer {
            translationX = if (isDragging) offsetX else 0f
            scaleX = if (isDragging) 1.1f else 1f // 드래그하면 살짝 커짐
            scaleY = if (isDragging) 1.1f else 1f
            alpha = if (isDragging) 0.9f else 1f
        }
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    onDragStart()
                    offsetX = 0f
                },
                onDragEnd = {
                    onDragEnd()
                    offsetX = 0f
                },
                onDragCancel = {
                    onDragEnd()
                    offsetX = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (isDragging) {
                        offsetX += dragAmount.x

                        // 감도 조절: 일정 거리 이상 움직이면 순서 교체 (아이템 너비의 70% 정도)
                        val threshold = itemWidth * 0.7

                        if (offsetX > threshold) {
                            onSwap(index, index + 1)
                            offsetX -= itemWidth // 교체 후 위치 보정
                        } else if (offsetX < -threshold) {
                            onSwap(index, index - 1)
                            offsetX += itemWidth
                        }
                    }
                }
            )
        }
}

object DataManager {
    private const val PREF_NAME = "guitar_app_prefs"
    private const val KEY_SONGS = "saved_songs"
    private val gson = Gson()

    // 노래 목록 저장하기 (기존과 동일)
    fun saveSongs(context: Context, songs: List<Song>) {
        val jsonString = gson.toJson(songs)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SONGS, jsonString).apply()
    }

    // [수정] 노래 목록 불러오기 (안전장치 추가)
    fun loadSongs(context: Context): MutableList<Song> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_SONGS, null)

        return if (jsonString != null) {
            try {
                // 데이터 불러오기 시도
                val type = object : TypeToken<MutableList<Song>>() {}.type
                gson.fromJson(jsonString, type)
            } catch (e: Exception) {
                // [핵심] 옛날 데이터 형식이라 에러가 나면? -> 초기화!
                e.printStackTrace() // 에러 로그 출력
                prefs.edit().remove(KEY_SONGS).apply() // 잘못된 데이터 삭제
                mutableListOf() // 빈 리스트 반환
            }
        } else {
            mutableListOf()
        }
    }
}

// [데이터 구조 변경] 노래 하나를 나타내는 데이터 클래스
// [수정] 가수 이름(artist) 필드 추가
// [수정] 마지막 연습 날짜 필드 추가
// [수정] capo 변수 추가 (기본값 0)
data class SongPart(val id: String = java.util.UUID.randomUUID().toString(),
                    var name: String,
                    val chords: MutableList<GuitarChord> = mutableListOf()
)

// 2. 노래 정의: 이제 chords 대신 'parts' 리스트를 가짐
data class Song(
    var title: String,
    var artist: String,
    val id: String = java.util.UUID.randomUUID().toString(),
    val parts: MutableList<SongPart> = mutableListOf(SongPart(name = "Intro")),
    val date: Long = System.currentTimeMillis(),
    var capo: Int = 0,
    var lastPlayed: Long = 0,
    // 생성 시간 (기본값: 현재 시간)
    val creationTime: Long = System.currentTimeMillis(),
    // 메모장 (기본값: 빈 칸)
    var memo: String = "",
    // 즐겨찾기 여부 (기본값: 거짓)
    var isFavorite: Boolean = false,
    // [추가] 유튜브 링크 저장용 (기본값: 빈 문자열)
    var youtubeUrl: String = "",
    // [추가] 튜닝 (기본값: Standard)
    var tuning: String = "Standard",
    // [추가] 6줄의 튜닝 음 (6번줄 -> 1번줄 순서: E, A, D, G, B, E)
    var tuningNotes: MutableList<String> = mutableListOf("E", "A", "D", "G", "B", "E"),
    // [추가] BPM (기본값: 0 -> 설정 안 함)
    var bpm: Int = 0
) {
    val chords: MutableList<GuitarChord>
        get() = if (parts.isNotEmpty()) parts[0].chords else mutableListOf()
}

// [Drag & Drop Helper]
// [Drag & Drop Helper]
@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    totalItemCount: Int, // [추가] 실제 데이터(노래) 개수를 받음
    onMove: (Int, Int) -> Unit
): DragDropState {
    val scope = rememberCoroutineScope()
    // totalItemCount가 바뀌면 상태도 갱신되어야 함
    val state = remember(lazyListState, totalItemCount) {
        DragDropState(state = lazyListState, totalDataCount = totalItemCount, onMove = onMove)
    }
    return state
}

class DragDropState(
    val state: LazyListState,
    val totalDataCount: Int, // [변경] 전체 UI 개수가 아닌 데이터 개수 저장
    val onMove: (Int, Int) -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
    var draggingItemOffset by mutableStateOf(0f)
    private var itemHeight = 0

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        // 아이템 높이 추정
        state.layoutInfo.visibleItemsInfo.firstOrNull()?.let {
            itemHeight = it.size
        }
    }

    fun onDragInterrupted() {
        draggingItemIndex = null
        draggingItemOffset = 0f
    }

    fun onDrag(offset: Float) {
        draggingItemOffset += offset

        val currentIndex = draggingItemIndex ?: return
        val threshold = if (itemHeight > 0) itemHeight else 150

        // 아래로 이동
        if (draggingItemOffset > threshold) {
            val nextIndex = currentIndex + 1

            // [핵심 수정] UI 개수가 아니라 전달받은 '노래 데이터 개수'로 제한
            if (nextIndex < totalDataCount) {
                onMove(currentIndex, nextIndex)
                draggingItemIndex = nextIndex
                draggingItemOffset -= threshold
            } else {
                // 더 이상 못 가면 막기
                draggingItemOffset = threshold.toFloat()
            }
        }
        // 위로 이동
        else if (draggingItemOffset < -threshold) {
            val prevIndex = currentIndex - 1

            if (prevIndex >= 0) {
                onMove(currentIndex, prevIndex)
                draggingItemIndex = prevIndex
                draggingItemOffset += threshold
            } else {
                // 더 이상 못 가면 막기
                draggingItemOffset = -threshold.toFloat()
            }
        }
    }
}

fun Modifier.dragContainer(dragDropState: DragDropState): Modifier {
    return this
}

fun Modifier.draggableHandle(dragDropState: DragDropState, index: Int): Modifier {
    return this.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                dragDropState.onDragStart(index)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                dragDropState.onDrag(dragAmount.y)
            },
            onDragEnd = { dragDropState.onDragInterrupted() },
            onDragCancel = { dragDropState.onDragInterrupted() }
        )
    }
}

// 리스트 이동 확장 함수 (안전장치 추가됨)
fun <T> androidx.compose.runtime.snapshots.SnapshotStateList<T>.move(from: Int, to: Int) {
    // [수정] 범위 체크: 인덱스가 리스트 크기를 벗어나면 즉시 종료 (앱 꺼짐 방지)
    if (from !in indices || to !in indices || from == to) return

    val item = this[from]
    this.removeAt(from)
    this.add(to, item)
}

// [추가] 그리드 드래그 앤 드롭을 위한 상태 관리 클래스
class DraggableGridState(
    val onMove: (Int, Int) -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
    var draggingItemOffset by mutableStateOf(Offset.Zero)

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        draggingItemOffset = Offset.Zero
    }

    fun onDrag(offset: Offset) {
        draggingItemOffset += offset
    }

    fun onDragEnd() {
        draggingItemIndex = null
        draggingItemOffset = Offset.Zero
    }
}

@Composable
fun rememberDraggableGridState(onMove: (Int, Int) -> Unit): DraggableGridState {
    return remember { DraggableGridState(onMove) }
}
