package com.example.chordzip.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// @Database: 이 클래스가 룸 데이터베이스의 본체임을 선언한다.
// entities: 이 DB가 관리할 테이블(Song)을 등록한다.
// version: DB 구조가 바뀌면 버전을 올려야 한다.
@Database(entities = [Song::class, DDayInfo::class], version = 2)
// @TypeConverters: 아까 만든 변환기를 등록
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAO를 꺼내 쓸 수 있는 추상 함수를 만든다.
    abstract fun songDao(): SongDao
    abstract fun dDayDao(): DDayDao
}