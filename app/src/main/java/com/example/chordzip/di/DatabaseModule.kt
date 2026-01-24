package com.example.chordzip.di

import android.content.Context
import androidx.room.Room
import com.example.chordzip.data.AppDatabase
import com.example.chordzip.data.DDayDao
import com.example.chordzip.data.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module // Hilt 모듈임을 선언
@InstallIn(SingletonComponent::class) // 이 모듈은 앱이 실행되는 동안 계속 살아있습니다.
object DatabaseModule {

    @Provides // 이 함수는 AppDatabase 객체를 제공합니다.
    @Singleton // 앱 전체에서 딱 하나만 만들어서 공유합니다 (싱글톤).
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "guitar_chord_db" // 폰에 저장될 실제 파일 이름
        ).build() // 데이터베이스 빌드
    }

    @Provides // 이 함수는 SongDao 객체를 제공합니다.
    fun provideSongDao(database: AppDatabase): SongDao {
        return database.songDao() // DB에서 DAO를 꺼내줍니다.
    }

    // DDayDao 제공 함수
    @Provides
    fun provideDDayDao(database: AppDatabase): DDayDao {
        return database.dDayDao()
    }
}