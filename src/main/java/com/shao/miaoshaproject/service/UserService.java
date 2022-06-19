package com.shao.miaoshaproject.service;

import com.shao.miaoshaproject.service.Model.UserModel;

import org.springframework.stereotype.Service;

/**
 * Created by hzllb on 2018/11/11.
 */
public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);

}
