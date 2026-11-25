package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DeliveryNoteEntity::class,
        DeliveryArticleEntity::class,
        RestockNoteEntity::class,
        JournalEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DepotDatabase : RoomDatabase() {
    abstract fun depotDao(): DepotDao

    companion object {
        @Volatile
        private var INSTANCE: DepotDatabase? = null

        fun getInstance(context: Context): DepotDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DepotDatabase::class.java,
                    "depot_note_offline.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
