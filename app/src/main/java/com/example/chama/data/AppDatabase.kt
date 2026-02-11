package com.example.chama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope, context, provider = { INSTANCE!! }))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Script de População
    private class AppDatabaseCallback(
        private val scope: CoroutineScope,
        private val context: Context,
        private val provider: () -> AppDatabase
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val crismandoDao = provider().crismandoDao()

                    try {
                        val jsonString = context.assets.open("crismandos_to_populate.json")
                            .bufferedReader()
                            .use { it.readText() }

                        val listaNomes: List<String> = Json.decodeFromString(jsonString)

                        listaNomes.forEach { nome ->
                            crismandoDao.inserir(Crismando(nome = nome))
                        }
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}