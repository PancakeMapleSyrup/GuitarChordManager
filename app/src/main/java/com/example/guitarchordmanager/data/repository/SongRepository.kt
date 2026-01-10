package com.example.guitarchordmanager.data.repository

import com.example.guitarchordmanager.data.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongsStream(): Flow<List<Song>>
    suspend fun getSongById(id: String): Song?
    suspend fun addSong(song: Song)
    suspend fun updateSong(song: Song)
    suspend fun deleteSong(id: String)
    suspend fun swapSongs(fromId: String, toId: String)
}