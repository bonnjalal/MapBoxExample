package com.bonnjalal.sowitmapboxtest.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bonnjalal.sowitmapboxtest.model.PolygonModel
import kotlinx.coroutines.flow.Flow

@Dao
interface PolygonDao {

    @Query("SELECT * FROM PolygonModel")
    fun getAllPolygons(): Flow<List<PolygonModel>>

    @Insert
    fun insert(vararg polygonModel: PolygonModel)

    @Delete
    fun delete(polygonModel: PolygonModel)
}