package com.shao.miaoshaproject.service;

import com.shao.miaoshaproject.error.BusinessException;
import com.shao.miaoshaproject.service.model.UserModel;

/**
 * Created by hzllb on 2018/11/11.
 */
public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     * 用户登录服务校验
     * @param telphone
     * @param encrptPassword
     * @return
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;

}
