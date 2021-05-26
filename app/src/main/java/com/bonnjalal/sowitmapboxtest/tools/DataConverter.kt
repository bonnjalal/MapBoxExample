package com.bonnjalal.sowitmapboxtest.tools

import androidx.room.TypeConverter
import com.bonnjalal.sowitmapboxtest.model.PointList
import com.google.gson.Gson


class DataConverter  {

    /**
     * convert object (PointList) to json string
     */
    @TypeConverter
    fun fromPointList(value: Any?) = Gson().toJson(value)

    /**
     * convert json string to object (PointList)
     */
    @TypeConverter
    fun toPointList(string: String?): PointList? {
        return Gson().fromJson(string, PointList::class.java)
    }
}