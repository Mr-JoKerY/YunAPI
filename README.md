# Yun API

## 项目介绍

一个简洁的API开放平台，为开发者提供实用的API调用体验。我们可以为用户提供各种类型的接口服务，使他们能够更高效地完成需求，例如：获取今日天气、获取金句、随机壁纸等服务。

项目后端使用语言为Java，包括现在市面上主流技术栈，采用微服务架构开发，解耦业务模块，前端使用React，Ant Design Pro + Ant Design组件库，使用现成组件库快速开发项目。

> 在线体验：[Yun API](http://111.230.61.108:888)

## 功能模块

* 核心模块
  * 浏览接口信息
  * 在线调用接口
  * 搜索接口
  * 购买接口
  * 用户管理（管理员）
  * 接口管理（管理员）
  * 接口分析（管理员）
* 用户模块
  * 登录注册
  * 个人信息（accessKey，secretKey）
* 接口模块
  * 自定义的模拟接口
* 订单模块
  * 浏览订单信息
  * 订单扫码支付

## 后端模块

| 目录              | 描述                                  |
|-----------------|-------------------------------------|
| api-backend     | 核心业务模块，用户登录、接口处理、接口调用等核心功能都在这里      |
| api-gateway     | 网关模块，负责各模块间服务的转发、用户鉴权、统一日志等操作       |
| api-interface   | 接口模块，存放一些自定义模拟接口，提供接口服务             |
| api-order       | 订单模块，提供订单支付，订单浏览的基本操作               |
| api-third-party | 第三方模块，包括腾讯云短信服务、阿里云对象存储服务和支付宝沙箱支付服务 |
| api-common      | 公共模块，定义了实体类，公共返回对象，以及RPC接口，用于服务间的通信 |
| api-client-sdk  | 自定义SDK工具包，用于调用模拟接口，提供给开发者使用         |
| api-config      | 存放各服务的配置文件，若不采用微服务配置中心的，可以忽略        |

## 技术栈

前端：

- 开发框架：React、Umi
- 脚手架：Ant Design Pro
- 组件库：Ant Design、Ant Design Components
- 语法扩展：TypeScript、Less
- 打包工具：Webpack
- 代码规范：ESLint、StyleLint、Prettier

后端：

+ 语言：Java
+ 开发框架：SpringBoot、Spring Cloud、Mybatis-plus
+ 数据库：MySQL、Redis
+ 中间件：RabbitMQ
+ 网关：Spring Cloud Gateway
+ 注册中心：Nacos
+ 服务调用：Dubbo

## 系统架构

> 仅供参考

![image0.png](api-backend%2Fdoc%2Fimage0.png)

## 自定义SDK

提供给开发者在代码层面实现远程调用平台所提供api的能力

### 环境准备

JDK 1.8+

SpringBoot 2.x

### Maven引入

```xml
<dependency>
  <groupId>io.github.Mr-JoKerY</groupId>
  <artifactId>API-Client-SDK</artifactId>
  <version>0.0.1</version>
</dependency>
```

### 代码示例

依赖成功引入后，需要在`application.yml`配置文件中进行相关配置

```yml
# 开发者签名认证
api:
  client:
    # ak/sk可以在Yun API平台的个人中心查看
    access-key: xxx
    secret-key: xxx
```

配置完成后，就可以启动项目使用客户端 CommonApiClient 去调用接口

## 项目展示

* 主页
![image2.png](api-backend%2Fdoc%2Fimage2.png)
* 接口详情以及在线调用
![image1.png](api-backend%2Fdoc%2Fimage1.png)
* 我的接口
![image3.png](api-backend%2Fdoc%2Fimage3.png)
* 购买接口
![image4.png](api-backend%2Fdoc%2Fimage4.png)
* 我的订单
![image5.png](api-backend%2Fdoc%2Fimage5.png)
* 扫码支付
![image6.png](api-backend%2Fdoc%2Fimage6.png)
* 用户管理、接口管理
![image7.png](api-backend%2Fdoc%2Fimage7.png)
![image8.png](api-backend%2Fdoc%2Fimage8.png)
* 接口分析
![image9.png](api-backend%2Fdoc%2Fimage9.png)
* 个人信息
![image10.png](api-backend%2Fdoc%2Fimage10.png)