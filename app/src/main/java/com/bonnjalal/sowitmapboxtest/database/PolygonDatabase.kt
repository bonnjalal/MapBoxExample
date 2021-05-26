package com.bonnjalal.sowitmapboxtest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bonnjalal.sowitmapboxtest.dao.PolygonDao
import com.bonnjalal.sowitmapboxtest.model.PolygonModel
import com.bonnjalal.sowitmapboxtest.tools.DataConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Database(entities = arrayOf(PolygonModel::class), version = 3, exportSchema = false)
@TypeConverters(DataConverter::class)
abstract class PolygonDatabase : RoomDatabase() {
    abstract fun polygonDao(): PolygonDao



    companion object {
        private val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)
        // Singleton prevents multiple instances of database opening at the
        // same time.

        @Volatile
        private var INSTANCE: PolygonDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PolygonDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        PolygonDatabase::class.java,
                        "polygonData"
                ).addCallback(WordDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class WordDatabaseCallback(
            private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.polygonDao())
                }
            }
        }

        suspend fun populateDatabase(polygonDao: PolygonDao) {

            // Add polygons data.
            val polygon = PolygonModel("Choose a polygon item", "location", "area", null)
            polygonDao.insert(polygon)

        }
    }

}