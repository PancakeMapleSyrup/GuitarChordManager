package com.example.guitarchordmanager.di

import com.example.guitarchordmanager.data.repository.FakeSongRepository
import com.example.guitarchordmanager.data.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 앱 전체에서 사용할 수 있게 설치
abstract class RepositoryModule {

    @Binds
    @Singleton // 앱이 켜져있는 동안 딱 1개만 만들어서 공유함 (데이터 유지됨)
    abstract fun bindSongRepository(
        fakeSongRepository: FakeSongRepository
    ): SongRepository
}
