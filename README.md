<h1 align="center"><a href="https://github.com/sabarku/miaosha" target="_blank">miaosha</a></h1>

<p align="center">
<a href="https://www.oscs1024.com/project/oscs/sabarku/miaosha?ref=badge_small" alt="OSCS Status"><img src="https://www.oscs1024.com/platform/badge/sabarku/miaosha.svg?size=small"/></a>
<a href="#"><img alt="JDK" src="https://img.shields.io/badge/JDK-1.8-yellow.svg?style=flat-square"/></a>
</p>

## 简介

> miaosha通过使用SpringBoot快速搭建前后端分离的电商基础秒杀项目。项目中会通过应用领域驱动型的分层模型设计方式去完成用户otp注册、登陆、查看、商品列表、进入商品详情以及倒计时秒杀开始后下
单购买的基本流程。

## 声明

> 本项目，主要宗旨在实现简单的秒杀项目基础上，以领域模型为前提的思维方式划分清楚业务模块，业务模型，并在操作的过程中注重扩展性，性能，流量承载，服务降级等策略，聚焦Java性能优化。

## 优化点：
> tomcat定制化开发：使用keepAlive，保证连接的复用性
  - 问题原因分析：单机服务的并发容量问题：无法承受过多的并发请求--》并发线程数上不去（tomcat配置问题）
  - 解决问题：修改tomcat配置，修改默认线程数，最大线程数等配置
> 分布式扩展部署：采用4台云服务器：2台部署秒杀应用程序，一台部署nginx代理服务，一台部署数据库服务。
  - 问题原因分析：单机的多线程调度有个阈值，过多线程导致cpu资源浪费在cpu调度上，以及单机的mysql的qps和tps压力
  - 解决问题：
    - 应用服务水平扩展：使用nginx负载均衡，降低单个应用服务的响应压力，是tps提升一个数量级
    - 数据库服务独立化：将mysq的qps和tps压力转到一台服务器上，降低应用服务器的响应压力
    - 也实现了系统的高可用
> redis做分布式会话管理：基于token的实现，解决分布式的session问题，单点登录问题
  - 原因分析：每次登录保存的sessionid是在对应的应用服务器上的，不会被共享。token相对于Cookie更适用与移动端，且token支持跨域，且更适用CDN。
  - 解决问题：解决分布式的session问题，单点登录问题，降低对数据服务的压力。（不用每次访问登录相关的资源和接口都去访问数据库）
> 数据库存取相关优化
  >> 查询优化之多级缓存：
  >> 查询优化之静态缓存：
## 使用须知

> 本项目主要用于学习分布式架构，使用jmeter做性能测试，提升自己对分布式、高并发等概念的深度理解
![image](https://user-images.githubusercontent.com/33751638/175000800-953a3402-6a8d-41b5-aebc-704d27d2a300.png)

## 快速开始

### 下载最新的 miaosha 安装包   

window

```bash
https://github.com/sabarku/hello-blog/releases/download/V1.0.0/miaosha-1.0-SNAPSHOT.jar
```

linux

```bash
wget https://github.com/sabarku/hello-blog/releases/download/V1.0.0/miaosha-1.0-SNAPSHOT.jar
```
