# canal + kafka 封装demo

### 准备工作

1. 复制 `canal` `config` 两个包完整内容到自己的项目（基础类相关）
2. 在自己项目新建一个 `dbchange` 及 `dbchange/listener` 包，留作 监听业务逻辑相关代码 存放使用
3. 如果项目没有引入kafka，需要自行导入相关依赖，SpringBoot项目可以参考本demo的 `pom.xml` 文件
4. 复制 `application.properties` 中 kafka相关配置到自己的项目
   * 如果自己项目已经在使用 kafka 请注意配置项`spring.kafka.consumer.enable-auto-commit:false`
   * 若自己的项目中该配置是true，可自行修改 `CanalRoot` 中的监听代码（去掉ack相关代码）
5. 修改 `CanalRoot.PACKAGE_OF_DBCHANGE` 变量，指定自己项目的 `dbchange` 完整包名
6. 开始使用

### 简单介绍
* `dbchange` 中存放与数据库表相对应的spring事件，命名规则：表名转驼峰+Event
* `dbchange/listener` 中存放监听各个表事件的方法，推荐命名规则（非强制）：表名转驼峰+业务描述
* 监听基类分两种
  * 异步消费：`DBChangeBaseListenerAsync` java获取到的数据库变动顺序可能是乱的
  * 同步消费：`DBChangeBaseListenerSync` java获取到的数据库变动是有序的
* 严谨情况下，不知道该用哪种基类，就用"同步版" `DBChangeBaseListenerSync`