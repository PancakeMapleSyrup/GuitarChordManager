package com.example.guitarchordmanager.songlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

import com.example.guitarchordmanager.data.Song
import com.example.guitarchordmanager.data.repository.SongRepository
import kotlinx.coroutines.launch

// UI 상태를 정의하는 Data Class
data class SongListUiState(
    val favoriteSongs: List<Song> = emptyList(), // 즐겨찾기 목록
    val normalSongs: List<Song> = emptyList(),   // 일반 목록
    val inputTitle: String = "",                 // 입력 중인 제목
    val inputArtist: String = ""                 // 입력 중인 가수
)

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val repository: SongRepository  // Repository 주입 받음
) : ViewModel() {

    private val _inputTitle = MutableStateFlow("")
    private val _inputArtist = MutableStateFlow("")


    // ViewModel에서 데이터를 가공해서 UiState로 만듦
    val uiState: StateFlow<SongListUiState> = combine(
        repository.getSongsStream(), // ⭐️ Repository의 데이터를 실시간 구독
        _inputTitle,
        _inputArtist
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
        viewModelScope.launch {
            // Repository에 저장 요청
            repository.addSong(Song(title = title, artist = finalArtist))

            // 입력창 초기화
            _inputTitle.value = ""
            _inputArtist.value = ""
        }
    }

    // 즐겨찾기 토글 (상태 변경)
    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            //  현재 노래 찾기
            val currentSong = repository.getSongById(songId) ?: return@launch
            //  상태 바꿔서 업데이트 요청
            repository.updateSong(currentSong.copy(isFavorite = !currentSong.isFavorite))
        }
    }

    // 노래 정보 수정 기능
    fun updateSong(id: String, newTitle: String, newArtist: String) {
        viewModelScope.launch {
            val currentSong = repository.getSongById(id) ?: return@launch
            repository.updateSong(currentSong.copy(title = newTitle, artist = newArtist))
        }
    }

    // 노래 삭제 기능
    fun deleteSong(id: String) {
        viewModelScope.launch {
            repository.deleteSong(id)
        }
    }

    fun reorderByKeys(fromId: String, toId: String) {
        viewModelScope.launch {
            repository.swapSongs(fromId, toId)
        } /** TODO: 이렇게 하면 앱이 켜져 있는 동안은 순서 변경이 완벽하게 유지됩니다!
                    (앱 껐다 켜도 유지되려면 나중에 Room DB 도입 시 order 필드 관리가 필요합니다) **/
    }
}
