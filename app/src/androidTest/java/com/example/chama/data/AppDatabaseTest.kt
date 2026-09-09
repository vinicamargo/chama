package com.example.chama.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun testDaosInstanciacao() {
        // Exercita as chamadas abstratas que o Room implementa
        assertNotNull(db.crismandoDao())
        assertNotNull(db.presencaDao())
        assertNotNull(db.vendedorDao())
        assertNotNull(db.rifaDao())
    }

    @Test
    fun testGetDatabaseSingleton() {
        val instance1 = AppDatabase.getDatabase(context)
        val instance2 = AppDatabase.getDatabase(context)

        assertNotNull(instance1)
        assertSame("Deve manter a mesma referência singleton", instance1, instance2)
    }
}