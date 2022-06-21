package com.shao.miaoshaproject;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 * 创建简单的web项目
 */
//自动化配置启动
//@EnableAutoConfiguration 异曲同工之妙 ：将其进行托管并指定其为启动类
@SpringBootApplication(scanBasePackages = {"com.shao.miaoshaproject"}) //将根目录下的包依次扫描，通过注解的方式发现Service，contriller等特定注解
//做路径映射..
//@RestController
@MapperScan("com.shao.miaoshaproject.dao") //dao存放的地方要放在这个注解上
public class App 
{
    @RequestMapping("/")
    public void test(){
        System.out.println("ces");
    }
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
