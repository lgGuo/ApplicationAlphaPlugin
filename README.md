# ApplicationAlphaPlugin
# apk内测发布插件
# 一键打包加固上传到蒲公英
## 第一步：在根目录build.gradle添加

```

    buildscript {
    
        repositories {
            maven {url "https://jitpack.io"}
        }

        dependencies {
            classpath 'com.github.lgGuo:ApplicationAlphaPlugin:1.0.2'
        }


    }

```

# 第2步：app的build.gradle添加

```
    apply plugin: 'alpha'

    publishApk{
        pgyApiKey = "蒲公英apikey"
        reinforce_username = "360加固登录用户名" //用户名 必填
        reinforce_password = "360加固登录密码"//密码 必填
        storeFile =  //签名文件路径
        storePassword = //签名密码 
        keyAlias = "tyche" //签名文件别名 
        keyPassword = //签名文件别名密码 

    }
```

