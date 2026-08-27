package com.example.chama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chama.BuildConfig
import com.example.chama.data.dao.CrismandoDao
import com.example.chama.data.dao.PresencaDao
import com.example.chama.data.dao.RifaDao
import com.example.chama.data.dao.VendedorDao
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Presenca
import com.example.chama.data.entity.Rifa
import com.example.chama.data.entity.Vendedor

@Database(
    entities = [Crismando::class, Presenca::class, Vendedor::class, Rifa::class],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun crismandoDao(): CrismandoDao
    abstract fun presencaDao(): PresencaDao
    abstract fun vendedorDao(): VendedorDao
    abstract fun rifaDao(): RifaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = if (BuildConfig.FLAVOR == "dev") "chama_dev.db" else "chama.db"

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}