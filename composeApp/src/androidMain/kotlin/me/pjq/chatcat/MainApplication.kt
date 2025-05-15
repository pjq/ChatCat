package me.pjq.chatcat

import android.content.Context

/**
 * Context provider for Android
 * Provides access to the application context
 */
object AndroidContextProvider {
    private var applicationContext: Context? = null
    
    fun init(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }
    }
    
    fun getApplicationContext(): Context {
        return applicationContext ?: throw IllegalStateException(
            "Context not initialized. Call AndroidContextProvider.init(context) in MainActivity.onCreate()"
        )
    }
}
