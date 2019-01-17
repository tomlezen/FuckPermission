package ocm.tlz.fuckpermission.example

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tlz.fuckpermission.FuckPermissionProcessor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_request_permission.setOnClickListener {
            FuckPermissionProcessor.request(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .onFuckPermissionBefore {
                    Toast.makeText(this, "权限请求开始", Toast.LENGTH_SHORT).show()
                }
                .onFuckPermissionGranted {
                    Toast.makeText(this, "权限已被允许", Toast.LENGTH_LONG).show()
                }
                .onFuckPermissionRevoked { operate, grantedPermissions, revokedPermissions, canShowRequestPermissionRationale ->
                    Toast.makeText(this, "${revokedPermissions.size}个权限被拒绝", Toast.LENGTH_LONG).show()
                    if (canShowRequestPermissionRationale) {
                        operate.request()
                    }
                }
                .onFuckPermissionAfter {
                    Toast.makeText(this, "权限请求结束", Toast.LENGTH_SHORT).show()
                }

        }

        tv_open_request_permission_activity.setOnClickListener {
            startActivity(Intent(this, PermissionTestActivity::class.java))
        }
    }

}
