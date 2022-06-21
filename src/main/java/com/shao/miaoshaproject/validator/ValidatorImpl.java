package com.shao.miaoshaproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Created by hzllb on 2018/11/18.
 */
@Component
public class ValidatorImpl implements InitializingBean{

    //可以认为是一个校验器
    private Validator validator;

    //实现校验方法并返回校验结果  校验bean各参数是否为null
    public ValidationResult validate(Object bean){
        final ValidationResult result = new ValidationResult();
        //入参就是要校验的bean，bena里面的参数规则若有被违背则会存储到constraintViolationSet中
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size() > 0){
            //有错误
            result.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return result;
    }


    @Override
    public void afterPropertiesSet() {
        //将hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
