package com.example.chordzip.data

import androidx.room.TypeConverter
import com.example.chordzip.songdetail.SongPart
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

// Room 데이터베이스가 'List<SongPart>' 같은 복잡한 데이터를 저장할 수 있도록 돕는 클래스
class Converters {
    // 저장할 때 List<SongPart> -> String (JSON 문자열)으로 변환
    @TypeConverter
    fun fromSongPartList(value: List<SongPart>): String {
        val gson = Gson()   // Gson 객체 생성
        return gson.toJson(value)   // 리스트를 JSON 문자열로 변환해서 반환
    }

    @TypeConverter
    fun toSongPartList(value: String): List<SongPart> {
        val gson = Gson() // Gson 객체 생성
        // 어떤 타입으로 바꿀지 명시 (List<SongPart> 타입을 정의)
        val type = object : TypeToken<List<SongPart>>() {}.type
        return gson.fromJson(value, type)   // 문자열을 다시 리스트로 변환해서 반환
    }

    // 날짜(LocalDate) -> 문자열(String)으로 변환
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    // 문자열(String) -> 날짜(LocalDate)로 변환
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }
}