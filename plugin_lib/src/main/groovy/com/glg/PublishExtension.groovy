
package com.glg

import org.gradle.api.Project


class PublishExtension {
    String pgyApiKey = null//蒲公英key
    String pgyUploadUrl = "https://www.pgyer.com/apiv2/app/upload/"//蒲公英上传IP
    String downLoadPwd = "12345"//apk下载密码
    String downLoadType = "2"//apk下载方式

    boolean openReinforce=true//是否开启加密
    String reinforce_username = null //360加固用户名
    String reinforce_password = null//360加固密码
    String storeFile = null //签名文件 默认获取android.buildTypes.release.signingConfig.storeFile
    String storePassword = null //签名密码 默认获取android.buildTypes.release.signingConfig.storePassword
    String keyAlias = null //别名 默认获取android.buildTypes.release.signingConfig.keyAlias
    String keyPassword = null ///别名密码 默认获取android.buildTypes.release.signingConfig.keyPassword

    String zipPath//360加固下载路径
    String unzipPath//360加固解压路径
    String jarPath//360加固程序路径
    String  macDownUrl="http://down.360safe.com/360Jiagu/360jiagubao_mac.zip"
    String windowsDownUrl= "http://down.360safe.com/360Jiagu/360jiagubao_windows_64.zip"

    PublishExtension(Project project) {
        zipPath=project.getRootProject().getProjectDir().getAbsolutePath()+"/jiagu/360jiagu.zip"
        unzipPath = project.getRootProject().getProjectDir().getAbsolutePath()+"/jiagu/360jiagubao/"
        jarPath= project.getRootProject().getProjectDir().getAbsolutePath()+'/jiagu/360jiagubao/jiagu/jiagu.jar'
    }

}