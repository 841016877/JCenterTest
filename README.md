## 优势
方便集成，统一管理library
gradle一句话依赖

## 本质
对jar/aar的引用


## JCenter介绍
* 定义：远程Android library文件服务器
* 作用：存储代码，提供接口供外部调用
>帮助理解：JCenter = 仓库，Bintray = 送货卡车，代码库 = 货物

本文主要通过bintray-release插件快速上传代码到bintray然后提交jcenter
主要过程：


## 步骤1：注册Bintray
Bintray https://bintray.com/signup/oss
* 注意这里使用的是oos个人账户地址，官网点击注册只能注册企业账号，有失效时间
* 注册可以使用gitHub账号，但是如果gitHub账号邮箱使用的国内邮箱的话，无法注册成功（点击注册按钮没有反应，看js日志显示接口访问400），这里我使用gmail邮箱

## 步骤2：在Bintray上建立仓库

### Package
创建完仓库之后就可以上传package
##### 什么是Package
你要发布一个库，必须要配置groupId、artifactId。在bintray，必须创建package，才能发布库，一个package对应一个库，这里的package并不是包名，而是与artifactId对应
例如：
`compile 'com.github.bumptech.glide:glide:4.0.0'` `com.github.bumptech.glide`就是groupId, `glide`就是artifactId，4.0.0就是该包的version
### Add New Package
可以在Bintray中的仓库里面直接创建package，另外在项目中直接push也可以直接推送上来，即便原来没有这个package，本篇文章先创建项目在publish

## 步骤3：在代码项目中创建Module，编写自己的库
## 步骤4：将项目上传到Github（没有太大作用）
-- 
## 开始上传Bintray配置
### 步骤1：配置bintray-release插件
[bintray-release](https://github.com/novoda/bintray-release) 是一个三方插件，方便配置和上传package到Bintray
包括在`Moudle` & 项目 的`Gradle`文件配置
#### 配置Moudle Gradle

```
<-- 配置代码 -->
// 配置1
apply plugin: 'com.novoda.bintray-release' // 添加bintray插件

// 配置2
publish {
    userOrg = 'carson-ho' // Binary用户名
    repoName = 'CircileView' // Binary上的刚才新建的仓库名（Repository）
    groupId = 'scut.carson_ho' // 依赖名compile 'x:y:z'中的包名x
    artifactId = 'CircileView' // 依赖名compile 'x:y:z'中的项目名y
    publishVersion = '1.0.0' // 依赖名compile 'x:y:z'中的版本号z
    desc = 'a CircileView' // 对该开源组件的说明
    website = 'https://github.com/Carson-Ho/DIY_View' // VCS地址，即填写项目上传的Github地址
}

// 特别注意：保持你的library module的名字同artifactId一样
// 1. 背景
    // 在Bintray上你的项目的maven-metadata.xml文件的路径=gruopId+"/"+module名称
    // 如你的groupId=scut.carson_ho，artifactId是CircileView，但module名称是circlelibrary
    // 此时，项目文件在scut.carson_ho.CircileView目录下的，但maven-metadata.xml文件却是在scut.carson_ho.circlelibrary目录下的。
// 2. 冲突：若你有多个项目groupId一样 & artifactId不一样，但module名称都是library的话，maven-metadata.xml文件的地址可能会一样，即都是：gruopId+"/"+module名称，那么就可能产生冲突
// 3. 解决方案：保持module名称和artifactId一致
```

#### 配置Project Gradle

```
<-- 配置代码 -->
// 配置1
classpath 'com.novoda:bintray-release:0.9.1'

// 配置2
allprojects {
    tasks.withType(Javadoc) {
        options.addStringOption('Xdoclint:none', '-quiet')
        options.addStringOption('encoding', 'UTF-8')
    }
}
allprojects {
    tasks.withType(Javadoc) {
        options{
            encoding "UTF-8"
            charSet 'UTF-8'
            links "http://docs.oracle.com/javase/7/docs/api"
        }
    }
}
```

### 步骤2：执行上传命令
> 在Android Studio的Terminal中输入一下命令
```
// 每行命令均用空格隔开，此处是为了展示才会分行
<-- Windows版本 -->
gradlew.bat clean build bintrayUpload 
-PbintrayUser=zhangyonghui // Binary用户名
-PbintrayKey=************* // Binary上的API key，具体获取见下说明
-PdryRun=false

<-- Mac版本 -->
./gradlew clean build bintrayUpload 
-PbintrayUser=zhangyonghui
-PbintrayKey=***************************** 
-PdryRun=false

// 最终示例：
./gradlew clean build bintrayUpload -PbintrayUser=zhangyonghui -PbintrayKey= **************************** -PdryRun=false
```

#### 上传成功

### 步骤3：提交审核到JCenter
正常在PackageInfo页面会有Add to JCenter按钮，用于提交到JCenter审核，此处已经审核通过，不再显示

审核一般几个小时，通过后会在站内私信提示


## 更新版本
当需要更新Android library版本的时候，需要以下2步
### 步骤1：在改Module下的Gradle重新配置包的版本号，其他什么都不用修改

### 步骤2：重新执行上传命令
`./gradlew clean build bintrayUpload -PbintrayUser=zhangyonghui -PbintrayKey= **************************** -PdryRun=false`

## 集成
在Bintray的PackageInfo页面可以看到该package的各种集成方式，这里选择gradle方式集成


至此：已经集成完毕

## 问题：
* `Unable to load class 'org.gradle.api.internal.component.Usage'`
>这是由于插件兼容问题导致的错误（和Android tools gradle版本有关系），如果有com.novoda:bintray-release请把版本修改为0.9.1（最新版）

* `Lint found errors in the project`
> Lint检查默认开启，Lint会检查项目中的语法错误，如果没有通过会停止build，需要在Module的build.gradle添加
```
android {
    lintOptions {
        abortOnError false
    }
}
```


参考：
    https://www.jianshu.com/p/b1fcbbab0bbd
    https://www.jianshu.com/p/9f81d5b5a451
    https://blog.csdn.net/qq_27818541/article/details/78590439
    问题：
    http://blog.qiji.tech/archives/9905
    http://www.jcodecraeer.com/a/anzhuokaifa/Android_Studio/2015/0515/2873.html
    [issues](https://github.com/novoda/bintray-release/issues)