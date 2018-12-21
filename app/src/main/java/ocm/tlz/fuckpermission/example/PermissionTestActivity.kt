package ocm.tlz.fuckpermission.example

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.tlz.fuckpermission.FuckPermissionCallback
import com.tlz.fuckpermission.FuckPermissionOperate
import com.tlz.fuckpermission.annotations.FuckPermission

/**
 * By tomlezen
 * Create at 2018/11/30
 */
@FuckPermission(permissions = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION])
class PermissionTestActivity : AppCompatActivity(), FuckPermissionCallback {

    override fun onFuckPermissionBefore() {
        Toast.makeText(this, "权限请求开始", Toast.LENGTH_SHORT).show()
    }

    override fun onFuckPermissionGranted(operate: FuckPermissionOperate) {
        Toast.makeText(this, "权限已被允许", Toast.LENGTH_LONG).show()
    }

    override fun onFuckPermissionRevoked(operate: FuckPermissionOperate, grantedPermissions: Array<String>, revokedPermissions: Array<String>, canShowRequestPermissionRationale: Boolean) {
        Toast.makeText(this, "${revokedPermissions.size}个权限被拒绝", Toast.LENGTH_LONG).show()
        if (revokedPermissions.isNotEmpty() && canShowRequestPermissionRationale) {
            operate.requestPermission()
        }
    }

    override fun onFuckPermissionAfter() {
        Toast.makeText(this, "权限请求结束", Toast.LENGTH_SHORT).show()
    }
}