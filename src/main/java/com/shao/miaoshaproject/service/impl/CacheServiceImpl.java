package com.shao.miaoshaproject.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.shao.miaoshaproject.service.CacheService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: SJP
 * Date: 2022/6/5
 * Time: 21:00
 *
 * @author sjp
 * Description: No Description
 */
@Service
public class CacheServiceImpl implements CacheService {
    private Cache<String,Object> commonCache = null;

    //在bean加载之前优先加载init（）
    @PostConstruct
    public void init(){
        commonCache = CacheBuilder.newBuilder()
                //设置缓存的初始容量
                .initialCapacity(10)
                //设置缓存最大可以存储100key，之后超过就按照LRU策略移除缓存
                .maximumSize(100)
                //设置写入缓存后的过期时间60s
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }


    @Override
    public void setCommonCache(String key, Object value) {
        commonCache.put(key,value);
    }

    @Override
    public Object getFromCommonCache(String key) {
        return commonCache.getIfPresent(key);
    }
}
