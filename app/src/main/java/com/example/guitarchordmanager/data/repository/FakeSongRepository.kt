package com.example.guitarchordmanager.data.repository

import com.example.guitarchordmanager.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSongRepository @Inject constructor() : SongRepository {
    // ViewModel에 있던 가짜 데이터를 여기로 이사시킴
    private val songs = MutableStateFlow<List<Song>>(
        // 초기 더미 데이터
        listOf(
            Song(title = "Hype Boy", artist = "NewJeans", isFavorite = true),
            Song(title = "Ditto", artist = "NewJeans"),
            Song(title = "Seven", artist = "Jung Kook"),
            Song(title = "I AM", artist = "IVE")
        )
    )

    override fun getSongsStream(): Flow<List<Song>> = songs

    override suspend fun getSongById(id: String): Song? {
        return songs.value.find { it.id == id }
    }

    override suspend fun addSong(song: Song) {
        songs.update { it + song }
    }

    override suspend fun updateSong(song: Song) {
        songs.update { list ->
            list.map { if (it.id == song.id) song else it }
        }
    }

    override suspend fun deleteSong(id: String) {
        songs.update { list -> list.filter { it.id != id } }
    }

    override suspend fun swapSongs(fromId: String, toId: String) {
        // 현재 리스트 가져오기
        val currentList = songs.value.toMutableList()

        // 인덱스 찾기
        val fromIndex = currentList.indexOfFirst { it.id == fromId }
        val toIndex = currentList.indexOfFirst { it.id == toId }

        // 둘 다 유효한 인덱스라면 교체 (Swap)가 아니라 이동 (Move)
        // 드래그 앤 드롭은 'A와 B를 맞바꾸는 것'이 아니라 'A를 뽑아서 B 자리로 끼워넣는 것'입니다.
        if (fromIndex != -1 && toIndex != -1) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)

            // 변경된 리스트를 다시 발행 -> UI 자동 업데이트
            songs.value = currentList
        }
    }
}