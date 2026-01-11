package com.example.guitarchordmanager.songdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject

import com.example.guitarchordmanager.data.Song
import com.example.guitarchordmanager.data.repository.SongRepository
import kotlinx.coroutines.launch

// --- 데이터 모델 ---
data class Chord(
    val id: String = UUID.randomUUID().toString(),
    val name: String // 예: "C", "Am7"
)

data class SongPart(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // 예: "Intro", "Chorus"
    val chords: List<Chord> = emptyList()
)

data class SongDetailUiState(
    val song: Song? = null, // 로딩 중일 때 null일 수 있으므로 nullable로 설정
    val parts: List<SongPart> = emptyList(),    // 파트 목록
    val isLoading: Boolean = false  // 로딩 상태
)

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,  // 네비게이션으로 넘어온 값을 받기 위한 도구
    private val repository: SongRepository // 리스트 대신 관리자 고용함
) : ViewModel() {
    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // 화면이s 켜지자마자 songId를 꺼냄 (네비게이션에서 보낸 값)
        val songId: String? = savedStateHandle.get<String>("songId")

        if (songId != null) {
            loadSong(songId)
        }
    }

    private fun loadSong(id: String) {
        viewModelScope.launch {
            val song = repository.getSongById(id)
            _uiState.update { it.copy(song = song) }
        }
    }

    // 파트 추가
    fun addPart(name: String) {
        val currentSong = _uiState.value.song ?: return
        val newPart = SongPart(name = name)

        // 현재 노래의 parts 리스트에 추가
        val updatedSong = currentSong.copy(parts = currentSong.parts + newPart)

        saveSongUpdate(updatedSong)
    }

    // 파트 순서 변경
    fun reorderParts(fromId: String, toId: String) {
        _uiState.update { state ->
            val list = state.parts.toMutableList()
            val fromIndex = list.indexOfFirst { it.id == fromId }
            val toIndex = list.indexOfFirst { it.id == toId }
            if (fromIndex != -1 && toIndex != -1) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            state.copy(parts = list)
        }
    }

    // 코드 추가 (특정 파트에)
    fun addChord(partId: String, chordName: String) {
        val currentSong = _uiState.value.song ?: return

        // 해당 파트를 찾아서 코드 추가
        val updatedParts = currentSong.parts.map { part ->
            if (part.id == partId) {
                part.copy(chords = part.chords + Chord(name = chordName))
            } else part
        }
        val updatedSong = currentSong.copy(parts = updatedParts)

        saveSongUpdate(updatedSong)
    }

    // 코드 순서 변경 (특정 파트 내에서)
    // 순서 변경은 Repository 구현 방식에 따라 다르지만,
    // 일단 FakeRepository에서는 전체 리스트를 갈아끼우는 함수가 필요합니다.
    // (간단하게 구현하려면 updateSong을 여러 번 호출하거나, reorder 함수를 Repository에 추가해야 함)
    fun reorderChords(partId: String, fromChordId: String, toChordId: String) {
        val currentSong = _uiState.value.song ?: return

        // 해당 파트를 찾아서 코드 리스트 순서 변경
        val updatedParts = currentSong.parts.map { part ->
            if (part.id == partId) {
                val chords = part.chords.toMutableList()
                val fromIndex = chords.indexOfFirst { it.id == fromChordId }
                val toIndex = chords.indexOfFirst { it.id == toChordId }

                // 순서 이동 (Swap이 아닌 Move)
                if (fromIndex != -1 && toIndex != -1) {
                    val item = chords.removeAt(fromIndex)
                    chords.add(toIndex, item)
                }

                // 변경된 코드 리스트로 파트 업데이트
                part.copy(chords = chords)
            } else {
                part
            }
        }

        // 파트가 업데이트된 새 노래 객체 생성
        val updatedSong = currentSong.copy(parts = updatedParts)

        // 저장소에 저장 (이래야 화면 나갔다 와도 유지됨)
        saveSongUpdate(updatedSong)
    }

    // 삭제 기능들
    fun deletePart(partId: String) {
        _uiState.update { it.copy(parts = it.parts.filter { part -> part.id != partId }) }
    }

    fun deleteChord(partId: String, chordId: String) {
        _uiState.update { state ->
            val newParts = state.parts.map { part ->
                if (part.id == partId) {
                    part.copy(chords = part.chords.filter { it.id != chordId })
                } else part
            }
            state.copy(parts = newParts)
        }
    }

    private fun saveSongUpdate(updatedSong: Song) {
        viewModelScope.launch {
            // DB에 저장
            repository.updateSong(updatedSong)
            // UI 업데이트
            _uiState.update { it.copy(song = updatedSong) }
        }
    }

    // 노래 메타데이터 수정 함수 추가
    fun updateSongInfo(
        newTitle: String,
        newArtist: String,
        newBpm: String,
        newCapo: String,
        newTuning: String
    ) {
        val currentSong = _uiState.value.song ?: return

        // 새로운 노래 객체 생성
        val updatedSong = currentSong.copy(
            title = newTitle,
            artist = newArtist,
            bpm = newBpm,
            capo = newCapo,
            tuning = newTuning
        )

        viewModelScope.launch {
            // 저장소에 업데이트 요청
            repository.updateSong(updatedSong)
        }

        _uiState.update { it.copy(song = updatedSong) }
    }
    // TODO: 실제로는 여기서 Repository.update(updatedSong)를 불러서 DB에 저장해야 함
}
