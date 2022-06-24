package com.shao.miaoshaproject.controller;

import com.shao.miaoshaproject.controller.viewobject.UserVO;
import com.shao.miaoshaproject.error.BusinessException;
import com.shao.miaoshaproject.error.EmBusinessError;
import com.shao.miaoshaproject.response.CommonReturnType;
import com.shao.miaoshaproject.service.model.UserModel;
import com.shao.miaoshaproject.service.UserService;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by hzllb on 2018/11/11.
 */
@Controller("user")
@RequestMapping("/user")
//解决ajax请求的跨域问题
//让reponse时刻能返回Access-Controller-Allow-Origin这个header为所有的域即*
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    @Resource
    private HttpServletRequest httpServletRequest;

    @Resource
    private RedisTemplate redisTemplate;

    //用户登陆接口
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name="telphone")String telphone,
                                  @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone)||
                StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登陆服务,用来校验用户登陆是否合法
        UserModel userModel = userService.validateLogin(telphone,this.EncodeByMd5(password));

        //将登陆凭证加入到用户登陆成功的session内
        //生成登录token,UUID(唯一性)
        String uuidToken = UUID.randomUUID().toString();
        //优化
        uuidToken = uuidToken.replace("-","");
        //建立token与用户登录状态的连接,存储在redis中
        redisTemplate.opsForValue().set(uuidToken,userModel);
        redisTemplate.expire(uuidToken,1, TimeUnit.HOURS);

        //this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);//ctrl+shift +f 全局文本查找
        //this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        //将token返回给前端,下发token
        return CommonReturnType.create(uuidToken);
    }


    /**
     * 用户注册接口
     * @param telphone
     * @param otpCode
     * @param name
     * @param gender
     * @param age
     * @param password
     * @return
     * @throws BusinessException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender")Integer gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的otpcode相符合
        HttpSession err = httpServletRequest.getSession();
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        System.out.println(otpCode+inSessionOtpCode);
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符合");
        }
        //使用redis的话
        // String inSessionOtpCode  = redisTemplate.get(telphone)
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
//        BASE64Encoder base64en = new BASE64Encoder();
//        //加密字符串
//        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        String newstr = Base64.encodeBase64String(md5.digest(str.getBytes("utf-8")));

        return newstr;
    }

    /**
     * 用户获取otp短信接口
     * @param telphone
     * @return
     */
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt =  random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);


        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定他的手机号与OTPCODE
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        System.out.println( (String) this.httpServletRequest.getSession().getAttribute(telphone));

        //在分布式架构中，则存储到redis中（做分布式会话管理）
        //redsitemplate.set(telphone,optCode);


        //将OTP验证码通过短信通道发送给用户,省略
        System.out.println("telphone = " + telphone + " & otpCode = "+otpCode);


        return CommonReturnType.create(null);
    }


    /**
     * 获取用户信息
     * @param id
     * @return
     * @throws BusinessException
     */
    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //直接返回从数据库中查询到UseModel给前端是十分不科学的，前端不一定要所有的数据
        //return userModel;

        //需要对返回值进行封装，前端好进行读取（json格式）即归一化
//        UserVO userVO  = convertFromModel(userModel);
//        return userVO;


        //用户信息不存在 异常处理
        if(userModel == null){
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //讲核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO  = convertFromModel(userModel);

        return CommonReturnType.create(userVO);

    }
    private UserVO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }


}
