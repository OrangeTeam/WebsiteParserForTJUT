# WebsiteParserForTJUT

WebsiteParserForTJUT是天津理工大学网站的解析器库。

目前它可以解析天津理工大学的[教务处网站][]、[计算机学院网站][]和[计算机学院学生网站][]的通知，以及[师生服务网站][]的个人信息、已选课程和成绩信息。

## API
您可以在[这里][API]查看Javadoc文档。

## 从源代码构建
WebsiteParserForTJUT使用[Gradle][]自动化构建系统。

### 准备

在进行以下操作前，请安装并配置好[JDK][] 7（或以上版本）和[Git][]。

### 克隆代码库
`git clone https://github.com/OrangeTeam/WebsiteParserForTJUT.git`

### 编译，测试，构建jar包
`./gradlew build`

您可以通过`./gradlew tasks`查找更多命令。


[教务处网站]: http://59.67.148.66
[计算机学院网站]: http://59.67.152.3
[计算机学院学生网站]: http://59.67.152.6
[师生服务网站]: http://ssfw.tjut.edu.cn
[API]: http://baijie.mezoka.com/projects/mobileTJUT/parser/javadoc/
[Gradle]: http://gradle.org
[JDK]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[Git]: http://git-scm.com
