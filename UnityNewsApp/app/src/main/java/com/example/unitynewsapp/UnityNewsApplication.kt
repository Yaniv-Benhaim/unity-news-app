package com.example.unitynewsapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class that starts Hilt.
 *
 * Hilt uses this as the root of the dependency graph, so classes like
 * NewsViewModel can ask for use cases instead of manually creating them.
 */
@HiltAndroidApp
class UnityNewsApplication : Application()
