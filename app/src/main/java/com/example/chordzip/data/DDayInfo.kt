package com.example.chordzip.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "dday_info")
data class DDayInfo(
    // 항상 1개의 데이터만 유지할 것이므로 ID를 0으로 고정
    // 이렇게 하면 데이터를 계속 추가해도 덮어쓰기가 되어 1개만 남는다.
    @PrimaryKey val id: Int = 0,

    val targetDate: LocalDate,   // 목표 날짜
    val goal: String            // 목표 내용
)