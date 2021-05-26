package com.bonnjalal.sowitmapboxtest.repository

import androidx.annotation.WorkerThread
import com.bonnjalal.sowitmapboxtest.dao.PolygonDao
import com.bonnjalal.sowitmapboxtest.database.PolygonDatabase
import com.bonnjalal.sowitmapboxtest.model.PolygonModel
import kotlinx.coroutines.flow.Flow

class PolygonRepository(private val polygonDao: PolygonDao) {

    /**
     * Get All stored polygons from room database
     */
    val allPolygons: Flow<List<PolygonModel>> = polygonDao.getAllPolygons()

    /**
     * insert new polygon data to roon database
     */
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(polygonModel: PolygonModel) {
        PolygonDatabase.databaseWriteExecutor.execute {
            polygonDao.insert(polygonModel)
        }

    }

    /**
     * delete a polygon item from room database
     */
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(polygonModel: PolygonModel) {
        PolygonDatabase.databaseWriteExecutor.execute {
            polygonDao.delete(polygonModel)
        }

    }
}
