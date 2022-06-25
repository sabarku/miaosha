package com.shao.miaoshaproject.service;

/**
 * Created with IntelliJ IDEA.
 * User: SJP
 * Date: 2022/6/5
 * Time: 20:58
 *
 * @author sjp
 * Description: No Description
 */
public interface CacheService {
    //存数据
    void setCommonCache(String key, Object value);

    //取数据
    Object getFromCommonCache(String key);
}
