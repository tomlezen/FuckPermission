# 一个简单的权限处理库，只能在Activity上使用
权限请求在Activity的**resume**生命周期执行，只会执行一次.
```
implementation 'com.github.tomlezen:FuckPermission:0.0.1'
```
### 1. 在Application里初始化注册
```
FuckPermissionProcessor().install(this)
```
### 2. 在需要请求权限的Activity添加注解
```
@FuckPermission(permissions = [Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION])
class xxxxxActivity : AppCompatActivity() 
```
### 3. 添加权限请求回调，实现**FuckPermissionCallback**接口
```
......
class xxxxxActivity : AppCompatActivity(), FuckPermissionCallback {

    /**
     * 权限请求之前回调.
     */
    override fun onFuckPermissionBefore() { }

    /**
     * 权限被授予.
     */
    override fun onFuckPermissionGranted(operate: FuckPermissionOperate) { }

    /**
     * 权限请求被拒绝.
     * @param action FuckPermissionAction 当有权限被拒绝时，可以调用**action.requestPermission()**再次请求权限
     * @param grantedPermissions Array<String> 被授予的权限
     * @param revokedPermissions Array<String> 被拒绝的权限
     * @param canShowRequestPermissionRationale Boolean 是否还可以显示权限申请框.
     */
    override fun onFuckPermissionRevoked(action: FuckPermissionOperate, grantedPermissions: Array<String>, revokedPermissions: Array<String>, canShowRequestPermissionRationale: Boolean) {   
      // 如果有权限被拒绝，但还可以提示，可以选择再次进行权限申请
      if (revokedPermissions.isNotEmpty() && canShowRequestPermissionRationale) {
         action.requestPermission()
      }
    }
    /**
     * 权限请求之后.
     */
    override fun onFuckPermissionAfter() { }
}
```

