package com.klaudia.mynotes.di

import android.content.Context
import androidx.room.Room
import com.klaudia.mynotes.data.db.entity.ImagesDatabase
import com.klaudia.mynotes.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    //@Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ImagesDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ImagesDatabase::class.java,
            name = Constants.IMAGES_DATABASE
        ).build()
    }


    // @Singleton
    @Provides
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUploadDao()


    @Provides
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDeleteDao()
}