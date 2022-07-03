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


  >> 查询优化之多级缓存：（对动态请求数据的缓存）
  -  缓存设计原则：
    - 内存，快速存取
    - 将缓存推送到离用户最近的地方，这样网络走的链路就少了，查询的效率高了
    - 脏数据清理（缓存一致性问题）
  -  原因分析：
    - 单独redis缓存有一定的网络开销，存在集中式的负载均衡的性能瓶颈
  - 方案选取：
    - 应用程序服务内存本地缓存：
      - 设计原则：1.热点数据 2.脏读不敏感 3.内存可控
      - 适用谷歌提供发guava cache（允许读写并发、过期时间、淘汰机制）
  - 后续优化点：
    - 在nginx服务上部署本地缓存：nginx proxy cache 缓存（即将请求转发到后端代理服务的公开服务器上）
      - 缺点：访问的不是内存的缓存，而是内存的地址，缓存数据在本地文件中，通过地址去访问本地文件
      - 优势：可以使用NAS做本地磁盘，进行容量的扩容。
    - 启用OpenResty的lua模块完成nginx层面的本地内存缓存部署
    
    
  >> 查询优化之静态缓存
  - 背景：单纯的将静态资源部署在nginx的服务器上，访问受地理区域等因素影响
  - 引入CDN（内容分发网络：无限大的内容磁盘缓存）![image](https://user-images.githubusercontent.com/33751638/177022557-4d65bb14-c5f0-4004-85eb-a40ee57d5228.png)


  >> 后续优化点：
  - 全页面静态化：即在服务端完成html、css甚至是js load渲染成html后，直至以静态资源的方式部署到CDN上
    - css、html等静态资源的CDN化
    - js、ajax动态请求CDN化


> 交易性能优化：
  >> 背景：交易压力下：1.交易验证完全依赖数据库  2.库存行锁存在优化点
  >> 策略方案：
  - 交易验证优化：
    - 用户风控策略模型化，减少对数据库的依赖（UerModel存入redis中）
    - 活动校验策略优化：引入活动发布流程，模型缓存化（ItemModel存入redis中）
  - 库存行锁优化：
    - 1.扣减库存缓存化（扣减缓存中的库存）
    - 2.异步同步数据库（rocketmq异步消息队列，保证数据库与缓存数据的一致性，但有可能失败【异步消息发送失败，扣减执行操作失败】:超卖现象）
    - 3.库存数据库最终一致性（事务型消息：解决上述可能失败的情况，进行回滚事务（库存回补））
      - 引入库存操作流水：记录秒杀活动各个时刻状态，即创建对应的操作日志log，记录状态status
      - 库存流水表引入（stock_log_id,item_id,amount,status）

> 后期优化：流量削峰、防刷限流
  - 再厉害的系统也经不住中国十几亿人口的同时访问，因此针对过载保护，限流令牌之类的流量削峰平滑操作措施是必不可少的，同时我们也需要知道并不是所有的流量都是正常的流量，有绝大部分的可能是你的流量都是羊毛党刷的，他们不但影响了正常用户的使用，更可能对你的系统造成致命的打击，因此识别羊毛党，防刷等操作必须是我们要面临承担的问题解决。

## 使用须知

> 本项目主要用于学习分布式架构，使用jmeter做性能测试，提升自己对分布式、高并发等概念的深度理解。
![image](https://user-images.githubusercontent.com/33751638/175000800-953a3402-6a8d-41b5-aebc-704d27d2a300.png)

> 我们需要知道没有最牛的系统，只有最合理的业务架构设计，因此在学习并掌握了业务建模和容量提升的基本方法后，我们需要结合电商业务的发展实际去设计系统，解决问题。

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
