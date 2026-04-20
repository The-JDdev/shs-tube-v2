package com.shslab.shstube.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DownloadEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            val cached = INSTANCE
            if (cached != null) return cached
            return synchronized(this) {
                val again = INSTANCE
                if (again != null) {
                    again
                } else {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "shstube.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = db
                    db
                }
            }
        }
    }
}
