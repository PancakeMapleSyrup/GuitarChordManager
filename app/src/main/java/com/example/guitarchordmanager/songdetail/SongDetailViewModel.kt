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
    val name: String, // 예: "C", "Am7"
    val positions: List<Int> = listOf(-1, -1, -1, -1, -1, -1)
)

data class SongPart(
    val id: String = UUID.randomUUID().toString(),
    val name: String, // 예: "Intro", "Chorus"
    val memo: String = "",
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
        // 화면이 켜지자마자 songId를 꺼냄 (네비게이션에서 보낸 값)
        val songId: String? = savedStateHandle.get<String>("songId")

        if (songId != null) {
            loadSong(songId)
        }
    }

    // 노래를 DB에서 불러오는 함수
    private fun loadSong(id: String) {
        viewModelScope.launch { // ViewModel이 살아있는 동안만 작동
            _uiState.update { it.copy(isLoading = true) } // 로딩 시작
            val song = repository.getSongById(id) // 관리자(Repository)에게 ID로 노래를 찾아오라고 시킴
            _uiState.update { it.copy(song = song, isLoading = false) } // 노래 도착. 로딩을 끝내고 찾아온 노래를 상태에 넣음
        }
    }

    // 파트 추가
    fun addPart(name: String) {
        // 현재 로딩된 노래가 없으면 아무것도 안하고 함수 종료
        val currentSong = _uiState.value.song ?: return
        // 새로운 파트 객체 생성
        val newPart = SongPart(name = name)
        // 현재 노래의 parts 리스트에 새로운 파트 추가
        val updatedSong = currentSong.copy(parts = currentSong.parts + newPart)
        // 변경한 노래를 저장
        saveSongUpdate(updatedSong)
    }

    // 파트 정보(이름, 메모) 수정
    fun updatePartInfo(partId: String, newName: String, newMemo: String) {
        val currentSong = _uiState.value.song ?: return
        // 해당 파트를 찾아 이름과 메모를 변경
        val updatedParts = currentSong.parts.map { part ->
            if (part.id == partId) {
                part.copy(name = newName, memo = newMemo)
            } else {
                part
            }
        }
        // 변경된 파트 리스트로 노래 업데이트
        val updatedSong = currentSong.copy(parts = updatedParts)
        // 저장 및 UI 갱신
        saveSongUpdate(updatedSong)
    }

    // 파트 순서 변경
    fun reorderParts(fromId: String, toId: String) {
        val currentSong = _uiState.value.song ?: return
        // 순서를 바꾸려면 '수정 가능한 리스트(MutableList)'로 변환해야 함
        val list = currentSong.parts.toMutableList()
        // 이동할 녀석(from)과 목적지(to)의 위치(index)를 찾는다
        val fromIndex = list.indexOfFirst { it.id == fromId }
        val toIndex = list.indexOfFirst { it.id == toId }

        // 둘 다 잘 찾았다면 위치를 바꾼다
        if (fromIndex != -1 && toIndex != -1) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)

            // 순서가 바뀐 리스트로 노래 업데이트
            val updatedSong = currentSong.copy(parts = list)
            saveSongUpdate(updatedSong)
        }
    }

    // 코드 추가 (특정 파트에)
    fun addChord(partId: String, chordName: String, positions: List<Int>) {
        val currentSong = _uiState.value.song ?: return

        // 해당 파트를 찾아서 코드 추가
        val updatedParts = currentSong.parts.map { part ->
            if (part.id == partId) {
                part.copy(chords = part.chords + Chord(name = chordName, positions = positions))
            } else part
        }
        val updatedSong = currentSong.copy(parts = updatedParts)

        saveSongUpdate(updatedSong)
    }

    // 파트 내 코드 순서 변경
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
        val currentSong = _uiState.value.song ?: return

        val updatedParts = currentSong.parts.filter { it.id != partId }

        val updatedSong = currentSong.copy(parts = updatedParts)
        saveSongUpdate(updatedSong)
    }

    fun deleteChord(partId: String, chordId: String) {
        val currentSong = _uiState.value.song ?: return

        val updatedParts = currentSong.parts.map { part ->
            if (part.id == partId) {
                part.copy(chords = part.chords.filter { it.id != chordId })
            } else part
        }

        val updatedSong = currentSong.copy(parts = updatedParts)
        saveSongUpdate(updatedSong)
    }

    // 노래 메타데이터 수정 함수 추가
    fun updateSongInfo(
        newTitle: String,
        newArtist: String,
        newBpm: String,
        newCapo: String,
        newTuning: String,
        newYoutubeLink: String
    ) {
        val currentSong = _uiState.value.song ?: return

        // 새로운 노래 객체 생성
        val updatedSong = currentSong.copy(
            title = newTitle,
            artist = newArtist,
            bpm = newBpm,
            capo = newCapo,
            tuning = newTuning,
            youtubeLink = newYoutubeLink
        )
        saveSongUpdate(updatedSong)
    }

    // DB 저장 및 화면 갱신
    private fun saveSongUpdate(updatedSong: Song) {
        viewModelScope.launch {
            // DB에 저장
            repository.updateSong(updatedSong)
            // UI 업데이트
            _uiState.update { it.copy(song = updatedSong) }
        }
    }
}
