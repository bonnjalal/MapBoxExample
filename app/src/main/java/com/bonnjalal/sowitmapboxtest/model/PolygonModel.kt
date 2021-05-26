package com.bonnjalal.sowitmapboxtest.model

import androidx.room.*
import com.bonnjalal.sowitmapboxtest.tools.DataConverter

@Entity
data class PolygonModel(
        @PrimaryKey(autoGenerate = true) val uid: Int,
        @ColumnInfo(name = "name") val polygonName: String?,
        @ColumnInfo(name = "location") val polygonLocation: String?,
        @ColumnInfo(name = "area") val polygonArea: String?,
        @ColumnInfo(name = "polygonList") val polygonObjectList: PointList?

){
    constructor(polygonName: String?,
                polygonLocation: String?,
                polygonArea: String?,
                polygonObjectList: PointList?) : this(0, polygonName,polygonLocation, polygonArea, polygonObjectList)

    constructor(polygonName: String?,
                polygonLocation: String?,
                polygonArea: String?) : this(0, polygonName,polygonLocation, polygonArea, null)
}