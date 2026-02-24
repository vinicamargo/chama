package com.example.chama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chama.data.dao.CrismandoDao
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Presenca
import com.example.data.PresencaDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Database(entities = [Crismando::class, Presenca::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun crismandoDao(): CrismandoDao
    abstract fun presencaDao(): PresencaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chama_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}