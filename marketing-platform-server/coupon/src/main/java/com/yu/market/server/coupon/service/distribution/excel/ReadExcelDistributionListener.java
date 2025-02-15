package com.yu.market.server.coupon.service.distribution.excel;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.yu.market.common.contants.CouponRedisKey;
import com.yu.market.common.utils.StockDecrementReturnCombinedUtil;
import com.yu.market.infrastructure.coupon.mapper.CouponTaskFailMapper;
import com.yu.market.infrastructure.coupon.pojo.CouponTask;
import com.yu.market.infrastructure.coupon.pojo.CouponTaskFail;
import com.yu.market.infrastructure.coupon.pojo.CouponTemplate;
import com.yu.market.server.coupon.mq.event.CouponTemplateDistributionEvent;
import com.yu.market.server.coupon.mq.producer.CouponExecuteDistributionProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Map;

/**
 * @author yu
 * @description 优惠券任务读取 Excel 分发监听器
 * @date 2025-02-15
 */
@RequiredArgsConstructor
public class ReadExcelDistributionListener extends AnalysisEventListener<CouponTaskExcelObject> {

    private final CouponTask couponTask;
    private final CouponTemplate couponTemplate;
    private final CouponTaskFailMapper couponTaskFailMapper;

    private final StringRedisTemplate stringRedisTemplate;
    private final CouponExecuteDistributionProducer couponExecuteDistributionProducer;

    private int rowCount = 1;
    private final static String STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH = "lua/stock_decrement_and_batch_save_user_record.lua";
    private final static int BATCH_USER_COUPON_SIZE = 5000;

    @Override
    public void invoke(CouponTaskExcelObject data, AnalysisContext context) {
        Long couponTaskId = couponTask.getId();

        // 获取当前进度，判断是否已经执行过。如果已执行，则跳过即可，防止执行到一半应用宕机
        String templateTaskExecuteProgressKey = String.format(CouponRedisKey.TEMPLATE_TASK_EXECUTE_PROGRESS_KEY, couponTaskId);
        String progress = stringRedisTemplate.opsForValue().get(templateTaskExecuteProgressKey);
        if (StrUtil.isNotBlank(progress) && Integer.parseInt(progress) >= rowCount) {
            rowCount++;
            return;
        }

        // 获取 LUA 脚本，并保存到 Hutool 的单例管理容器，下次直接获取不需要加载
        DefaultRedisScript<Long> buildLuaScript = Singleton.get(STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(STOCK_DECREMENT_AND_BATCH_SAVE_USER_RECORD_LUA_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });

        // 执行 LUA 脚本进行扣减库存以及增加 Redis 用户领券记录
        String couponTemplateKey = String.format(CouponRedisKey.COUPON_TEMPLATE_KEY, couponTemplate.getId());
        String batchUserSetKey = String.format(CouponRedisKey.TEMPLATE_TASK_EXECUTE_BATCH_USER_KEY, couponTaskId);
        Map<Object, Object> userRowNumMap = MapUtil.builder()
                .put("userId", data.getUserId())
                .put("rowNum", rowCount + 1)
                .build();
        // couponTemplateKey：优惠券模板的 Key
        // batchUserSetKey：当前任务领取用户集合的 Key
        // userRowNumMap：包含用户 ID 和 Excel 中的行号，用于传递给 Lua 脚本，记录用户领取信息。
        Long combinedFiled = stringRedisTemplate.execute(buildLuaScript, ListUtil.of(couponTemplateKey, batchUserSetKey), JSON.toJSONString(userRowNumMap));

        // firstField 为 false 说明优惠券已经没有库存了
        boolean firstField = StockDecrementReturnCombinedUtil.extractFirstField(combinedFiled);
        if (!firstField) {
            // 同步当前执行进度到缓存
            stringRedisTemplate.opsForValue().set(templateTaskExecuteProgressKey, String.valueOf(rowCount));
            rowCount++;

            // 添加到 t_coupon_task_fail 并标记错误原因，方便后续查看未成功发送的原因和记录
            Map<Object, Object> objectMap = MapUtil.builder()
                    .put("rowNum", rowCount + 1)
                    .put("cause", "优惠券模板无库存")
                    .build();
            CouponTaskFail couponTaskFailDO = CouponTaskFail.builder()
                    .batchId(couponTask.getBatchId())
                    .jsonObject(JSON.toJSONString(objectMap, SerializerFeature.WriteMapNullValue))
                    .build();
            couponTaskFailMapper.insert(couponTaskFailDO);
            return;
        }

        // 获取用户领券集合长度
        int batchUserSetSize = StockDecrementReturnCombinedUtil.extractSecondField(combinedFiled.intValue());

        // 如果没有消息通知需求，仅在 batchUserSetSize = BATCH_USER_COUPON_SIZE 时发送消息消费。不满足条件仅记录执行进度即可
        if (batchUserSetSize < BATCH_USER_COUPON_SIZE && StrUtil.isBlank(couponTask.getNotifyType())) {
            // 同步当前 Excel 执行进度到缓存
            stringRedisTemplate.opsForValue().set(templateTaskExecuteProgressKey, String.valueOf(rowCount));
            ++rowCount;
            return;
        }

        CouponTemplateDistributionEvent couponTemplateDistributionEvent = CouponTemplateDistributionEvent.builder()
                .userId(data.getUserId())
                .mail(data.getMail())
                .phone(data.getPhone())
                .couponTaskId(couponTaskId)
                .notifyType(couponTask.getNotifyType())
                .shopNumber(couponTask.getShopNumber())
                .couponTemplateId(couponTemplate.getId())
                .couponTaskBatchId(couponTask.getBatchId())
                .couponTemplateConsumeRule(couponTemplate.getConsumeRule())
                .batchUserSetSize(batchUserSetSize)
                .distributionEndFlag(Boolean.FALSE)
                .build();
        couponExecuteDistributionProducer.sendMessage(couponTemplateDistributionEvent);

        // 同步当前执行进度到缓存
        stringRedisTemplate.opsForValue().set(templateTaskExecuteProgressKey, String.valueOf(rowCount));
        ++rowCount;
    }

    /**
     * 当所有数据都读取完成后，EasyExcel 会调用这个方法
     *
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 发送 Excel 解析完成标识，即使不满足批量保存的数量也得保存到数据库
        CouponTemplateDistributionEvent couponTemplateExecuteEvent = CouponTemplateDistributionEvent.builder()
                .distributionEndFlag(Boolean.TRUE) // 设置解析完成标识
                .shopNumber(couponTask.getShopNumber())
                .couponTemplateId(couponTemplate.getId())
                .couponTemplateConsumeRule(couponTemplate.getConsumeRule())
                .couponTaskBatchId(couponTask.getBatchId())
                .couponTaskId(couponTask.getId())
                .build();
        couponExecuteDistributionProducer.sendMessage(couponTemplateExecuteEvent);
    }
}
