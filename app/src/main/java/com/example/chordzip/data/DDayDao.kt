package com.example.chordzip.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DDayDao {
    // [조회] 저장된 D-day 정보를 가져온다.
    // 데이터가 없으면 null을 반환할 수 있음
    // Flow를 사용하여 데이터가 변경되면 즉시 UI에 알려준다.
    @Query("SELECT * FROM dday_info WHERE id = 0")
    fun getDDayInfo(): Flow<DDayInfo?>

    // [저장] 정보를 저장한다.
    // REPLACE: ID가 0인 데이터가 이미 있으면 덮어쓴다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDDayInfo(info: DDayInfo)
}