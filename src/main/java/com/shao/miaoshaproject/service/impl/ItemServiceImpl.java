package com.shao.miaoshaproject.service.impl;

import com.shao.miaoshaproject.dao.ItemDOMapper;
import com.shao.miaoshaproject.dao.ItemStockDOMapper;

import com.shao.miaoshaproject.dataobject.ItemDO;
import com.shao.miaoshaproject.dataobject.ItemStockDO;

import com.shao.miaoshaproject.error.BusinessException;
import com.shao.miaoshaproject.error.EmBusinessError;

import com.shao.miaoshaproject.mq.MqProducer;
import com.shao.miaoshaproject.service.ItemService;

import com.shao.miaoshaproject.service.PromoService;
import com.shao.miaoshaproject.service.model.ItemModel;
import com.shao.miaoshaproject.service.model.PromoModel;
import com.shao.miaoshaproject.validator.ValidationResult;
import com.shao.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by hzllb on 2018/11/18.
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Resource
    private ItemDOMapper itemDOMapper;

    @Resource
    private ItemStockDOMapper itemStockDOMapper;

    @Resource
    private PromoService promoService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private MqProducer mqProducer;

    private ItemDO convertItemDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }
    private ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    /**
     * ??????????????????
     * @param itemModel
     * @return
     * @throws BusinessException
     */
    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //????????????
        ValidationResult result = validator.validate(itemModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }

        //??????itemmodel->dataobject
        ItemDO itemDO = this.convertItemDOFromItemModel(itemModel);

        //???????????????
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemModel);

        itemStockDOMapper.insertSelective(itemStockDO);

        //???????????????????????????
        return this.getItemById(itemModel.getId());
    }

    /**
     * ????????????
     * @return
     */
    @Override
    public List<ItemModel> listItem() {
        List<ItemDO> itemDOList = itemDOMapper.listItem();
        List<ItemModel> itemModelList =  itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.convertModelFromDataObject(itemDO,itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null){
            return null;
        }
        //????????????????????????
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());


        //???dataobject->model
        ItemModel itemModel = convertModelFromDataObject(itemDO,itemStockDO);

        //????????????????????????
        PromoModel promoModel = promoService.getPromoByItemId(itemModel.getId());
        if(promoModel != null && promoModel.getStatus().intValue() != 3){
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }

    @Override
    public ItemModel getItemByIdInCache(Integer id) {
        ItemModel itemModel = (ItemModel) redisTemplate.opsForValue().get("item_validata_" + id);
        if(itemModel == null){
            itemModel = this.getItemById(id);
            redisTemplate.opsForValue().set("item_validata_"+id,itemModel);
            redisTemplate.expire("item_validata_"+id,10, TimeUnit.MINUTES);
        }
        return itemModel;
    }

    /**
     * ????????????
     * @param itemId
     * @param amount
     * @return
     * @throws BusinessException
     */
    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        //int affectedRow =  itemStockDOMapper.decreaseStock(itemId,amount);
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        long affectedRow = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue()*-1);

        if(affectedRow > 0){
            //??????????????????
           boolean mqResult = mqProducer.asyncReduceStock(itemId,amount);
            if(!mqResult){
                //????????????????????????????????????
                redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
                return false;
            }
            return true;
        }else if(affectedRow == 0){
            return true;
        }else{
            //??????????????????
            redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue());
            return false;
        }

    }

    /**
     * ??????????????????
     * @param itemId
     * @param amount
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }

    private ItemModel convertModelFromDataObject(ItemDO itemDO, ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO,itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());

        return itemModel;
    }

}
