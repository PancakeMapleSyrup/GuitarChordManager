package com.example.guitarchordmanager.songlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

import com.example.guitarchordmanager.data.Song

// UI 상태를 정의하는 Data Class
data class SongListUiState(
    val favoriteSongs: List<Song> = emptyList(), // 즐겨찾기 목록
    val normalSongs: List<Song> = emptyList(),   // 일반 목록
    val inputTitle: String = "",                 // 입력 중인 제목
    val inputArtist: String = ""                 // 입력 중인 가수
)

@HiltViewModel
class SongListViewModel @Inject constructor() : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(
        // 초기 더미 데이터
        listOf(
            Song(title = "Hype Boy", artist = "NewJeans", isFavorite = true),
            Song(title = "Ditto", artist = "NewJeans"),
            Song(title = "Seven", artist = "Jung Kook"),
            Song(title = "I AM", artist = "IVE")
        )
    )

    private val _inputTitle = MutableStateFlow("")
    private val _inputArtist = MutableStateFlow("")


    // ViewModel에서 데이터를 가공해서 UiState로 만듦
    val uiState: StateFlow<SongListUiState> = combine(
        _songs, _inputTitle, _inputArtist
    ) {
        songs, title, artist ->
        SongListUiState(
            favoriteSongs = songs.filter { it.isFavorite },
            normalSongs = songs.filter { !it.isFavorite },
            inputTitle = title,
            inputArtist = artist
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SongListUiState()
    )

    // --- 입력 값 업데이트 함수 ---
    fun updateInputTitle(text: String) {
        _inputTitle.value = text
    }

    fun updateInputArtist(text: String) {
        _inputArtist.value = text
    }

    // 노래 추가
    fun addSong() {
        val title = _inputTitle.value
        val artist = _inputArtist.value

        if (title.isBlank()) return

        val finalArtist = if (artist.isBlank()) "Unknown Artist" else artist
        val newSong = Song(title = title, artist = finalArtist)
        _songs.update { it + newSong }

        _inputTitle.value = ""
        _inputArtist.value = ""
    }

    // 즐겨찾기 토글 (상태 변경)
    fun toggleFavorite(songId: String) {
        _songs.update { list ->
            list.map {
                if (it.id == songId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }

    // 노래 정보 수정 기능
    fun updateSong(id: String, newTitle: String, newArtist: String) {
        _songs.update { list ->
            list.map {
                if (it.id == id ) it.copy(title = newTitle, artist = newArtist) else it
            }
        }
    }

    // 노래 삭제 기능
    fun deleteSong(id: String) {
        _songs.update { list -> list.filter { it.id != id } }
    }

    // 순서 변경 (드래그 앤 드롭)
    // 중요: 즐겨찾기가 아닌 항목들끼리만 순서를 바꾼다.
    fun reorderByKeys(fromId: String, toId: String) {
        _songs.update { list ->
            // 전체 리스트 복사
            val currentList = list.toMutableList()

            // 움직인 아이템과 목표 지점 아이템의 '전체 리스트 기준' 인덱스 찾기
            val fromIndex = currentList.indexOfFirst { it.id == fromId }
            val toIndex = currentList.indexOfFirst { it.id == toId }

            // 둘 다 유효한 인덱스일 때만 교체 진행
            if (fromIndex != -1 && toIndex != -1) {
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
            }

            currentList // 업데이트된 리스트 반환
        }
    }
}
