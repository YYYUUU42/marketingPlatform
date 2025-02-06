package com.yu.market.server.activity.service.product;

import com.yu.market.server.activity.model.bo.SkuProductBO;
import com.yu.market.server.activity.respository.IActivityRepository;
import com.yu.market.server.activity.service.IRaffleActivitySkuProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yu
 * @description sku商品服务
 * @date 2025-02-06
 */
@Service
@RequiredArgsConstructor
public class RaffleActivitySkuProductService implements IRaffleActivitySkuProductService {
    
    private final IActivityRepository activityRepository;

    @Override
    public List<SkuProductBO> querySkuProductBOListByActivityId(Long activityId) {
        return activityRepository.querySkuProductBOListByActivityId(activityId);
    }

}
