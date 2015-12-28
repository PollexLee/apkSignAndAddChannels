# apkSignandAddChannels
###JAVA版本 自动为apk签名，并打上渠道信息。
由于项目用到了美团大神打渠道包的方案，最开始用的python版本，但是公司需求有变化，apk加固后签名信息丢失，需要重新签名，才能继续打包，所以自己模仿python版本修改了一下，写了一个JAVA版本，上传到这里，希望能对有此等需求的小伙伴有所帮助。

#####这里是python版本[AndroidMultiChannelBuildTool](https://github.com/GavinCT/AndroidMultiChannelBuildTool)

##使用方法
1，在javaIDE创建项目，将`ChangeAPKChannel.java`文件和`jar`文件拷贝到项目中，适当修改代码，打包插件部分即可；   
2，APK需要增加部分代码，详情请见[AndroidMultiChannelBuildTool](https://github.com/GavinCT/AndroidMultiChannelBuildTool)  
3，项目运行后，会出现一个弹窗，将要签名和打渠道的apk拖进窗口里，点击按钮即可签名并添加渠道信息。

*注意事项：
我测试的脚本是在Windows环境下运行的，代码中路径分隔符用的"//"，如果你的运行环境是Linux或者Mac，出现路径找不到的问题可以自行修改。
有问题可以加QQ群：437471364
