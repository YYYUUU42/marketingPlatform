package com.yu.market.common.exception.errorCode;

/**
 * @author yu
 * @description 基础错误码定义
 * @date 2024-12-19
 */
public enum BaseErrorCode implements IErrorCode {
    
    // ==========客户端错误 ==========
    CLIENT_ERROR("A000001", "客户端发生错误"),

    // ========== 用户注册相关错误 ==========
    USER_REGISTER_ERROR("A000100", "用户注册失败"),
    USER_NAME_VERIFY_ERROR("A000110", "用户名校验失败"),
    USER_NAME_EXIST_ERROR("A000111", "用户名已存在"),
    USER_NAME_SENSITIVE_ERROR("A000112", "用户名包含敏感词"),
    USER_NAME_SPECIAL_CHARACTER_ERROR("A000113", "用户名包含特殊字符"),
    PASSWORD_VERIFY_ERROR("A000120", "密码校验失败"),
    PASSWORD_SHORT_ERROR("A000121", "密码长度不足"),
    PHONE_VERIFY_ERROR("A000151", "手机号码格式校验失败"),

    // ========== 幂等性相关错误 ==========
    IDEMPOTENT_TOKEN_NULL_ERROR("A000200", "幂等性校验 Token 缺失"),
    IDEMPOTENT_TOKEN_DELETE_ERROR("A000201", "幂等性校验 Token 已失效或被使用"),

    // ========== 查询参数相关错误 ==========
    SEARCH_AMOUNT_EXCEEDS_LIMIT("A000300", "查询数据量超过上限"),

    // ========== 系统执行错误 ==========
    SERVICE_ERROR("B000001", "系统执行错误"),
    ILLEGAL_PARAMETER("B000002", "非法参数"),

    // ========== 系统执行超时 ==========
    SERVICE_TIMEOUT_ERROR("B000100", "系统执行超时"),

    // ========== 调用第三方服务错误 ==========
    REMOTE_ERROR("C000001", "调用第三方服务发生错误"),

    // ========== 调用第三方服务错误 ==========
    INDEX_DUP("D00003", "唯一索引冲突"),

    // ========== 业务相关错误 ==========
    STRATEGY_RULE_WEIGHT_IS_NULL("ERR_BIZ_001", "业务异常，策略规则中 rule_weight 权重规则已适用但未配置"),
    UN_ASSEMBLED_STRATEGY_ARMORY("ERR_BIZ_002", "抽奖策略配置未装配，请通过IStrategyArmory完成装配"),
    STATE_MACHINE_TRANSITION_FAILED("ERR_BIZ_003", "状态机转换失败"),
    ACTIVITY_STATE_ERROR("ERR_BIZ_004", "活动未开启（非open状态）"),
    ACTIVITY_DATE_ERROR("ERR_BIZ_005", "非活动日期范围"),
    ACTIVITY_SKU_STOCK_ERROR("ERR_BIZ_006", "活动库存不足"),
    ACCOUNT_QUOTA_ERROR("ERR_BIZ_007","账户总额度不足"),
    ACCOUNT_MONTH_QUOTA_ERROR("ERR_BIZ_008","账户月额度不足"),
    ACCOUNT_DAY_QUOTA_ERROR("ERR_BIZ_009","账户日额度不足"),
    USER_CREDIT_ACCOUNT_NO_AVAILABLE_AMOUNT("ERR_CREDIT_001", "用户积分账户额度不足")
    ;


    private final String code;

    private final String message;

    BaseErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
