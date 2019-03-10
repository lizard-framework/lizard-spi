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
  定义一个接口
  ```java
    @SPI // @SPI注解标明该接口的实现类是SPI扩展
    public interface Filter {
    
        /**
         * 模拟执行方法
         */
        String doFilter();
    
    }
  ```
  编写接口的实现类
  ```java
    @Extensions // @Extensions注解标明该类是一个SPI扩展，默认为单例
    public class SimpleFilterImpl implements Filter {
        @Override
        public String doFilter() {
            return this.getClass().getName() + " hashCode: " + this.hashCode();
        }
    }
  ```
  编写扩展描述文件，具体路径可以为当前工程的 resources 目录下的 META-INF/lizard/services 或者 META-INF/lizard/internal/ 路径
  
  与JAVA SPI 约定的一致，以扩展实现的接口名为文件名。一个文件中可以描述多个该接口的实现类，以 key-value 的模式进行区分
  
  key 为实现类的名称：要在该类的实现类中唯一；value 是具体实现类的路径
  
  io.lizardframework.spi.filter.Filter
  ```
    # defalut为该接口的默认扩展名称
    default=io.lizardframework.spi.filter.SimpleFilterImpl
    # 有多个实现类的情况下，请按照如下的格式编写即可
    # xxx = xxx.xxxImpl
  ```
  
  以单例模式调用接口的默认扩展
  ```java
    public class ExtensionTest {
	    public void test1() {
            ExtensionLoader<Filter> loader = ExtensionFactory.getExtensionLoader(Filter.class);
            System.out.println(loader.getExtension().doFilter());
	    }
    }
  ```
## API说明
   - @io.lizardframework.spi.SPI：标明该接口的实现类是SPI扩展 
        - value：该接口扩展的默认名称，默认值 default
        
   - @io.lizardframework.spi.Extensions: 标明该类是一个SPI扩展，默认为单例
        - singleton: 是否单例，默认值 true
   
   - io.lizardframework.spi.ExtensionFactory: 扩展点加载器工厂
        - public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> clazz): 获取指定SPI接口的扩展加载器
        
   - io.lizardframework.spi.ExtensionLoader：扩展点加载器，用来加载指定接口的具体扩展实现
        - public T getExtension()：加载名称为 default 且构造函数无参数的默认实现类
        
        - public T getExtension(String name, Class[] argTypes, Object[] args)：加载指定名称和构造函数的实现类
            
            - name：接口SPI扩展的实现类名称
            - argTypes：实现类构造函数参数类型数组
            - args：实现类构造函数参数数组
   

## 更新说明
  - 1.0-SNAPHSHOT
    1. 初步完成SPI整体功能
    2. 完成单元测试，验证多线程场景下单例扩展的线程安全性
  
## 未来展望
  - 支持运行时动态生成扩展类
  - 支持监听器，使类被初始化和实例化的过程中可以被动态控制
  - 支持未来开源的 lizard-prc