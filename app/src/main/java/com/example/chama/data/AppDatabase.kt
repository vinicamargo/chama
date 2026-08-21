package com.example.chama.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chama.BuildConfig
import com.example.chama.data.dao.CrismandoDao
import com.example.chama.data.entity.Crismando
import com.example.chama.data.entity.Presenca
import com.example.chama.data.dao.PresencaDao
import com.example.chama.data.dao.RifaDao
import com.example.chama.data.dao.VendedorDao
import com.example.chama.data.entity.Rifa
import com.example.chama.data.entity.Vendedor
import com.example.chama.utils.TipoVendedor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = if (BuildConfig.FLAVOR == "dev") "chama_dev.db" else "chama.db"

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                ).fallbackToDestructiveMigration()

                if (BuildConfig.FLAVOR == "dev") {
                    builder.addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            scope.launch(Dispatchers.IO) {
                                INSTANCE?.let { database ->
                                    popularBancoDev(context, database)
                                }
                            }
                        }
                    })
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun popularBancoDev(context: Context, database: AppDatabase) {
            try {
                context.assets.open("mock_dados.csv").use { inputStream ->
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
                    val reader = inputStream.bufferedReader()
                    val linhas = reader.readLines()
                    if (linhas.isEmpty()) return

                    val datasBruta = linhas[0].split(",", limit = 2)[1].split(",")
                    val datasLista = datasBruta.map { dataString ->
                        LocalDate.parse(dataString.trim(), formatter).toString()
                    }

                    val crismandoDao = database.crismandoDao()
                    val vendedorDao = database.vendedorDao()
                    val presencaDao = database.presencaDao()

                    linhas.drop(1).filter { it.isNotBlank() }.forEach { linha ->
                        val colunas = linha.split(",", limit = 2)
                        val nome = colunas[0].trim()
                        val presencasBruta = colunas.getOrNull(1)?.split(",") ?: emptyList()
                        val presencasLista = presencasBruta.map { it.trim() == "O" }

                        val crismando = Crismando(nome = nome)
                        val crismandoId = crismandoDao.inserir(crismando)

                        vendedorDao.inserirVendedor(
                            Vendedor(vendedorId = crismandoId, tipo = TipoVendedor.CRISMANDO)
                        )

                        val presencas = mutableListOf<Presenca>()
                        for (i in datasLista.indices) {
                            val presente = presencasLista.getOrElse(i) { false }
                            presencas.add(
                                Presenca(
                                    crismandoId = crismandoId,
                                    data = datasLista[i],
                                    estaPresente = presente
                                )
                            )
                        }

                        presencaDao.gerarListaPresenca(presencas)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

