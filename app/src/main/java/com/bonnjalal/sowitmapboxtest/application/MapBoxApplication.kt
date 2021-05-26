package com.bonnjalal.sowitmapboxtest.application

import android.app.Application
import com.bonnjalal.sowitmapboxtest.database.PolygonDatabase
import com.bonnjalal.sowitmapboxtest.repository.PolygonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class MapBoxApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { PolygonDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { PolygonRepository(database.polygonDao()) }
}