# 伙伴匹配系统

## 大家找到志同道合的朋友的移动端网站（APP风格） 后端



**项目地址：[jane.fit](http://jane.fit)**

**前端地址： https://github.com/qimu666/jujiao-garden-frontend**

 **后端地址： https://github.com/qimu666/jujiao-yuan-backend**

🙏🏻 **大家喜欢这个项目的话，感谢动手点点 star**

## 项目描述

- 一个帮助大家找到志同道合的伙伴的网站，网站包含用户管理、按标签检 索用户、推荐相似用户、组队、聊天功能、添加好友、对文章的点赞、收藏等功能的实现
- 使用了各种设计模式如，单例，工厂，过滤
- 自定义了权限，限流，异常处理等功能，提供了邮箱，微信公众，微信扫描，支付宝等接口作为后续开发

## 技术选型

### 前端

1. Vue 3

2. Vant UI 组件库（移动端）

3. Vite 脚手架

4. Axios 请求库

5. vue-quill-text-editor 富文本编辑器

 ### 后端

1. Java SpringBoot 2.7.x 框架
2. MySQL 数据库
3. MyBatis-Plus
4. MyBatis X 自动生成
5. Redis 缓存（Spring Data Redis 等多种实现方式）
6. Redisson 分布式锁
7. Swagger + Knife4j 接口文档
8. Gson JSON 序列化库
9. elasticsearch 搜索引擎
10. Netty 消息转发
11. RabbitMQ 中间件
12. hutool 工具
13. easyexcel 

## 功能特性

#### 匹配功能 

- 编辑距离算法，想要了解具体原理的可以看这个文章：https://blog.csdn.net/DBC_121/article/details/104198838

#### 聊天功能

- 使用了Netty作为消息的转发
- 同时支持ChatGPT回答

