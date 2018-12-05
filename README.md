[![build status](https://git.tmindtech.com/api-server/server-base/badges/master/build.svg)](https://git.tmindtech.com/api-server/server-base/commits/master) 
[![coverage report](https://git.tmindtech.com/api-server/server-base/badges/master/coverage.svg)](https://git.tmindtech.com/api-server/server-base/commits/master)

# 项目说明
本项目为API服务的基础工程项目

后续其他API服务的项目构建 从本项目Fork后展开

# 框架
* Java
* Spring Boot
* Gradle
* MyBatis

# 如何开始
## IDE
Intellij IDEA

### 插件
* FindBugs-IDEA
    * 用于静态代码分析，检测隐藏错误
* CheckStyle-IDEA
    * 用于约束代码规范

# 规范
* Java代码必须符合[checkstyle](./codestyle_checks.xml)的约束
* Git commit的comment必须符合[提交规范][]

[提交规范]: https://github.com/sparkbox/standard/tree/master/style/git

## 数据库脚本
使用[Flyway][]来进行管理

[Flyway]: https://flywaydb.org

### 规则
本项目仅维护匹配开发版本的脚本，不考虑升级用脚本。
升级脚本的维护应当在发布上线后，建立独立的脚本管理。

#### sql脚本组织规则
* 脚本文件应当放置于对应的模块目录中
* 命名规则为V[模块序号].[模块内序号]__[模块名].[描述].sql
    * 模块序号
        * 脚本运行的模块顺序
    * 模块内序号
        * 脚本运行的模块内顺序
    * 模块名和描述
        * 脚本在Flyway中的描述信息

# FAQ
**Q:** 建立工程时一直卡在“building gradle project info”

**A:** 原因是在下载gradle，而services.gradle.org的访问速度缓慢导致

解决方案是通过配置代理或更换网络加速gradle的下载

通过"./gradlew tasks"可以直观的看到下载的进展

# 持续集成
使用Gitlab CI进行持续集成

# 部署说明
[DEPLOY](./deploy.md)


系统名词定义
商户号           mch_id
商户手续费       mch_charge
商户订单号       mch_order_id



用户id           user_id