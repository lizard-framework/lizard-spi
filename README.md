# lizard-spi
## 简介
  lizard-spi 是一个基于 Java Spi 和 Dubbo Spi 思想基础上的 SPI 支持框架，让使用方快速灵活的扩展应用程序。
  
## 功能特性
  - 支持 单例扩展 和 prototype 扩展
  - 扩展描述文件支持 key value 模式的多实现配置
  - 类似 Alibaba Dubbo Spi 的使用方式，但不与任何框架强耦合

## 快速入门
### 引入 lizard-spi
  请先将源码 clone 到本地，然后执行 mvn install 安装到本地仓库进行测试与使用调试。具体命令如下：
    
    git clone git@github.com:lizard-framework/lizard-spi.git
    
    cd lizard-spi
    
    mvn clean install
  
  在工程的 pom.xml文件中引入 lizard-spi 的依赖
  
  ```xml
  <dependency>
    <groupId>io.lizardframework</groupId>
    <artifactId>lizard-spi</artifactId>
    <version>1.0-SNAPHSHOT</version>
  </dependency>
  ```
  至此 lizard-spi 已经成功引入到您的工程中，接下来让我们尝试一下具体的使用吧！
  
### 编写一个简单的单例扩展实例


## 更新说明
  - 1.0-SNAPHSHOT
    1. 初步完成SPI整体功能
    2. 完成单元测试 
  
