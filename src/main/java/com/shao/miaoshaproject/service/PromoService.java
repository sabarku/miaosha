package com.shao.miaoshaproject.service;

import com.shao.miaoshaproject.service.model.PromoModel;

/**
 * Created by hzllb on 2018/11/18.
 */
public interface PromoService {
    //根据itemid获取即将进行的或正在进行的秒杀活动
    PromoModel getPromoByItemId(Integer itemId);
    /**
     * 活动发布
     * @param promoId
     */
    void publishPromo(Integer promoId);

}
