package com.glg

import org.gradle.api.Plugin
import org.gradle.api.Project

class AppAlphaPlugin implements Plugin<Project>{

    static final String EXTENSION_NAME = 'publishApk'

    @Override
    void apply(Project project) {
        // 接收外部参数
        project.extensions.create(EXTENSION_NAME, PublishExtension,project)

        if (project.android.hasProperty("applicationVariants")) {
            project.android.applicationVariants.all { variant ->
                if ("debug" != variant.name) {
                    //防止debug时，手机不能安装app
                    def flag = variant.versionName.replaceAll("\\.", '')
                    def outputPath = project.getRootProject().getProjectDir().getAbsolutePath() + "/app/" + variant.name + "/v" + flag
                    def outputName = project.getRootProject().getName() + "_v${flag}${variant.name}.apk"
                    variant.outputs.all {
                        output ->
                            outputFileName=outputName
                    }

                    variant.getPackageApplication().outputDirectory = new File(outputPath)
                    def upCaseName = variant.name.substring(0, 1).toUpperCase() + variant.name.substring(1)
                    project.tasks.create("uploadApk" + upCaseName,PublishApkTask.class) {
                        setApkOutputPath(outputPath)
                        setApkOutputName(outputName)
                        dependsOn("assemble" + upCaseName)
                        setGroup("publish")
                    }


                }

            }


        }
    }
}