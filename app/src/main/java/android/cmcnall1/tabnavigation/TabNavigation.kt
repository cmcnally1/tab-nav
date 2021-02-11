package android.cmcnall1.tabnavigation

import android.app.Application
import org.koin.android.ext.android.startKoin

class TabNavigation: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(koin))
    }
}