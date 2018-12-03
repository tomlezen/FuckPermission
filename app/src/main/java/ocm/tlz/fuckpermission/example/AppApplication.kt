package ocm.tlz.fuckpermission.example

import android.app.Application
import com.tlz.fuckpermission.FuckPermissionProcessor

/**
 * By tomlezen
 * Create at 2018/11/30
 */
class AppApplication: Application(){

    override fun onCreate() {
        super.onCreate()
        FuckPermissionProcessor().install(this)
    }

}