package com.example.guitarchordmanager.songdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject

import com.example.guitarchordmanager.data.Song

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
    val parts: List<SongPart> = emptyList()
)

@HiltViewModel
class SongDetailViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SongDetailUiState())
    val uiState = _uiState.asStateFlow()

    // 파트 추가
    fun addPart(name: String) {
        val newPart = SongPart(name = name)
        _uiState.update { it.copy(parts = it.parts + newPart) }
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
        _uiState.update { state ->
            val newParts = state.parts.map { part ->
                if (part.id == partId) {
                    part.copy(chords = part.chords + Chord(name = chordName))
                } else part
            }
            state.copy(parts = newParts)
        }
    }

    // 코드 순서 변경 (특정 파트 내에서)
    fun reorderChords(partId: String, fromChordId: String, toChordId: String) {
        _uiState.update { state ->
            val newParts = state.parts.map { part ->
                if (part.id == partId) {
                    val chords = part.chords.toMutableList()
                    val fromIndex = chords.indexOfFirst { it.id == fromChordId }
                    val toIndex = chords.indexOfFirst { it.id == toChordId }

                    if (fromIndex != -1 && toIndex != -1) {
                        val item = chords.removeAt(fromIndex)
                        chords.add(toIndex, item)
                    }
                    part.copy(chords = chords)
                } else part
            }
            state.copy(parts = newParts)
        }
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

    // 노래 메타데이터 수정 함수 추가
    fun updateSongInfo(
        newtitle: String,
        newArtist: String,
        newBpm: String,
        newCapo: String,
        newTuning: String
    ) {

    }
}
