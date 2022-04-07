
package com.glg

import groovy.json.JsonSlurper
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class PublishApkTask extends DefaultTask {


    private String apkOutputPath
    private String apkOutputName

    void setApkOutputPath(String apkOutputPath) {
        this.apkOutputPath = apkOutputPath
    }

    void setApkOutputName(String apkOutputName) {
        this.apkOutputName = apkOutputName
    }


/**
 * 上传文件到蒲公英
 * @param apkPath apk本地路径
 * @return 上传结果
 */
    private void uploadPGY(PublishExtension publishExtension,String apkDir) {


        if (!new File(apkDir).exists()){
            println "apk不存在"
            return
        }

        def fileDir = new File(apkDir)
        def apkFile
        if(publishExtension.openReinforce){
            for (File f:fileDir.listFiles()){
                if (f.getName().contains("jiagu")){
                    apkFile=f
                    break
                }
        }
        }else{

            apkFile=fileDir
        }
        

        def stdout = new ByteArrayOutputStream()
        try {
            project.exec {
                executable = 'curl'
                args = ['-F', "file=@${apkFile}", '-F', "_api_key=${publishExtension.pgyApiKey}", '-F', "buildInstallType=${publishExtension.downLoadType}", '-F', "buildPassword=${publishExtension.downLoadPwd}", publishExtension.pgyUploadUrl]
                standardOutput = stdout
            }
            String output = stdout.toString()
            def parsedJson = new JsonSlurper().parseText(output)
            def qrcode = parsedJson.data.buildQRCodeURL
            println "下载链接：" + parsedJson.data.buildQRCodeURL
            println "版本号：" + parsedJson.data.buildVersion
            def cmd = Os.isFamily(Os.FAMILY_WINDOWS) ? "cmd /c start" : "open"
            Runtime.getRuntime().exec("$cmd $qrcode")

        } catch (Exception e) {
            e.printStackTrace()
            println "上传错误：" + e.getMessage()
        }finally{
            stdout.close()
        }


    }


    /**
     * 检查360加固环境
     */
    private void checkJiaGuEnvironment(PublishExtension publishExtension) {
        //如果 Zip 文件不存在就进行下载
        File zipFile =new File(publishExtension.zipPath)
        if (!zipFile.exists()) {
            println "==================本地没有360加固宝，准备下载============"
            if (!zipFile.parentFile.exists()) {
                zipFile.parentFile.mkdirs()
            }
            println "==============开始下载360加固宝============="
            project.exec {
                executable = 'curl'
                args = ['-o', publishExtension.zipPath, Os.isFamily(Os.FAMILY_WINDOWS) ? publishExtension.windowsDownUrl : publishExtension.macDownUrl]
            }
            println "==================下载360加固宝成功============"
        }

        File unZipFile = new File(publishExtension.unzipPath)
        File zipFile2 = new File(publishExtension.zipPath)
        if (!unZipFile.exists() && zipFile2.exists()) {
            //解压 Zip 文件
            println "==================开始解压360加固宝============"
            ant.unzip(src: publishExtension.zipPath, dest: publishExtension.unzipPath, encoding: "GBK")
            //将解压后的文件开启读写权限，防止执行 Jar 文件没有权限执行
            project.exec {
                executable = 'chmod'
                args = ['-R', '777', publishExtension.unzipPath]
            }
            println "==================解压360加固宝成功============"
        }


    }
/**
 * 加固
 * @param apkPath 要加固的文件路径
 * @param outputPath 输出路径
 */
    private void reinforce(PublishExtension publishExtension,String apkPath, String outputPath) {

        checkJiaGuEnvironment(publishExtension)

        File file = new File(apkPath)
        if (!file.exists()) {
            throw new Exception("目标加固文件不存在")
        }

        File outfile = new File(outputPath)
        if (outfile.exists()) {
            outfile.delete()
        }
        println "==================开始360加固============"

        //首次使用必须先登录
        project.exec {
            executable = 'java'
            args = ['-jar', publishExtension.jarPath, '-login', publishExtension.reinforce_username, publishExtension.reinforce_password]
        }
        //升级到最新版本
        project.exec {
            executable = 'java'
            args = ['-jar', publishExtension.jarPath, '-update']
        }
        //显示当前版本号
        project.exec {
            executable = 'java'
            args = ['-jar', publishExtension.jarPath, '-version']
        }

        //导入签名信息
        project.exec {
            executable = 'java'
            args = ['-jar', publishExtension.jarPath, '-importsign',
                    publishExtension.storeFile,
                    publishExtension.storePassword,
                    publishExtension.keyAlias,
                    publishExtension.keyPassword]
        }

        //配置加固可选项
        project.exec {
            executable = 'java'
            args = ['-jar', publishExtension.jarPath, '-config', "-"]
        }

        //加固命令
        def jiaGuArgs
        jiaGuArgs = ['-jar', publishExtension.jarPath, '-jiagu',
                     apkPath,
                     outputPath,
                     '-autosign'
        ]

        project.exec {
            executable = 'java'
            args = jiaGuArgs
        }
        println "加固的文件路径：${apkPath}"
        println "加固后的文件路径：${outputPath}"

        uploadPGY(publishExtension,outputPath)
    }

    /**
     * 插件开始
     */
    @TaskAction
    void start() {
        def publishExtension = project.extensions.findByName(AppAlphaPlugin.EXTENSION_NAME) as PublishExtension
        if (publishExtension.openReinforce&&new File(apkOutputPath).exists()) {
            reinforce(publishExtension,apkOutputPath+"/"+apkOutputName,apkOutputPath)
        }else {
            uploadPGY(publishExtension,apkOutputPath+"/"+apkOutputName)
        }

    }
}