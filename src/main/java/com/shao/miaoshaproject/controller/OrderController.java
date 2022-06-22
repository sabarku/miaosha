package com.shao.miaoshaproject.controller;

import com.shao.miaoshaproject.error.BusinessException;
import com.shao.miaoshaproject.error.EmBusinessError;

import com.shao.miaoshaproject.response.CommonReturnType;
import com.shao.miaoshaproject.service.ItemService;
import com.shao.miaoshaproject.service.OrderService;

import com.shao.miaoshaproject.service.model.OrderModel;
import com.shao.miaoshaproject.service.model.UserModel;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by hzllb on 2018/11/18.
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;

    @Resource
    private ItemService itemService;

    @Autowired
    private HttpServletRequest httpServletRequest;
    @Resource
    private RedisTemplate redisTemplate;

    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId) throws BusinessException {


        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if(isLogin == null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
//        String token = httpServletRequest.getParameterMap().get("token")[0];
//        if (StringUtils.isEmpty(token)) {
//            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
//        }
        //获取用户的登陆信息
//        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        //会话过期
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登陆，不能下单");
        }

        //获取用户的登陆信息
        //UserModel userModel = (UserModel)httpServletRequest.getSession().getAttribute("LOGIN_USER");
        OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,amount);

//        //判断库存是否已经售罄，若对应的售罄key存储则直接返回下单失败
//        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
//            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
//        }

        return CommonReturnType.create(null);
    }
}
