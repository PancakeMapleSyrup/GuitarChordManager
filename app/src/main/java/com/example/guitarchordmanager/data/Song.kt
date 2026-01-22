package com.example.guitarchordmanager.data

import com.example.guitarchordmanager.songdetail.SongPart
import java.util.UUID

// 이 클래스는 앱 전체에서 공용으로 사용
data class Song(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "Unknown Artist",
    val isFavorite: Boolean = false,
    val bpm: String = "-",
    val capo: String = "None",
    val tuning: String = "Standard",
    val youtubeLink: String = "",
    val parts: List<SongPart> = emptyList()
)